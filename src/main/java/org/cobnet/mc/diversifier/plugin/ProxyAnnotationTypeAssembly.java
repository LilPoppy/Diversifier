package org.cobnet.mc.diversifier.plugin;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

public interface ProxyAnnotationTypeAssembly<T extends Annotation> extends ProxyTypeAssembly<T>, AnnotationTypeAssembly<T> {

    public @NotNull AnnotationProxyBuilder<T> build(@NotNull AnnotatedElement carrier, Object... args);
}
