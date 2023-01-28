package org.cobnet.mc.diversifier.plugin.support.spigot;

import org.bukkit.plugin.java.JavaPlugin;
import org.cobnet.mc.diversifier.Diversifier;
import org.cobnet.mc.diversifier.exception.ProxyException;
import org.cobnet.mc.diversifier.plugin.*;
import org.cobnet.mc.diversifier.plugin.annotation.ConfigurationProperty;
import org.cobnet.mc.diversifier.plugin.annotation.Signal;
import org.cobnet.mc.diversifier.plugin.annotation.Tester;
import org.cobnet.mc.diversifier.plugin.support.ProceduralPlugin;
import org.cobnet.mc.diversifier.plugin.support.spigot.listener.PluginEventListener;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.stream.Stream;

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
        ProxyFactory factory = Diversifier.getProxyFactory();
        System.out.println(factory.build(Tester.class, Diversifier.class).build());
        System.out.println(factory.build(Signal.class, Diversifier.class).build());
        ProxyAnnotationTypeAssembly<Signal> assembly = Diversifier.getTypeFactory().getProxyAnnotationTypeAssembly(Signal.class);
        System.out.println("#########" + assembly.create("Diversifier"));
        System.out.println("===========");
        Signal signal = factory.build(Signal.class, ProxyException.class).value("name", "Diversifier").build();
        System.out.println("@@@@@@@@@@" + signal.name());
        System.out.println(Arrays.toString(Diversifier.getProxyFactory().getProxies(Signal.class)));
    }

    @Override
    public @NotNull ProceduralPlatformContext<SpigotPlugin> create(Object... args) {
        SpigotPlatformContext context = new SpigotPlatformContext();
        this.getServer().getPluginManager().registerEvents(new PluginEventListener(context.getPlugin()), this);
        return context;
    }

    @Override
    public PluginInfo getPluginInfo() {
        return Diversifier.getPlatformContext().getPlugin().getPluginInfo();
    }

    @Override
    public @NotNull Stream<PluginAssembly<?>> getChildrenAsStream() {
        return Diversifier.getPlatformContext().getPlugin().getChildrenAsStream();
    }

    @Override
    public @NotNull String[] getChildrenNames() {
        return new String[0];
    }

    @Override
    public PlatformContext<SpigotPlugin> getContext() {
        return (PlatformContext<SpigotPlugin>) Diversifier.getPlatformContext();
    }


    final class SpigotPlatformContext extends Diversifier.PlatformContext<SpigotPlugin> {
        private SpigotPlatformContext() {
            super(SpigotPlugin.this);
        }
    }
}
