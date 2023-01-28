package org.cobnet.mc.diversifier.plugin.support;

import lombok.Getter;
import org.cobnet.mc.diversifier.exception.ProxyException;
import org.cobnet.mc.diversifier.plugin.*;
import org.cobnet.mc.diversifier.plugin.enums.ProxyScope;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public sealed class DynamicProxyTypeAssembly<T> extends ManagedTypeAssembly<T> implements ProxyTypeAssembly<T> permits DynamicProxyAnnotationTypeAssembly {

    @Getter
    protected final TypeAssembly<? super T> original;

    private final Field field;

    protected DynamicProxyTypeAssembly(PluginAssembly<?> plugin, TypeAssembly<? super T> type, Class<T> proxy, List<MemberAssembly<T, ?>> members) {
        super(plugin, proxy, members);
        this.original = type;
        try {
            this.field = this.instance.getDeclaredField(DynamicProxyFactory.PROXY_CONTEXT_FIELD);
        } catch (NoSuchFieldException e) {
            throw new ProxyException("Cannot find proxy context field", e);
        }
    }

    @Override
    public <E extends Annotation> E getAnnotation(@NotNull Class<E> annotationClass) {
        return this.original.getAnnotation(annotationClass);
    }

    @Override
    public Annotation[] getAnnotations() {
        return this.original.getAnnotations();
    }

    @Override
    public Annotation[] getDeclaredAnnotations() {
        return this.original.getDeclaredAnnotations();
    }

    @Override
    public boolean isAnnotationPresent(@NotNull Class<? extends Annotation> annotationClass) {
        return this.original.isAnnotationPresent(annotationClass);
    }

    @Override
    public <E extends Annotation> E[] getAnnotationsByType(Class<E> annotationClass) {
        return this.original.getAnnotationsByType(annotationClass);
    }

    @Override
    public <E extends Annotation> E getDeclaredAnnotation(Class<E> annotationClass) {
        return this.original.getDeclaredAnnotation(annotationClass);
    }

    @Override
    public <E extends Annotation> E[] getDeclaredAnnotationsByType(Class<E> annotationClass) {
        return this.original.getDeclaredAnnotationsByType(annotationClass);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull T create(String name, Object... args) throws ProxyException {
        return (T) this.create(name, ProxyScope.SINGLETON, args);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull ProxyContext<T> create(String name, Scope scope, Object... args) throws ProxyException {
        try {
            ProxyFactory factory = this.getPluginAssembly().getParent().getContext().getProxyFactory();
            T instance = super.create(args);
            if(ProxyContext.is(instance) && factory instanceof DynamicProxyFactory<?> dynamic) {
                ProxyContext<T> context = create_context(name, scope, instance);
                field.setAccessible(true);
                field.set(instance, context);
                return (ProxyContext<T>) dynamic.insert(context).context;
            }
            throw new ProxyException("The proxy class " + this.getName() + " does not implement ProxyContext");
        } catch (Throwable e) {
            throw new ProxyException("Failed to create proxy for " + original.getName(), e);
        }
    }

    @Override
    public @NotNull ProxyBuilder.Singleton<T> build(String name, Object... args) {
        return new DynamicSingletonBuilder(name, args);
    }

    protected DynamicProxyContext<T> create_context(String name, Scope scope, T instance) {
        return new DynamicProxyContext<>(name, scope, this, instance);
    }

    private record DynamicProxyInfo<T>(@Getter String name, @Getter Scope scope, @Getter ProxyTypeAssembly<T> assembly, @Getter T instance) implements ProxyInformation<T> {}

    sealed abstract class DynamicProxyBuilder implements ProxyBuilder<T> permits DynamicPrototypeBuilder, DynamicSingletonBuilder {

        protected final Scope scope;

        protected String name;

        protected final Object[] args;

        DynamicProxyBuilder(Scope scope, String name, Object... args) {
            this(scope, args);
            this.name = name;
        }

        DynamicProxyBuilder(Scope scope, Object... args) {
            this.scope = scope;
            this.args = args;
        }

        @Override
        public @NotNull ProxyBuilder<T> name(@NotNull String name) {
            this.name = name;
            return this;
        }
    }

    final class DynamicSingletonBuilder extends DynamicProxyBuilder implements ProxyBuilder.Singleton<T> {

        DynamicSingletonBuilder(String name, Object... args) {
            super(ProxyScope.SINGLETON, name, args);
        }

        DynamicSingletonBuilder(Object... args) {
            this(DynamicProxyTypeAssembly.this.getName(), args);
        }

        @Override
        public @NotNull T build() throws ProxyException {
            return DynamicProxyTypeAssembly.this.create(this.name, ProxyScope.SINGLETON, this.args).getInstance();
        }

        @Override
        public @NotNull ProxyBuilder.Singleton<T> name(@NotNull String name) {
            return (Singleton<T>) super.name(name);
        }

        @Override
        public @NotNull <E> ProxyBuilder.Prototype<T, E> prototype(@NotNull E carrier) {
            return new DynamicPrototypeBuilder<>(carrier, this.name.hashCode() == DynamicProxyTypeAssembly.this.getName().hashCode() ? null : this.name, this.args);
        }
    }

    sealed class DynamicPrototypeBuilder<E> extends DynamicProxyBuilder implements ProxyBuilder.Prototype<T, E> permits DynamicProxyAnnotationTypeAssembly.DynamicAnnotationProxyBuilder{

        protected final NamedCarrier<E> carrier;

        DynamicPrototypeBuilder(E carrier, Object... args) {
            this(carrier, null, args);
        }

        DynamicPrototypeBuilder(E carrier, String name, Object... args) {
            this(new NamedCarrier<>(carrier), name, args);
        }

        private DynamicPrototypeBuilder(NamedCarrier<E> carrier, String name, Object... args) {
            super(ProxyScope.PROTOTYPE, name == null ? carrier.getName() : String.join("_", carrier.getName(), name), args);
            this.carrier = carrier;
        }


        @Override
        public @NotNull ProxyBuilder.Prototype<T, E> carrier(@NotNull E carrier) {
            this.carrier.element = carrier;
            return this;
        }

        @Override
        public @NotNull Singleton<T> singleton() {
            return new DynamicSingletonBuilder(this.name);
        }

        @Override
        public @NotNull ProxyBuilder.Prototype<T, E> name(@NotNull String name) {
            return (Prototype<T, E>) super.name(name);
        }

        @Override
        public @NotNull T build() throws ProxyException {
            ProxyContext<T> context = DynamicProxyTypeAssembly.this.create(name, scope, args);
            if(context instanceof DynamicProxyContext<T> dynamic) {
                if(dynamic.carrier != null) throw new ProxyException("The proxy context of " + "'" + dynamic.getInstance() + "'" + "is already bound to " + dynamic.carrier);
                dynamic.carrier = carrier;
                return dynamic.getInstance();
            }
            throw new ProxyException("The proxy class " + carrier.getName() + " does not use correct proxy context.");
        }
    }

    final class NamedCarrier<E> {

        transient E element;

        NamedCarrier(E element) {
            this.element = element;
        }

        public String getName() {
            if(element instanceof Class<?> type) return name(type.getName());
            if(element instanceof Field field) return name(field.getName());
            if(element instanceof Method method) return name(method.getName());
            if(element instanceof Assembly<?,?> assembly) return name(assembly.getName());
            return name(element == null ? null : element.toString());
        }

        private String name(String name) {
            if(name == null) return DynamicProxyTypeAssembly.this.getName();
            return String.join("$", DynamicProxyTypeAssembly.this.getName(), name);
        }
    }


    public static sealed class DynamicProxyContext<T> implements ProxyContext<T> permits DynamicProxyAnnotationTypeAssembly.DynamicProxyContext {

        private final ProxyInformation<T> info;

        protected Object carrier;

        DynamicProxyContext(String name, Scope scope, ProxyTypeAssembly<T> assembly, T instance) {
            this.info = new DynamicProxyInfo<>(name, scope, assembly, instance);
        }

        @Override
        public @NotNull Scope getScope() {
            return info.scope();
        }

        @Override
        public @NotNull ProxyTypeAssembly<T> getAssembly() {
            return info.assembly();
        }

        @Override
        public @NotNull String getName() {
            return info.name();
        }

        @Override
        public @NotNull T getInstance() {
            return info.instance();
        }

        @Override
        @SuppressWarnings("unchecked")
        public <E extends ProxyContext<T>> @NotNull E getDelegate() {
            return (E) this;
        }
    }
}
