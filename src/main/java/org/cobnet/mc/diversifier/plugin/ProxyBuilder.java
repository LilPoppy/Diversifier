package org.cobnet.mc.diversifier.plugin;

import org.cobnet.mc.diversifier.exception.ProxyException;
import org.jetbrains.annotations.NotNull;

public interface ProxyBuilder<T> {

    public @NotNull ProxyBuilder<T> name(@NotNull String name);

    public @NotNull T build() throws ProxyException;

    interface Singleton<T> extends ProxyBuilder<T> {

        @Override
        public @NotNull ProxyBuilder.Singleton<T> name(@NotNull String name);

        public <E> @NotNull Prototype<T, E> prototype(@NotNull E carrier);
    }

    interface Prototype<T, E> extends ProxyBuilder<T> {

        @Override
        public @NotNull ProxyBuilder.Prototype<T, E> name(@NotNull String name);

        public @NotNull ProxyBuilder.Prototype<T, E> carrier(@NotNull E carrier);

        public @NotNull Singleton<T> singleton();
    }
}
