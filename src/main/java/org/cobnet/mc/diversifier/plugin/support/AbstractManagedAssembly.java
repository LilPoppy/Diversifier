package org.cobnet.mc.diversifier.plugin.support;

import lombok.Getter;
import org.cobnet.mc.diversifier.plugin.Assembly;
import org.cobnet.mc.diversifier.plugin.HierarchicalAssembly;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

public abstract class AbstractManagedAssembly<T, K extends Assembly<?, ?> ,V extends HierarchicalAssembly<?,?,?>> extends AbstractAssembly<T, V> implements HierarchicalAssembly<T, K, V> {

    @Getter
    private final K parent;

    protected final List<V> children;

    protected AbstractManagedAssembly(K parent, T instance, List<V> children) {
        super(instance);
        this.parent = parent;
        this.children = children;
    }

    @Override
    public @NotNull Stream<V> getChildrenAsStream() {
        return this.children.stream();
    }

    @Override
    public @NotNull String[] getChildrenNames() {
        return getChildrenAsStream().map(HierarchicalAssembly::getName).toArray(String[]::new);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AbstractManagedAssembly<?, ?, ?> other = (AbstractManagedAssembly<?, ?, ?>) o;
        return Objects.equals(children, other.children) && Objects.equals(parent, other.parent);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (children != null ? children.hashCode() : 0);
        return result;
    }
}
