package oaa.bloomfilter;

import java.util.List;
import java.util.function.ToIntFunction;

public class BloomFilter<E> {

    private final boolean[] bitSet;
    private final List<ToIntFunction<E>> hashFunctionList;
    private int amountOfElements;

    /**
     * Create Bloom Filter implementation.
     *
     * @param bitsAmount       - amount of bits that will be used for filter.
     * @param hashFunctionList - family of hash functions that will be used for calculation of bits addresses.
     */
    public BloomFilter(int bitsAmount, List<ToIntFunction<E>> hashFunctionList) {
        this.bitSet = new boolean[bitsAmount];
        this.hashFunctionList = hashFunctionList;
    }

    /**
     * Puts an element in the filter.
     *
     * @param element - item that will be put.
     */
    public void put(E element) {
        for (ToIntFunction<E> hashFunction : hashFunctionList) {
            int bitIndex = getBitIndex(element, hashFunction);
            bitSet[bitIndex] = true;
        }
        amountOfElements++;
    }

    /**
     * Calculate index of bit in bitSet that corresponds to specified element and hashFunction.
     *
     * @param element      - item whose bit index is to be looked for.
     * @param hashFunction - function whose value is used for calculating bits index.
     * @return - index of bit in bitSet that corresponds to specified element and hashFunction.
     */
    private int getBitIndex(E element, ToIntFunction<E> hashFunction) {
        return hashFunction.applyAsInt(element) % bitSet.length;
    }

    /**
     * Check if the element was added in filter before.
     *
     * @param element - item whose presence is to be tested.
     * @return - true if the element might have been add to this filter,
     * false if the element is definitely have not been add.
     */
    public boolean contains(E element) {
        for (ToIntFunction<E> hashFunction : hashFunctionList) {
            int bitIndex = getBitIndex(element, hashFunction);
            if (!bitSet[bitIndex]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Calculate the probability of a false positive result based on added elements amount.
     *
     * @return - probability of a false positive result.
     */
    public double getFalsePositiveProbability() {
        // (1 - e ^ (- #func * #elem / #bits)) ^ #func
        int functionsAmount = hashFunctionList.size();
        double elementsPerBit = (double) amountOfElements / (double) bitSet.length;
        return Math.pow((1 - Math.exp(-functionsAmount * elementsPerBit)), functionsAmount);
    }
}
