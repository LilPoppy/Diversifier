package org.cobnet.mc.diversifier.plugin.support;

import org.cobnet.mc.diversifier.Diversifier;
import org.cobnet.mc.diversifier.exception.MissingResourceException;
import org.cobnet.mc.diversifier.plugin.*;
import org.cobnet.mc.diversifier.utils.BooleanUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Stream;

public class ManagedTypeFactory implements TypeFactory {

    private final Node root = new Node();

    private List<String> packageNames;

    transient int size;

    private transient TypeAssembly<?> current;

    protected transient PackageNode node;

    @Override
    public @NotNull List<String> getPackageNames() {
        if(this.packageNames != null) return this.packageNames;
        this.packageNames = new ArrayList<>();
        this.packageNames.addAll(traverse_package_names(root, new StringBuilder()));
        return Collections.unmodifiableList(this.packageNames);
    }

    @Override
    public @NotNull Stream<TypeAssembly<?>> getTypesAsStream() {
        return traverse_package_types_stream(get_node(""));
    }

    @Override
    public @NotNull TypeAssembly<?>[] getTypesByPackage(@NotNull String packageName) {
        Objects.requireNonNull(packageName, "Package name cannot be null");
        return get_node(packageName).types.values().stream().toArray(TypeAssembly[]::new);
    }

    @Override
    public @NotNull TypeAssembly<?>[] getAllTypesByPackage(@NotNull String packageName) {
        Objects.requireNonNull(packageName, "Package name cannot be null.");
        return traverse_package_types_stream(get_node(packageName)).toArray(TypeAssembly[]::new);
    }

    @Override
    public @Nullable <T> TypeAssembly<T> loadClass(@NotNull PluginAssembly<?> assembly, @NotNull Class<T> type) throws NoSuchMethodException {
        Objects.requireNonNull(assembly, "Assembly cannot be null.");
        Objects.requireNonNull(type, "Type cannot be null.");
        String packageName = type.getPackageName();
        return load_class(get_node(packageName), assembly, type, packageName, type.getSimpleName());
    }

    @Override
    public @Nullable TypeAssembly<?> loadClass(@NotNull PluginAssembly<?> assembly, @NotNull String className) {
        Objects.requireNonNull(assembly, "Assembly cannot be null.");
        Objects.requireNonNull(className, "Class name cannot be null.");
        String[] nodes = className.split("\\.");
        if(nodes.length == 0) return null;
        String[] temp = new String[nodes.length - 1];
        System.arraycopy(nodes, 0, temp, 0, temp.length - 1);
        String packageName = String.join(".", temp);
        String name = nodes[nodes.length - 1];
        return loadClass(assembly, packageName, name);
    }

    @Override
    public @Nullable TypeAssembly<?> loadClass(@NotNull PluginAssembly<?> assembly, @NotNull String packageName, @NotNull String className) {
        Objects.requireNonNull(assembly, "Assembly cannot be null.");
        Objects.requireNonNull(packageName, "Package name cannot be null.");
        Objects.requireNonNull(className, "File name cannot be null.");
        Node node = get_node(packageName);
        TypeAssembly<?> type = node.get(className);
        if(type != null) return type;
        try {
            String path = packageName + "." + className;
            ClassLoader loader = assembly.get().getClass().getClassLoader();
            Class<?> clazz = loader.loadClass(path);
            TypeAssembly<?> result = load_class(node, assembly, clazz, packageName, className);
            if(assembly instanceof ManagedPluginAssembly<?> plugin) {
                plugin.children.add(result);
                return result;
            }
            throw new UnsupportedOperationException("Unsupported assembly type: " + assembly.getClass().getName());
        } catch (ClassNotFoundException | NoClassDefFoundError ex) {
            return null;
        }
    }

    private <T> TypeAssembly<T> load_class(Node node, PluginAssembly<?> assembly, Class<T> clazz, String packageName, String name) {
        if(node == null) node = get_node(packageName);
        this.packageNames = null;
        ManagedTypeAssembly<T> type = clazz.isAnnotation() ? new ManagedAnnotationTypeAssembly(assembly, clazz) : new ManagedTypeAssembly<>(assembly, clazz);
        if(node.types.put(name, type) == null) {
            size++;
            MemberFactory factory = Diversifier.getMemberFactory();
            for(Field field : type.get().getDeclaredFields()) {
                factory.loadMember(type, field);
            }
            for(Method method : type.get().getDeclaredMethods()) {
                factory.loadMember(type, method);
            }
            for(Constructor<?> constructor : type.get().getDeclaredConstructors()) {
                factory.loadMember(type, constructor);
            }
        }
        return type;
    }

    @Override
    public @Nullable <T> TypeAssembly<T> getTypeAssembly(@NotNull Class<T> type) {
        Objects.requireNonNull(type, "Type cannot be null.");
        String packageName = type.getPackageName();
        String name = type.getSimpleName();
        if(current == null || current.get() != type) this.current = get_node(packageName).get(name);
        return (TypeAssembly<T>) this.current;
    }

    @Override
    public @NotNull <T> TypeAssembly<? extends T>[] getSubTypesOf(@NotNull Class<T> type) throws MissingResourceException {
        Objects.requireNonNull(type, "Type cannot be null.");
        TypeAssembly<T> assembly = this.getTypeAssembly(type);
        if(assembly == null) throw new MissingResourceException(type.getPackageName(), type.getSimpleName());
        return assembly.getSubTypes();
    }

    @Override
    public <T> @NotNull TypeAssembly<? super T>[] getSuperTypesOf(@NotNull Class<T> type) throws MissingResourceException {
        Objects.requireNonNull(type, "Type cannot be null.");
        TypeAssembly<T> assembly = this.getTypeAssembly(type);
        if(assembly == null) throw new MissingResourceException(type.getPackageName(), type.getSimpleName());
        return assembly.getSuperTypes();
    }

    @Override
    public int getSize() {
        return this.size;
    }

    private Stream<TypeAssembly<?>> traverse_package_types_stream(Node node) {
        if(node == null) return Stream.empty();
        Stream<TypeAssembly<?>> types = Stream.empty();
        for(Node child : node.children) {
            types = Stream.concat(types, traverse_package_types_stream(child));
        }
        return Stream.concat(types, node.types.values().stream());
    }

    private List<String> traverse_package_names(Node node, StringBuilder sb) {
        List<String> packages = new ArrayList<>();
        if (node == null) return packages;
        int len = node.name.length() > 0 ? 1 : 0;
        if (len > 0) sb.append(node.name);
        if (node.children.isEmpty()) {
            packages.add(sb.toString());
            pop_end(sb, node.name.length(), sb.length());
            return packages;
        }
        packages.add(sb.toString());
        if (len > 0) sb.append(".");
        for (Node child : node.children) {
            packages.addAll(traverse_package_names(child, sb));
        }
        pop_end(sb, node.name.length() + len, sb.length());
        return packages;
    }

    private StringBuilder pop_end(StringBuilder sb, int n, int m) {
        return sb.delete(m - n, m);
    }

    private Node get_node(String packageName) {
        if(this.node != null && this.node.packageName.equals(packageName)) return this.node.node;
        char[] chars = packageName.toCharArray();
        Node node = root;
        for (int i = 0, j = 0, n = chars.length, k = 0; i < n; i++, k = BooleanUtils.toInt(n - 1 == i)) {
            if (chars[i] == '.' || k > 0) {
                String name = new String(chars, j, i - j + k);
                node = node.getChild(name);
                j = i + 1;
            }
        }
        this.node = new PackageNode(packageName, node);
        return this.node.node;
    }

    final static class PackageNode {

        final String packageName;

        final Node node;

        private PackageNode(String packageName, Node node) {
            this.packageName = packageName;
            this.node = node;
        }
    }

    final static class Node {
        private final String name;
        private final List<Node> children;
        private final Map<String, TypeAssembly<?>> types;
        private TypeAssembly<?> current;

        private Node(String name) {
            this.name = name;
            this.children = new ArrayList<>();
            this.types = new HashMap<>();
        }

        private Node() {
            this("");
        }

        private TypeAssembly<?> get(String name) {
            if(this.current != null && this.current.getName().equals(name)) return this.current;
            this.current = this.types.get(name);
            return this.current;
        }

        private Node getChild(String name) {
            for (Node child : children) if (child.name.equals(name)) return child;
            Node node = new Node(name);
            children.add(node);
            return node;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node node = (Node) o;
            return name.equals(node.name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }
}
