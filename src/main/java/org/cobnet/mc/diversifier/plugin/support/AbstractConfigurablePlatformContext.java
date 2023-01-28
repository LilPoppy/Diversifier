package org.cobnet.mc.diversifier.plugin.support;

import lombok.Getter;
import org.cobnet.mc.diversifier.Diversifier;
import org.cobnet.mc.diversifier.plugin.*;
import org.cobnet.mc.diversifier.reference.GlobalPluginConfiguration;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Locale;

sealed abstract class AbstractConfigurablePlatformContext<T extends ProceduralPlugin<T>> extends SealedSecuredPlatformContext<T> implements ConfigurablePlatformContext<T> permits AbstractProceduralPlatformContext {

    @Getter
    private final PluginConfiguration configuration;

    protected AbstractConfigurablePlatformContext(T plugin, Locale locale) {
        super(plugin, locale);
        try {
            this.configuration = new YAMLConfiguration(new File(new File(Diversifier.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toURI().resolve(".")).toPath().resolve(GlobalPluginConfiguration.PLUGIN_CONFIGURATION_DIRECTORY).resolve(GlobalPluginConfiguration.PLUGIN_CONFIGURATION_FILE_NAME).toFile());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
