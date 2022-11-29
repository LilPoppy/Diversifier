package org.cobnet.mc.diversifier.plugin.support;

import org.cobnet.mc.diversifier.plugin.*;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.util.List;

sealed class ManagedMemberAssembly<T, E extends Member & AnnotatedElement> extends AbstractManagedAnnotatedAssembly<E, TypeAssembly<T>, MethodAssembly<?>> implements MemberAssembly<T, E> permits ManagedFieldAssembly, ManagedInvocableMemberAssembly {

    protected ManagedMemberAssembly(TypeAssembly<T> parent, E member, List<MethodAssembly<?>> children) {
        super(parent, member, children);
    }

    @Override
    public @NotNull String getName() {
        return this.instance.getName();
    }

    @Override
    public TypeAssembly<T> getDeclaredType() {
        return this.getParent();
    }
}
