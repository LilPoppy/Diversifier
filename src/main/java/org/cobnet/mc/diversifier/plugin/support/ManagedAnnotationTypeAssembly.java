package org.cobnet.mc.diversifier.plugin.support;

import org.cobnet.mc.diversifier.plugin.*;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.List;

//TODO: custom annotation extensions system
public final class ManagedAnnotationTypeAssembly<T extends Annotation> extends ManagedTypeAssembly<T> implements AnnotationTypeAssembly<T>, ProxyTypeAssemblyGenerator<T> {

    ManagedAnnotationTypeAssembly(PluginAssembly<?> parent, Class<T> instance, List<?> members) {
        super(parent, instance, (List<MemberAssembly<T, ?>>) members);
    }

    @Override
    public TypeAssembly<? extends T>[] getSubTypes() {
        return super.getSubTypes();
    }

    @Override
    public boolean isSubTypeOf(@NotNull TypeAssembly<?> type) {
        return super.isSubTypeOf(type);
    }

    @Override
    public @NotNull TypeAssembly<? super T>[] getSuperTypes() {
        return super.getSuperTypes();
    }

    @Override
    public boolean isSubTypeOf(@NotNull Class<?> type) {
        return super.isSubTypeOf(type);
    }

    @Override
    public boolean isSuperTypeOf(@NotNull Class<?> type) {
        if(!type.isAnnotation()) return false;
        System.out.println("Checking if " + type + " is a subtype of " + this.instance);
        for(var clazz : this.instance.getAnnotations()) {
            System.out.println(clazz.annotationType());
        }
        return super.isSuperTypeOf(type);
    }

    @Override
    public boolean isAssignableFrom(@NotNull Class<?> type) {

        return super.isAssignableFrom(type);
    }

    @Override
    public boolean isAnnotation() {
        return true;
    }

    @Override
    public @NotNull <E extends T> ProxyTypeAssembly<E> generate(@NotNull PluginAssembly<?> plugin, @NotNull Class<E> type, @NotNull List<MemberAssembly<E, ?>> members) {
        return new DynamicProxyAnnotationTypeAssembly<>(plugin, this, type, members);
    }

    @Override
    public @NotNull <E extends T> ProxyTypeAssembly<E> generate(@NotNull Class<E> type, @NotNull List<MemberAssembly<E, ?>> members) {
        return generate(this.getPluginAssembly(), type, members);
    }
}
