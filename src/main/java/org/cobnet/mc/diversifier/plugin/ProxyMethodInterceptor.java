package org.cobnet.mc.diversifier.plugin;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

public interface ProxyMethodInterceptor extends MethodInterceptor {

    @Override
    public default Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        if(obj instanceof ProxyContext<?> context) {
            if(this.isMatch(context.getAssembly())) {
                return this.intercept(context.getAssembly(), context, method, args, proxy, this.next());
            }
            ProxyMethodInterceptor next;
            if((next = this.next()) != null) {
                return next.intercept(obj, method, args, proxy);
            }
            return proxy.invokeSuper(obj, args);
        }
        throw new UnsupportedOperationException("Cannot intercept method " + method.getName() + " on object " + obj);
    }

    public Object intercept(@NotNull TypeAssembly<?> type, @NotNull ProxyContext<?> context, @NotNull Method method, @NotNull Object[] args, @NotNull MethodProxy proxy, @Nullable ProxyMethodInterceptor next) throws Throwable;

    public boolean isMatch(@NotNull TypeAssembly<?> type);

    public boolean hasNext();

    public @Nullable ProxyMethodInterceptor next();

}
