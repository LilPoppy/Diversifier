package org.cobnet.mc.diversifier.plugin.support;

import org.cobnet.mc.diversifier.exception.ProxyException;
import org.cobnet.mc.diversifier.plugin.*;
import org.cobnet.mc.diversifier.plugin.enums.ProxyScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class DynamicProxyAnnotationTypeAssembly<T extends Annotation> extends DynamicProxyTypeAssembly<T> implements ProxyAnnotationTypeAssembly<T> {

    DynamicProxyAnnotationTypeAssembly(PluginAssembly<?> plugin, TypeAssembly<? super T> type, Class<T> proxy, List<MemberAssembly<T, ?>> members) {
        super(plugin, type, proxy, members);
    }

    @Override
    protected DynamicProxyContext<T> create_context(String name, Scope scope, T instance) {
        return new DynamicProxyContext<>(name, scope, this, instance);
    }

    @Override
    public @NotNull T create(String name, Object... args) throws ProxyException {
        return this.create(name, ProxyScope.PROTOTYPE, args).getInstance();
    }

    @Override
    public @NotNull ProxyContext<T> create(String name, Scope scope, Object... args) throws ProxyException {
        if(ProxyScope.PROTOTYPE.compareTo(scope) != 0) throw new ProxyException("Annotation proxy can only be created with prototype scope");
        return super.create(name, scope, args);
    }

    @Override
    public ProxyBuilder.@NotNull Singleton<T> build(String name, Object... args) {
        throw new UnsupportedOperationException("Cannot build annotation proxy as singleton scope.");
    }

    @Override
    public @NotNull AnnotationProxyBuilder<T> build(@NotNull AnnotatedElement carrier, Object... args) {
        return new DynamicAnnotationProxyBuilder(carrier, args);
    }


    final class DynamicAnnotationProxyBuilder extends DynamicPrototypeBuilder<AnnotatedElement> implements AnnotationProxyBuilder<T> {

        private final Map<MethodAssembly<T>, Object> values = new HashMap<>();

        DynamicAnnotationProxyBuilder(AnnotatedElement carrier, Object... args) {
            super(carrier, args);
        }

        @Override
        public @NotNull AnnotationProxyBuilder<T> carrier(@NotNull AnnotatedElement carrier) {
            return (AnnotationProxyBuilder<T>) super.carrier(carrier);
        }

        @Override
        public @NotNull AnnotationProxyBuilder<T> name(@NotNull String name) {
            return (AnnotationProxyBuilder<T>) super.name(name);
        }

        @Override
        public @NotNull AnnotationProxyBuilder<T> value(@NotNull String key, @Nullable Object value) {
            Objects.requireNonNull(key, "Key cannot be null");
            Objects.requireNonNull(value, "Value cannot be null");
            MethodAssembly<T> method = DynamicProxyAnnotationTypeAssembly.this.getMethod(key);
            if(method == null) throw new IllegalArgumentException("No such method " + key + " in " + DynamicProxyAnnotationTypeAssembly.this.getName());
            return this.value(method, value);
        }

        @Override
        public @NotNull AnnotationProxyBuilder<T> value(@NotNull MethodAssembly<T> key, @Nullable Object value) {
            Objects.requireNonNull(key, "Key cannot be null");
            Objects.requireNonNull(value, "Value cannot be null");
            this.values.put(key, value);
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public @NotNull T build() throws ProxyException {
            ProxyContext<T> context = (ProxyContext<T>) super.build();
            if(context.getDelegate() instanceof DynamicProxyAnnotationTypeAssembly.DynamicProxyContext<T> dynamic) {
                dynamic.properties.putAll(this.values);
                return dynamic.getInstance();
            }
            throw new ProxyException("The proxy class " + carrier.getName() + " does not use correct proxy context.");
        }

        @Override
        public @NotNull Singleton<T> singleton() {
            throw new UnsupportedOperationException("Singletons are not supported for annotation proxies.");
        }
    }

    public static final class DynamicProxyContext<T extends Annotation> extends DynamicProxyTypeAssembly.DynamicProxyContext<T> implements ExtendableAnnotation<T> {

        DynamicProxyContext(String name, Scope scope, ProxyTypeAssembly<T> assembly, T instance) {
            super(name, scope, assembly, instance);
        }

        final List<ExtendableAnnotation<?>> annotations = new LinkedList<>();

        final Map<MethodAssembly<T>, Object> properties = new ConcurrentHashMap<>();

        @Override
        public ExtendableAnnotation<?>[] getParents() {
            return annotations.toArray(ExtendableAnnotation[]::new);
        }

        @Override
        public Object getValue(MethodAssembly<?> key) {
            return properties.get(key);
        }

        @Override
        public ProxyContext<T> getProxyContext() {
            return DynamicProxyContext.this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public AnnotationTypeAssembly<T> getType() {
            return (AnnotationTypeAssembly<T>) getProxyContext().getAssembly();
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return DynamicProxyContext.this.getInstance().annotationType();
        }

        @Override
        public @NotNull T getInstance() {
            return super.getInstance();
        }
    }
}
