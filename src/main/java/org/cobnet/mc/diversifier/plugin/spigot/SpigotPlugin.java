package org.cobnet.mc.diversifier.plugin.spigot;

import org.bukkit.plugin.java.JavaPlugin;
import org.cobnet.mc.diversifier.Diversifier;
import org.cobnet.mc.diversifier.plugin.Plugin;

import java.util.logging.Level;

public class SpigotPlugin extends JavaPlugin implements Plugin {

    @Override
    public void onEnable() {
        try {
            this.getLogger().setLevel(Level.ALL);
            Diversifier.startup(this);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void onDisable() {
    }

    @Override
    public void onInitialized() {
    }
}
