package org.cobnet.mc.diversifier.plugin;

import org.jetbrains.annotations.NotNull;

public interface ConfigurablePlatformContext<T extends ManagedPlugin & PlatformAssembly<T>> extends SecuredPlatformContext<T> {

    public @NotNull PluginConfiguration getConfiguration();
}
