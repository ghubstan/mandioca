package mandioca.bitcoin.function;

import java.util.Objects;
import java.util.function.Function;

@FunctionalInterface
public interface ThrowingTriFunction<T, U, V, R> extends TriFunction<T, U, V, R> {

    @Override
    default R apply(T t, U u, V v) {
        try {
            return applyThrows(t, u, v);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    R applyThrows(T t, U u, V v) throws Exception;


    default <X> TriFunction<T, U, V, X> andThen(Function<? super R, ? extends X> after) {
        Objects.requireNonNull(after);
        try {
            return (T t, U u, V v) -> after.apply(apply(t, u, v));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
