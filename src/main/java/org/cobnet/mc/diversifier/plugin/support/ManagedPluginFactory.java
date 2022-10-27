package org.cobnet.mc.diversifier.plugin.support;

import org.cobnet.mc.diversifier.Diversifier;
import org.cobnet.mc.diversifier.external.JarFileScanner;
import org.cobnet.mc.diversifier.plugin.*;
import org.cobnet.mc.diversifier.plugin.annotation.Signal;
import org.cobnet.mc.diversifier.reference.LoggerLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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
        PluginConfiguration config = (PluginConfiguration) Diversifier.getConfiguration();
        long ms = System.currentTimeMillis();
        PluginAssembly<T> assembly = this.getPluginAssembly(plugin);
        if(assembly != null)  return assembly;
        assembly = new ManagedPluginAssembly<>(plugin, version);
        this.plugins.add(assembly);
        TypeFactory factory = Diversifier.getTypeFactory();
        String path = plugin.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        try(JarFileScanner scanner = new JarFileScanner(path, config.getClassLoaderFileExtensions(), config.getClassLoaderExcludedPackages())) {
            Map<String, List<String>> map = scanner.scan();
            Iterator<Map.Entry<String, List<String>>> it = map.entrySet().iterator();
            Map.Entry<String, List<String>> entry;
            while (it.hasNext() && (entry = it.next()) != null) {
                String packageName = entry.getKey();
                List<String> classNames = entry.getValue();
                for (String name : classNames) {
                    if (factory.loadClass(assembly, packageName, name) != null) continue;
                    plugin.getLogger().log(Level.FINEST, "Failed to load class " + name + " in package " + packageName);
                }
            }
            Diversifier.getLogger().log(LoggerLevel.INFO, "Loaded " + factory.getSize() + " types for '" + plugin + "' in " + (System.currentTimeMillis() - ms) + "ms.");

            //DEBUGGING
            factory.getPackageNames().forEach(System.out::println);
            factory.getTypesAsStream().forEach(System.out::println);
            if(Diversifier.getProxyFactory() instanceof DynamicProxyFactory manager) {
                System.out.println(manager.create_proxy("test", factory.getTypeAssembly(Signal.class)).addMethod(new ManagedMethodAssembly(factory.getTypeAssembly(Signal.class), Signal.class.getMethod("name")), "TETETETE").getProxy().name());
            }
            //DEBUGGING

            return assembly;
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
