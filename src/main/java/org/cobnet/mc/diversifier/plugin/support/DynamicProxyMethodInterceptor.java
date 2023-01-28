package org.cobnet.mc.diversifier.plugin.support;

import net.bytebuddy.implementation.bind.annotation.*;
import org.cobnet.mc.diversifier.exception.ProxyException;
import org.cobnet.mc.diversifier.plugin.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

public final class DynamicProxyMethodInterceptor implements MethodInterceptor {

    DynamicProxyMethodInterceptor(){}

    @RuntimeType
    @Override
    public @Nullable Object intercept(@This @NotNull ProxyContext<?> context, @NotNull @Origin Method method, @NotNull @AllArguments Object[] args, @NotNull @Morph ParameterizedCallable callable) throws Throwable {
        return commit(context, method, args, callable);
    }

    @RuntimeType
    @Override
    public @Nullable Object intercept(@NotNull @This ProxyContext<?> context, @NotNull @Origin Method method, @NotNull @AllArguments Object[] args) throws Throwable {
        return commit(context, method, args, null);
    }

    private Object commit(ProxyContext<?> context, Method method, Object[] args, ParameterizedCallable callable) throws Throwable {
        Object result = null;
        MethodAssembly<?> assembly = context.getAssembly().getMethod(method.getName(), method.getParameterTypes());
        commit_before(context, assembly, args, callable);
        assert assembly != null;
        //call before interceptors
        ProxyContext<?> delegate = context.getDelegate();
        if(delegate instanceof DynamicProxyTypeAssembly.DynamicProxyContext<?>) {

        }
        if (context.getDelegate() instanceof DynamicProxyAnnotationTypeAssembly.DynamicProxyContext<?> dynamic) {
            if(dynamic.carrier == null) throw new ProxyException("Proxy '" + context.getName() + "' is not initialized");
            if (dynamic.properties.size() > 0 && (result = dynamic.properties.get(assembly)) != null)
                return commit_after(context, assembly, result);
        }
        if (callable != null) {
            result = callable.call(args);
        }
        return commit_after(context, assembly, result);
    }

    private Object commit_before(ProxyContext<?> context, MethodAssembly<?> assembly, Object[] args, ParameterizedCallable callable) throws Throwable {
        System.out.println("bytebuddy method call before");
        return null;
    }

    private Object commit_after(ProxyContext<?> context, MethodAssembly<?> assembly, Object result) {
        System.out.println("bytebuddy method call after");
        return result;
    }
}
