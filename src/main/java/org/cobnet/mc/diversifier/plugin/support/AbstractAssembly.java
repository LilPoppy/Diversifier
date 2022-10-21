package org.cobnet.mc.diversifier.plugin.support;

import org.cobnet.mc.diversifier.plugin.Assembly;
import org.cobnet.mc.diversifier.plugin.HierarchicalAssembly;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class AbstractAssembly<T, V extends HierarchicalAssembly<?, ?, ?>> implements Assembly<T, V> {

    private final T instance;

    protected AbstractAssembly(T instance) {
        this.instance = instance;
    }

    @Override
    public @NotNull T get() {
        return this.instance;
    }

    @Override
    public String toString() {
        return this.get().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractAssembly<?, ?> that = (AbstractAssembly<?, ?>) o;
        return Objects.equals(instance, that.instance);
    }

    @Override
    public int hashCode() {
        return instance != null ? instance.hashCode() : 0;
    }
}
