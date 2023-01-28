package org.cobnet.mc.diversifier.plugin;

import org.jetbrains.annotations.NotNull;

public interface AssemblyFactory {

    public boolean isPartOf(@NotNull Class<? extends Assembly<?, ?>> assembly);

    public @NotNull ProxyFactory getProxyFactory();

    public @NotNull TypeFactory getTypeFactory();

    public @NotNull MemberFactory getMemberFactory();
}
