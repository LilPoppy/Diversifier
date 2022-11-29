package org.cobnet.mc.diversifier.plugin;

import org.jetbrains.annotations.NotNull;

public interface PlatformContext<T extends ManagedPlugin> {

    public @NotNull T getPlugin();

    public @NotNull PluginFactory getPluginFactory();

    public @NotNull ProxyFactory getProxyFactory();

    //public @NotNull FunctionFactory getFunctionFactory();
}
