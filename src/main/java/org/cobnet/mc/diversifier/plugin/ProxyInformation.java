package org.cobnet.mc.diversifier.plugin;

import org.jetbrains.annotations.NotNull;

public interface ProxyInformation<T> {

    public @NotNull String name();

    public @NotNull Scope scope();

    public @NotNull ProxyTypeAssembly<T> assembly();

    public @NotNull T instance();
}
