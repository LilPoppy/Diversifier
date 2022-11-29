package org.cobnet.mc.diversifier.plugin.support;

import org.cobnet.mc.diversifier.plugin.InvocableMemberAssembly;
import org.cobnet.mc.diversifier.plugin.MethodAssembly;
import org.cobnet.mc.diversifier.plugin.TypeAssembly;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Executable;
import java.util.List;

sealed abstract class ManagedInvocableMemberAssembly<T, E extends Executable, R>  extends ManagedMemberAssembly<T, E> implements InvocableMemberAssembly<T, E, R> permits ManagedConstructorAssembly, ManagedMethodAssembly {


    protected ManagedInvocableMemberAssembly(TypeAssembly<T> parent, E member, List<MethodAssembly<?>> children) {
        super(parent, member, children);
    }

    @Override
    public @NotNull Class<?>[] getParameterTypes() {
        return this.instance.getParameterTypes();
    }
}
