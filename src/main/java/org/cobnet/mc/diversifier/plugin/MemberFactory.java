package org.cobnet.mc.diversifier.plugin;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.util.stream.Stream;

public interface MemberFactory {

    public @NotNull TypeFactory getTypeFactory();

    public @NotNull Stream<MemberAssembly<?, ?>> getMembersAsStream();

    public @NotNull MemberAssembly<?, ?>[] getMembers(@NotNull String name);

    public <T> @NotNull MemberAssembly<T, ?>[] getMembers(@NotNull Class<T> type, @NotNull String name);

    public <T> @Nullable ConstructorAssembly<T> getConstructor(@NotNull Class<T> type, Class<?>... parameterTypes);

    public <T> @NotNull Stream<ConstructorAssembly<T>> getConstructorsAsStream(@NotNull Class<T> type);

    public <T> @Nullable MethodAssembly<T> getMethod(@NotNull Class<T> type, @NotNull String name, Class<?>... parameterTypes);

    public @NotNull Stream<MethodAssembly<?>> getMethodsAsStream(@NotNull String name);

    public <T> @Nullable FieldAssembly<T> getField(@NotNull Class<T> type, @NotNull String name);

    public @NotNull Stream<FieldAssembly<?>> getFieldsAsStream(@NotNull String name);

    public <T extends Member & AnnotatedElement> @Nullable MemberAssembly<T, ?> getMemberAssembly(@NotNull T member);

    public <T, E extends Member & AnnotatedElement> @NotNull MemberAssembly<T, E> loadMember(@NotNull TypeAssembly<T> type, @NotNull E member);

    public int getSize();
}
