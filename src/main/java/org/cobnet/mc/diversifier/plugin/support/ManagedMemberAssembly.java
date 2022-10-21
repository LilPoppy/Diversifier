package org.cobnet.mc.diversifier.plugin.support;

import org.cobnet.mc.diversifier.plugin.*;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;

public sealed class ManagedMemberAssembly<T, E extends Member & AnnotatedElement> extends AbstractManagedAnnotatedAssembly<E, TypeAssembly<T>, MethodAssembly<?>> implements MemberAssembly<T, E> permits ManagedMethodAssembly, ManagedFieldAssembly, ManagedConstructorAssembly {

    protected ManagedMemberAssembly(TypeAssembly<T> parent, E member) {
        super(parent, member);
    }

    @Override
    public @NotNull String getName() {
        return this.get().getName();
    }

    @Override
    public TypeAssembly<T> getDeclaredType() {
        return this.getParent();
    }
}
