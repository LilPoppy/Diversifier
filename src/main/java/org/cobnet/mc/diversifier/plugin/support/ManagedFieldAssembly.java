package org.cobnet.mc.diversifier.plugin.support;

import org.cobnet.mc.diversifier.plugin.FieldAssembly;
import org.cobnet.mc.diversifier.plugin.TypeAssembly;

import java.lang.reflect.Field;

public non-sealed class ManagedFieldAssembly<T> extends ManagedMemberAssembly<T, Field> implements FieldAssembly<T> {

    protected ManagedFieldAssembly(TypeAssembly<T> parent, Field field) {
        super(parent, field);
    }

}
