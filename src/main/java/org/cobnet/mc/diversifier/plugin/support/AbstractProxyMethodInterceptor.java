package org.cobnet.mc.diversifier.plugin.support;

import org.cobnet.mc.diversifier.plugin.ProxyMethodInterceptor;

public abstract class AbstractProxyMethodInterceptor implements ProxyMethodInterceptor {

    protected volatile ProxyMethodInterceptor next;

    protected AbstractProxyMethodInterceptor() {}

    protected AbstractProxyMethodInterceptor(ProxyMethodInterceptor next) {
        this.next = next;
    }

    @Override
    public boolean hasNext() {
        return this.next != null;
    }

    @Override
    public ProxyMethodInterceptor next() {
        return this.next;
    }

}
