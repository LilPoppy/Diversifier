package org.cobnet.mc.diversifier.plugin.support;

import org.cobnet.mc.diversifier.plugin.HierarchicalAssembly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public abstract class AbstractManagedAssembly<T, V extends HierarchicalAssembly<?, ?, ?>> extends AbstractAssembly<T, V> {

    protected final Set<V> children;

    protected AbstractManagedAssembly(T instance) {
        super(instance);
        this.children = new HashSet<>();
    }

    @Override
    public @NotNull List<V> getChildren(String name) {
        return this.children.stream().filter(child -> child.getName().equals(name)).toList();
    }

    @Override
    public @NotNull String[] getChildrenNames() {
        return children.stream().map(HierarchicalAssembly::getName).toArray(String[]::new);
    }

    @Override
    public boolean containsChild(String name) {
        return getChildren(name).size() > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AbstractManagedAssembly<?, ?> that = (AbstractManagedAssembly<?, ?>) o;
        return Objects.equals(children, that.children);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (children != null ? children.hashCode() : 0);
        return result;
    }
}
