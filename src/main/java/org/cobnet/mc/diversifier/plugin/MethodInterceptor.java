package org.cobnet.mc.diversifier.plugin;

import net.bytebuddy.implementation.bind.annotation.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;


public interface MethodInterceptor {

    @RuntimeType
    public @Nullable Object intercept(@This @NotNull Object instance, @Origin @NotNull Method method, @AllArguments @NotNull Object[] args) throws Throwable;

    @RuntimeType
    public @Nullable Object intercept(@This @NotNull Object instance, @Origin @NotNull Method method, @AllArguments @NotNull Object[] args, @SuperCall @NotNull ParameterizedCallable<?> callable) throws Throwable;

}
