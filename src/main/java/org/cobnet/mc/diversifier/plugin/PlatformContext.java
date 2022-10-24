package org.cobnet.mc.diversifier.plugin;

public interface PlatformContext<T extends Plugin> {

    public T getPlugin();

    public PlatformContext<T> run(Object... args);

    public PluginFactory getPluginFactory();

    public TypeFactory getTypeFactory();

    public MemberFactory getMemberFactory();

    public ProxyFactory getProxyFactory();

    public Configuration getConfiguration();
}
