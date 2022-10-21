package org.cobnet.mc.diversifier.plugin;

import org.jetbrains.annotations.NotNull;

public interface HierarchicalAssembly<T, K extends Assembly<?, ?>, V extends HierarchicalAssembly<?, ?, ?>> extends Assembly<T, V> {

    /**
     * Get the parent node of the assembly.
     * @return The parent assembly node
     */
    public @NotNull K getParent();
}
