package org.cobnet.mc.diversifier.plugin;

import java.lang.reflect.AnnotatedElement;

public interface AnnotatedAssembly<T extends AnnotatedElement, K extends Assembly<?, ?>, V extends HierarchicalAssembly<?, ?, ?>> extends HierarchicalAssembly<T, K, V>, AnnotatedElement {

}
