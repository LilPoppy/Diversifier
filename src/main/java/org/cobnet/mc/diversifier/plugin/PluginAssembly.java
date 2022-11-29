package org.cobnet.mc.diversifier.plugin;

public interface PluginAssembly<T extends ManagedPlugin> extends HierarchicalAssembly<T, PlatformAssembly<?>, TypeAssembly<?>> {

    public Version getVersion();

    public ClassLoader getClassLoader();
}
