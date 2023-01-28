package org.cobnet.mc.diversifier.plugin.support;

public abstract non-sealed class AbstractManagedPlatformContext<T extends ProceduralPlugin<T>> extends DynamicProxyFactory<T> {

    protected AbstractManagedPlatformContext(T plugin) {
        super(plugin);
    }
}
