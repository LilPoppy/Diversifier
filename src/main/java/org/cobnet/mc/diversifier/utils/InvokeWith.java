package org.cobnet.mc.diversifier.utils;

import lombok.Getter;
import org.cobnet.mc.diversifier.Delegate;

import java.util.function.Consumer;
import java.util.function.Function;

public record InvokeWith<T>(@Getter T delegator) implements Delegate<T> {

    public <E> E invoke(Function<T, E> function) {
        return function.apply(this.delegator);
    }

    public T call(Consumer<T> consumer) {
        consumer.accept(this.delegator);
        return this.delegator;
    }

    public static <T> InvokeWith<T> of(T delegator) {
        return new InvokeWith<>(delegator);
    }
}
