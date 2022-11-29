package org.cobnet.mc.diversifier.plugin.support;

import lombok.Getter;
import org.cobnet.mc.diversifier.exception.ProxyException;
import org.cobnet.mc.diversifier.plugin.*;
import org.cobnet.mc.diversifier.plugin.enums.ProxyScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class DynamicProxyAnnotationTypeAssembly<T extends Annotation> extends DynamicProxyTypeAssembly<T> implements ProxyAnnotationTypeAssembly<T> {

    DynamicProxyAnnotationTypeAssembly(PluginAssembly<?> plugin, TypeAssembly<? super T> type, Class<T> proxy, List<MemberAssembly<T, ?>> members) {
        super(plugin, type, proxy, members);
    }

    @Override
    public @NotNull ProxyContext<T> create(String name, Scope scope, Object... args) throws ProxyException {
        ProxyContext<T> context = super.create(name, scope, args);
        if(context.getProxyContextInstance() instanceof DynamicProxyAnnotationTypeAssembly.DynamicProxyContext<T> dynamic) {
            try {
                Field field = this.get().getDeclaredField(DynamicProxyFactory.EXTENDABLE_ANNOTATION_FIELD);
                field.setAccessible(true);
                field.set(context.getInstance(), dynamic.annotation);
                field.setAccessible(false);
                return context;
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new ProxyException("Failed to create proxy for " + original.getName(), e);
            }
        }
        throw new ProxyException("The proxy class " + this.get().getName() + " does not use correct proxy context.");
    }

    @Override
    protected DynamicProxyContext<T> create_context(String name, Scope scope, T instance) {
        return new DynamicProxyContext<>(name, scope, this, instance);
    }

    @Override
    public AnnotationProxyBuilder<T> create(String name) {
        return new DynamicAnnotationProxyBuilder(name);
    }


    final class DynamicAnnotationProxyBuilder implements AnnotationProxyBuilder<T> {

        private final Map<MethodAssembly<T>, Object> values = new HashMap<>();
        @Getter
        private final String name;
        @Getter
        private Scope scope;

        DynamicAnnotationProxyBuilder(String name) {
            this.name = name;
            this.scope = ProxyScope.PROTOTYPE;
        }

        @Override
        public @NotNull AnnotationProxyBuilder<T> set(@NotNull String key, @Nullable Object value) {
            Objects.requireNonNull(key, "Key cannot be null");
            Objects.requireNonNull(value, "Value cannot be null");
            MethodAssembly<T> method = DynamicProxyAnnotationTypeAssembly.this.getMethod(key);
            if(method == null) throw new IllegalArgumentException("No such method " + key + " in " + DynamicProxyAnnotationTypeAssembly.this.get().getName());
            return this.set(method, value);
        }

        @Override
        public @NotNull AnnotationProxyBuilder<T> set(@NotNull MethodAssembly<T> key, @Nullable Object value) {
            Objects.requireNonNull(key, "Key cannot be null");
            Objects.requireNonNull(value, "Value cannot be null");
            this.values.put(key, value);
            return this;
        }

        @Override
        public @NotNull T build() throws ProxyException {
            ProxyContext<T> context = DynamicProxyAnnotationTypeAssembly.this.create(name, scope);
            if(context.getProxyContextInstance() instanceof DynamicProxyAnnotationTypeAssembly.DynamicProxyContext<T> dynamic) {
                dynamic.annotation.properties.putAll(this.values);
                return dynamic.getInstance();
            }
            throw new ProxyException("The proxy class " + name + " does not use correct proxy context.");
        }

        @Override
        public @NotNull AnnotationProxyBuilder<T> scope(Scope scope) throws ProxyException {
             this.scope = scope;
             return this;
        }
    }

    public static final class DynamicProxyContext<T extends Annotation> extends DynamicProxyTypeAssembly.DynamicProxyContext<T> {

        final DynamicExtendableAnnotation annotation = new DynamicExtendableAnnotation();

        DynamicProxyContext(String name, Scope scope, ProxyTypeAssembly<T> assembly, T instance) {
            super(name, scope, assembly, instance);
        }

        public final class DynamicExtendableAnnotation implements ExtendableAnnotation<T> {

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
            public AnnotationTypeAssembly<T> getType() {
                return (AnnotationTypeAssembly<T>) getProxyContext().getAssembly();
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return getType().get();
            }
        }
    }
}
