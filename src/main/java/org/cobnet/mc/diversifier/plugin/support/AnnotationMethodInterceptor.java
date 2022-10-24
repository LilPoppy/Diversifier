package org.cobnet.mc.diversifier.plugin.support;

import org.cobnet.mc.diversifier.plugin.TypeAssembly;

public class AnnotationMethodInterceptor extends AbstractProxyMethodInterceptor{
//    @Override
//    public Object intercept(@NotNull TypeAssembly<?> type, @NotNull ProxyContext<?> context, @NotNull Method method, @NotNull Object[] args, @NotNull MethodProxy proxy, @Nullable ProxyMethodInterceptor next) throws Throwable {
//        return proxy.invokeSuper(context, args);
//    }

    @Override
    public boolean isMatch(TypeAssembly<?> type) {
        return type.isAnnotationType();
    }
}
