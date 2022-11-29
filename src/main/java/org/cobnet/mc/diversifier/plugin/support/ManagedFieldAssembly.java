package org.cobnet.mc.diversifier.plugin.support;

import org.cobnet.mc.diversifier.plugin.FieldAssembly;
import org.cobnet.mc.diversifier.plugin.MethodAssembly;
import org.cobnet.mc.diversifier.plugin.TypeAssembly;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.List;

final class ManagedFieldAssembly<T> extends ManagedMemberAssembly<T, Field> implements FieldAssembly<T> {

    ManagedFieldAssembly(TypeAssembly<T> parent, Field field, List<MethodAssembly<?>> children) {
        super(parent, field, children);
    }
}
