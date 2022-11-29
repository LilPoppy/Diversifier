package org.cobnet.mc.diversifier.plugin.support;

import org.cobnet.mc.diversifier.plugin.MethodAssembly;
import org.cobnet.mc.diversifier.plugin.TypeAssembly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Stream;

final class ManagedMethodAssembly<T> extends ManagedInvocableMemberAssembly<T, Method, Object> implements MethodAssembly<T> {

    ManagedMethodAssembly(TypeAssembly<T> parent, Method method, List<MethodAssembly<?>> children) {
        super(parent, method, children);
    }

    @Override
    public Object invoke(T instance, Object... args) throws Throwable {
        return this.instance.invoke(instance, args);
    }
}
