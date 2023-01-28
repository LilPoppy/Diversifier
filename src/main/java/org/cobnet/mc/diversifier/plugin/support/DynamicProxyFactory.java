package org.cobnet.mc.diversifier.plugin.support;

import lombok.Getter;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.annotation.AnnotationValue;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.modifier.FieldManifestation;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.dynamic.scaffold.InstrumentedType;
import net.bytebuddy.dynamic.scaffold.MethodGraph;
import net.bytebuddy.implementation.*;
import net.bytebuddy.implementation.bind.annotation.FieldProxy;
import net.bytebuddy.implementation.bind.annotation.Morph;
import net.bytebuddy.matcher.ElementMatchers;
import org.checkerframework.checker.signature.qual.FieldDescriptor;
import org.cobnet.mc.diversifier.exception.DuplicateProxyException;
import org.cobnet.mc.diversifier.exception.ProxyException;
import org.cobnet.mc.diversifier.plugin.*;
import org.cobnet.mc.diversifier.plugin.enums.ProxyScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.invoke.TypeDescriptor;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

abstract sealed class DynamicProxyFactory<T extends ProceduralPlugin<T>> extends ManagedPluginFactory<T> implements ProxyFactory permits AbstractManagedPlatformContext {

    final static String PROXY_CONTEXT_FIELD = "__proxy_context__";

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
    public <E> @NotNull E create(@Nullable String name, @NotNull Class<E> type, Object... args) throws ProxyException {
        Objects.requireNonNull(name, "Name cannot be null.");
        Objects.requireNonNull(type, "Type cannot be null.");
        TypeFactory factory = this.getTypeFactory();
        TypeAssembly<E> assembly = factory.getTypeAssembly(type);
        assert assembly != null;
        return this.create(name, assembly, args);
    }

    @Override
    public <E> @NotNull E create(@NotNull Class<E> type, Object... args) throws ProxyException {
        return this.create(null, type, args);
    }


    @SuppressWarnings("unchecked")
    private <E> ProxyContext<? extends E> create_proxy(TypeAssembly<E> type, String name, Object... args) throws ProxyException {
        Objects.requireNonNull(type, "Type cannot be null.");
        ProxyTypeAssembly<? extends E> assembly = get_proxy_assembly(type.getPluginAssembly(), type);
        if(!type.isAnnotation()) {
            return (ProxyContext<? extends E>) assembly.build(name, args).build();
        }
        throw new ProxyException("Cannot create annotation proxy as singleton.");
    }

    private <E> ProxyTypeAssembly<? extends E> get_proxy_assembly(PluginAssembly<?> plugin, TypeAssembly<E> type) {
        if(type instanceof ProxyTypeAssembly<?> proxy) return (ProxyTypeAssembly<? extends E>) proxy;
        TypeFactory factory = getTypeFactory();
        ProxyTypeAssembly<? extends E> assembly = factory.getProxyTypeAssembly(type);
        if(assembly != null) return assembly;
        Class<? extends E> proxy = ByteBuddy.LOAD(type.getClassLoader(), ByteBuddy.BUILD(type, DynamicProxyFactory.DEFAULT_METHOD_INTERCEPTOR));
        assembly = (ProxyTypeAssembly<? extends E>) factory.loadClass(plugin, proxy);
        if(assembly == null) throw new ProxyException("Type assembly for type '" + type.getName() + "' is not managed.");
        return assembly;
    }

    ContextNode insert(ProxyContext<?> context) {
        ContextNode node = new ContextNode(context), left = null, right = null, head;
        TypeNode typed = new TypeNode(node), current = this.root, prior = null;
        if(current == null) this.root = typed;
        else {
            if(this.current != null && this.current.compareTo(current) <= 0 && this.current.head.compareTo(node) < 0) current = this.current;
            do {
                head = current.head;
                int cmp = node.compareTo(head);
                if (cmp == 0) throw new DuplicateProxyException(node.context);
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
                else if (cmp == 0) throw new DuplicateProxyException(node.context);
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
        if(right == left) right = null;
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

    @SuppressWarnings("PointlessBooleanExpression")
    private boolean test(String name, int cmp, boolean flag) {
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
        ContextNode current = node;
        String name = value.context.getName();
        do current = current.next == null ? current : current.next;
        while(current != node && current.next != null && (!flag || current.compareTo(value) < 0) && test(name, current.next.compareTo(value), flag));
        if(current.next != null && current.next == node) throw new ProxyException("Algorithm overflow error.");
        return current;
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

    private TypeNode get_node(TypeAssembly<?> assembly) {
        if(assembly == null) return null;
        if(assembly instanceof ManagedTypeAssembly<?> managed) return get_node(managed.instance);
        throw new UnsupportedOperationException("Unsupported type assembly: " + assembly);
    }

    private TypeNode get_node(Class<?> type) {
        if(this.root == null) return null;
        if(this.current != null && (this.current.type().equals(type))) return this.current;
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

    private ContextNode get_node(TypeAssembly<?> assembly, String name) {
        if(assembly == null) return get_node(name);
        if(assembly instanceof ManagedTypeAssembly<?> managed) return get_node(managed.instance, name);
        throw new UnsupportedOperationException("Unsupported type assembly: " + assembly);
    }

    private ContextNode get_node(Class<?> type, String name) {
        if(type == null) return get_node(name);
        if(this.record != null && this.record.context.getAssembly().equals(type) && this.record.context.getName().equals(name)) return this.record;
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
    public <E> @NotNull E create(@Nullable String name, @NotNull TypeAssembly<E> type, Object... args) throws ProxyException {
        Objects.requireNonNull(type, "Type cannot be null.");
        if(type instanceof ProxyAnnotationTypeAssembly<?>) throw new ProxyException("Cannot create proxy for annotation type.");
        return  create_proxy(type, name, args).getInstance();
    }

    @Override
    public <E> @NotNull E create(@NotNull TypeAssembly<E> type, Object... args) throws ProxyException {
        return this.create(null, type, args);
    }

    @Override
    public @NotNull <E> ProxyBuilder.Singleton<E> build(@NotNull Class<E> type, Object... args) throws ProxyException {
        ProxyTypeAssembly<E> assembly = this.getTypeFactory().getProxyTypeAssembly(type);
        if(assembly == null) throw new ProxyException("Type assembly not found for type: " + type);
        return assembly.build(type.getName(), args);
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull <E extends Annotation> AnnotationProxyBuilder<E> build(@NotNull Class<E> type, @NotNull AnnotatedElement carrier) throws ProxyException {
        Objects.requireNonNull(type, "Type cannot be null.");
        Objects.requireNonNull(carrier, "Carrier cannot be null.");
        AnnotationTypeAssembly<E> assembly = this.getTypeFactory().getTypeAssembly(type);
        return (AnnotationProxyBuilder<E>) create_builder(carrier, assembly);
    }

    @SuppressWarnings("unchecked")
    private <E extends Annotation> AnnotationProxyBuilder<? extends E> create_builder(AnnotatedElement element, AnnotationTypeAssembly<E> type, Object... args) {
        ProxyTypeAssembly<? extends E> proxy = get_proxy_assembly(type.getPluginAssembly(), type);
        if(proxy instanceof ProxyAnnotationTypeAssembly<?> assembly) return (AnnotationProxyBuilder<? extends E>) assembly.build(element, args);
        throw new ProxyException("Cannot create proxy annotation builder for type " + type.getName() + " because it is not a proxy annotation type.");
    }

    @Override
    public <E> @Nullable E getProxy(@NotNull String name) {
        return (E) get_node(name).context.getInstance();
    }

    @Override
    public <E> @Nullable E getProxy(@NotNull String name, @NotNull TypeAssembly<E> type) {
        return (E) get_node(type, name).context.getInstance();
    }

    @Override
    public <E> @Nullable E getProxy(@NotNull TypeAssembly<E> type) {
        //TODO 寻找最适合的类型
        return (E) get_node(type).head.context.getInstance();
    }

    @Override
    public <E> @Nullable E getProxy(@NotNull String name, @NotNull Class<E> type) {
        return (E) get_node(type, name).context.getInstance();
    }

    @Override
    public <E> @Nullable E getProxy(@NotNull Class<E> type) {
        return null;
    }

    @Override
    public <E> @NotNull E[] getProxies(@NotNull Class<E> type) {
        TypeFactory factory = this.getTypeFactory();
        TypeAssembly<E> assembly = factory.getTypeAssembly(type);
        assert assembly != null;
        return getProxies(assembly);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E> @NotNull E[] getProxies(@NotNull TypeAssembly<E> type) {
        TypeFactory factory = this.getTypeFactory();
        ProxyContext<?>[] empty = new ProxyContext<?>[0];
        if(!(type instanceof ProxyTypeAssembly<E>)) {
            type = (TypeAssembly<E>) factory.getProxyTypeAssembly(type);
            if(type == null) return (E[]) empty;
        }
        TypeNode node = get_node(type);
        List<E> result = new ArrayList<>();
        if(node == null) return (E[]) empty;
        ContextNode head = node.head;
        if(head == null) return (E[]) empty;
        ContextNode current = head;
        do result.add((E) current.context.getInstance());
        while((current = current.after) != null);
        return (E[]) result.toArray(empty);
    }

    static final class ByteBuddy {

        static <E> DynamicType.Builder<? extends E> BUILD(TypeAssembly<E> type, MethodInterceptor interceptor) {
            Class<E> clazz = type instanceof ManagedTypeAssembly<E> managed ? managed.instance : null;
            if(clazz == null) throw new UnsupportedOperationException("Cannot create proxy for type " + type.getName() + " because it is not a managed type.");
            if(clazz.isArray() || clazz.isPrimitive() || Modifier.isFinal(clazz.getModifiers())) throw new ProxyException("Cannot create proxy for type " + type.getName() + " because it is final, primitive or an array.");
            DynamicType.Builder<? extends E> builder = new net.bytebuddy.ByteBuddy().subclass(clazz).implement(ProxyContext.class).name(String.join("$", type.getName(), String.format("Proxy%s", type.getSimpleName()))).defineConstructor(Modifier.PUBLIC);
            if(type.isAnnotation()) {
                builder = builder.defineField(DynamicProxyFactory.PROXY_CONTEXT_FIELD, DynamicProxyAnnotationTypeAssembly.DynamicProxyContext.class, Visibility.PRIVATE, FieldManifestation.FINAL)
                        .implement(ExtendableAnnotation.class).method(ElementMatchers.isDeclaredBy(ExtendableAnnotation.class)).intercept(MethodDelegation.toField(DynamicProxyFactory.PROXY_CONTEXT_FIELD));
                Method[] methods = clazz.getDeclaredMethods();
                String prefix = "__";
                for(Method method : methods) {
                    int mod = method.getModifiers();
                    if(!Modifier.isAbstract(mod) || Modifier.isStatic(mod) || !Modifier.isPublic(mod)) continue;
                    String name = method.getName();
                    String field = prefix + name;
                    builder.defineField(field, method.getReturnType(), Visibility.PRIVATE, FieldManifestation.FINAL)
                            .method(ElementMatchers.isDeclaredBy(clazz).and(ElementMatchers.named(name)))
                            .intercept(FieldAccessor.ofField(field).appender());
                    System.out.println("###" + name);
                    //builder = builder.defineMethod(method.getName(), method.getReturnType(), Visibility.PUBLIC, Ownership.STATIC, method.getExceptionTypes()).intercept(MethodDelegation.toField(DynamicProxyFactory.PROXY_CONTEXT_FIELD));
                }
                System.out.println("@@@@@@" + Arrays.toString(methods));
            } else builder.defineField(DynamicProxyFactory.PROXY_CONTEXT_FIELD, DynamicProxyTypeAssembly.DynamicProxyContext.class, Visibility.PRIVATE, FieldManifestation.FINAL);
            return builder.method(ElementMatchers.isDeclaredBy(ProxyContext.class)).intercept(MethodDelegation.toField(DynamicProxyFactory.PROXY_CONTEXT_FIELD))
                    .method(ElementMatchers.isDeclaredBy(clazz)).intercept(MethodDelegation.withDefaultConfiguration().withBinders(Morph.Binder.install(ParameterizedCallable.class)).to(interceptor));
        }

        static <E> Class<? extends E> LOAD(ClassLoader loader, DynamicType.Builder<? extends E> builder) throws ProxyException {
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
            return compare(assembly);
        }

        private int compare(@NotNull TypeAssembly<?> o) {
            return Integer.compare(Objects.requireNonNull(type()).getName().hashCode(), o.getName().hashCode());
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
