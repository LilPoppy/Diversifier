package org.cobnet.mc.diversifier.plugin.support.spigot;

import org.bukkit.plugin.java.JavaPlugin;
import org.cobnet.mc.diversifier.Diversifier;
import org.cobnet.mc.diversifier.plugin.PlatformContext;
import org.cobnet.mc.diversifier.plugin.Plugin;
import org.cobnet.mc.diversifier.plugin.support.spigot.listener.PluginEventListener;
import org.cobnet.mc.diversifier.plugin.support.*;
import org.cobnet.mc.diversifier.reference.GlobalPluginConfiguration;

import java.io.File;
import java.net.URISyntaxException;

public class SpigotPlatformContext<T extends JavaPlugin & Plugin> extends AbstractPlatformContext<T> {

    public SpigotPlatformContext(T plugin) throws URISyntaxException {
        super(plugin, new ManagedPluginFactory(), new ManagedTypeFactory(), new ManagedMemberFactory(), new DynamicProxyFactory(), new YAMLConfiguration(new File(new File(Diversifier.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toURI().resolve(".")).toPath().resolve(GlobalPluginConfiguration.PLUGIN_CONFIGURATION_DIRECTORY).resolve(GlobalPluginConfiguration.PLUGIN_CONFIGURATION_FILE_NAME).toFile()));
    }

    @Override
    public PlatformContext<T> run(Object... args) {
        this.getPlugin().getServer().getPluginManager().registerEvents(new PluginEventListener(), this.getPlugin());
        return this;
    }
}

