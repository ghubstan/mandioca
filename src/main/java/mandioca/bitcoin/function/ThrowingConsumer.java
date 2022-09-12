package mandioca.bitcoin.function;

import java.util.Objects;
import java.util.function.Consumer;

@FunctionalInterface
public interface ThrowingConsumer<T> extends Consumer<T> {
    @Override
    default void accept(T t) {
        try {
            acceptThrows(t);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    void acceptThrows(T t) throws Exception;

    default ThrowingConsumer<T> andThen(Consumer<? super T> after) {
        Objects.requireNonNull(after);
        try {
            return (T t) -> {
                accept(t);
                after.accept(t);
            };
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
