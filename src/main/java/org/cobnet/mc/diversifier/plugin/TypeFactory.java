package org.cobnet.mc.diversifier.plugin;

import org.cobnet.mc.diversifier.exception.MissingResourceException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

public interface TypeFactory {

    public @NotNull List<String> getPackageNames();

    public @NotNull Stream<TypeAssembly<?>> getTypesAsStream();

    public @NotNull TypeAssembly<?>[] getTypesByPackage(@NotNull String packageName);

    public @NotNull TypeAssembly<?>[] getAllTypesByPackage(@NotNull String packageName);

    public @Nullable <T> TypeAssembly<T> loadClass(@NotNull PluginAssembly<?> assembly, @NotNull Class<T> type) throws NoSuchMethodException;

    public @Nullable TypeAssembly<?> loadClass(@NotNull PluginAssembly<?> assembly, @NotNull String className);

    public @Nullable TypeAssembly<?> loadClass(@NotNull PluginAssembly<?> assembly, @NotNull String packageName, @NotNull String className);

    public @Nullable <T> TypeAssembly<T> getTypeAssembly(@NotNull Class<T> type);

    public @NotNull <T> TypeAssembly<? extends T>[] getSubTypesOf(@NotNull Class<T> type) throws MissingResourceException;

    public @NotNull <T> TypeAssembly<? super T>[] getSuperTypesOf(@NotNull Class<T> type) throws MissingResourceException;

    public int getSize();

}
