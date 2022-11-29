package org.cobnet.mc.diversifier.plugin.support;

import org.cobnet.mc.diversifier.plugin.PluginConfiguration;
import org.cobnet.mc.diversifier.plugin.annotation.ConfigurationProperty;
import org.cobnet.mc.diversifier.reference.GlobalPluginConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractConfiguration implements PluginConfiguration {

    final static String VERSION_ORDER_KEY;

    final static String VERSION_DEFAULT_KEY;

    final static String CLASS_LOADER_EXCLUDED_PACKAGES_KEY;

    final static String CLASS_LOADER_FILE_EXTENSIONS_KEY;

    static {
        try {
            VERSION_ORDER_KEY = GlobalPluginConfiguration.class.getDeclaredField("VERSION_ORDER").getAnnotation(ConfigurationProperty.class).name();
            VERSION_DEFAULT_KEY = GlobalPluginConfiguration.class.getDeclaredField("VERSION_DEFAULT").getAnnotation(ConfigurationProperty.class).name();
            CLASS_LOADER_EXCLUDED_PACKAGES_KEY = GlobalPluginConfiguration.class.getDeclaredField("CLASS_LOADER_EXCLUDED_PACKAGES").getAnnotation(ConfigurationProperty.class).name();
            CLASS_LOADER_FILE_EXTENSIONS_KEY = GlobalPluginConfiguration.class.getDeclaredField("CLASS_LOADER_FILE_EXTENSIONS").getAnnotation(ConfigurationProperty.class).name();
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

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

    @Override
    public @NotNull List<String> getVersionOrder() {
        return (List<String>) this.get(AbstractConfiguration.VERSION_ORDER_KEY);
    }

    @Override
    public @NotNull String getVersionDefault() {
        return (String) this.get(AbstractConfiguration.VERSION_DEFAULT_KEY);
    }

    @Override
    public @NotNull List<String> getClassLoaderFileExtensions() {
        return (List<String>) this.get(AbstractConfiguration.CLASS_LOADER_FILE_EXTENSIONS_KEY);
    }

    @Override
    public @NotNull List<String> getClassLoaderExcludedPackages() {
        return (List<String>) this.get(AbstractConfiguration.CLASS_LOADER_EXCLUDED_PACKAGES_KEY);
    }
}
