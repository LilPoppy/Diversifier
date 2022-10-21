package org.cobnet.mc.diversifier.plugin.support;

import org.cobnet.mc.diversifier.Diversifier;
import org.cobnet.mc.diversifier.external.JarFileScanner;
import org.cobnet.mc.diversifier.plugin.*;
import org.cobnet.mc.diversifier.plugin.annotation.Signal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class ManagedPluginFactory implements PluginFactory {

    protected final List<PluginAssembly<?>> plugins;

    private transient PluginAssembly<?> current;

    public ManagedPluginFactory() {
        this.plugins = new ArrayList<>();
    }

    @Override
    public @Nullable <T extends Plugin> PluginAssembly<T> getPluginAssembly(@NotNull Plugin plugin) {
        Objects.requireNonNull(plugin, "Plugin cannot be null.");
        if(current != null && current.getName().equals(plugin.getName())) {
            return (PluginAssembly<T>) current;
        }
        for(PluginAssembly<?> assembly : this.plugins) {
            if(assembly.getName().equals(plugin.getName())) {
                this.current = assembly;
                return (PluginAssembly<T>) this.current;
            }
        }
        return null;
    }

    @Override
    public @NotNull String[] getPluginNames() {
        return this.plugins.stream().map(Assembly::getName).toArray(String[]::new);
    }

    @Override
    public @NotNull <T extends Plugin> PluginAssembly<T> loadPlugin(@NotNull T plugin, @NotNull Version version) throws IOException {
        Objects.requireNonNull(plugin, "Plugin cannot be null.");
        Objects.requireNonNull(version, "Version cannot be null.");
        long ms = System.currentTimeMillis();
        PluginAssembly<T> assembly = this.getPluginAssembly(plugin);
        if(assembly == null) {
            assembly = new ManagedPluginAssembly<>(plugin, version);
            this.plugins.add(assembly);
        }
        TypeFactory factory = Diversifier.getTypeFactory();
        String path = plugin.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        try(JarFileScanner scanner = new JarFileScanner(path)) {
            Map<String, List<String>> map = scanner.scan();
            Iterator<Map.Entry<String, List<String>>> it = map.entrySet().iterator();
            Map.Entry<String, List<String>> entry;
            while (it.hasNext() && (entry = it.next()) != null) {
                String packageName = entry.getKey();
                List<String> fileNames = entry.getValue();
                for (String fileName : fileNames) {
                    String className = fileName.substring(0, fileName.length() - 6);
                    if (factory.loadClass(assembly, packageName, className) != null) continue;
                    plugin.getLogger().log(Level.FINEST, "Failed to load class " + className + " in package " + packageName);
                }
            }
            Diversifier.getLogger().log(Level.INFO, "Loaded " + factory.getSize() + " types for '" + plugin + "-v" + version + "' in " + (System.currentTimeMillis() - ms) + "ms.");
            if(Diversifier.getProxyFactory() instanceof DynamicProxyFactory manager) {
                System.out.println(manager.create_proxy("test", factory.getTypeAssembly(Signal.class)).addMethod(new ManagedMethodAssembly(factory.getTypeAssembly(Signal.class), Signal.class.getMethod("name")), "TETETETE").getProxy().name());
            }
            return assembly;
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
