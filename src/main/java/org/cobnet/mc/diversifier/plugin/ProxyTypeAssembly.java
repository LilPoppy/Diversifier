package org.cobnet.mc.diversifier.plugin;

import org.cobnet.mc.diversifier.exception.ProxyException;
import org.jetbrains.annotations.NotNull;

public interface ProxyTypeAssembly<T> extends TypeAssembly<T> {

    public @NotNull TypeAssembly<? super T> getOriginal();

    public @NotNull T create(String name, Object... args) throws ProxyException;

    public @NotNull ProxyContext<T> create(String name, Scope scope, Object... args) throws ProxyException;

    public @NotNull ProxyBuilder.Singleton<T> build(String name, Object... args);
}
