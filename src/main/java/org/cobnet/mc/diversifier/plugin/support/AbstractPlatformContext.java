package org.cobnet.mc.diversifier.plugin.support;

public abstract non-sealed class AbstractPlatformContext<T extends ProceduralPlugin<T>> extends DynamicProxyFactory<T> {

    protected AbstractPlatformContext(T plugin) {
        super(plugin);
    }
}
