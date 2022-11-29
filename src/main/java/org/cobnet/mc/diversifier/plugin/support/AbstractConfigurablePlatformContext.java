package org.cobnet.mc.diversifier.plugin.support;

import lombok.Getter;
import org.cobnet.mc.diversifier.Diversifier;
import org.cobnet.mc.diversifier.plugin.ConfigurablePlatformContext;
import org.cobnet.mc.diversifier.plugin.ManagedPlugin;
import org.cobnet.mc.diversifier.plugin.PluginConfiguration;
import org.cobnet.mc.diversifier.reference.GlobalPluginConfiguration;

import java.io.File;
import java.net.URISyntaxException;

abstract sealed class AbstractConfigurablePlatformContext<T extends ManagedPlugin> implements ConfigurablePlatformContext<T> permits AbstractProceduralPlatformContext {

    @Getter
    private final T plugin;

    @Getter
    private final PluginConfiguration configuration;

    protected AbstractConfigurablePlatformContext(T plugin) {
        this.plugin = plugin;
        try {
            this.configuration = new YAMLConfiguration(new File(new File(Diversifier.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toURI().resolve(".")).toPath().resolve(GlobalPluginConfiguration.PLUGIN_CONFIGURATION_DIRECTORY).resolve(GlobalPluginConfiguration.PLUGIN_CONFIGURATION_FILE_NAME).toFile());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
