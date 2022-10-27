package org.cobnet.mc.diversifier.plugin.support.spigot;

import org.bukkit.plugin.java.JavaPlugin;
import org.cobnet.mc.diversifier.Diversifier;
import org.cobnet.mc.diversifier.plugin.Plugin;

import java.net.URISyntaxException;
import java.util.logging.Level;

public class SpigotPlugin extends JavaPlugin implements Plugin {

    @Override
    public void onEnable() {
        this.getLogger().setLevel(Level.ALL);
        try {
            Diversifier.startup(this);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDisable() {
    }

    @Override
    public void onInitialized() {
    }
}
