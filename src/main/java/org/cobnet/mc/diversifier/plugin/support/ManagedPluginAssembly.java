package org.cobnet.mc.diversifier.plugin.support;

import lombok.Getter;
import org.cobnet.mc.diversifier.plugin.*;
import org.jetbrains.annotations.NotNull;

@Getter
public class ManagedPluginAssembly<T extends Plugin> extends AbstractManagedAssembly<T, TypeAssembly<?>> implements PluginAssembly<T> {

    private final Version version;

    protected ManagedPluginAssembly(T plugin, Version version) {
        super(plugin);
        this.version = version;
    }

    @Override
    public @NotNull String getName() {
        return this.get().getName();
    }
}
