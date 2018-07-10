package com.webcommander.annotations.event

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * Created by LocalZobair on 18/12/2016.*/
@Target([ElementType.TYPE])
@Retention(RetentionPolicy.RUNTIME)
@interface EventHandler {
}