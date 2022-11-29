package org.cobnet.mc.diversifier.plugin.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cobnet.mc.diversifier.Diversifier;
import org.cobnet.mc.diversifier.plugin.*;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.*;
import java.util.stream.Stream;

public abstract class AbstractManagedAnnotatedAssembly<T extends AnnotatedElement, K extends Assembly<?, ?>, V extends HierarchicalAssembly<?,?,?>> extends AbstractManagedAssembly<T, K, V> implements AnnotatedAssembly<T, K, V> {

    private final static byte ANNOTATION = 0x0;

    private final static byte DECLARED_ANNOTATIONS = 0x01;

    protected transient Map<Byte, Annotation[]> annotations = new HashMap<>();

    protected AbstractManagedAnnotatedAssembly(K parent, T instance, List<V> children) {
        super(parent, instance, children);
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
        Annotation[] annotations;
        if((annotations = this.annotations.get(AbstractManagedAnnotatedAssembly.ANNOTATION)) != null) return annotations;
        annotations = traverse(this.instance.getAnnotations()).toArray(Annotation[]::new);
        this.annotations.put(AbstractManagedAnnotatedAssembly.ANNOTATION, annotations);
        return annotations;
    }

    private List<Annotation> traverse(Annotation[] annotations) {
        TypeFactory factory = Diversifier.getTypeFactory();
        Stream<Annotation> stream = Stream.empty();
        for(Annotation annotation : annotations) {
            final TypeAssembly<? extends Annotation> type = factory.getTypeAssembly(annotation.annotationType());
            if(type == null) {
                stream = Stream.concat(stream, Stream.of(annotation));
                continue;
            }
            stream = Stream.concat(stream, merge(type, annotation, Arrays.stream(type.getAnnotations()), annotations).filter(Objects::nonNull));
        }
        return stream.toList();
    }

    private Stream<Annotation> merge(TypeAssembly<?> type, Annotation parent, Stream<Annotation> annotations, Annotation[] existed) {
        final ProxyFactory factory = Diversifier.getProxyFactory();
        final ProxyContext<?> context = (ProxyContext<?>) factory.create(type);
        Map<?, ?> map = new ObjectMapper().convertValue(parent, Map.class);
        for(Map.Entry<?,?> entry : map.entrySet()) {
            System.out.println(entry.getKey() + " = " + entry.getValue());
        }
        return Stream.concat(Stream.of((Annotation) context.getInstance()), annotations.map(annotation -> {
            if (type.equals(annotation.annotationType())) return null;
            //for(proxy.getAssembly().)
            return Arrays.stream(existed).anyMatch(exists -> exists.annotationType().equals(annotation.annotationType())) ? null : annotation;
        }));
    }


    @Override
    public Annotation[] getDeclaredAnnotations() {
        Annotation[] annotations;
        if((annotations = this.annotations.get(AbstractManagedAnnotatedAssembly.DECLARED_ANNOTATIONS)) != null) return annotations;
        annotations = traverse(this.instance.getDeclaredAnnotations()).toArray(Annotation[]::new);
        this.annotations.put(AbstractManagedAnnotatedAssembly.DECLARED_ANNOTATIONS, annotations);
        return annotations;
    }
}
