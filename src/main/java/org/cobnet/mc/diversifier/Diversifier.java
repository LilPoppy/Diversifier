package org.cobnet.mc.diversifier;

import lombok.Getter;
import org.cobnet.mc.diversifier.plugin.*;
import org.cobnet.mc.diversifier.plugin.annotation.Signal;
import org.cobnet.mc.diversifier.plugin.annotation.Tester;
import org.cobnet.mc.diversifier.plugin.support.AbstractManagedPlatformContext;
import org.cobnet.mc.diversifier.plugin.support.ComparableVersion;
import org.cobnet.mc.diversifier.plugin.support.ProceduralPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Stream;

@Signal(name = "Diversifier")
@Tester
public record Diversifier<T extends ProceduralPlugin<T>>(@Getter Diversifier.PlatformContext<T> context) implements PlatformAssembly<T> {

    static Diversifier<?> INSTANCE;

    @Getter
    final static PluginInfo INFO = new PluginInfo() {
        @Override
        public @NotNull String getName() {
            return "Diversifier";
        }

        @Override
        public @NotNull List<String> getAuthors() {
            return List.of("LilPoppy");
        }

        @Override
        public @NotNull Version getVersion() {
            return new ComparableVersion(1, 0, 0);
        }
    };


    public static ConfigurablePlatformContext<?> startup(ProceduralPlugin<?> plugin) {
        return plugin.create();
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

    @Override
    public PluginInfo getPluginInfo() {
        return this.context.getPlugin().getPluginInfo();
    }

    public static class PlatformContext<T extends ProceduralPlugin<T>> extends AbstractManagedPlatformContext<T> {

        protected PlatformContext(T plugin) {
            super(plugin);
            Diversifier.INSTANCE = new Diversifier<>(this);
        }

        @Override
        public final boolean isThirdParty(@NotNull PlatformAssembly<?> assembly) {
            return !(assembly instanceof Diversifier);
        }
    }
}
