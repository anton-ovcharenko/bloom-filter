package oaa.bloomfilter;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.String.format;

public class SimpleHashFunctionsBuilder<E> {
    private final String DEFAULT_HASH_ALGORITHM = "MD5";
    private final int DEFAULT_FUNCTIONS_AMOUNT = 10;
    private final ToByteArrayFunction<E> DEFAULT_TO_BYTE_ARRAY_FUNCTION =
            element -> element.toString().getBytes();

    private MessageDigest messageDigest;
    private ToByteArrayFunction<E> toByteArrayFunction = DEFAULT_TO_BYTE_ARRAY_FUNCTION;
    private int amountOfFunctions = DEFAULT_FUNCTIONS_AMOUNT;
    private String algorithm = DEFAULT_HASH_ALGORITHM;

    public SimpleHashFunctionsBuilder<E> toByteArrayFunction(ToByteArrayFunction<E> toByteArrayFunction) {
        this.toByteArrayFunction = toByteArrayFunction;
        return this;
    }

    public SimpleHashFunctionsBuilder<E> amountOfFunctions(int amountOfFunctions) {
        this.amountOfFunctions = amountOfFunctions;
        return this;
    }

    public SimpleHashFunctionsBuilder<E> algorithm(String algorithm) {
        this.algorithm = algorithm;
        return this;
    }

    /**
     * Build family of functions that convert element to some int value based on digest of element and some salt.
     *
     * @return - list of functions that convert element to int.
     */
    public List<ToIntFunction<E>> build() {
        messageDigest = initMessageDigest(algorithm);
        return IntStream
                .range(0, amountOfFunctions)
                .mapToObj(this::generateToIntFunctionWithSalt)
                .collect(Collectors.toList());
    }

    /**
     * Generate ToIntFunction that convert element to int value of its hash.
     * It calculate int representation of the following hash: HASH(HASH(salt) + element).
     *
     * @param salt - value that will be used as salt.
     * @return - function that convert element to int value of its hash.
     */
    private ToIntFunction<E> generateToIntFunctionWithSalt(int salt) {
        return value -> {
            byte[] digest;
            synchronized (this) {
                byte[] saltDigest = messageDigest.digest(intToBytes(salt));
                messageDigest.update(saltDigest);
                digest = messageDigest.digest(toByteArrayFunction.getBytes(value));
            }
            return bytesToPositiveInt(digest);
        };
    }

    private byte[] intToBytes(int salt) {
        return ByteBuffer
                .allocate(4)
                .putInt(salt)
                .array();
    }

    private int bytesToPositiveInt(byte[] bytes) {
        return Math.abs(ByteBuffer.wrap(bytes).getInt());
    }

    private MessageDigest initMessageDigest(String algorithm) {
        Objects.requireNonNull(algorithm);
        try {
            return MessageDigest.getInstance(algorithm.toUpperCase());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(format("Can not initialize MessageDigest object, message: %s",
                    e.getMessage()));
        }
    }
}
