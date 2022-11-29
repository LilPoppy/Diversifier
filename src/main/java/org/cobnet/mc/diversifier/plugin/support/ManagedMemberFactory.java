package org.cobnet.mc.diversifier.plugin.support;

import com.google.common.collect.Lists;
import org.cobnet.mc.diversifier.plugin.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

final class ManagedMemberFactory implements MemberFactory {

    private final TypeFactory factory;

    private volatile Node root;

    transient int size;

    static final byte RED = 0, BLACK = Byte.MAX_VALUE;

    ManagedMemberFactory(TypeFactory factory) {
        this.factory = factory;
    }

    @Override
    public @NotNull Stream<MemberAssembly<?, ?>> getMembersAsStream() {
        return traverse_members_stream(this.root);
    }

    @Override
    public @NotNull MemberAssembly<?, ?>[] getMembers(@NotNull String name) {
        Objects.requireNonNull(name, "Name cannot be null.");
        return get_node(this.root, name).get_members().toArray(MemberAssembly[]::new);
    }

    private List<Node> traverse_nodes(Node node) {
        List<Node> nodes = new ArrayList<>();
        if(node == null) return Lists.newArrayList();
        nodes.add(node);
        nodes.addAll(traverse_nodes(node.left));
        nodes.addAll(traverse_nodes(node.right));
        return nodes;
    }

    private Stream<MemberAssembly<?, ?>> traverse_members_stream(Node node) {
        if(node == null) return Stream.empty();
        Stream<MemberAssembly<?, ?>> members = Stream.empty();
        members = Stream.concat(members, node.get_members());
        members = Stream.concat(members, traverse_members_stream(node.left));
        members = Stream.concat(members, traverse_members_stream(node.right));
        return members;
    }

    @Override
    public <T> @NotNull MemberAssembly<T, ?>[] getMembers(@NotNull Class<T> type, @NotNull String name) {
        Objects.requireNonNull(type, "Type cannot be null.");
        Objects.requireNonNull(name, "Name cannot be null.");
        return get_node(this.root, name).get(factory.getTypeAssembly(type)).members.toArray(MemberAssembly[]::new);
    }

    @Override
    public <T> @Nullable ConstructorAssembly<T> getConstructor(@NotNull Class<T> type, Class<?>... parameterTypes) {
        Objects.requireNonNull(type, "Type cannot be null.");
        Objects.requireNonNull(parameterTypes, "Parameter types cannot be null.");
        return getConstructorsAsStream(type).filter(constructor -> Arrays.equals(constructor.getParameterTypes(), parameterTypes)).findFirst().orElse(null);
    }

    @Override
    public <T> @NotNull Stream<ConstructorAssembly<T>> getConstructorsAsStream(@NotNull Class<T> type) {
        Objects.requireNonNull(type, "Type cannot be null.");
        return get_node(this.root, type.getName()).get(factory.getTypeAssembly(type)).members.stream().filter(member -> member instanceof ConstructorAssembly<?>).map(member -> (ConstructorAssembly<T>) member);
    }

    @Override
    public <T> @Nullable MethodAssembly<T> getMethod(@NotNull Class<T> type, @NotNull String name, Class<?>... parameterTypes) {
        Objects.requireNonNull(type, "Type cannot be null.");
        Objects.requireNonNull(name, "Name cannot be null.");
        return (MethodAssembly<T>) getMethodsAsStream(name).filter(method -> method.getDeclaredType().equals(type) && Arrays.equals(method.getParameterTypes(), parameterTypes)).findFirst().orElse(null);
    }

    @Override
    public @NotNull Stream<MethodAssembly<?>> getMethodsAsStream(@NotNull String name) {
        Objects.requireNonNull(name, "Name cannot be null.");
        return get_node(this.root, name).get_members().filter(member -> member instanceof MethodAssembly<?>).map(member -> (MethodAssembly<?>) member);
    }

    @Override
    public <T> @Nullable FieldAssembly<T> getField(@NotNull Class<T> type, @NotNull String name) {
        Objects.requireNonNull(type, "Type cannot be null.");
        return (FieldAssembly<T>) getFieldsAsStream(name).filter(field -> field.getDeclaredType().equals(type)).findFirst().orElse(null);
    }

    @Override
    public @NotNull Stream<FieldAssembly<?>> getFieldsAsStream(@NotNull String name) {
        Objects.requireNonNull(name, "Name cannot be null.");
        return get_node(this.root, name).get_members().filter(member -> member instanceof FieldAssembly<?>).map(member -> (FieldAssembly<?>) member);
    }

    @Override
    public @Nullable <T extends Member & AnnotatedElement> MemberAssembly<T, ?> getMemberAssembly(@NotNull T member) {
        Objects.requireNonNull(member, "Member cannot be null.");
        return (MemberAssembly<T, ?>) get_node(this.root, member.getName()).get(factory.getTypeAssembly(member.getDeclaringClass())).get(member);
    }

    @Override
    public <T, E extends Member & AnnotatedElement> @NotNull MemberAssembly<T, E> loadMember(@NotNull TypeAssembly<T> type, @NotNull E member) {
        Objects.requireNonNull(type, "Type cannot be null.");
        Objects.requireNonNull(member, "Member cannot be null.");
        MemberAssembly<T, E> assembly = create_assembly(type, member);
        try {
            this.root = insert(this.root, member.getName(), type, assembly);
            this.root.to_black();
            if(type instanceof ManagedTypeAssembly<T> parent) parent.children.add(assembly);
            return assembly;
        } catch (KeyAlreadyExistsException e) {
            return (MemberAssembly<T, E>) getMemberAssembly(member);
        }
    }

    <T, E extends Member & AnnotatedElement> MemberAssembly<T, E> load_member(ManagedTypeFactory.TypeInfo<T> info, E member, List<MethodAssembly<?>> methods) {
        MemberAssembly<T, E> assembly = create_assembly(info.assembly, member, methods);
        info.members.add(assembly);
        this.root = insert(this.root, member.getName(), info);
    }

    @Override
    public int getSize() {
        return this.size;
    }

    private <T, E extends Member & AnnotatedElement> MemberAssembly<T, E> create_assembly(TypeAssembly<T> type, E member, List<MethodAssembly<?>> methods) {
        if(member instanceof Constructor<?> constructor) return (MemberAssembly<T, E>) new ManagedConstructorAssembly<>(type, (Constructor<T>) constructor, methods);
        else if(member instanceof Method method) return (MemberAssembly<T, E>) new ManagedMethodAssembly<>(type, method, methods);
        else if(member instanceof Field field)  return (MemberAssembly<T, E>) new ManagedFieldAssembly<>(type, field, methods);
        throw new IllegalArgumentException("Member must be a constructor, method or field.");
    }

    private <T, E extends Member & AnnotatedElement> Node insert(Node node, String name, TypeInfo<T> info) throws KeyAlreadyExistsException {
        if(node == null) {
            size++;
            return new Node(name, info);
        }
        int cmp = node.compareTo(name);
        if(cmp == 0 && node.get(info.assembly))
        switch (cmp) {
            case 0 -> {
                if (node.get(type).add(assembly)) size++;
                else throw new KeyAlreadyExistsException("Member '" + assembly + "' already exists in node '" + node + "'.");
            }
            case 1 -> node.left = insert(node.left, name, type, assembly);
            case -1 -> node.right = insert(node.right, name, type, assembly);
        }
        if(Node.isRed(node.right) && Node.isBlack(node.left)) node = rotate_left(node);
        if(Node.isRed(node.left) && Node.isRed(node.left.left)) node = rotate_right(node);
        if(Node.isRed(node.left) && Node.isRed(node.right)) {
            node.flip_color();
            node.left.flip_color();
            node.right.flip_color();
        }
        return node;
    }

    private Node get_node(Node node, String name) {
        if(node == null) return null;
        int cmp = node.compareTo(name);
        if(cmp > 0) return get_node(node.left, name);
        if(cmp < 0) return get_node(node.right, name);
        return node;
    }

    private Node rotate_left(Node node) {
        Node right = node.right;
        node.right = right.left;
        right.left = node;
        right.color = right.left.color;
        right.left.to_red();
        return right;
    }

    private Node rotate_right(Node node) {
        Node left = node.left;
        node.left = left.right;
        left.right = node;
        left.color = left.right.color;
        left.right.to_red();
        return left;
    }

    static final class Node implements Comparable<Node> {

        private final String name;
        private Node left, right;
        private TypeInfo<?> info;
        private byte color;
        transient TypeInfo<?> current;

        private Node(String name) {
            this.name = name;
        }

        private <T> Node(String name, TypeInfo<T> info) {
            this(name);
            this.info = info;
        }

        private boolean is_red() {
            return this.color == ManagedMemberFactory.RED;
        }

        private boolean is_black() {
            return this.color == ManagedMemberFactory.BLACK;
        }

        static boolean isRed(Node node) {
            return node != null && node.is_red();
        }

        static boolean isBlack(Node node) {
            return node == null || node.is_black();
        }

        private void to_red() {
            this.color = ManagedMemberFactory.RED;
        }

        private void to_black() {
            this.color = ManagedMemberFactory.BLACK;
        }

        private void flip_color() {
            if(this.color == ManagedMemberFactory.RED) this.to_black();
            else this.to_red();
        }

        private <T> TypeInfo<T> get(TypeAssembly<T> type) {
            if(this.current != null && this.current.assembly == type) return (TypeInfo<T>) this.current;
            if(this.info != null) {
                this.current = this.info.find(type);
                return (TypeInfo<T>) this.current;
            }
            this.info = new TypeInfo<>(type);
            this.current = this.info;
            return (TypeInfo<T>) this.current;
        }

        private Stream<MemberAssembly<?, ?>> get_members() {
            Stream<MemberAssembly<?, ?>> members = Stream.empty();
            TypeInfo<?> current = this.info;
            while(current != null) {
                members = Stream.concat(members, current.members.stream());
                current = current.next;
            }
            return members;
        }

        @Override
        public int compareTo(@NotNull ManagedMemberFactory.Node o) {
            return Integer.compare(name.hashCode(), o.name.hashCode());
        }

        int compareTo(String name) {
            return Integer.compare(this.name.hashCode(), name.hashCode());
        }
    }

    static final class TypeInfo<T> extends ManagedTypeFactory.TypeInfo<T> {

        private TypeInfo<?> next;

        private TypeInfo(TypeAssembly<T> type, List<MemberAssembly<T, ?>> members) {
            super(type, members);
        }

        private TypeInfo(TypeAssembly<T> type, MemberAssembly<T, ?>... members) {
            this(type, Lists.newArrayList(members));
        }

        private <E extends Member & AnnotatedElement> MemberAssembly<T, E> get(E member) {
            for(MemberAssembly<T, ?> assembly : members) {
                if(assembly.equals(member)) return (MemberAssembly<T, E>) assembly;
            }
            return null;
        }

        private boolean add(MemberAssembly<T, ?> member) {
            for(MemberAssembly<T, ?> assembly : members) {
                if(assembly.equals(member)) return false;
            }
            return this.members.add(member);
        }

        public TypeInfo<?> find(TypeAssembly<?> type) {
            TypeInfo<?> current = this, tail = null;
            while(current != null) {
                if(current.assembly.equals(type)) return current;
                tail = current;
                current = current.next;
            }
            current = new TypeInfo<>(type);
            tail.next = current;
            return current;
        }
    }
}
