package org.cobnet.mc.diversifier.plugin.support;

import org.cobnet.mc.diversifier.Diversifier;
import org.cobnet.mc.diversifier.plugin.annotation.Signal;
import org.cobnet.mc.diversifier.plugin.*;
import org.cobnet.mc.diversifier.utils.ProxyUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractManagedAnnotatedAssembly<T extends AnnotatedElement, K extends Assembly<?, ?>, V extends HierarchicalAssembly<?,?,?>> extends AbstractManagedHierarchicalAssembly<T, K, V> implements AnnotatedAssembly<T, K, V> {

    protected AbstractManagedAnnotatedAssembly(K parent, T instance) {
        super(parent, instance);
    }

    @Override
    public <T extends Annotation> T getAnnotation(@NotNull Class<T> annotationClass) {
        for(Annotation annotation : getAnnotations()) {
            if(annotation.annotationType().equals(annotationClass)) {
                return (T) annotation;
            }
        }
        return null;
    }

    @Override
    public Annotation[] getAnnotations() {
        TypeFactory factory = Diversifier.getTypeFactory();
        Set<Annotation> annotations = new HashSet<>();
        for(Annotation annotation : this.get().getAnnotations()) {
            if(annotation instanceof Signal signal) {
                System.out.println(ProxyUtils.createProxy(Signal.class).name());

            }
            annotations.add(annotation);
            TypeAssembly<?> type = factory.getTypeAssembly(annotation.annotationType());
            if(type == null) continue;
            Collections.addAll(annotations, type.getAnnotations());
        }
        return annotations.toArray(Annotation[]::new);
    }

    @Override
    public Annotation[] getDeclaredAnnotations() {
        TypeFactory factory = Diversifier.getTypeFactory();
        Set<Annotation> annotations = new HashSet<>();
        for(Annotation annotation : this.get().getDeclaredAnnotations()) {
            annotations.add(annotation);
            TypeAssembly<?> type = factory.getTypeAssembly(annotation.annotationType());
            if(type == null) continue;
            Collections.addAll(annotations, type.getDeclaredAnnotations());
        }
        return annotations.toArray(Annotation[]::new);
    }
}
