package org.cobnet.mc.diversifier.plugin.support;

import com.google.common.collect.Lists;
import org.cobnet.mc.diversifier.plugin.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class ManagedMemberFactory implements MemberFactory {

    private volatile Node root;

    private transient int size;

    static final byte BLACK = Byte.MAX_VALUE;

    static final byte RED = 0;

    static final int HASH_BITS = 0x7fffffff;

    @Override
    public @NotNull Stream<MemberAssembly<?, ?>> getMembersAsStream() {
        return traverse_members_stream(this.root);
    }

    @Override
    public @NotNull MemberAssembly<?, ?>[] getMembers(@NotNull String name) {
        Objects.requireNonNull(name, "Name cannot be null.");
        return get_node(this.root, name).get_members().toArray(MemberAssembly[]::new);
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
        return get_node(this.root, name).get_entry(type).members.toArray(MemberAssembly[]::new);
    }

    @Override
    public <T> @Nullable ConstructorAssembly<T> getConstructor(@NotNull Class<T> type, Class<?>... parameterTypes) {
        Objects.requireNonNull(type, "Type cannot be null.");
        Objects.requireNonNull(parameterTypes, "Parameter types cannot be null.");
        return getConstructorsAsStream(type).filter(constructor -> Arrays.equals(constructor.get().getParameterTypes(), parameterTypes)).findFirst().orElse(null);
    }

    @Override
    public <T> @NotNull Stream<ConstructorAssembly<T>> getConstructorsAsStream(@NotNull Class<T> type) {
        Objects.requireNonNull(type, "Type cannot be null.");
        return get_node(this.root, type.getName()).get_entry(type).members.stream().filter(member -> member instanceof ConstructorAssembly<?>).map(member -> (ConstructorAssembly<T>) member);
    }

    @Override
    public <T> @Nullable MethodAssembly<T> getMethod(@NotNull Class<T> type, @NotNull String name, Class<?>... parameterTypes) {
        Objects.requireNonNull(type, "Type cannot be null.");
        Objects.requireNonNull(name, "Name cannot be null.");
        return (MethodAssembly<T>) getMethodsAsStream(name).filter(method -> method.getDeclaredType().equals(type) && Arrays.equals(method.get().getParameterTypes(), parameterTypes)).findFirst().orElse(null);
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
        return (MemberAssembly<T, ?>) get_node(this.root, member.getName()).get_entry(member.getDeclaringClass()).get(member);
    }

    @Override
    public <T, E extends Member & AnnotatedElement> @NotNull MemberAssembly<T, E> loadMember(@NotNull TypeAssembly<T> type, @NotNull E member) {
        Objects.requireNonNull(type, "Type cannot be null.");
        Objects.requireNonNull(member, "Member cannot be null.");
        MemberAssembly<T, E> assembly = create_assembly(type, member);
        try {
            this.root = insert(this.root, member.getName(), type.get(), assembly);
            this.root.to_black();
            if(type instanceof ManagedTypeAssembly<T> parent) parent.children.add(assembly);
            return assembly;
        } catch (KeyAlreadyExistsException e) {
            return (MemberAssembly<T, E>) getMemberAssembly(member);
        }
    }

    @Override
    public int getSize() {
        return this.size;
    }

    private <T, E extends Member & AnnotatedElement> MemberAssembly<T, E> create_assembly(TypeAssembly<T> type, E member) {
        if(member instanceof Constructor<?> constructor) return (MemberAssembly<T, E>) new ManagedConstructorAssembly<>(type, (Constructor<T>) constructor);
        else if(member instanceof Method method) return (MemberAssembly<T, E>) new ManagedMethodAssembly<>(type, method);
        else if(member instanceof Field field)  return (MemberAssembly<T, E>) new ManagedFieldAssembly<>(type, field);
        throw new IllegalArgumentException("Member must be a constructor, method or field.");
    }

    private <T, E extends Member & AnnotatedElement> Node insert(Node node, String name, Class<T> type, MemberAssembly<T, E> assembly) throws KeyAlreadyExistsException {
        if(node == null) {
            size++;
            return new Node(name, type, assembly);
        }
        int cmp = node.compareTo(name);
        switch (cmp) {
            case 0 -> {
                if (node.get_entry(type).add(assembly)) size++;
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
        private final int hash;
        private Node left, right;
        private Entry<?> entry;
        private byte color;
        private transient Entry<?> current;

        static int spread(int h) {
            return (h ^ (h >>> 16)) & ManagedMemberFactory.HASH_BITS;
        }

        private Node(String name) {
            this.name = name;
            this.hash = spread(name.hashCode());
        }

        private <T> Node(String name, Class<T> type, MemberAssembly<T, ?>... members) {
            this(name);
            this.entry = new Entry<>(type, members);
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

        private <T> Entry<T> get_entry(Class<T> type) {
            if(this.current != null && this.current.type == type) return (Entry<T>) this.current;
            if(this.entry != null) {
                this.current = this.entry.getByType(type);
                return (Entry<T>) this.current;
            }
            this.entry = new Entry<>(type);
            this.current = this.entry;
            return (Entry<T>) this.current;
        }

        private Stream<MemberAssembly<?, ?>> get_members() {
            Stream<MemberAssembly<?, ?>> members = Stream.empty();
            Entry<?> current = this.entry;
            while(current != null) {
                members = Stream.concat(members, current.members.stream());
                current = current.next;
            }
            return members;
        }

        @Override
        public int compareTo(@NotNull ManagedMemberFactory.Node o) {
            return Integer.compare(hash, o.hash);
        }

        public int compareTo(String name) {
            return Integer.compare(hash, spread(name.hashCode()));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node node = (Node) o;
            return hash == node.hash;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public String toString() {
            return "Node{" +
                    "name='" + name + '\'' +
                    "hash=" + hash +
                    '}';
        }
    }

    static final class Entry<T> {

        private final Class<T> type;

        private final List<MemberAssembly<T, ?>> members;

        private Entry<?> next;

        private Entry(Class<T> type, List<MemberAssembly<T, ?>> members) {
            this.type = type;
            this.members = members;
        }

        private Entry(Class<T> type, MemberAssembly<T, ?>... members) {
            this(type, Lists.newArrayList(members));
        }

        private <E extends Member & AnnotatedElement> MemberAssembly<T, E> get(E member) {
            for(MemberAssembly<T, ?> assembly : members) {
                if(assembly.get() == member) return (MemberAssembly<T, E>) assembly;
            }
            return null;
        }

        private boolean add(MemberAssembly<T, ?> member) {
            for(MemberAssembly<T, ?> assembly : members) {
                if(assembly.equals(member)) return false;
            }
            return this.members.add(member);
        }

        public Entry<?> getByType(Class<?> type) {
            Entry<?> current = this, tail = null;
            while(current != null) {
                if(current.type == type) return current;
                tail = current;
                current = current.next;
            }
            current = new Entry<>(type);
            tail.next = current;
            return current;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Entry<?> entry = (Entry<?>) o;
            if (!Objects.equals(type, entry.type)) return false;
            return Objects.equals(members, entry.members);
        }

        @Override
        public int hashCode() {
            int result = type != null ? type.hashCode() : 0;
            result = 31 * result + (members != null ? members.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "Entry{" +
                    "type=" + type +
                    ", members=" + members +
                    ", next=" + next +
                    '}';
        }
    }
}
