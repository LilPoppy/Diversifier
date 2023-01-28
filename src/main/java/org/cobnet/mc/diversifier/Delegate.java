package org.cobnet.mc.diversifier;

import org.jetbrains.annotations.Nullable;

public interface Delegate<T> {

    public @Nullable T delegator();
}
