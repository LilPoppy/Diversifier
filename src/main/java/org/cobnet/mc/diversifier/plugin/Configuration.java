package org.cobnet.mc.diversifier.plugin;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public interface Configuration {

    public @Nullable Object get(@NotNull String key);

    public @NotNull String[] getKeys();

    public boolean hasKey(@NotNull String key);

    public @Nullable Object set(@NotNull String key, @Nullable Object value);

    public void save() throws IOException;

    public void load() throws IOException;
}
