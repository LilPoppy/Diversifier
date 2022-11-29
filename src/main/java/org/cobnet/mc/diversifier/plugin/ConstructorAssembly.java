package org.cobnet.mc.diversifier.plugin;

import java.lang.reflect.Constructor;

public interface ConstructorAssembly<T> extends InvocableMemberAssembly<T, Constructor<T>, T> {

    public T newInstance(Object... args) throws Throwable;
}
