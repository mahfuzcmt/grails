package com.webcommander.plugin.product_custom_fields.domain

import grails.gorm.dirty.checking.DirtyCheck

/**
 * Created by zobair on 17/04/2014.*/
@DirtyCheck
abstract class CheckoutField {

    Long id

    String type //single select (radio), single select (drop down), multi select (checkbox), multi select (list box), text, long text
    String label
    String name
    String title
    String clazz
    String value
    String placeholder
    String validation

    Collection<String> options = []

    static hasMany = [options: String]

    static constraints = {
        validation nullable: true
        placeholder nullable: true
        value nullable: true
        clazz nullable: true
        title nullable: true
    }

    static mapping = {
        type length: 50
        validation length: 100
        placeholder length: 100
        value length: 500
        clazz length: 100
        title length: 100
        label length: 100
    }

}
