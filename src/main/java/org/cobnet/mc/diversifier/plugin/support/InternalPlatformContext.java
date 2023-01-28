package org.cobnet.mc.diversifier.plugin.support;

import org.cobnet.mc.diversifier.Diversifier;
import org.cobnet.mc.diversifier.MessageSource;
import org.cobnet.mc.diversifier.plugin.ManagedPlugin;
import org.cobnet.mc.diversifier.plugin.PlatformAssembly;
import org.cobnet.mc.diversifier.plugin.support.spigot.SpigotPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

sealed abstract class InternalPlatformContext<T extends ProceduralPlugin<T>> extends AbstractProceduralPlatformContext<T> implements MessageSource permits ManagedPluginFactory {

    protected InternalPlatformContext(T plugin) {
        super(plugin, Locale.getDefault());
    }

    @Override
    public @NotNull MessageSource getMessageSource() {
        return this;
    }

    @Override
    public boolean isThirdParty(@NotNull ManagedPlugin plugin) {
        return !(plugin instanceof SpigotPlugin);
    }

    @Override
    public boolean isThirdParty(@NotNull PlatformAssembly<?> assembly) {
        return !(assembly instanceof Diversifier<?>);
    }

    @Override
    public String getMessage(String key, Object... args) {
        return null;
    }

    @Override
    public String getMessage(String key, Locale locale, Object... args) {
        return null;
    }

    @Override
    public String getMessageOrDefault(String key, Locale locale, Object... args) {
        return null;
    }
}
