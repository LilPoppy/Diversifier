package org.cobnet.mc.diversifier.plugin.support;

import lombok.Getter;
import net.bytebuddy.description.modifier.FieldManifestation;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.Morph;
import net.bytebuddy.matcher.ElementMatchers;
import org.cobnet.mc.diversifier.exception.ProxyException;
import org.cobnet.mc.diversifier.plugin.*;
import org.cobnet.mc.diversifier.plugin.enums.ProxyScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.Objects;

abstract sealed class DynamicProxyFactory<T extends ProceduralPlugin<T>> extends ManagedPluginFactory<T> implements ProxyFactory permits AbstractPlatformContext {

    final static String PROXY_CONTEXT_FIELD = "__proxy_context__", EXTENDABLE_ANNOTATION_FIELD = "__extendable_annotation__";

    final static MethodInterceptor DEFAULT_METHOD_INTERCEPTOR = new DynamicProxyMethodInterceptor();

    private final static boolean PREVIOUS = true, NEXT = false;

    volatile TypeNode root;

    transient TypeNode current;

    transient ContextNode record;

    @Getter
    transient int size;

    protected DynamicProxyFactory(T plugin) {
        super(plugin);
    }

    @Override
    public <E> @NotNull E create(@NotNull String name, @NotNull Class<E> type, @NotNull Scope scope, Object... args) throws ProxyException {
        TypeAssembly<E> assembly = this.getTypeFactory().getTypeAssembly(type);
        if(assembly == null) throw new ProxyException("Type assembly for type " + type.getName() + " is not managed.");
        return this.create(name, assembly, scope, args);
    }

    @Override
    public <E> @NotNull E create(@NotNull String name, @NotNull Class<E> type, Object... args) throws ProxyException {
        return this.create(name, type, ProxyScope.PROTOTYPE, args);
    }

    @Override
    public <E> @NotNull E create(@NotNull Class<E> type, @NotNull Scope scope, Object... args) throws ProxyException {
        return this.create(type.getName(), type, scope, args);
    }

    @Override
    public <E> @NotNull E create(@NotNull Class<E> type, Object... args) throws ProxyException {
        return this.create(type, ProxyScope.PROTOTYPE, args);
    }

    @Override
    public <E> @NotNull E create(@NotNull String name, @NotNull TypeAssembly<E> type, @NotNull Scope scope, Object... args) throws ProxyException {
        Objects.requireNonNull(name, "Name cannot be null.");
        Objects.requireNonNull(type, "Type cannot be null.");
        Objects.requireNonNull(scope, "Scope cannot be null.");
        return create_proxy(type, name, scope, args).getInstance();
    }

    private <E> ProxyContext<? extends E> create_proxy(TypeAssembly<E> type, String name, Scope scope, Object... args) throws ProxyException {
        ProxyTypeAssembly<? extends E> assembly = find_proxy_type(type.getPluginAssembly(), type.get());
        ProxyContext<? extends E> context = assembly.create(name, scope, args);
        ContextNode node = insert(context);
        return context;
    }

    private <E> ProxyTypeAssembly<? extends E> find_proxy_type(PluginAssembly<?> plugin, Class<E> type) {
        TypeFactory factory = getTypeFactory();
        ProxyTypeAssembly<? extends E> assembly = factory.getProxyTypeAssembly(type);
        if(assembly != null) return assembly;
        Class<? extends E> proxy = ByteBuddy.CREATE(type.getClassLoader(), ByteBuddy.BUILDER(type, DynamicProxyFactory.DEFAULT_METHOD_INTERCEPTOR).method(ElementMatchers.isDefaultConstructor().or(ElementMatchers.isConstructor())).intercept(MethodDelegation.withDefaultConfiguration().withBinders(Morph.Binder.install(ParameterizedCallable.class)).to(DEFAULT_METHOD_INTERCEPTOR)));
        assembly = (ProxyTypeAssembly<? extends E>) factory.loadClass(plugin, proxy);
        if(assembly == null) throw new ProxyException("Type assembly for type '" + type.getName() + "' is not managed.");
        return assembly;
    }

    private ContextNode insert(ProxyContext<?> context) {
        ContextNode node = new ContextNode(context), left = null, right = null, head;
        TypeNode typed = new TypeNode(node), current = this.root, prior = null;
        if(current == null) this.root = typed;
        else {
            if(this.current != null && this.current.compareTo(current) <= 0 && this.current.head.compareTo(node) < 0) current = this.current;
            do {
                head = current.head;
                int cmp = node.compareTo(head);
                if (cmp == 0) throw new ProxyException("Duplicate proxy context name: '" + context.getName() + "'.");
                left = cmp > 0 ? min(head, left) : left;
                right = cmp < 0 ? max(head, right) : right;
                cmp = typed.compareTo(current);
                if(cmp < 0) prior = current;
                else if(cmp == 0) {
                    if(current.head.context.getScope().compareTo(ProxyScope.SINGLETON) == 0) throw new ProxyException("Singleton proxy context already exists: '" + current.head.context.getName() + "'.");
                    if(context.getScope().compareTo(ProxyScope.SINGLETON) == 0) throw new ProxyException("Cannot override singleton proxy context: '" + context.getName() + "'.");
                    return insert(prior, current, node, left, right);
                } else {
                    size++;
                    if(prior == null) this.root = typed;
                    else prior.next = typed;
                    typed.next = current;
                    set_prev_next(node, left, right);
                    return node;
                }
            } while ((current = current.next) != null);
            prior.next = typed;
            set_prev_next(node, left == null ? prior.head : left, right);
        }
        size++;
        return node;
    }

    private ContextNode insert(TypeNode previous, TypeNode typed, ContextNode node, ContextNode left, ContextNode right) {
        ContextNode current = typed.head, prior = null;
        if(current != null) {
            do {
                int cmp = node.compareTo(current);
                if(cmp < 0) left = min(current, left);
                else if (cmp == 0) throw new ProxyException("Duplicate proxy context name: '" + node.context.getName() + "'");
                else {
                    right = max(current, right);
                    if (prior == null) typed.head = node;
                    else prior.after = node;
                    node.after = current;
                    size++;
                    if(left == null) left = min(previous.head, prior);
                    set_prev_next(node, left, right);
                    return node;
                }
                prior = current;
            } while ((current= current.after) != null);
            prior.after = node;
            if(right == null) right = find_next(prior, node);
        } else typed.head = node;
        set_prev_next(node, left, right);
        size++;
        return node;
    }

    private void set_prev_next(ContextNode node, ContextNode left, ContextNode right) {
        if((left = find_previous(left, node)) != null && left != node) set_next(left, node);
        if(right == null && node.next == null) right = find_next(left, node);
        if((right = min(node.next, right)) != null && right != node) node.next = right;
    }

    private ContextNode max(ContextNode left, ContextNode right) {
        if(left == null) return right;
        if(right == null) return left;
        return left.compareTo(right) > 0 ? left : right;
    }

    private ContextNode min(ContextNode left, ContextNode right) {
        if(left == null) return right;
        if(right == null) return left;
        return left.compareTo(right) < 0 ? left : right;
    }

    private <E extends Node<E>> void set_next(E node, E value) {
        if(node == null) return;
        if(value != null) value.next = node.next;
        node.next = value;
    }

    private boolean is_continue(String name, int cmp, boolean flag) {
        if(cmp < 0) return true;
        else if(cmp == 0) throw new ProxyException("Context with name '" + name + "' already exists.");
        return false ^ flag;
    }

    private ContextNode find_next(ContextNode node, ContextNode value) {
        return find(node, value, DynamicProxyFactory.NEXT);
    }

    private ContextNode find_previous(ContextNode node, ContextNode value) {
        return find(node, value, DynamicProxyFactory.PREVIOUS);
    }

    private ContextNode find(ContextNode node, ContextNode value, boolean flag) {
        if(node == null) return null;
        if(value == null) return node;
        String name = value.context.getName();
        do node = node.next == null ? node : node.next;
        while(node.next != null && (!flag || node.compareTo(value) < 0) && is_continue(name, node.next.compareTo(value), flag));
        return node;
    }

    private ContextNode get_node(String name) {
        if(this.root == null) return null;
        if(this.record != null && this.record.context.getName().equals(name)) return this.record;
        TypeNode node = this.root;
        ContextNode head = node.head;
        if(name == null || head == null) return null;
        ContextNode current = head;
        while(current != null) {
            int cmp = current.context.getName().compareTo(name);
            if(cmp == 0) {
                this.record = current;
                return current;
            }
            else if(cmp > 0) return null;
            current = current.next;
        }
        return null;
    }


    private TypeNode get_node(Class<?> type) {
        if(this.root == null) return null;
        if(this.current != null && this.current.type().get() == type) return this.current;
        if(this.root == null) return null;
        TypeNode node = this.root;
        while(node != null) {
            int cmp = node.compare(type);
            if(cmp < 0) node = node.next;
            else if(cmp == 0) {
                this.current = node;
                return node;
            } else return null;
        }
        return null;
    }

    private ContextNode get_node(Class<?> type, String name) {
        if(type == null) return get_node(name);
        if(this.record != null && this.record.context.getAssembly().get() == type && this.record.context.getName().equals(name)) return this.record;
        return get_node(get_node(type), name);
    }

    private ContextNode get_node(TypeNode node, String name) {
        if(node == null) return null;
        ContextNode current = node.head;
        do {
            int cmp = current.compare(name);
            if(cmp < 0) continue;
            if(cmp == 0) {
                this.record = current;
                return current;
            } else return null;
        } while((current = current.after) != null);
        return null;
    }

    @Override
    public <E> @NotNull E create(@NotNull String name, @NotNull TypeAssembly<E> type, Object... args) throws ProxyException {
        return this.create(name, type, ProxyScope.SINGLETON, args);
    }

    @Override
    public <E> @NotNull E create(@NotNull TypeAssembly<E> type, @NotNull Scope scope, Object... args) throws ProxyException {
        return this.create(type.get().getSimpleName(), type, scope, args);
    }

    @Override
    public <E> @NotNull E create(@NotNull TypeAssembly<E> type, Object... args) throws ProxyException {
        return this.create(type, ProxyScope.SINGLETON, args);
    }

    @Override
    public @NotNull <E extends Annotation> AnnotationProxyBuilder<? extends E> create(@NotNull String name, @NotNull AnnotationTypeAssembly<E> type) throws ProxyException {
        Objects.requireNonNull(name, "Name cannot be null.");
        Objects.requireNonNull(type, "Annotation type cannot be null.");
        if(type instanceof ProxyAnnotationTypeAssembly<? extends E> assembly) return assembly.create(name);
        ProxyTypeAssembly<? extends E> assembly = find_proxy_type(type.getPluginAssembly(), type.get());
        if(assembly instanceof ProxyAnnotationTypeAssembly<?> annotation) return (AnnotationProxyBuilder<? extends E>) annotation.create(name);
        throw new ProxyException("Cannot create proxy annotation builder for type " + type.get().getName() + " because it is not a proxy annotation type.");
    }

    @Override
    public @NotNull <E extends Annotation> AnnotationProxyBuilder<? extends E> create(@NotNull AnnotatedAssembly<?, ?, ?> owner, @NotNull Class<E> type) throws ProxyException {
        Objects.requireNonNull(type, "Annotation type cannot be null.");
        AnnotationTypeAssembly<E> assembly = getTypeFactory().getTypeAssembly(type);
        if(assembly == null) throw new ProxyException("Cannot create proxy for type " + type.getName() + " because it is not a valid annotation type.");
        return create(String.join(".", owner.getName(), assembly.get().getSimpleName()), assembly);
    }

    @Override
    public <E> @Nullable E getProxy(@NotNull String name) {
        return (E) get_node(name).context.getInstance();
    }

    @Override
    public <E> @Nullable E getProxy(@NotNull String name, @NotNull TypeAssembly<E> type) {
        return (E) get_node(type.get(), name).context.getInstance();
    }

    @Override
    public <E> @Nullable E getProxy(@NotNull TypeAssembly<E> type) {
        //TODO 寻找最适合的类型
        return (E) get_node(type.get()).head.context.getInstance();
    }

    @Override
    public <E> @Nullable E getProxy(@NotNull String name, @NotNull Class<E> type) {
        return (E) get_node(type, name).context.getInstance();
    }

    @Override
    public <E> @Nullable E getProxy(@NotNull Class<E> type) {
        return null;
    }

    static final class ByteBuddy {

        static <E> DynamicType.Builder<? extends E> BUILDER(Class<E> type, MethodInterceptor interceptor) {
            if(type.isArray() || type.isPrimitive() || Modifier.isFinal(type.getModifiers())) throw new ProxyException("Cannot create proxy for type " + type.getName() + " because it is final, primitive or an array.");
            DynamicType.Builder<? extends E> builder = new net.bytebuddy.ByteBuddy().subclass(type).implement(ProxyContext.class).name(String.join("$", type.getName(), String.format("Proxy%s", type.getSimpleName())));
            if(type.isAnnotation()) builder = builder.implement(ExtendableAnnotation.class).defineField(EXTENDABLE_ANNOTATION_FIELD, ExtendableAnnotation.class, Visibility.PRIVATE, FieldManifestation.FINAL)
                    .method(ElementMatchers.isDeclaredBy(ExtendableAnnotation.class)).intercept(MethodDelegation.toField(EXTENDABLE_ANNOTATION_FIELD));
            return builder.defineField(PROXY_CONTEXT_FIELD, ProxyContext.class, Visibility.PRIVATE, FieldManifestation.FINAL)
                    .method(ElementMatchers.isDeclaredBy(ProxyContext.class)).intercept(MethodDelegation.toField(PROXY_CONTEXT_FIELD))
                    .method(ElementMatchers.isDeclaredBy(type)).intercept(MethodDelegation.withDefaultConfiguration().withBinders(Morph.Binder.install(ParameterizedCallable.class)).to(interceptor));
        }

        static <E> Class<? extends E> CREATE(ClassLoader loader, DynamicType.Builder<? extends E> builder) throws ProxyException {
            try(DynamicType.Unloaded<? extends E> unloaded = builder.make()) {
                return unloaded.load(loader, ClassLoadingStrategy.Default.INJECTION).getLoaded();
            } catch (IOException e) {
                throw new ProxyException(e);
            }
        }
    }

    static abstract class Node<T extends Node<T>> implements Comparable<T> {
        T next;
    }

    final static class TypeNode extends Node<TypeNode> {

        ContextNode head;

        public TypeNode(ContextNode head) {
            this.head = head;
        }

        TypeAssembly<?> type() {
            return this.head == null ? null : this.head.context.getAssembly();
        }

        private int compare(Class<?> type) {
            TypeAssembly<?> assembly = type();
            if(assembly == null) throw new ProxyException("Cannot compare type node because it has no type.");
            return Integer.compare(assembly.getName().hashCode(), type.getName().hashCode());
        }

        @Override
        public int compareTo(TypeNode node) {
            TypeAssembly<?> assembly = node.type();
            if(assembly == null) throw new ProxyException("Cannot compare type node because it has no type.");
            return compare(assembly.get());
        }

        private int compare(@NotNull TypeAssembly<?> o) {
            return compare(o.get());
        }
    }
    final static class ContextNode extends Node<ContextNode> {

        ProxyContext<?> context;

        ContextNode after;

        ContextNode(ProxyContext<?> context) {
            this(context, null);
        }

        ContextNode(ProxyContext<?> context, ContextNode next) {
            this(context, next, null);
        }

        ContextNode(ProxyContext<?> context, ContextNode next, ContextNode after) {
            this.context = context;
            this.next = next;
            this.after = after;
        }

        @Override
        public int hashCode() {
            return this.context.getName().hashCode();
        }

        private int compare(String name) {
            int cmp = Integer.compare(this.context.getName().length(), name.length());
            if(cmp == 0) return Integer.compare(hashCode(), name.hashCode());
            return cmp;
        }
        @Override
        public int compareTo(@NotNull DynamicProxyFactory.ContextNode o) {
            return compare(o.context.getName());
        }
    }

}
