package org.kkarad.lambda;

import org.junit.Ignore;
import org.junit.Test;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.lang.Math.abs;
import static java.util.Arrays.asList;

public class Chapter2Test {
    @Test
    public void exersise1() throws Exception {
        List<String> words = asList("test", "konstantinos", "karadamoglou", "biblos", "piraulokinitiras", "alexanderthegreat", "thalisomilisios", "thoukidides");

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
        List<String> words = asList("test", "konstantinos", "karadamoglou", "biblos", "piraulokinitiras", "alexanderthegreat",
                "thalisomilisios", "thoukidides", "perikleus", "aristoteles", "parapleuros", "katantia", "fthora", "ananeosi", "foul");

        long startSeq = System.nanoTime();
        final Predicate<String> longWordPredicate = s -> s.length() >= 12;
        long counter = words.stream().filter(longWordPredicate).count();
        long sequeceDuration = System.nanoTime() - startSeq;
        System.out.printf("Serial stream counter: %s, duration: %s ns%n", counter, sequeceDuration);

        long startPar = System.nanoTime();
        counter = words.parallelStream().filter(longWordPredicate).count();
        long parallelDuration = System.nanoTime() - startPar;
        System.out.printf("Parallel stream counter: %s, duration: %s ns%n", counter, parallelDuration);

        System.out.printf("%s stream is faster by %s nanoseconds%n%n%n",
                parallelDuration > sequeceDuration ? "Parallel" : "Sequence", abs(parallelDuration - sequeceDuration));

        List<String> warAndPeaceWords = Files.lines(Paths.get(ClassLoader.getSystemResource("war-and-peace.txt").toURI()))
                .flatMap(line -> Arrays.stream(line.split("\\W+"))).collect(Collectors.toCollection(ArrayList<String>::new));

        long startWpSeq = System.nanoTime();
        long counterWp = warAndPeaceWords.stream().filter(longWordPredicate).count();
        long sequenceWpDuration = System.nanoTime() - startWpSeq;
        System.out.printf("Serial war and peace stream counter: %s, duration: %s ns%n", counterWp, sequenceWpDuration);

        long startWpPar = System.nanoTime();
        counterWp = warAndPeaceWords.parallelStream().filter(longWordPredicate).count();
        long parallelWpDuration = System.nanoTime() - startWpPar;
        System.out.printf("Parallel war and peace stream counter: %s, duration: %s ns%n", counterWp, parallelWpDuration);
        System.out.printf("%s war and peace stream is faster by %s nanoseconds%n%n%n",
                parallelWpDuration > sequenceWpDuration ? "Parallel" : "Sequence", abs(parallelWpDuration - sequenceWpDuration));
    }

    @Test
    public void exercise4() throws Exception {
        int[] values = {1, 4, 9, 16};
        Stream<int[]> stream = Stream.of(values); //autoboxing doesn't work for primitive arrays

        Integer[] typedValues = {1, 4, 9, 16};
        Stream<Integer> typedStream = Stream.of(typedValues);

        IntStream intStream = IntStream.of(values);
        intStream.forEach(System.out::println);
    }

    @Test
    public void exercise5() throws Exception {
        randomStream(System.currentTimeMillis(), 25214903917L, 11L, new BigInteger("2").pow(48).longValue())
                .limit(1000)
                .forEach(System.out::println);
    }

    private Stream<Long> randomStream(long seed, long a, long c, long m) {
        return Stream.iterate(seed, aLong -> (a * aLong + c) % m);
    }

    @Test
    public void exercise6() throws Exception {
        String value = "Hello World";
        Stream<Character> characterStream = toCharacterStream(value);
        characterStream.forEach(System.out::print);
    }

    private Stream<Character> toCharacterStream(String value) {
        return IntStream.range(0, value.length()).mapToObj(value::charAt);
        //return Stream.iterate(0, i -> i + 1).limit(value.length()).map(value::charAt);
    }

    /**
     * We can't determine whether a Stream is finite of not. It's backing data structure is abstracted away.
     * This is a wrong implementation of which test never ends.
     */
    @Test
    @Ignore
    public void exercise7() throws Exception {
        Stream<Double> infiniteStream = Stream.generate(Math::random);
        System.out.println(isFinite(infiniteStream));
    }

    static <T> boolean isFinite(Stream<T> stream) {
        return stream.count() > 0;
    }

    @Test
    public void exercise8() throws Exception {
        zip(Stream.of(1, 3, 5, 7), Stream.of(2, 4, 6, 8)).forEach(System.out::println);
    }

    static <T> Stream<T> zip(Stream<T> first, Stream<T> second) {
        Iterator<T> firstIterator = first.iterator();
        Iterator<T> secondIterator = second.iterator();
        final Iterator<T> zipIterator = new Iterator<T>() {
            boolean alternate = true;

            @Override
            public boolean hasNext() {
                return firstIterator.hasNext() || secondIterator.hasNext();
            }

            @Override
            public T next() {
                final Iterator<T> nextIterator = alternate ? firstIterator : secondIterator;
                alternate = !alternate;
                return nextIterator.next();
            }
        };

        final Iterable<T> iterable = () -> zipIterator;
        return StreamSupport.stream(iterable.spliterator(), false);
    }
}
