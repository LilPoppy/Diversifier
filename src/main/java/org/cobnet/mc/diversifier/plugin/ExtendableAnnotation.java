package org.cobnet.mc.diversifier.plugin;

import java.lang.annotation.Annotation;

public interface ExtendableAnnotation<T extends Annotation> extends Annotation {

    public ExtendableAnnotation<?>[] getParents();

    public Object getValue(MethodAssembly<?> key);

    public ProxyContext<T> getProxyContext();

    public AnnotationTypeAssembly<T> getType();
}
