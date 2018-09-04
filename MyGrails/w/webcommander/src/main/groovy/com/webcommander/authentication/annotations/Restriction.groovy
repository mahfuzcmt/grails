package com.webcommander.authentication.annotations

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * Created by zobair on 21/07/2014.
 */
@Target([ElementType.METHOD])
@Retention(RetentionPolicy.RUNTIME)
@interface Restriction {
    String permission()
    String params_not_exist() default ""
    String params_exist() default ""
    String[] params_match_key() default []
    String[] params_match_value() default []
    String entity_param() default ""
    Class domain() default void.class
    Class condition() default void.class
    String owner_field() default ""
}

@Target([ElementType.METHOD])
@Retention(RetentionPolicy.RUNTIME)
@interface Restrictions {
    Restriction[] value()
}
