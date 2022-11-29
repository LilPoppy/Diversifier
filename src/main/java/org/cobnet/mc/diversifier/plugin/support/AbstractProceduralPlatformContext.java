package org.cobnet.mc.diversifier.plugin.support;

import org.cobnet.mc.diversifier.plugin.PluginFactory;
import org.cobnet.mc.diversifier.plugin.ProceduralPlatformContext;
import org.cobnet.mc.diversifier.plugin.ProxyFactory;
import org.cobnet.mc.diversifier.plugin.TypeFactory;
import org.jetbrains.annotations.NotNull;

public abstract sealed class AbstractProceduralPlatformContext<T extends ProceduralPlugin<T>> extends AbstractConfigurablePlatformContext<T> implements ProceduralPlatformContext<T> permits ManagedPluginFactory {

    protected AbstractProceduralPlatformContext(T plugin) {
        super(plugin);
    }

    @Override
    public @NotNull ProxyFactory getProxyFactory() {
        return this;
    }

    @Override
    public @NotNull PluginFactory getPluginFactory() {
        return this;
    }

    @Override
    public @NotNull TypeFactory getTypeFactory() {
        return this.getPluginFactory().getTypeFactory();
    }
}
