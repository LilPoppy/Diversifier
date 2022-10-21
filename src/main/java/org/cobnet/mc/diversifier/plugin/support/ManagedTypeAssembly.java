package org.cobnet.mc.diversifier.plugin.support;

import org.cobnet.mc.diversifier.Diversifier;
import org.cobnet.mc.diversifier.exception.MissingResourceException;
import org.cobnet.mc.diversifier.plugin.MemberAssembly;
import org.cobnet.mc.diversifier.plugin.PluginAssembly;
import org.cobnet.mc.diversifier.plugin.TypeAssembly;
import org.cobnet.mc.diversifier.plugin.TypeFactory;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ManagedTypeAssembly<T> extends AbstractManagedAnnotatedAssembly<Class<T>, PluginAssembly<?>, MemberAssembly<?, ?>> implements TypeAssembly<T> {

    protected transient TypeAssembly<? extends T>[] subTypes;

    transient int modSize;

    protected ManagedTypeAssembly(PluginAssembly<?> parent, Class<T> instance) {
        super(parent, instance);
    }

    @Override
    public PluginAssembly<?> getPluginAssembly() {
        return this.getParent();
    }

    @Override
    public TypeAssembly<? extends T>[] getSubTypes() {
        TypeFactory factory = Diversifier.getTypeFactory();
        int size = factory.getSize();
        if(this.subTypes == null || this.modSize != size) {
            this.modSize = size;
            this.subTypes = factory.getTypesAsStream().filter(type -> this.isSuperTypeOf(type)).toArray(TypeAssembly[]::new);
        }
        TypeAssembly<? extends T>[] types = new TypeAssembly[this.subTypes.length];
        System.arraycopy(this.subTypes, 0, types, 0, this.subTypes.length);
        return types;
    }

    @Override
    public boolean isSubTypeOf(@NotNull Class<?> type) {
        return type.isAssignableFrom(this.get()) && type != this.get();
    }

    private Set<Class<?>> traverse_superclasses(Class<?> type) {
        Set<Class<?>> types = new HashSet<>();
        Class<?> superclass = type.getSuperclass();
        if(superclass != null) {
            types.add(superclass);
            types.addAll(this.traverse_superclasses(superclass));
        }
        for(Class<?> clazz : type.getInterfaces()) {
            types.add(clazz);
            types.addAll(this.traverse_superclasses(clazz));
        }
        return types;
    }
    @Override
    public @NotNull TypeAssembly<? super T>[] getSuperTypes() {
        TypeFactory factory = Diversifier.getTypeFactory();
        Set<Class<?>> types = traverse_superclasses(this.get());
        int n = types.size(), i = 0;
        TypeAssembly<? super T>[] superTypes = new TypeAssembly[n];
        Iterator<Class<?>> it = types.iterator();
        while(it.hasNext()) {
            Class<? super T> type = (Class<? super T>) it.next();
            if(type == null) continue;
            try {
                TypeAssembly<? super T> assembly = factory.getTypeAssembly(type);
                if(assembly == null) continue;
                superTypes[i++] = assembly;
            } catch (MissingResourceException e) {}
        }
        TypeAssembly<? super T>[] ret = new TypeAssembly[i];
        System.arraycopy(superTypes, 0, ret, 0, i);
        return ret;
    }

    @Override
    public boolean isSuperTypeOf(@NotNull Class<?> type) {
        return this.get().isAssignableFrom(type) && type != this.get();
    }

    @Override
    public boolean isAssignableFrom(@NotNull Class<?> type) {
        return this.get().isAssignableFrom(type);
    }

    @Override
    public @NotNull String getName() {
        return this.get().getName();
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof Class<?> type) return this.get().equals(type);
        return super.equals(o);
    }
}
