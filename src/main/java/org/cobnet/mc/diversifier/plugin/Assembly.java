package org.cobnet.mc.diversifier.plugin;

import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;


public interface Assembly<T, V extends HierarchicalAssembly<?, ?, ?>> {

    public @NotNull String getName();

    public @NotNull Stream<V> getChildrenAsStream();

    public default @NotNull Stream<V> getChildrenAsStream(@NotNull String name) {
        return getChildrenAsStream().filter(child -> child.getName().equals(name));
    }

    public @NotNull String[] getChildrenNames();

    public default boolean containsChild(@NotNull String name) {
        return getChildrenAsStream().anyMatch(child -> child.getName().equals(name));
    }
}
