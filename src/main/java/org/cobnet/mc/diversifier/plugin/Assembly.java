package org.cobnet.mc.diversifier.plugin;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;


public interface Assembly<T, V extends HierarchicalAssembly<?, ?, ?>> {

    /**
     * Get the instance of the assembly represented by this context.
     * @return The instance of the context.
     */
    public @NotNull T get();

    /**
     * Get the name of the context.
     * @return The name of the assembly
     */
    public @NotNull String getName();

    /**
     * Get the children context of specific name.
     * @param name the name of the assembly
     * @return The child assembly
     */
    public @NotNull List<V> getChildren(String name);

    /**
     * Get all children context names of the assembly.
     * @return The children assemblies names
     */
    public @NotNull String[] getChildrenNames();

    /**
     * Return whether the assembly contains a child of the given name,
     * @return whether a child with the given name is defined in the assembly
     */
    public boolean containsChild(String name);

}
