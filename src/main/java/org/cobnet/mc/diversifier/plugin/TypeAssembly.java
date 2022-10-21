package org.cobnet.mc.diversifier.plugin;

import org.jetbrains.annotations.NotNull;

public interface TypeAssembly<T> extends AnnotatedAssembly<Class<T>, PluginAssembly<?>, MemberAssembly<?, ?>> {

    public PluginAssembly<?> getPluginAssembly();

    public @NotNull TypeAssembly<? extends T>[] getSubTypes();

    public default boolean isSubTypeOf(@NotNull TypeAssembly<?> type) {
        return this.isSubTypeOf(type.get());
    }

    public boolean isSubTypeOf(@NotNull Class<?> type);

    public @NotNull TypeAssembly<? super T>[] getSuperTypes();

    public default boolean isSuperTypeOf(@NotNull TypeAssembly<?> type) {
        return this.isSuperTypeOf(type.get());
    }

    public boolean isSuperTypeOf(@NotNull Class<?> type);

    public default boolean isAssignableFrom(@NotNull TypeAssembly<?> type) {
        return this.isAssignableFrom(type.get());
    }

    public boolean isAssignableFrom(@NotNull Class<?> type);

    public default boolean isAnnotationType() {
        return this.get().isAnnotation();
    }

}
