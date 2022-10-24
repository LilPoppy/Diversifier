package org.cobnet.mc.diversifier.plugin.spigot;

import org.bukkit.plugin.java.JavaPlugin;
import org.cobnet.mc.diversifier.Diversifier;
import org.cobnet.mc.diversifier.plugin.PlatformContext;
import org.cobnet.mc.diversifier.plugin.Plugin;
import org.cobnet.mc.diversifier.plugin.spigot.listener.PluginEventListener;
import org.cobnet.mc.diversifier.plugin.support.*;
import org.cobnet.mc.diversifier.reference.GlobalPluginConfiguration;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class SpigotPlatformContext<T extends JavaPlugin & Plugin> extends AbstractPlatformContext<T> {

    public SpigotPlatformContext(T plugin) throws URISyntaxException {
        super(plugin, new ManagedPluginFactory(), new ManagedTypeFactory(), new ManagedMemberFactory(), new DynamicProxyFactory(), new YAMLConfiguration(FileSystems.getDefault().getPath(Diversifier.class.getProtectionDomain().getCodeSource().getLocation().toURI().resolve(".").getPath()).resolve(GlobalPluginConfiguration.PLUGIN_CONFIGURATION_DIRECTORY).resolve(GlobalPluginConfiguration.PLUGIN_CONFIGURATION_FILE_NAME).toFile()));
    }

    @Override
    public PlatformContext<T> run(Object... args) {
        this.getPlugin().getServer().getPluginManager().registerEvents(new PluginEventListener(), this.getPlugin());
        return this;
    }
}

