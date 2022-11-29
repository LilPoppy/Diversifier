package org.cobnet.mc.diversifier.plugin.support;

import org.cobnet.mc.diversifier.Diversifier;
import org.cobnet.mc.diversifier.external.JarFileScanner;
import org.cobnet.mc.diversifier.plugin.*;
import org.cobnet.mc.diversifier.reference.LoggerLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

abstract sealed class ManagedPluginFactory<T extends ProceduralPlugin<T>> extends AbstractProceduralPlatformContext<T> implements PluginFactory permits DynamicProxyFactory {

    private final TypeFactory factory;

    private volatile Node root;

    private transient Node current;

    protected ManagedPluginFactory(T plugin) {
        super(plugin);
        this.factory = new ManagedTypeFactory();
    }

    @Override
    public @NotNull Stream<PluginAssembly<?>> getPluginsAsStream() {
        return StreamSupport.stream(new Iterable(this.root).spliterator(), false);
    }

    @Override
    public @Nullable <E extends ManagedPlugin> PluginAssembly<E> getPluginAssembly(@NotNull E plugin) {
        Objects.requireNonNull(plugin, "Plugin cannot be null");
        Node node = find(plugin);
        return node == null ? null : (PluginAssembly<E>) node.assembly;
    }

    @Override
    public @NotNull String[] getPluginNames() {
        return getPluginsAsStream().map(Assembly::getName).toArray(String[]::new);
    }

    @Override
    public @NotNull TypeFactory getTypeFactory() {
        return this.factory;
    }

    @Override
    public @NotNull <E extends ManagedPlugin> PluginAssembly<E> loadPlugin(@NotNull PlatformAssembly<?> platform, @NotNull E plugin, @NotNull Version version) throws IOException {
        Objects.requireNonNull(plugin, "Plugin cannot be null.");
        Objects.requireNonNull(version, "Version cannot be null.");
        PluginConfiguration config = Diversifier.getConfiguration();
        long ms = System.currentTimeMillis();
        PluginAssembly<E> assembly = this.getPluginAssembly(plugin);
        if(assembly != null) return assembly;
        List<TypeAssembly<?>> children = new ArrayList<>();
        assembly = new ManagedPluginAssembly<>(platform, plugin, version, children);
        Node node = insert(assembly, children);
        TypeFactory factory = Diversifier.getTypeFactory();
        String path = plugin.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        try(JarFileScanner scanner = new JarFileScanner(path, config.getClassLoaderFileExtensions(), config.getClassLoaderExcludedPackages())) {
            Map<String, List<String>> map = scanner.scan();
            Iterator<Map.Entry<String, List<String>>> it = map.entrySet().iterator();
            Map.Entry<String, List<String>> entry;
            while (it.hasNext() && (entry = it.next()) != null) {
                String packageName = entry.getKey();
                List<String> classNames = entry.getValue();
                TypeAssembly<?> type;
                for (String name : classNames) {
                    if ((type = factory.loadClass(assembly, packageName, name)) == null) plugin.getLogger().log(LoggerLevel.TRACING, "Failed to load class " + name + " in package " + packageName);
                    else node.children.add(type);
                }
            }
            Diversifier.getLogger().log(LoggerLevel.DEBUG, "Loaded " + factory.getSize() + " types for '" + plugin + "' in " + (System.currentTimeMillis() - ms) + "ms.");
            return assembly;
        }
    }

    final Node find(ManagedPlugin plugin) {
        return find(plugin.getName());
    }

    final Node find(String name) {
        if(this.current != null && this.current.assembly.getName().hashCode() == name.hashCode()) return this.current;
        Node current = this.root;
        if(current == null) return null;
        while(current != null) {
            int cmp = Integer.compare(current.assembly.getName().hashCode(), name.hashCode());
            if(cmp > 0) current = current.next;
            else if(cmp == 0) return (this.current = current);
            else break;
        }
        return null;
    }

    final Node insert(PluginAssembly<?> assembly, List<TypeAssembly<?>> children) {
        Node node = new Node(assembly, children), current = this.root, prior = null;
        if(this.root == null) this.root = node;
        if(current == null) return node;
        do {
            int cmp = current.compareTo(node);
            if (cmp > 0) prior = current;
            else if (cmp == 0) {
                if(prior != null) prior.next = node;
                else this.root = node;
                node.next = current.next;
                return node;
            }
            else {
                node.next = current.next;
                return (current.next = node);
            }
        } while ((current = current.next) != null);
        return (prior.next = node);
    }

    final static class Iterable implements java.lang.Iterable<PluginAssembly<?>> {

        private final Node root;

        Iterable(Node root) {
            this.root = root;
        }
        @NotNull
        @Override
        public java.util.Iterator<PluginAssembly<?>> iterator() {
            return new Iterator();
        }

        final class Iterator implements java.util.Iterator<PluginAssembly<?>> {

            @Override
            public boolean hasNext() {
                return Iterable.this.root.next != null;
            }

            @Override
            public PluginAssembly<?> next() {
                return Iterable.this.root.next.assembly;
            }
        }
    }


    final static class Node implements Comparable<Node> {

        final PluginAssembly<?> assembly;
        Node next;
        final List<TypeAssembly<?>> children;

        Node(PluginAssembly<?> assembly, List<TypeAssembly<?>> children) {
            this.assembly = assembly;
            this.children = children;
        }

        @Override
        public int compareTo(@NotNull ManagedPluginFactory.Node o) {
            return Integer.compare(this.assembly.getName().hashCode(), o.assembly.getName().hashCode());
        }
    }
}
