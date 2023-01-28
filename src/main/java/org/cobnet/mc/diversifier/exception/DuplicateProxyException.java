package org.cobnet.mc.diversifier.exception;

import org.cobnet.mc.diversifier.plugin.ProxyContext;

public class DuplicateProxyException extends ProxyException {

    public DuplicateProxyException(ProxyContext<?> proxy) {
        super("Duplicate proxy instance named: '" + proxy.getName() + "' of " + proxy.getInstance() + " is already in factory.");
    }
}
