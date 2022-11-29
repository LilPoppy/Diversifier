package org.cobnet.mc.diversifier.plugin;

import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public interface HierarchicalAssembly<T, K extends Assembly<?, ?>, V extends HierarchicalAssembly<?, ?, ?>> extends Assembly<T, V> {

    public @NotNull K getParent();

    @NotNull default Stream<Assembly<?, ?>> getAllChildrenAsStream() {
        return getChildrenAsStream().flatMap(child -> child.getAllChildrenAsStream());
    }

    @NotNull default Stream<Assembly<?, ?>> getAllChildrenAsStream(String name) {
        return getAllChildrenAsStream().filter(child -> child.getName().equals(name));
    }

    @Override
    default boolean containsChild(@NotNull String name) {
        return getAllChildrenAsStream().anyMatch(child -> child.getName().equals(name));
    }
}
