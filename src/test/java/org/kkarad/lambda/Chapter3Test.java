package org.kkarad.lambda;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.junit.Test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class Chapter3Test {

    Level currentLevel = Level.FINEST;

    @Test
    public void exercise1() throws Exception {
        int[] a = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
        int i = 11;
        logIf(Level.FINEST, () -> i == 10, () -> "a[10]= " + a[10]);

    }

    enum Level {
        ERROR, INFO, DEBUG, FINEST
    }

    void logIf(Level level, BooleanSupplier condition, Supplier<String> log) {
        if (currentLevel.ordinal() >= level.ordinal()) {
            if (condition.getAsBoolean()) System.out.println(log.get());
        }
    }

    @Test
    public void exercise2() throws Exception {
        ReentrantLock myLock = new ReentrantLock();
        withLock(myLock, () -> System.out.println("Hello world!"));
    }

    private void withLock(Lock lock, Runnable action) {
        lock.lock();
        try {
            action.run();
        } finally {
            lock.unlock();
        }
    }

    @Test(expected = AssertionError.class)
    public void exercise3() throws Exception {
        assertThat(() -> 1 + 1 == 3);
    }

    void assertThat(BooleanSupplier condition) {
        if (!condition.getAsBoolean()) throw new AssertionError("Condition not met");
    }

    @Test
    public void exercise5() throws Exception {
        Application.launch(Exercise5App.class);
    }

    public static Image transform(Image in, ColorTransformer colorTransformer) {
        int width = (int) in.getWidth();
        int height = (int) in.getHeight();
        WritableImage out = new WritableImage(width, height);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                out.getPixelWriter().setColor(x, y,
                        colorTransformer.apply(x, y, in.getPixelReader().getColor(x, y)));
            }
        }

        return out;
    }

    @FunctionalInterface
    interface ColorTransformer {
        Color apply(int x, int y, Color colorAtXY);
    }

    public static class Exercise5App extends Application {

        @Override
        public void start(Stage stage) throws Exception {
            Image image = new Image("sailboat.jpg");
            Image newImage = transform(image, (x, y, colorAtXY) -> {
                final int width = (int) image.getWidth();
                final int height = (int) image.getHeight();
                if (x <= 10 || y <= 10 || x >= width - 10 || y >= height - 10) {
                    return Color.GRAY;
                } else {
                    return colorAtXY;
                }
            });
            stage.setScene(new Scene(new HBox(new ImageView(image), new ImageView(newImage))));
            stage.show();
        }
    }

    @Test
    public void exercise6() throws Exception {
        Application.launch(Exercise6App.class);
    }

    public static class Exercise6App extends Application {
        @Override
        public void start(Stage stage) throws Exception {
            Image image = new Image("sailboat.jpg");
            Image newImage = transform(image, 1.5D, (color, factor) -> color.deriveColor(1, 1, factor, 1));
            stage.setScene(new Scene(new HBox(new ImageView(image), new ImageView(newImage))));
            stage.show();
        }
    }

    static <T> Image transform(Image in, T arg, BiFunction<Color, T, Color> f) {
        return transform(in, (x, y, colorAtXY) -> f.apply(colorAtXY, arg));
    }

    @Test
    public void exercise7() throws Exception {
        String[] words = new String[]{" test", "dimitrios", "poliorkitis", "biblos", "piraulokinitiras", "alexanderthegreat",
                "thalisomilisios", "Thoukidides", "perikleus", "aristoteles", "parapleuros", "katantia", "fthora", "ananeosi", "foul"};

        Arrays.sort(words, stringComparator(false, false, false));

        System.out.println(Arrays.toString(words));
    }

    static Comparator<String> stringComparator(boolean reverse, boolean caseSensitive, boolean spaceSensitive) {
        return (left, right) -> {
            left = spaceSensitive ? left : left.trim();
            right = spaceSensitive ? right : right.trim();
            left = caseSensitive ? left : left.toLowerCase();
            right = caseSensitive ? right : right.toLowerCase();
            return reverse ? right.compareTo(left) : left.compareTo(right);
        };
    }

    @Test
    public void exercise8() throws Exception {
        Application.launch(Exercise8App.class);
    }

    public static class Exercise8App extends Application {
        @Override
        public void start(Stage stage) throws Exception {
            Image image = new Image("sailboat.jpg");
            Image newImage = transform(image, frameColorTransformer(((int) image.getWidth()), ((int) image.getHeight()), Color.SALMON, 50));
            stage.setScene(new Scene(new HBox(new ImageView(image), new ImageView(newImage))));
            stage.show();
        }
    }

    static ColorTransformer frameColorTransformer(int width, int height, Color color, int thickness) {
        return (x, y, colorAtXY) -> {
            if (x <= thickness || y <= thickness || x >= width - thickness || y >= height - thickness) {
                return color;
            } else {
                return colorAtXY;
            }
        };
    }
}
