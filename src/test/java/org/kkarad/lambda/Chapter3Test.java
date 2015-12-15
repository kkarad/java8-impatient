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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

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

    @Test
    public void exersise9() throws Exception {
        final Person[] persons = {new Person("John", "Smith"), new Person("Paul", "Duncan"), new Person("Duncan", "Aaron")};
        Arrays.sort(persons, lexicographicComparator("lastName", "firstName"));
        System.out.println(Arrays.toString(persons));
    }

    private <T extends Object> Comparator<T> lexicographicComparator(String... fieldNames) {
        return (left, right) -> {
            for (String fieldName : fieldNames) {
                try {
                    final Field leftField = left.getClass().getDeclaredField(fieldName);
                    final Field rightField = right.getClass().getDeclaredField(fieldName);
                    final Object leftFieldValue = leftField.get(left);
                    final Object rightFieldValue = rightField.get(right);
                    final int comparisonResult = leftFieldValue.toString().compareTo(rightFieldValue.toString());
                    if (comparisonResult != 0) return comparisonResult;
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException("Unable to compare field: " + fieldName, e);
                }
            }
            return 0;
        };
    }

    private static class Person {
        final String firstName;
        final String lastName;

        public Person(String firstName, String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
        }

        @Override
        public String toString() {
            return "Person{" +
                    "firstName='" + firstName + '\'' +
                    ", lastName='" + lastName + '\'' +
                    '}';
        }
    }

    /**
     * Java's labdas support nominal but not structural
     */
    @Test
    public void exercise10() throws Exception {
        final UnaryOperator<Color> op = Color::brighter;
        //transforme(new Image("sailboat.jpg"), op.compose(Color::grayscale));
        op.compose(Color::grayscale);
    }

    Image transforme(Image in, UnaryOperator<Color> op) {
        return null;
    }

    @Test
    public void exercise11() throws Exception {
        Application.launch(Exercise11App.class);
    }

    static ColorTransformer compose(ColorTransformer before, ColorTransformer after) {
        return (x, y, colorAtXY) -> {
            final Color nextColor = before.apply(x, y, colorAtXY);
            return after.apply(x, y, nextColor);
        };
    }

    static ColorTransformer toColorTransformer(UnaryOperator<Color> unaryOp) {
        return (x, y, colorAtXY) -> unaryOp.apply(colorAtXY);
    }

    public static class Exercise11App extends Application {
        @Override
        public void start(Stage stage) throws Exception {
            Image image = new Image("sailboat.jpg");
            Image newImage = transform(image, compose(toColorTransformer(Color::brighter), frameColorTransformer(((int) image.getWidth()), ((int) image.getHeight()), Color.GRAY, 50)));
            stage.setScene(new Scene(new HBox(new ImageView(image), new ImageView(newImage))));
            stage.show();
        }
    }

    @Test
    public void exercise12() throws Exception {
        final Image in = new Image("sailboat.jpg");
        LatentImage latent = LatentImage.from(in).transform(Color::brighter).transform(frameColorTransformer((int) in.getWidth(), (int) in.getHeight(), Color.AQUA, 50));
    }

    static class LatentImage {
        private final Image in;
        private List<ColorTransformer> transformers = new ArrayList<>();

        private LatentImage(Image in) {
            this.in = in;
        }

        public static LatentImage from(Image in) {
            return new LatentImage(in);
        }

        public LatentImage transform(ColorTransformer transformer) {
            transformers.add(transformer);
            return this;
        }

        public LatentImage transform(UnaryOperator<Color> op) {
            transformers.add(toColorTransformer(op));
            return this;
        }

        public Image toImage() {
            int width = (int) in.getWidth();
            int height = (int) in.getHeight();
            WritableImage out = new WritableImage(width, height);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    Color c = in.getPixelReader().getColor(x, y);
                    for (ColorTransformer transformer : transformers) {
                        c = transformer.apply(x, y, c);
                    }
                    out.getPixelWriter().setColor(x, y, c);
                }
            }
            return out;
        }
    }
}
