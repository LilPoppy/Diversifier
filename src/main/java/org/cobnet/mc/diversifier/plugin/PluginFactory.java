package org.cobnet.mc.diversifier.plugin;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;

public interface PluginFactory {

    public @Nullable <T extends Plugin> PluginAssembly<T> getPluginAssembly(@NotNull Plugin plugin);

    public @NotNull String[] getPluginNames();

    public @NotNull <T extends Plugin> PluginAssembly<T> loadPlugin(@NotNull T plugin, @NotNull Version version) throws IOException;
}
