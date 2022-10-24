package org.cobnet.mc.diversifier.reference;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConfigurationProperty {

    public String name();

    public String description() default "";
}
