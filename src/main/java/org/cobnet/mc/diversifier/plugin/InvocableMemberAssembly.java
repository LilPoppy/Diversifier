package org.cobnet.mc.diversifier.plugin;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Executable;

public interface InvocableMemberAssembly<T, E extends Executable, R> extends MemberAssembly<T, E> {

    public @NotNull Class<?>[] getParameterTypes();
}
