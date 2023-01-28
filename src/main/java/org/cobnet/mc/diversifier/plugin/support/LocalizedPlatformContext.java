package org.cobnet.mc.diversifier.plugin.support;

import lombok.Getter;
import org.cobnet.mc.diversifier.plugin.ManagedPlugin;
import org.cobnet.mc.diversifier.plugin.PlatformContext;

import java.util.Locale;

public abstract class LocalizedPlatformContext<T extends ManagedPlugin> implements PlatformContext<T> {

    @Getter
    private final T plugin;

    @Getter
    private final Locale locale;

    protected LocalizedPlatformContext(T plugin, Locale locale) {
        this.plugin = plugin;
        this.locale = locale;
    }
}
