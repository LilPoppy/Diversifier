package org.cobnet.mc.diversifier;

import java.util.Locale;

public interface MessageSource {

    public String getMessage(String key, Object... args);

    public String getMessage(String key, Locale locale, Object... args);

    public String getMessageOrDefault(String key, Locale locale, Object... args);
}
