package org.cobnet.mc.diversifier;

import lombok.Getter;
import org.cobnet.mc.diversifier.plugin.*;
import org.cobnet.mc.diversifier.plugin.annotation.Signal;
import org.cobnet.mc.diversifier.plugin.annotation.Tester;
import org.cobnet.mc.diversifier.plugin.support.AbstractPlatformContext;
import org.cobnet.mc.diversifier.plugin.support.ProceduralPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;
import java.util.stream.Stream;

@Signal(name = "Diversifier")
@Tester
public record Diversifier<T extends ProceduralPlugin<T>>(@Getter Diversifier.PlatformContext<?> context) implements PlatformAssembly<T> {

    static Diversifier<?> INSTANCE;

    public static ConfigurablePlatformContext<?> startup(ProceduralPlugin<?> plugin) {
        return plugin.create();
    }

    public static Logger getLogger() {
        return Diversifier.getPlatformContext().getPlugin().getLogger();
    }

    public static PluginFactory getPluginFactory() {
        return Diversifier.getPlatformContext().getPluginFactory();
    }

    public static TypeFactory getTypeFactory() {
        return Diversifier.getPlatformContext().getPluginFactory().getTypeFactory();
    }

    public static MemberFactory getMemberFactory() {
        return Diversifier.getTypeFactory().getMemberFactory();
    }

    public static ConfigurablePlatformContext<?> getPlatformContext() {
        return Diversifier.INSTANCE.context;
    }

    public static ProxyFactory getProxyFactory() {
        return Diversifier.getPlatformContext().getProxyFactory();
    }

    public static PluginConfiguration getConfiguration() {
        return Diversifier.getPlatformContext().getConfiguration();
    }

    @Override
    public @NotNull String getName() {
        return Diversifier.class.getSimpleName();
    }

    @Override
    public @NotNull Stream<PluginAssembly<?>> getChildrenAsStream() {
        return this.getContext().getPluginsAsStream();
    }

    @Override
    public @NotNull String[] getChildrenNames() {
        return this.getChildrenAsStream().map(Assembly::getName).toArray(String[]::new);
    }

    public static class PlatformContext<T extends ProceduralPlugin<T>> extends AbstractPlatformContext<T> {

        protected PlatformContext(T plugin) {
            super(plugin);
            Diversifier.INSTANCE = new Diversifier<T>(this);
        }

        public PlatformAssembly<?> getAssembly() {
            return Diversifier.INSTANCE;
        }
    }
}
