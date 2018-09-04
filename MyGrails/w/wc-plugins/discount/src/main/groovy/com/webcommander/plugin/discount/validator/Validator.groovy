package com.webcommander.plugin.discount.validator

/**
 * Created by sharif ul islam on 14/03/2018.
 */
interface Validator {

    Map<String, Object> validate(Map<String, Object> context);

}