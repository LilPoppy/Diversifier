package org.cobnet.mc.diversifier.plugin;

import org.jetbrains.annotations.NotNull;

public interface ConfigurablePlatformContext<T extends ManagedPlugin> extends PlatformContext<T> {

    public @NotNull PluginConfiguration getConfiguration();
}
