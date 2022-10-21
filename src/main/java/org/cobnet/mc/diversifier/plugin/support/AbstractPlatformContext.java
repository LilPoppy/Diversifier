package org.cobnet.mc.diversifier.plugin.support;

import lombok.Getter;
import org.cobnet.mc.diversifier.plugin.*;

@Getter
public abstract class AbstractPlatformContext<T extends Plugin> implements PlatformContext<T> {

    private final T plugin;

    private final PluginFactory pluginFactory;

    private final TypeFactory typeFactory;

    private final MemberFactory memberFactory;

    private final ProxyFactory proxyFactory;

    protected AbstractPlatformContext(T plugin, PluginFactory pluginFactory, TypeFactory typeFactory, MemberFactory memberFactory, ProxyFactory proxyFactory) {
        this.plugin = plugin;
        this.pluginFactory = pluginFactory;
        this.typeFactory = typeFactory;
        this.memberFactory = memberFactory;
        this.proxyFactory = proxyFactory;
    }
}
