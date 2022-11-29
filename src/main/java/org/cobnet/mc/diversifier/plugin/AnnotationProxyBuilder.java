package org.cobnet.mc.diversifier.plugin;

import org.cobnet.mc.diversifier.exception.ProxyException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;

public interface AnnotationProxyBuilder<T extends Annotation> {

    public @NotNull AnnotationProxyBuilder<T> set(@NotNull String key, @Nullable Object value);

    public @NotNull AnnotationProxyBuilder<T> set(@NotNull MethodAssembly<T> key, @Nullable Object value);

    public @NotNull T build() throws ProxyException;

    public @NotNull AnnotationProxyBuilder<T> scope(Scope scope) throws ProxyException;
}
