package org.cobnet.mc.diversifier.plugin.support.spigot.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.cobnet.mc.diversifier.Diversifier;
import org.cobnet.mc.diversifier.plugin.ManagedPlugin;
import org.cobnet.mc.diversifier.plugin.PlatformAssembly;
import org.cobnet.mc.diversifier.plugin.support.ComparableVersion;

import java.io.IOException;

public class PluginEventListener implements Listener {

    private final PlatformAssembly<?> platform;

    public PluginEventListener(PlatformAssembly<?> platform) {
        this.platform = platform;
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) throws IOException {
        if(event.getPlugin() instanceof ManagedPlugin plugin) {
            Diversifier.getPluginFactory().loadPlugin(platform, plugin, new ComparableVersion(event.getPlugin().getDescription().getVersion()));
            plugin.onInitialized();
        }

    }
}
