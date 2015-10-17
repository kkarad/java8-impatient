package org.kkarad.lambda;

import org.junit.Test;

import java.io.File;
import java.util.Arrays;

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
}