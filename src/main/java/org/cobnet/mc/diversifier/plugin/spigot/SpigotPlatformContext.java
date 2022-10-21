package org.cobnet.mc.diversifier.plugin.spigot;

import org.bukkit.plugin.java.JavaPlugin;
import org.cobnet.mc.diversifier.plugin.PlatformContext;
import org.cobnet.mc.diversifier.plugin.Plugin;
import org.cobnet.mc.diversifier.plugin.spigot.listener.PluginEventListener;
import org.cobnet.mc.diversifier.plugin.support.*;

public class SpigotPlatformContext<T extends JavaPlugin & Plugin> extends AbstractPlatformContext<T> {

    public SpigotPlatformContext(T plugin) {
        super(plugin, new ManagedPluginFactory(), new ManagedTypeFactory(), new ManagedMemberFactory(), new DynamicProxyFactory());
    }

    @Override
    public PlatformContext<T> run(Object... args) {
        this.getPlugin().getServer().getPluginManager().registerEvents(new PluginEventListener(), this.getPlugin());
        return this;
    }
}

