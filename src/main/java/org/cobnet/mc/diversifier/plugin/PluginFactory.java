package org.cobnet.mc.diversifier.plugin;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.stream.Stream;

public interface PluginFactory {

    public @NotNull TypeFactory getTypeFactory();

    public @Nullable <T extends ManagedPlugin> PluginAssembly<T> getPluginAssembly(@NotNull T plugin);

    public @NotNull String[] getPluginNames();

    public @NotNull Stream<PluginAssembly<?>> getPluginsAsStream();

    public @NotNull <T extends ManagedPlugin> PluginAssembly<T> loadPlugin(@NotNull PlatformAssembly<?> platform, @NotNull T plugin, @NotNull Version version) throws IOException;
}
