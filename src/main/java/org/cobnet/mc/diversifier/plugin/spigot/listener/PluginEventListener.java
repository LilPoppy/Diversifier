package org.cobnet.mc.diversifier.plugin.spigot.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.cobnet.mc.diversifier.Diversifier;
import org.cobnet.mc.diversifier.plugin.Plugin;
import org.cobnet.mc.diversifier.plugin.support.ComparableVersion;

import java.io.IOException;

public class PluginEventListener implements Listener {

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) throws IOException {
        if(event.getPlugin() instanceof Plugin plugin) {
            Diversifier.getPluginFactory().loadPlugin(plugin, new ComparableVersion(event.getPlugin().getDescription().getVersion()));
            plugin.onInitialized();
        }

    }
}
