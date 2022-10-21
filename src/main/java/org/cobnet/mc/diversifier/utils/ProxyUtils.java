package org.cobnet.mc.diversifier.utils;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.Arrays;

public class ProxyUtils {

    public static <T> T createProxy(Class<T> clazz, Object... args) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clazz);
        enhancer.setCallback(new ECallback());
        return (T) enhancer.create(Arrays.stream(args).map(Object::getClass).toArray(Class[]::new), args);
    }

    public final static class ECallback implements MethodInterceptor {

        @Override
        public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
            if(!method.getName().equals("toString") && !method.getName().equals("hashCode")) {
                System.out.println(method.getName());
            }
            return methodProxy.invokeSuper(o, objects);
        }
    }

}
