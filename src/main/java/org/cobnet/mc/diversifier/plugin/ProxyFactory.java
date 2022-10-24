package org.cobnet.mc.diversifier.plugin;

import org.cobnet.mc.diversifier.exception.ProxyException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;

public interface ProxyFactory {

    public <T> @NotNull T create(@NotNull String name, @NotNull Class<T> type, Object... args) throws ProxyException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException;

    public <T> @NotNull T create(@NotNull Class<T> type, Object... args) throws ProxyException;

    public <T> @NotNull T create(@NotNull String name, @NotNull TypeAssembly<T> type, Object... args) throws ProxyException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException;

    public <T> @NotNull T create(@NotNull TypeAssembly<T> type, Object... args) throws ProxyException;

    public <T> @Nullable T getProxy(@NotNull String name);

    public <T> @Nullable T getProxy(@NotNull String name, @NotNull TypeAssembly<T> type);

    public <T> @Nullable T getProxy(@NotNull TypeAssembly<T> type);

    public <T> @Nullable T getProxy(@NotNull String name, @NotNull Class<T> type);

    public <T> @Nullable T getProxy(@NotNull Class<T> type);
}
