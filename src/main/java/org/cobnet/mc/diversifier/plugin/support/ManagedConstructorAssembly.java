package org.cobnet.mc.diversifier.plugin.support;

import org.cobnet.mc.diversifier.plugin.ConstructorAssembly;
import org.cobnet.mc.diversifier.plugin.TypeAssembly;

import java.lang.reflect.Constructor;

public non-sealed class ManagedConstructorAssembly<T> extends ManagedMemberAssembly<T, Constructor<T>> implements ConstructorAssembly<T> {

    protected ManagedConstructorAssembly(TypeAssembly<T> parent, Constructor<T> constructor) {
        super(parent, constructor);
    }
}
