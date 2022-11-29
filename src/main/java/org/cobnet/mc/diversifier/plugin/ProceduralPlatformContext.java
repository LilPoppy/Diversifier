package org.cobnet.mc.diversifier.plugin;

import org.cobnet.mc.diversifier.plugin.support.ProceduralPlugin;

public interface ProceduralPlatformContext<T extends ProceduralPlugin<T>> extends ConfigurablePlatformContext<T>, ProxyFactory, PluginFactory {

}
