package org.cobnet.mc.diversifier.plugin;

public interface PlatformAssembly<T extends ManagedPlugin> extends Assembly<PlatformContext<T>, PluginAssembly<?>> {

    public PlatformContext<T> getContext();

    public PluginInfo getPluginInfo();
}
