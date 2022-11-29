package org.cobnet.mc.diversifier.plugin;

import java.lang.reflect.Method;

public interface MethodAssembly<T> extends InvocableMemberAssembly<T, Method, Object> {

    public Object invoke(T instance, Object... args) throws Throwable;
}
