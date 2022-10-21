package org.cobnet.mc.diversifier.plugin.support;

import org.cobnet.mc.diversifier.plugin.MethodAssembly;
import org.cobnet.mc.diversifier.plugin.TypeAssembly;

import java.lang.reflect.Method;

public non-sealed class ManagedMethodAssembly<T> extends ManagedMemberAssembly<T, Method> implements MethodAssembly<T> {

    protected ManagedMethodAssembly(TypeAssembly<T> parent, Method method) {
        super(parent, method);
    }

}
