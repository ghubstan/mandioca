package mandioca.bitcoin.function;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

// Stolen from http://codingjunkie.net/functional-iterface-exceptions


@FunctionalInterface
public interface ThrowingBiFunction<T, U, R> extends BiFunction<T, U, R> {

    @Override
    default R apply(T t, U u) {
        try {
            return applyThrows(t, u);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    R applyThrows(T t, U u) throws Exception;


    default <V> BiFunction<T, U, V> andThen(Function<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        try {
            return (T t, U u) -> after.apply(apply(t, u));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
