package org.cobnet.mc.diversifier;

import org.cobnet.mc.diversifier.plugin.*;
import org.cobnet.mc.diversifier.plugin.annotation.Signal;
import org.cobnet.mc.diversifier.plugin.annotation.Tester;
import org.cobnet.mc.diversifier.plugin.spigot.SpigotPlatformContext;
import org.cobnet.mc.diversifier.plugin.spigot.SpigotPlugin;
import org.cobnet.mc.diversifier.plugin.support.ComparableVersion;

import java.lang.management.ManagementFactory;
import java.util.logging.Logger;

@Signal(name = "Diversifier")
@Tester
public final class Diversifier {

    final static String REQUIRED_VM_ADD_OPEN = "--add-opens=java.base/java.lang=ALL-UNNAMED";

    private static PlatformContext<?> CONTEXT;

    public static PlatformContext<?> startup(Plugin plugin) throws RuntimeException {
        if(!Diversifier.isLegalAccessing()) throw new RuntimeException("Illegal accessing detected! For 'Java-" + Runtime.version() + "' please add the following VM argument: '" + Diversifier.REQUIRED_VM_ADD_OPEN + "' and re-run it again.");
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

    static boolean isLegalAccessing() {
        return new ComparableVersion(Runtime.version().toString()).compareTo(new ComparableVersion("16")) < 0 || ManagementFactory.getRuntimeMXBean().getInputArguments().contains(REQUIRED_VM_ADD_OPEN);
    }
}
