package org.cobnet.mc.diversifier.plugin;

import org.jetbrains.annotations.NotNull;

public interface Version extends Comparable<Version>, Cloneable {

    /**
     *
     * @return
     */
    public @NotNull String version();

    public @NotNull Version clone();
}
