package org.cobnet.mc.diversifier.plugin;

import org.jetbrains.annotations.NotNull;

public interface ProxyContext<T> {

    public @NotNull String getName();

    public @NotNull T getInstance();

    public <E extends ProxyContext<T>> @NotNull E getProxyContextInstance();

    public @NotNull Scope getScope();

    public @NotNull ProxyTypeAssembly<T> getAssembly();

    public static <T> ProxyContext<T> from(T instance) {
        return ProxyContext.is(instance) ? (ProxyContext<T>) instance : null;
    }

    public static <T> boolean is(T instance) {
        return instance instanceof ProxyContext<?>;
    }

}
