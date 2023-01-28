package org.cobnet.mc.diversifier.plugin.support;

import lombok.Getter;
import org.cobnet.mc.diversifier.Diversifier;
import org.cobnet.mc.diversifier.plugin.ManagedPlugin;
import org.cobnet.mc.diversifier.plugin.PlatformAssembly;
import org.cobnet.mc.diversifier.plugin.SecuredPlatformContext;
import org.cobnet.mc.diversifier.reference.LoggerLevel;

import java.util.Locale;

abstract sealed class SealedSecuredPlatformContext<T extends ManagedPlugin & PlatformAssembly<T>> extends LocalizedPlatformContext<T> implements SecuredPlatformContext<T> permits AbstractConfigurablePlatformContext {

    protected SealedSecuredPlatformContext(T plugin, Locale locale) {
        super(plugin, locale);
        if(this.isThirdParty((PlatformAssembly<?>) plugin)) {
            if(this.isThirdParty((ManagedPlugin) plugin)) plugin.getLogger().log(LoggerLevel.WARN, String.format("This plugin is not part of the official distribution from %s. Please be aware that this plugin may not be compatible with the platform and the security reason.", Diversifier.class.getSimpleName()));
            else plugin.getLogger().log(LoggerLevel.WARN, "The assembly ");
        } else if (this.isThirdParty((ManagedPlugin) plugin)) plugin.getLogger().log(LoggerLevel.INFO, String.format("This plugin is not part of the official distribution from %s. Please be aware that this plugin may not be compatible with the platform.", Diversifier.class.getSimpleName()));
    }

    protected String getThirdPartyPluginNotice() {
        return String.format("The platform context is no longer supported by %s, ");
    }

    protected String getThirdPartyAssemblyNotice(SecuredPlatformContext<?> context) {
        return String.format("All function", context.getPlugin().getName());
    }


    private static void DISPLAY_THIRD_PARTY_PLUGIN_MESSAGE(ManagedPlugin plugin) {
        String.format("The plugin %s is not part of the official distribution from %s. Please be aware that this plugin may not be compatible with the platform.", plugin.getName(), Diversifier.class.getSimpleName());
    }




}
