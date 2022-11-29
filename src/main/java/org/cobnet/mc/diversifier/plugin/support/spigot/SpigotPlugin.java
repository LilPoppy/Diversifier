package org.cobnet.mc.diversifier.plugin.support.spigot;

import org.bukkit.plugin.java.JavaPlugin;
import org.cobnet.mc.diversifier.Diversifier;
import org.cobnet.mc.diversifier.plugin.ProceduralPlatformContext;
import org.cobnet.mc.diversifier.plugin.ProxyContext;
import org.cobnet.mc.diversifier.plugin.annotation.Signal;
import org.cobnet.mc.diversifier.plugin.annotation.Tester;
import org.cobnet.mc.diversifier.plugin.support.ProceduralPlugin;
import org.cobnet.mc.diversifier.plugin.support.spigot.listener.PluginEventListener;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public final class SpigotPlugin extends JavaPlugin implements ProceduralPlugin<SpigotPlugin> {

    @Override
    public void onEnable() {
        this.getLogger().setLevel(Level.ALL);
        Diversifier.startup(this);
    }

    @Override
    public void onDisable() {}

    @Override
    public void onInitialized() {
        System.out.println(Diversifier.getProxyFactory().create("Tester", Tester.class));
        System.out.println(Diversifier.getProxyFactory().create(Signal.class));
        Signal signal = Diversifier.getProxyFactory().create(ProxyContext.from(Diversifier.getProxyFactory().create(Tester.class)).getAssembly(), Signal.class).set("name", "Diversifier").build();
        System.out.println("@@@@@@@@@@" + signal.name());
    }

    @Override
    public @NotNull ProceduralPlatformContext<SpigotPlugin> create(Object... args) {
        SpigotPlatformContext context = new SpigotPlatformContext();
        this.getServer().getPluginManager().registerEvents(new PluginEventListener(context.getAssembly()), this);
        return context;
    }


    final class SpigotPlatformContext extends Diversifier.PlatformContext<SpigotPlugin> {
        private SpigotPlatformContext() {
            super(SpigotPlugin.this);
        }

    }
}
