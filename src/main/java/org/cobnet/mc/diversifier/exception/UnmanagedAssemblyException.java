package org.cobnet.mc.diversifier.exception;

public class UnmanagedAssemblyException extends MissingResourceException{

    public UnmanagedAssemblyException(String message) {
        super(message);
    }

    public UnmanagedAssemblyException(Object resource) {
        super(String.format("The resource of '%s' is not managed by the plugin.", resource));
    }
}
