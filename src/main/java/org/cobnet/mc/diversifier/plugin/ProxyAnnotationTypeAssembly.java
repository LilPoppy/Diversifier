package org.cobnet.mc.diversifier.plugin;

import java.lang.annotation.Annotation;

public interface ProxyAnnotationTypeAssembly<T extends Annotation> extends ProxyTypeAssembly<T>, AnnotationTypeAssembly<T> {

    AnnotationProxyBuilder<T> create(String name);
}
