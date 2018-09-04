package com.webcommander.authentication.annotations

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * To check whether a controller or action requires customer authentication or not.
 */
@Target([ElementType.METHOD, ElementType.TYPE])
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresCustomer {
    /**
     * For controller level use it will make the actions defined in exceptions list free from authentication check
     */
    public String[] exceptions() default [];
}
