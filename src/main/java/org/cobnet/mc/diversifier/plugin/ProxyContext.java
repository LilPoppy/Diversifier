package org.cobnet.mc.diversifier.plugin;

public interface ProxyContext<T> {

    public String getName();

    public default T getProxy() {
        return (T) this;
    }

    public Scope getScope();

    public TypeAssembly<T> getAssembly();

    public ProxyMethodInterceptor getChain();

    public ProxyContext<T> addMethod(MethodAssembly<?> method, Object value);
}
