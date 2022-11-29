package org.cobnet.mc.diversifier.plugin;

import org.cobnet.mc.diversifier.exception.ProxyException;
import org.jetbrains.annotations.NotNull;

public interface ProxyTypeAssembly<T> extends TypeAssembly<T> {

    public @NotNull TypeAssembly<? super T> getOriginal();

    @Override
    public @NotNull T create(Object... args) throws ProxyException;

    public @NotNull ProxyContext<T> create(String name, Scope scope, Object... args) throws ProxyException;
}
