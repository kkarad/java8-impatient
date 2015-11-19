package org.kkarad.lambda;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static java.util.Arrays.asList;

public class Chapter2Test {
    @Test
    public void exersise1() throws Exception {
        List<String> words = asList("test", "konstantinos", "karadamoglou", "biblos", "piraulokinitiras", "alexanderthegreat", "thanosomilisios", "thoukidides");

        final int processors = Runtime.getRuntime().availableProcessors();
        final int size = words.size();
        int segment = size / processors;
        if (segment < 1) segment = 1;

        List<Callable<Integer>> results = new ArrayList<>(processors);
        int fromIndex = 0;
        while (fromIndex < size) {
            int remainder = size - fromIndex;
            int toIndex = fromIndex + (segment > remainder ? remainder : segment) - 1;
            results.add(findLongWords(words, fromIndex, toIndex));
            fromIndex = toIndex + 1;
        }

        ExecutorService executor = Executors.newFixedThreadPool(processors);
        List<Future<Integer>> futures = executor.invokeAll(results);
        int sum = 0;
        for (Future<Integer> future : futures) {
            sum += future.get();
        }
        System.out.printf("Long words are %s", sum);
    }

    private Callable<Integer> findLongWords(List<String> words, int fromIndex, int toIndex) {
        return () -> {
            int counter = 0;
            for (int i = fromIndex; i < toIndex + 1; i++) {
                String word = words.get(i);
                if (word.length() >= 12) counter++;
            }
            return counter;
        };
    }

    @Test
    public void exercise2() throws Exception {
        List<String> words = asList("test", "konstantinos", "karadamoglou", "biblos", "piraulokinitiras", "alexanderthegreat", "thanosomilisios", "thoukidides");
        words.stream().limit(5).forEach(System.out::println);
    }

    @Test
    public void exercize3() throws Exception {
        //TODO
    }
}
