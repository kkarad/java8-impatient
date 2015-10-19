package org.kkarad.lambda;

import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class LambdaTest {

    /**
     * The comparator code runs on the same thread with the call to Arrays.sort
     */
    @Test
    public void exercise1() throws Exception {
        Integer[] i = {2, 3, 1};
        System.out.println(Arrays.toString(i));
        Arrays.sort(i, Integer::compare);
        System.out.println(Arrays.toString(i));
    }

    @Test
    public void exercise2() throws Exception {
        File[] dirs = new File("/").listFiles(File::isDirectory);
        System.out.println(Arrays.toString(dirs));
    }

    @Test
    public void exercise3() throws Exception {
        String ext = "java";
        String[] files = new File("./src/test/java/org/kkarad/lambda")
                .list((dir, name) -> name.endsWith(ext));
        System.out.println(Arrays.toString(files));
    }

    @Test
    public void exercise4() throws Exception {
        String basePath = "/home/kostas/dev/work/java8-impatient/src/test/resources/org/kkarad/lambda/ex4/";
        File[] files = new File[] {
                new File(basePath + "b-dir"),
                new File(basePath + "a.txt"),
                new File(basePath + "c.txt"),
                new File(basePath + "c-dir"),
                new File(basePath + "a-dir"),
                new File(basePath + "b.txt")
        };

        System.out.println(Arrays.toString(files));

        Arrays.sort(files, (o1, o2) -> {
            if((o1.isDirectory() && o2.isDirectory()) || (o1.isFile() && o2.isFile())) {
                return o1.getName().compareTo(o2.getName());
            } else {
                return o1.isDirectory() ? -1 : 1;
            }
        });

        System.out.println(Arrays.toString(files));
    }

    @Test
    public void exercise6() throws Exception {
        new Thread(uncheck(() -> {
            System.out.println("Zzz");
            Thread.sleep(1000);
        })).start();

        new Thread(uncheck2(() -> {
            System.out.println("Zzz");
            Thread.sleep(1000);
            return null;
        })).start();
    }

    private static Runnable uncheck2(Callable<Void> call) {
        return () -> {
            try {
                call.call();
            } catch (Exception e) {
                throw new RuntimeException("Execution exception thrown", e);
            }
        };
    }

    private static Runnable uncheck(RunnableEx ex) {
        return () -> {
            try {
                ex.run();
            } catch (Exception e) {
                throw new RuntimeException("Execution exception thrown", e);
            }
        };
    }

    @FunctionalInterface
    private interface RunnableEx {
        void run() throws Exception;
    }

    @Test
    public void exercise7() throws Exception {
        andThen(() -> System.out.print("Hello "), () -> System.out.println("world")).run();
    }

    private static Runnable andThen(Runnable firstRun, Runnable secondRun) {
        return () -> {
            firstRun.run();
            secondRun.run();
        };
    }

    @Test
    public void exercise8() throws Exception {
        final String[] names = {"Peter", "Paul", "Mary"};
        List<Runnable> runners = new ArrayList<>();

        for (String name : names) {
            runners.add(() -> System.out.println(name));
        }

        for (Runnable runner : runners) {
            new Thread(runner).start();
        }

        runners.clear();

        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            runners.add(() -> System.out.println(name)); //can't use names[i] in lambda as i is not final
        }

        for (Runnable runner : runners) {
            new Thread(runner).start();
        }
    }

    @Test
    public void exercise9() throws Exception {
        ArrayList2<String> strings = new ArrayList2<>();
        strings.add("1");
        strings.add("2");
        strings.add("3");

        strings.forEachIf(System.out::println, s -> s.equals("1"));
    }

    interface Collection2<T> extends Collection<T> {
        default void forEachIf(Consumer<T> action, Predicate<T> filter) {
            forEach(t -> {if (filter.test(t)) action.accept(t);});
        }
    }

    private class ArrayList2<T> extends ArrayList<T> implements Collection2<T> {}
}