package mandioca.bitcoin.function;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface ThrowingBiConsumer<T, U> extends BiConsumer<T, U> {

    // TODO TEST

    @Override
    default void accept(T t, U u) {
        try {
            acceptThrows(t, u);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    void acceptThrows(T t, U u) throws Exception;

    default ThrowingBiConsumer<T, U> andThen(BiConsumer<? super T, ? super U> after) {
        Objects.requireNonNull(after);
        try {
            return (l, r) -> {
                accept(l, r);
                after.accept(l, r);
            };
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
