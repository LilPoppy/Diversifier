package org.cobnet.mc.diversifier.plugin;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;

public interface MemberAssembly<T, E extends Member & AnnotatedElement> extends AnnotatedAssembly<E, TypeAssembly<T>, MethodAssembly<?>> {

    public TypeAssembly<T> getDeclaredType();

}
