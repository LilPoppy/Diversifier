package org.cobnet.mc.diversifier.plugin.support;

import org.cobnet.mc.diversifier.plugin.Configuration;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractConfiguration implements Configuration {

    protected final Map<String, Object> map = new HashMap<>();

    protected AbstractConfiguration() {}

    protected AbstractConfiguration(Map<String, Object> map) {
        this.map.putAll(map);
    }

    @Override
    public Object get(@NotNull String key) {
        return this.map.get(key);
    }

    @Override
    public String[] getKeys() {
        return this.map.keySet().toArray(String[]::new);
    }

    @Override
    public boolean hasKey(@NotNull String key) {
        return this.map.containsKey(key);
    }

    @Override
    public Object set(@NotNull String key, Object value) {
        return this.map.put(key, value);
    }
}
