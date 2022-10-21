package org.cobnet.mc.diversifier.plugin.support;

import org.cobnet.mc.diversifier.plugin.AnnotationTypeAssembly;
import org.cobnet.mc.diversifier.plugin.PluginAssembly;
import org.cobnet.mc.diversifier.plugin.TypeAssembly;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;

//TODO: custom annotation extensions system
public class ManagedAnnotationTypeAssembly<T extends Annotation> extends ManagedTypeAssembly<T> implements AnnotationTypeAssembly<T> {

    protected ManagedAnnotationTypeAssembly(PluginAssembly<?> parent, Class<T> instance) {
        super(parent, instance);
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
        System.out.println("Checking if " + type + " is a subtype of " + this.get());
        for(var clazz : this.get().getAnnotations()) {
            System.out.println(clazz.annotationType());
        }
        return super.isSuperTypeOf(type);
    }

    @Override
    public boolean isAssignableFrom(@NotNull Class<?> type) {

        return super.isAssignableFrom(type);
    }
}
