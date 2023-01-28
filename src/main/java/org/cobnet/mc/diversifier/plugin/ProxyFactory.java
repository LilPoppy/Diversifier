package org.cobnet.mc.diversifier.plugin;

import org.cobnet.mc.diversifier.exception.ProxyException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

public interface ProxyFactory {

    public <T> @NotNull T create(@Nullable String name, @NotNull Class<T> type, Object... args) throws ProxyException;

    public <T> @NotNull T create(@NotNull Class<T> type, Object... args) throws ProxyException;

    public <T> @NotNull T create(@Nullable String name, @NotNull TypeAssembly<T> type, Object... args) throws ProxyException;

    public <T> @NotNull T create(@NotNull TypeAssembly<T> type, Object... args) throws ProxyException;

    public <T extends Annotation> @NotNull AnnotationProxyBuilder<T> build(@NotNull Class<T> type, @NotNull AnnotatedElement carrier) throws ProxyException;

    public <T> ProxyBuilder.@NotNull Singleton<T> build(@NotNull Class<T> type, Object... args) throws ProxyException;

    public <T> @Nullable T getProxy(@NotNull String name);

    public <T> @Nullable T getProxy(@NotNull String name, @NotNull TypeAssembly<T> type);

    public <T> @Nullable T getProxy(@NotNull TypeAssembly<T> type);

    public <T> @Nullable T getProxy(@NotNull String name, @NotNull Class<T> type);

    public <T> @Nullable T getProxy(@NotNull Class<T> type);

    public <T> @NotNull T[] getProxies(@NotNull Class<T> type);

    public <T> @NotNull T[] getProxies(@NotNull TypeAssembly<T> type);

    public int getSize();
}
