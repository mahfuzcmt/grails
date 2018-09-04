package com.webcommander.authentication.annotations

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * Created by zobair on 04/02/2015.
 */
@Target([ElementType.METHOD])
@Retention(RetentionPolicy.RUNTIME)
@interface License {
    String required()
    Class checker() default void.class;
}