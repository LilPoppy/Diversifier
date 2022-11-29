package org.cobnet.mc.diversifier.plugin.support;

import org.cobnet.mc.diversifier.plugin.MemberAssembly;
import org.cobnet.mc.diversifier.plugin.PluginAssembly;
import org.cobnet.mc.diversifier.plugin.ProxyTypeAssembly;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ProxyTypeAssemblyGenerator<T> {

    public <E extends T> @NotNull ProxyTypeAssembly<E> generate(@NotNull PluginAssembly<?> plugin, @NotNull Class<E> type, @NotNull List<MemberAssembly<E, ?>> members);

    public <E extends T>  @NotNull ProxyTypeAssembly<E> generate(@NotNull Class<E> type, @NotNull List<MemberAssembly<E, ?>> members);
}
