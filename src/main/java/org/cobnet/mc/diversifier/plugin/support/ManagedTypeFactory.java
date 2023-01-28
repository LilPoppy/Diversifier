package org.cobnet.mc.diversifier.plugin.support;

import org.cobnet.mc.diversifier.exception.MissingResourceException;
import org.cobnet.mc.diversifier.exception.ProxyException;
import org.cobnet.mc.diversifier.plugin.*;
import org.cobnet.mc.diversifier.utils.BooleanUtils;
import org.cobnet.mc.diversifier.utils.LoopUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Stream;

final class ManagedTypeFactory implements TypeFactory {

    private final Node root = new Node();

    private List<String> packageNames;

    transient int size;

    private transient TypeAssembly<?> current;

    transient PackageNode record;

    transient MemberFactory factory;

    ManagedTypeFactory() {
        this.factory = new ManagedMemberFactory(this);
    }

    @Override
    public @NotNull MemberFactory getMemberFactory() {
        return this.factory;
    }

    @Override
    public @NotNull List<String> getPackageNames() {
        if(this.packageNames != null) return this.packageNames;
        this.packageNames = new ArrayList<>();
        this.packageNames.addAll(get_package_names(root, new StringBuilder()));
        return Collections.unmodifiableList(this.packageNames);
    }

    @Override
    public @NotNull Stream<TypeAssembly<?>> getTypesAsStream() {
        return get_all_package_types__stream(get_node(""));
    }


    @Override
    public @NotNull TypeAssembly<?>[] getTypesByPackage(@NotNull String packageName) {
        Objects.requireNonNull(packageName, "Package name cannot be null.");
        return get_node(packageName).types.values().toArray(TypeAssembly[]::new);
    }

    @Override
    public @NotNull TypeAssembly<?>[] getAllTypesByPackage(@NotNull String packageName) {
        Objects.requireNonNull(packageName, "Package name cannot be null.");
        return get_all_package_types__stream(get_node(packageName)).toArray(TypeAssembly[]::new);
    }


    @Override
    public @Nullable <T, E extends TypeAssembly<T>> E loadClass(@NotNull PluginAssembly<?> assembly, @NotNull Class<T> type) {
        Objects.requireNonNull(assembly, "Assembly cannot be null.");
        Objects.requireNonNull(type, "Type cannot be null.");
        String packageName = type.getPackageName();
        return load_class(get_node(packageName), assembly, type, packageName, type.getSimpleName());
    }

    @Override
    public @Nullable <E extends TypeAssembly<?>> E loadClass(@NotNull PluginAssembly<?> assembly, @NotNull String className) {
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
    public @Nullable <E extends TypeAssembly<?>> E loadClass(@NotNull PluginAssembly<?> assembly, @NotNull String packageName, @NotNull String className) {
        Objects.requireNonNull(assembly, "Assembly cannot be null.");
        Objects.requireNonNull(packageName, "Package name cannot be null.");
        Objects.requireNonNull(className, "Class name cannot be null.");
        E type = null;
        Node node = get_node(packageName);
        TypeInfo<E> info = (TypeInfo<E>) node.get(className);
        if(info != null) type = (E) info.assembly;
        if(type != null) return type;
        try {
            String path = packageName + "." + className;
            ClassLoader loader = assembly.getClassLoader();
            Class<?> clazz = loader.loadClass(path);
            TypeAssembly<?> result = load_class(node, assembly, clazz, packageName, className);
            if(assembly instanceof ManagedPluginAssembly<?> plugin) {
                plugin.children.add(result);
                return (E) result;
            }
            throw new UnsupportedOperationException("Unsupported assembly type: " + assembly.getClass().getName());
        } catch (ClassNotFoundException | NoClassDefFoundError ex) {
            return null;
        }
    }

    private ManagedAnnotationTypeAssembly<? extends Annotation> generate_assembly(PluginAssembly<?> assembly, Class<? extends Annotation> clazz, List<MemberAssembly<? extends Annotation, ?>> members) {
        return new ManagedAnnotationTypeAssembly<>(assembly, clazz, members);
    }

    private <T, E extends TypeAssembly<T>> E load_class(Node node, PluginAssembly<?> assembly, Class<T> clazz, String packageName, String name) {
        if(node == null) node = get_node(packageName);
        this.packageNames = null;
        TypeAssembly<T> type = null;
        List<MemberAssembly<T, ?>> members = new ArrayList<>();
        if(ProxyContext.class.isAssignableFrom(clazz)) {
            ProxyTypeAssemblyGenerator<? super T> generator = this.find_proxy_generator(node, name);
            if(generator != null) type = generator.generate(clazz, members);
        }
        if(type == null) type = clazz.isAnnotation() ? (TypeAssembly<T>) new ManagedAnnotationTypeAssembly<>(assembly, (Class<? extends Annotation>) clazz, members) : new ManagedTypeAssembly<>(assembly, clazz, members);
        TypeInfo<?> info;
        if((info = node.types.put(name, new TypeInfo<>(type, members))) == null) size++;
        else {
            System.out.println("Put: " + name);
            node.types.put(name, info);
            throw new IllegalStateException("Type already exists: " + name);
        }
        MemberFactory factory = this.getMemberFactory();
        if(type instanceof ManagedTypeAssembly<?> managed) {
            for(Field field : managed.instance.getDeclaredFields()) {
                members.add(factory.loadMember(type, field));
            }
            for(Method method : managed.instance.getDeclaredMethods()) {
                members.add(factory.loadMember(type, method));
            }
            for(Constructor<?> constructor : managed.instance.getDeclaredConstructors()) {
                members.add(factory.loadMember(type, constructor));
            }
        }
        return (E) type;
    }

    private <T extends ProxyTypeAssemblyGenerator<?>> T find_proxy_generator(Node node, String name) {
        char[] prefix = new char[]{'$', 'P', 'r', 'o', 'x', 'y'};
        char[] chars = name.toCharArray();
        int n = chars.length, m = prefix.length;
        for(int i = n - 1, k = m - 1; i >= 0; i--) {
            if(k >= 0) {
                if (chars[i] == prefix[k]) k--;
                else if(k + 1 != m) k++;
                continue;
            }
            TypeAssembly<?> assembly = node.get(new String(chars, i + m + 1, n - i - m - 1)).assembly;
            if(assembly == null) return null;
            if(assembly instanceof ProxyTypeAssemblyGenerator<?> generator) return  (T) generator;
            throw new ProxyException("Cannot find proxy type assembly generator for " + name);
        }
        return null;
    }

    <T, E extends ProxyTypeAssembly<? extends T>> E get_proxy(TypeAssembly<T> type) {
        String name = type.getSimpleName();
        Node node = get_node(type.getPackageName());
        if(node == null) return null;
        TypeAssembly<T> assembly = (TypeAssembly<T>) node.get(name).assembly;
        if(assembly == null) throw new ProxyException("Type assembly not found for type: " + type.getName());
        return get_proxy(node, assembly);
    }

    private <T, E extends ProxyTypeAssembly<? extends T>> E get_proxy(Node node, TypeAssembly<T> type) {
        if(node == null) return null;
        Iterator<TypeInfo<?>> it = node.types.values().iterator();
        while(it.hasNext()) {
            TypeAssembly<?> assembly = it.next().assembly;
            if(assembly instanceof ProxyTypeAssembly<?> proxy && proxy.getOriginal() == type) return (E) proxy;
        }
        return null;
    }

    @Override
    public @Nullable <T, E extends TypeAssembly<T>> E getTypeAssembly(@NotNull Class<T> type) {
        Objects.requireNonNull(type, "Type cannot be null.");
        String packageName = type.getPackageName();
        String name = type.getSimpleName();
        if(current == null || current.compareTo(type) != 0) this.current = get_node(packageName).get(name).assembly;
        return (E) this.current;
    }

    @Override
    public @Nullable <T, E extends ProxyTypeAssembly<? extends T>> E getProxyTypeAssembly(@NotNull TypeAssembly<T> type) {
        Objects.requireNonNull(type, "Type cannot be null.");
        return get_proxy(type);
    }

    @Override
    public <T, E extends ProxyTypeAssembly<? extends T>> @Nullable E getProxyTypeAssembly(@NotNull Class<T> type) {
        Objects.requireNonNull(type, "Type cannot be null.");
        TypeAssembly<T> assembly = getTypeAssembly(type);
        assert assembly != null;
        return getProxyTypeAssembly(assembly);
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

    private Stream<TypeAssembly<?>> get_all_package_types__stream(Node node) {
        if(node == null) return Stream.empty();
        Stream<TypeAssembly<?>> types = Stream.empty();
        for(Node child : node.children) {
            types = Stream.concat(types, get_all_package_types__stream(child));
        }
        return Stream.concat(types, node.types.values().stream().map(info -> info.assembly));
    }

    private List<String> get_package_names(Node node, StringBuilder sb) {
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
            packages.addAll(get_package_names(child, sb));
        }
        pop_end(sb, node.name.length() + len, sb.length());
        return packages;
    }

    private void pop_end(StringBuilder sb, int n, int m) {
        sb.delete(m - n, m);
    }

    private Node get_node(String packageName) {
        char[] chars = packageName.toCharArray();
        Node node = root;
        for (int i = 0, j = 0, n = chars.length, k = 0; i < n; i++, k = BooleanUtils.toIntAsBinary(LoopUtils.isTouchEnd(i, n))) {
            if (chars[i] == '.' || k > 0) {
                String name = new String(chars, j, i - j + k);
                node = node.get_child(name);
                j = i + 1;
            }
        }
        return node;
    }

    final static class PackageNode {

        final String name;

        final Node node;

        private PackageNode(String name, Node node) {
            this.name = name;
            this.node = node;
        }
    }

    static class TypeInfo<T> {

        TypeAssembly<T> assembly;

        List<MemberAssembly<T, ?>> members;

        TypeInfo(TypeAssembly<T> assembly, List<MemberAssembly<T, ?>> members) {
            this.assembly = assembly;
            this.members = members;
        }
    }

    final static class Node {
        private final String name;
        private final List<Node> children;
        private final Map<String, TypeInfo<?>> types;

        private Node(String name) {
            this.name = name;
            this.children = new ArrayList<>();
            this.types = new HashMap<>();
        }

        private Node() {
            this("");
        }


        private TypeInfo<?> get(String name) {
            return this.types.get(name);
        }

        private Node get_child(String name) {
            for (Node child : children) if (child.name.equals(name))
                return child;
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
