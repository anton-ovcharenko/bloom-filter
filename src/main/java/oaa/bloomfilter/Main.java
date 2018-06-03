package oaa.bloomfilter;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.ToIntFunction;

import static java.lang.String.format;

public class Main {
    private static BloomFilter<String> bloomFilter;
    private static List<String> lines;

    public static void main(String[] args) {
        lines = getFileLines("wordlist.txt");
        System.out.println(lines.size());

        List<ToIntFunction<String>> hashFunctions = new SimpleHashFunctionsBuilder<String>()
                .toByteArrayFunction(String::getBytes)
                .amountOfFunctions(5)
                .build();
        bloomFilter = new BloomFilter<>(2_400_000, hashFunctions);
        for (String line : lines) {
            bloomFilter.put(line);
        }

        check("ABA");
        check("Something that does not exist");
        check("AIDS");
        check("йtrenness");

        System.out.println(format("False positive probability: %s", bloomFilter.getFalsePositiveProbability()));
    }

    private static void check(String value) {
        boolean existsInList = lines.contains(value);
        boolean existsByFilter = bloomFilter.contains(value);
        System.out.println(format("Result: %s (value: [%s], existsInList: %s, existsByFilter: %s)",
                existsByFilter == existsInList, value, existsInList, existsByFilter));
    }

    private static List<String> getFileLines(String fileName) {
        List<String> stringList = new ArrayList<>();
        try (Scanner scanner = new Scanner(new File("./src/main/resources/" + fileName))) {
            while (scanner.hasNextLine()) {
                stringList.add(scanner.nextLine());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return stringList;
    }
}