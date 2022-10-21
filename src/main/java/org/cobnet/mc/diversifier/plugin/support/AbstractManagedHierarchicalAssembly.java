package org.cobnet.mc.diversifier.plugin.support;

import lombok.Getter;
import org.cobnet.mc.diversifier.plugin.Assembly;
import org.cobnet.mc.diversifier.plugin.HierarchicalAssembly;

public abstract class AbstractManagedHierarchicalAssembly<T, K extends Assembly<?, ?> ,V extends HierarchicalAssembly<?,?,?>> extends AbstractManagedAssembly<T, V> implements HierarchicalAssembly<T, K, V> {

    @Getter
    private final K parent;

    protected AbstractManagedHierarchicalAssembly(K parent, T instance) {
        super(instance);
        this.parent = parent;
    }
}
