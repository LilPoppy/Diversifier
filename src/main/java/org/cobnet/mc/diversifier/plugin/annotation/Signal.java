package org.cobnet.mc.diversifier.plugin.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Signal {

    String name() default "";
}
