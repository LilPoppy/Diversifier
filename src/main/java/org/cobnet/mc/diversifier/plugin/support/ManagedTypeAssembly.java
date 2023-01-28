package org.cobnet.mc.diversifier.plugin.support;

import org.cobnet.mc.diversifier.Diversifier;
import org.cobnet.mc.diversifier.exception.MissingResourceException;
import org.cobnet.mc.diversifier.plugin.*;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

sealed class ManagedTypeAssembly<T> extends AbstractManagedAnnotatedAssembly<Class<T>, PluginAssembly<?>, MemberAssembly<T, ?>> implements TypeAssembly<T>, ProxyTypeAssemblyGenerator<T> permits DynamicProxyTypeAssembly, ManagedAnnotationTypeAssembly {

    protected transient TypeAssembly<? extends T>[] subTypes;

    transient int modSize;

    protected ManagedTypeAssembly(PluginAssembly<?> parent, Class<T> type, List<MemberAssembly<T, ?>> members) {
        super(parent, type, members);
    }

    @Override
    public String getSimpleName() {
        return this.instance.getSimpleName();
    }

    @Override
    public String getPackageName() {
        return this.instance.getPackageName();
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
    public boolean isSubTypeOf(@NotNull TypeAssembly<?> type) {
        return type.isSuperTypeOf(this.instance);
    }

    @Override
    public boolean isSubTypeOf(@NotNull Class<?> type) {
        return type.isAssignableFrom(this.instance) && type != this.instance;
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
        Set<Class<?>> types = traverse_superclasses(this.instance);
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
            } catch (MissingResourceException ignored) {}
        }
        TypeAssembly<? super T>[] ret = new TypeAssembly[i];
        System.arraycopy(superTypes, 0, ret, 0, i);
        return ret;
    }

    @Override
    public boolean isSuperTypeOf(@NotNull TypeAssembly<?> type) {
        return type.isSubTypeOf(this.instance);
    }

    @Override
    public boolean isSuperTypeOf(@NotNull Class<?> type) {
        return this.instance.isAssignableFrom(type) && type != this.instance;
    }

    @Override
    public boolean isAssignableFrom(@NotNull TypeAssembly<?> type) {
        return false;
    }

    @Override
    public boolean isAssignableFrom(@NotNull Class<?> type) {
        return this.instance.isAssignableFrom(type);
    }

    @Override
    public boolean isAnnotation() {
        return false;
    }

    @Override
    public @NotNull T create(Object... args) throws Throwable {
        //TODO: 需要实现当参数有为null的时候自动匹配
        Class<?>[] types = Arrays.stream(args).map(Object::getClass).toArray(Class[]::new);
        ConstructorAssembly<T> constructor = ManagedTypeAssembly.this.getConstructor(types);
        if(constructor == null) throw new NoSuchMethodException("No constructor found for arguments: " + Arrays.toString(types));
        return constructor.newInstance(args);
    }

    @Override
    public ClassLoader getClassLoader() {
        return this.instance.getClassLoader();
    }

    @Override
    public @NotNull String getName() {
        return this.instance.getName();
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof Class<?> type) return this.instance.equals(type);
        if(o instanceof TypeAssembly<?>) return super.equals(o);
        return false;
    }

    @Override
    public @NotNull <E extends T> ProxyTypeAssembly<E> generate(@NotNull PluginAssembly<?> plugin, @NotNull Class<E> type, @NotNull List<MemberAssembly<E, ?>> members) {
        return new DynamicProxyTypeAssembly<>(plugin, this, type, members);
    }

    @Override
    public @NotNull <E extends T> ProxyTypeAssembly<E> generate(@NotNull Class<E> type, @NotNull List<MemberAssembly<E, ?>> members) {
        return generate(this.getPluginAssembly(), type, members);
    }

    @Override
    public int compareTo(@NotNull Class<?> o) {
        return Integer.compare(this.instance.getName().hashCode(), o.getName().hashCode());
    }
}
