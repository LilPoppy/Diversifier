package org.cobnet.mc.diversifier.plugin;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface PluginInfo {

    public @NotNull String getName();

    public @NotNull List<String> getAuthors();

    public @NotNull Version getVersion();
}
