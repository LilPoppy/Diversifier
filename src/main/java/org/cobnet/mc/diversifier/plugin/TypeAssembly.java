package org.cobnet.mc.diversifier.plugin;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public interface TypeAssembly<T> extends AnnotatedAssembly<Class<T>, PluginAssembly<?>, MemberAssembly<T, ?>>, Comparable<Class<?>> {

    public PluginAssembly<?> getPluginAssembly();

    public @NotNull TypeAssembly<? extends T>[] getSubTypes();

    public boolean isSubTypeOf(@NotNull TypeAssembly<?> type);

    public boolean isSubTypeOf(@NotNull Class<?> type);

    public @NotNull TypeAssembly<? super T>[] getSuperTypes();

    public boolean isSuperTypeOf(@NotNull TypeAssembly<?> type);

    public boolean isSuperTypeOf(@NotNull Class<?> type);

    public boolean isAssignableFrom(@NotNull TypeAssembly<?> type);

    public boolean isAssignableFrom(@NotNull Class<?> type);

    public boolean isAnnotation();

    public @NotNull T create(Object... args) throws Throwable;

    public default @NotNull Stream<MemberAssembly<T, ?>> getMembersAsStream(@NotNull String name) {
        return this.getChildrenAsStream(name);
    }

    public default @NotNull Stream<MethodAssembly<T>> getMethodsAsStream(@NotNull String name) {
        return this.getMembersAsStream(name).filter(MethodAssembly.class::isInstance).map(member -> (MethodAssembly<T>) member);
    }

    public default @Nullable MethodAssembly<T> getMethod(@NotNull String name, @NotNull Class<?>... parameterTypes) {
        return this.getMethodsAsStream(name).filter(method -> {
            Class<?>[] types = method.getParameterTypes();
            if(types.length != parameterTypes.length) return false;
            for(int i = 0; i < types.length; i++) {
                if(!types[i].isAssignableFrom(parameterTypes[i])) return false;
            }
            return true;
        }).findFirst().orElse(null);
    }

    public default @Nullable FieldAssembly<T> getField(@NotNull String name) {
        return this.getMembersAsStream(name).filter(FieldAssembly.class::isInstance).map(member -> (FieldAssembly<T>) member).findFirst().orElse(null);
    }

    public default @NotNull Stream<ConstructorAssembly<T>> getConstructorsAsStream() {
        return this.getMembersAsStream(this.getName()).filter(ConstructorAssembly.class::isInstance).map(member -> (ConstructorAssembly<T>) member);
    }

    public default @Nullable ConstructorAssembly<T> getConstructor(@NotNull Class<?>... parameterTypes) {
        return this.getConstructorsAsStream().filter(constructor -> {
            Class<?>[] types = constructor.getParameterTypes();
            if(types.length != parameterTypes.length) return false;
            for(int i = 0; i < types.length; i++) {
                if(!types[i].isAssignableFrom(parameterTypes[i])) return false;
            }
            return true;
        }).findFirst().orElse(null);
    }

    public ClassLoader getClassLoader();

    public default int compareTo(@NotNull TypeAssembly<?> o) {
        return Integer.compare(this.getName().hashCode(), o.getName().hashCode());
    }
}
