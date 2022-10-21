package org.cobnet.mc.diversifier.exception;

public class MissingResourceException extends RuntimeException {

    public MissingResourceException(String message) {
        super(message);
    }

    public MissingResourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public MissingResourceException(Throwable cause) {
        super(cause);
    }

    public MissingResourceException(String packageName, String name) {
        this(String.format("Resource '%s' in package '%s' not found.", name, packageName));
    }

    public MissingResourceException(String packageName, String name, Throwable cause) {
        this(String.format("Resource '%s' in package '%s' not found.", name, packageName), cause);
    }
}
