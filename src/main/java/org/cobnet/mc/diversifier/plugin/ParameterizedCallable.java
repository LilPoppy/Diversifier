package org.cobnet.mc.diversifier.plugin;

public interface ParameterizedCallable<T> {

    public T getInstance();

    public Object call(Object... args) throws Throwable;
}
