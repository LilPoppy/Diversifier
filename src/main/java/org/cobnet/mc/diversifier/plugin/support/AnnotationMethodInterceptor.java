package org.cobnet.mc.diversifier.plugin.support;

import net.sf.cglib.proxy.MethodProxy;
import org.cobnet.mc.diversifier.plugin.ProxyContext;
import org.cobnet.mc.diversifier.plugin.ProxyMethodInterceptor;
import org.cobnet.mc.diversifier.plugin.TypeAssembly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

public class AnnotationMethodInterceptor extends AbstractProxyMethodInterceptor{
    @Override
    public Object intercept(@NotNull TypeAssembly<?> type, @NotNull ProxyContext<?> context, @NotNull Method method, @NotNull Object[] args, @NotNull MethodProxy proxy, @Nullable ProxyMethodInterceptor next) throws Throwable {
        return proxy.invokeSuper(context, args);
    }

    @Override
    public boolean isMatch(TypeAssembly<?> type) {
        return type.isAnnotationType();
    }
}
