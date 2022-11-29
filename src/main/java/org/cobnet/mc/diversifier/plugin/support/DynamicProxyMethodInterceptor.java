package org.cobnet.mc.diversifier.plugin.support;

import net.bytebuddy.implementation.bind.annotation.*;
import org.cobnet.mc.diversifier.plugin.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

public final class DynamicProxyMethodInterceptor implements MethodInterceptor {

    DynamicProxyMethodInterceptor(){}

    @RuntimeType
    @Override
    public @Nullable Object intercept(@This @NotNull Object instance, @NotNull @Origin Method method, @NotNull @AllArguments Object[] args, @NotNull @Morph ParameterizedCallable<?> callable) throws Throwable {
        return commit(instance, method, args, callable);
    }

    @RuntimeType
    @Override
    public @Nullable Object intercept(@NotNull @This Object instance, @NotNull @Origin Method method, @NotNull @AllArguments Object[] args) throws Throwable {
        return commit(instance, method, args, null);
    }

    private Object commit(Object instance, Method method, Object[] args, ParameterizedCallable<?> callable) throws Throwable {
        Object result = null;
        ProxyContext<?> context = (ProxyContext<?>) instance;
        MethodAssembly<?> assembly = context.getAssembly().getMethod(method.getName(), method.getParameterTypes());
        commit_before(instance, assembly, args, callable);
        assert assembly != null;
        //call before interceptors
        System.out.println(method);
        if (context.getProxyContextInstance() instanceof DynamicProxyAnnotationTypeAssembly.DynamicProxyContext<?> dynamic) {
            if (dynamic.annotation.properties.size() > 0 && (result = dynamic.annotation.properties.get(assembly)) != null)
                return commit_after(instance, assembly, result);
        }
        if (callable != null) {
            result = callable.call(instance, method, args);
        }
        return commit_after(instance, assembly, result);
    }

    private Object commit_before(Object instance, MethodAssembly<?> assembly, Object[] args, ParameterizedCallable<?> callable) throws Throwable {
        System.out.println("bytebuddy method call before");
        return null;
    }

    private Object commit_after(Object instance, MethodAssembly<?> assembly, Object result) {
        System.out.println("bytebuddy method call after");
        return result;
    }
}
