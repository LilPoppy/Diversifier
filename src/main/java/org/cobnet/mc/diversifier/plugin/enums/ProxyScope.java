package org.cobnet.mc.diversifier.plugin.enums;

import lombok.Getter;
import org.cobnet.mc.diversifier.plugin.Scope;

@Getter
public enum ProxyScope implements Scope {

    SINGLETON("SINGLETON"),
    PROTOTYPE("PROTOTYPE");

    private final String name;

    ProxyScope(String name) {
        this.name = name;
    }

    @Override
    public int compareTo(Scope o) {
        return Integer.compare(name.hashCode(), o.getName().hashCode());
    }
}
