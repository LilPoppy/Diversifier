package org.cobnet.mc.diversifier.plugin.support;

import org.cobnet.mc.diversifier.plugin.*;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public non-sealed abstract class AbstractProceduralPlatformContext<T extends ProceduralPlugin<T> & PlatformAssembly<T>> extends AbstractConfigurablePlatformContext<T> implements ProceduralPlatformContext<T> {

    protected AbstractProceduralPlatformContext(T plugin, Locale locale) {
        super(plugin, locale);
    }

    @Override
    public @NotNull ProxyFactory getProxyFactory() {
        return this;
    }

    @Override
    public @NotNull TypeFactory getTypeFactory() {
        return this.getPluginFactory().getTypeFactory();
    }

    @Override
    public @NotNull MemberFactory getMemberFactory() {
        return this.getTypeFactory().getMemberFactory();
    }
}
