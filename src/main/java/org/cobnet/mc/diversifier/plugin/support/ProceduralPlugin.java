package org.cobnet.mc.diversifier.plugin.support;

import org.cobnet.mc.diversifier.plugin.ManagedPlugin;
import org.cobnet.mc.diversifier.plugin.ProceduralPlatformContext;
import org.jetbrains.annotations.NotNull;

public interface ProceduralPlugin<T extends ProceduralPlugin<T>> extends ManagedPlugin {

    public @NotNull ProceduralPlatformContext<T> create(Object... args);
}
