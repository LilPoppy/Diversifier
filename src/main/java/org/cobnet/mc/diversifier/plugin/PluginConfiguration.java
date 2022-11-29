package org.cobnet.mc.diversifier.plugin;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface PluginConfiguration extends Configuration {

    public @NotNull List<String> getVersionOrder();

    public @NotNull String getVersionDefault();

    public @NotNull List<String> getClassLoaderFileExtensions();

    public @NotNull List<String> getClassLoaderExcludedPackages();

}
