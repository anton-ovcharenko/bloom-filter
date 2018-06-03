package oaa.bloomfilter

import spock.lang.Specification
import spock.lang.Unroll

import java.util.function.ToIntFunction

class BloomFilterTest extends Specification {

    ToIntFunction hashFunction1 = Mock(ToIntFunction)
    ToIntFunction hashFunction2 = Mock(ToIntFunction)

    def "contains should return true when put element and check that it exists"() {
        given: 'bloom filter with 2 hash functions'
        String element = "some value"
        int func1BitIndex = 1
        int func2BitIndex = 2
        BloomFilter bf = new BloomFilter(10, [hashFunction1, hashFunction2])

        when: 'put element to filter'
        bf.put(element)

        then: 'calculate hash value by each function'
        1 * hashFunction1.applyAsInt(element) >> func1BitIndex
        1 * hashFunction2.applyAsInt(element) >> func2BitIndex
        0 * _

        when: 'check that bf contains element'
        def result = bf.contains(element)

        then: 'hash functions return same values'
        1 * hashFunction1.applyAsInt(element) >> func1BitIndex
        1 * hashFunction2.applyAsInt(element) >> func2BitIndex
        0 * _
        and: 'contains(...) return true'
        result
    }

    def "contains should return false when put one element and check another"() {
        given: 'bloom filter with 2 hash functions'
        String element = "some value"
        String anotherElement = "another value"
        int func1BitIndex = 1
        int func2BitIndex = 2
        BloomFilter bf = new BloomFilter(100, [hashFunction1, hashFunction2])

        when: 'put element to filter'
        bf.put(element)

        then: 'calculate hash value by each function'
        1 * hashFunction1.applyAsInt(element) >> func1BitIndex
        1 * hashFunction2.applyAsInt(element) >> func2BitIndex
        0 * _

        when: 'check that bf contains another element'
        def result = bf.contains(anotherElement)

        then: 'first hash function return another value'
        1 * hashFunction1.applyAsInt(anotherElement) >> func1BitIndex + 10
        0 * _
        and: 'contains(...) return false'
        !result

        when: 'check that bf contains another element'
        result = bf.contains(anotherElement)

        then: 'second hash function return another value'
        1 * hashFunction1.applyAsInt(anotherElement) >> func1BitIndex
        1 * hashFunction2.applyAsInt(anotherElement) >> func2BitIndex + 10
        0 * _
        and: 'contains(...) return false'
        !result
    }

    def "contains should return true when element was not put but hash functions return existed hashes"() {
        given: 'bloom filter with 2 hash functions'
        String element1 = "some value1"
        String element2 = "some value2"
        String element3 = "some value3"
        int element1func1BitIndex = 1
        int element1func2BitIndex = 2
        int element2func1BitIndex = 3
        int element2func2BitIndex = 4
        BloomFilter bf = new BloomFilter(10, [hashFunction1, hashFunction2])

        when: 'put element1 to filter'
        bf.put(element1)

        then: 'calculate hash value by each function for element one'
        1 * hashFunction1.applyAsInt(element1) >> element1func1BitIndex
        1 * hashFunction2.applyAsInt(element1) >> element1func2BitIndex
        0 * _

        when: 'put element2 to filter'
        bf.put(element2)

        then: 'calculate hash value by each function for element two'
        1 * hashFunction1.applyAsInt(element2) >> element2func1BitIndex
        1 * hashFunction2.applyAsInt(element2) >> element2func2BitIndex
        0 * _

        when: 'check that bf contains element3'
        def result = bf.contains(element3)

        then: 'calculate hash value by each function'
        1 * hashFunction1.applyAsInt(element3) >> element1func1BitIndex
        1 * hashFunction2.applyAsInt(element3) >> element2func2BitIndex
        0 * _
        and: 'contains(...) return true'
        result
    }

    @Unroll
    "getFalsePositiveProbability should return valid value when k = 2 and #description"() {
        //based on tables in http://pages.cs.wisc.edu/~cao/papers/summary-cache/node8.html
        given:
        BloomFilter bf = new BloomFilter(100, [hashFunction1, hashFunction2])
        (1..elements).each { bf.put("$it") }

        when:
        def result = bf.getFalsePositiveProbability()

        then:
        (Math.round(result * 1000) / 1000) as double == expected

        where:
        description | expected | elements
        "m/n = 2"   | 0.4D     | 50
        "m/n = 3"   | 0.233D   | 33
        "m/n = 4"   | 0.155D   | 25
        "m/n = 5"   | 0.109D   | 20
    }
}