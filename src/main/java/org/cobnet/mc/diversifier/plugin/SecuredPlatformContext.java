package org.cobnet.mc.diversifier.plugin;

import org.jetbrains.annotations.NotNull;

public interface SecuredPlatformContext<T extends ManagedPlugin & PlatformAssembly<T>> extends PlatformContext<T> {

    public boolean isThirdParty(@NotNull ManagedPlugin plugin);

    public boolean isThirdParty(@NotNull PlatformAssembly<?> assembly);
}
