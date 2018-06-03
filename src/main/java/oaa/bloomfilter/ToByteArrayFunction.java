package oaa.bloomfilter;

@FunctionalInterface
public interface ToByteArrayFunction<E> {
    byte[] getBytes(E value);
}
