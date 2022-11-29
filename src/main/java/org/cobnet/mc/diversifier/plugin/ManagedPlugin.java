package org.cobnet.mc.diversifier.plugin;

import java.util.logging.Logger;

public interface ManagedPlugin {

    public String getName();

    public void onInitialized();

    public Logger getLogger();

}
