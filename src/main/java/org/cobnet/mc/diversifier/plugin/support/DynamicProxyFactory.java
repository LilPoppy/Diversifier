package org.cobnet.mc.diversifier.plugin.support;

import lombok.Getter;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.pool.TypePool;
import org.cobnet.mc.diversifier.Diversifier;
import org.cobnet.mc.diversifier.exception.ProxyException;
import org.cobnet.mc.diversifier.plugin.*;
import org.cobnet.mc.diversifier.plugin.enums.ProxyScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class DynamicProxyFactory implements ProxyFactory {

    private final Map<Class<?>, ProxyContext<?>> contexts = new ConcurrentHashMap<>();

    private final ProxyMethodInterceptor chain;

    public DynamicProxyFactory() {
        this.chain = new AnnotationMethodInterceptor();
    }

    final <T> ProxyContext<T> create_proxy(String name, TypeAssembly<T> type, Object... args) throws ProxyException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if(Modifier.isFinal(type.get().getModifiers())) throw new ProxyException("Cannot create proxy for final class " + type.get().getName());
        DynamicType.Unloaded<T> unloaded = new ByteBuddy().subclass(type.get()).implement(ProxyContext.class).make();
        ///.make(new DynamicProxyFactory.DynamicProxyContext(name, type, ProxyScope.SINGLETON, this.chain))
        ProxyContext<T> context = (ProxyContext<T>) unloaded.load(type.get().getClassLoader()).getLoaded().getConstructor(Arrays.stream(args).map(Object::getClass).toArray(Class[]::new)).newInstance(args);

        this.contexts.put(type.get(), context);
        return context;
    }

    @Override
    public <T> @NotNull T create(@NotNull String name, @NotNull Class<T> type, Object... args) throws ProxyException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        return create(name, Objects.requireNonNull(Diversifier.getTypeFactory().getTypeAssembly(type), "Type assembly for type " + type.getName() + " not found."), args);
    }

    @Override
    public <T> @NotNull T create(@NotNull Class<T> type, Object... args) throws ProxyException {
        return null;
    }

    @Override
    public <T> @NotNull T create(@NotNull String name, @NotNull TypeAssembly<T> type, Object... args) throws ProxyException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        return (T) create_proxy(name, type, args);
    }

    @Override
    public <T> @NotNull T create(@NotNull TypeAssembly<T> type, Object... args) throws ProxyException {
        return null;
    }

    @Override
    public <T> @Nullable T getProxy(@NotNull String name) {
        return null;
    }

    @Override
    public <T> @Nullable T getProxy(@NotNull String name, @NotNull TypeAssembly<T> type) {
        return null;
    }

    @Override
    public <T> @Nullable T getProxy(@NotNull TypeAssembly<T> type) {
        return null;
    }

    @Override
    public <T> @Nullable T getProxy(@NotNull String name, @NotNull Class<T> type) {
        return null;
    }

    @Override
    public <T> @Nullable T getProxy(@NotNull Class<T> type) {
        return null;
    }


    public record DynamicProxyContext<T>(@Getter String name, @Getter TypeAssembly<T> assembly, @Getter Scope scope, @Getter ProxyMethodInterceptor chain, Map<Class<?>, Map<Method, Object>> methods) implements ProxyContext<T> {

        final static Method GET_NAME_METHOD;

        final static Method GET_PROXY_METHOD;

        final static Method GET_ASSEMBLY_METHOD;

        final static Method GET_SCOPE_METHOD;

        final static Method GET_CHAIN_METHOD;

        final static Method ADD_METHOD_METHOD;

        static {
            try {
                GET_NAME_METHOD = ProxyContext.class.getMethod("getName");
                GET_PROXY_METHOD = ProxyContext.class.getMethod("getProxy");
                GET_ASSEMBLY_METHOD = ProxyContext.class.getMethod("getAssembly");
                GET_SCOPE_METHOD = ProxyContext.class.getMethod("getScope");
                GET_CHAIN_METHOD = ProxyContext.class.getMethod("getChain");
                ADD_METHOD_METHOD = ProxyContext.class.getMethod("addMethod", MethodAssembly.class, Object.class);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }

        DynamicProxyContext(String name, TypeAssembly<T> assembly, Scope scope, ProxyMethodInterceptor chain) {
            this(name, assembly, scope, chain, new HashMap<>());
        }

        DynamicProxyContext(TypeAssembly<T> assembly, Scope scope, ProxyMethodInterceptor chain) {
            this(assembly.get().getSimpleName(), assembly, scope, chain);
        }

        DynamicProxyContext(TypeAssembly<T> assembly, ProxyMethodInterceptor chain) {
            this(assembly, ProxyScope.SINGLETON, chain);
        }

        DynamicProxyContext(String name, TypeAssembly<T> assembly, ProxyMethodInterceptor chain) {
            this(name, assembly, ProxyScope.SINGLETON, chain, new HashMap<>());
        }

//        @Override
//        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
//            if(method.equals(DynamicProxyContext.GET_NAME_METHOD)) return this.name();
//            if(method.equals(DynamicProxyContext.GET_PROXY_METHOD)) return obj;
//            if(method.equals(DynamicProxyContext.GET_ASSEMBLY_METHOD)) return this.assembly();
//            if(method.equals(DynamicProxyContext.GET_SCOPE_METHOD)) return this.scope();
//            if(method.equals(DynamicProxyContext.GET_CHAIN_METHOD)) return this.chain();
//            if(method.equals(DynamicProxyContext.ADD_METHOD_METHOD)) {
//                 this.addMethod((MethodAssembly<?>) args[0], args[1]);
//                 return obj;
//            }
//            return this.chain().intercept(obj, method, args, proxy);
//        }

        @Override
        public ProxyContext<T> addMethod(MethodAssembly<?> method, Object value) {
            Map<Method, Object> values = methods.computeIfAbsent(method.get().getDeclaringClass(), k -> new HashMap<>());
            values.put(method.get(), value);
            return this;
        }
    }

}
