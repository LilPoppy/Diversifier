package org.cobnet.mc.diversifier.plugin;

import java.util.logging.Logger;

public interface Plugin {

    public String getName();

    public void onInitialized();

    public Logger getLogger();

}
