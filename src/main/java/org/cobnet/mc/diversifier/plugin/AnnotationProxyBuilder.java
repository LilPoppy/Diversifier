package org.cobnet.mc.diversifier.plugin;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

public interface AnnotationProxyBuilder<T extends Annotation> extends ProxyBuilder.Prototype<T, AnnotatedElement> {

    @Override
    public @NotNull AnnotationProxyBuilder<T> carrier(@NotNull AnnotatedElement carrier);

    @Override
    public @NotNull AnnotationProxyBuilder<T> name(@NotNull String name);

    public @NotNull AnnotationProxyBuilder<T> value(@NotNull String key, @Nullable Object value);

    public @NotNull AnnotationProxyBuilder<T> value(@NotNull MethodAssembly<T> key, @Nullable Object value);
}
