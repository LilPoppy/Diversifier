package org.cobnet.mc.diversifier.plugin;

import org.cobnet.mc.diversifier.exception.ProxyException;
import org.jetbrains.annotations.NotNull;

public interface ProxyContext<T> {

    public @NotNull String getName() throws ProxyException;

    public @NotNull T getInstance() throws ProxyException;

    public <E extends ProxyContext<T>> @NotNull E getDelegate() throws ProxyException;

    public @NotNull Scope getScope() throws ProxyException;

    public @NotNull ProxyTypeAssembly<T> getAssembly() throws ProxyException;

    @SuppressWarnings("unchecked")
    public static <T> ProxyContext<T> from(T instance) {
        return ProxyContext.is(instance) ? (ProxyContext<T>) instance : null;
    }

    public static <T> boolean is(T instance) {
        return instance instanceof ProxyContext<?>;
    }

}
