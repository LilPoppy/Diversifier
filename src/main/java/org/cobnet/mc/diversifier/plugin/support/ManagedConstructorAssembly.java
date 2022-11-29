package org.cobnet.mc.diversifier.plugin.support;

import org.cobnet.mc.diversifier.plugin.ConstructorAssembly;
import org.cobnet.mc.diversifier.plugin.MethodAssembly;
import org.cobnet.mc.diversifier.plugin.TypeAssembly;

import java.lang.reflect.Constructor;
import java.util.List;

final class ManagedConstructorAssembly<T> extends ManagedInvocableMemberAssembly<T, Constructor<T>, T> implements ConstructorAssembly<T> {

    ManagedConstructorAssembly(TypeAssembly<T> parent, Constructor<T> constructor, List<MethodAssembly<?>> children) {
        super(parent, constructor, children);
    }

    @Override
    public T newInstance(Object... args) throws Throwable {
        return this.instance.newInstance(args);
    }
}
