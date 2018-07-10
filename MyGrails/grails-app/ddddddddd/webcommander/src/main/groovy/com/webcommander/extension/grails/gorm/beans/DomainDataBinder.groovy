package com.webcommander.extension.grails.gorm.beans

import grails.core.GrailsApplication
import grails.web.databinding.GrailsWebDataBinder

/**
 * Created by LocalZobair on 26/10/2016.*/
class DomainDataBinder extends GrailsWebDataBinder {
    DomainDataBinder(GrailsApplication grailsApplication) {
        super(grailsApplication)
    }

    protected def getDefaultCollectionInstanceForType(Class type) {
        def val
        if (SortedSet.isAssignableFrom(type)) {
            val = new TreeSet()
        } else if (Collection.isAssignableFrom(type)) {
            val = []
        }
        val
    }
}