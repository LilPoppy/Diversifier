package org.cobnet.mc.diversifier.plugin.support;

import lombok.Getter;
import org.cobnet.mc.diversifier.exception.ProxyException;
import org.cobnet.mc.diversifier.plugin.*;
import org.cobnet.mc.diversifier.plugin.enums.ProxyScope;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public sealed class DynamicProxyTypeAssembly<T> extends ManagedTypeAssembly<T> implements ProxyTypeAssembly<T> permits DynamicProxyAnnotationTypeAssembly {

    @Getter
    protected final TypeAssembly<? super T> original;

    protected DynamicProxyTypeAssembly(PluginAssembly<?> plugin, TypeAssembly<? super T> type, Class<T> proxy, List<MemberAssembly<T, ?>> members) {
        super(plugin, proxy, members);
        this.original = type;
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
    public @NotNull T create(Object... args) throws ProxyException {
        return (T) this.create(this.get().getSimpleName(), ProxyScope.SINGLETON, args);
    }

    //可以在拦截器中设置当对象被创建时判断是否加入到代理工厂中
    @Override
    public @NotNull ProxyContext<T> create(String name, Scope scope, Object... args) throws ProxyException {
        try {
            T instance = super.create(args);
            if(ProxyContext.is(instance)) {
                ProxyContext<T> context = create_context(name, scope, instance);
                Field field = this.get().getDeclaredField(DynamicProxyFactory.PROXY_CONTEXT_FIELD);
                field.setAccessible(true);
                field.set(instance, context);
                field.setAccessible(false);
                return context;
            }
            throw new ProxyException("The proxy class " + this.get().getName() + " does not implement ProxyContext");
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchFieldException e) {
            throw new ProxyException("Failed to create proxy for " + original.getName(), e);
        }
    }

    protected DynamicProxyContext<T> create_context(String name, Scope scope, T instance) {
        return new DynamicProxyContext<>(name, scope, this, instance);
    }

    private record DynamicProxyInfo<T>(@Getter String name, @Getter Scope scope, @Getter ProxyTypeAssembly<T> assembly, @Getter T instance) implements ProxyInformation<T> {}

    public static sealed class DynamicProxyContext<T> implements ProxyContext<T> permits DynamicProxyAnnotationTypeAssembly.DynamicProxyContext {

        private final ProxyInformation<T> info;

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
        public <E extends ProxyContext<T>> @NotNull E getProxyContextInstance() {
            return (E) this;
        }
    }
}
