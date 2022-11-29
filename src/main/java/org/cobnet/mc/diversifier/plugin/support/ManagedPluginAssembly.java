package org.cobnet.mc.diversifier.plugin.support;

import lombok.Getter;
import org.cobnet.mc.diversifier.plugin.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

final class ManagedPluginAssembly<T extends ManagedPlugin> extends AbstractManagedAssembly<T, PlatformAssembly<?>, TypeAssembly<?>> implements PluginAssembly<T> {

    @Getter
    private final Version version;

    ManagedPluginAssembly(PlatformAssembly<?> parent, T plugin, Version version, List<TypeAssembly<?>> children) {
        super(parent, plugin, children);
        this.version = version;
    }

    @Override
    public @NotNull String getName() {
        return this.instance.getName();
    }

    @Override
    public ClassLoader getClassLoader() {
        return this.instance.getClass().getClassLoader();
    }
}
