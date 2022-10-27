package org.cobnet.mc.diversifier;

import org.cobnet.mc.diversifier.plugin.*;
import org.cobnet.mc.diversifier.plugin.annotation.Signal;
import org.cobnet.mc.diversifier.plugin.annotation.Tester;
import org.cobnet.mc.diversifier.plugin.support.spigot.SpigotPlatformContext;
import org.cobnet.mc.diversifier.plugin.support.spigot.SpigotPlugin;

import java.net.URISyntaxException;
import java.util.logging.Logger;

@Signal(name = "Diversifier")
@Tester
public final class Diversifier {
    private static PlatformContext<?> CONTEXT;

    public static PlatformContext<?> startup(Plugin plugin) throws RuntimeException, URISyntaxException {
        if(plugin instanceof SpigotPlugin spigot) {
            Diversifier.CONTEXT = new SpigotPlatformContext(spigot).run();
        }
        return Diversifier.CONTEXT;
    }

    public static Logger getLogger() {
        return Diversifier.CONTEXT.getPlugin().getLogger();
    }

    public static PluginFactory getPluginFactory() {
        return Diversifier.CONTEXT.getPluginFactory();
    }

    public static TypeFactory getTypeFactory() {
        return Diversifier.CONTEXT.getTypeFactory();
    }

    public static MemberFactory getMemberFactory() {
        return Diversifier.CONTEXT.getMemberFactory();
    }

    public static ProxyFactory getProxyFactory() {
        return Diversifier.CONTEXT.getProxyFactory();
    }

    public static Configuration getConfiguration() {
        return Diversifier.CONTEXT.getConfiguration();
    }
}
