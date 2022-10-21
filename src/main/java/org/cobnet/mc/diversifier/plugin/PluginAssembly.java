package org.cobnet.mc.diversifier.plugin;

public interface PluginAssembly<T extends Plugin> extends Assembly<T, TypeAssembly<?>> {

    public Version getVersion();
}
