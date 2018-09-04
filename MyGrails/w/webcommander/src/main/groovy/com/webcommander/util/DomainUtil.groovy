package com.webcommander.util

import grails.core.GrailsClass
import grails.core.GrailsDomainClassProperty
import grails.util.Holders
import org.grails.core.DefaultGrailsDomainClassProperty
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.hibernate.proxy.HibernateProxyHelper

class DomainUtil {
    public static Object clone(domainInstanceToClone, List exclude = [], List reference = []) {
        if (domainInstanceToClone == null) {
            return null;
        }
        domainInstanceToClone = GrailsHibernateUtil.unwrapIfProxy(domainInstanceToClone)
        GrailsClass domainClass = Holders.grailsApplication.getDomainClass(domainInstanceToClone.class.name)
        def newDomainInstance = domainClass.newInstance()
        List notCloneable = domainClass.getPropertyValue("clone_exclude")
        if(notCloneable) {
            exclude.addAll(notCloneable)
        }
        exclude.addAll(["class", "metaClass", "properties", "id", "created", "updated"])
        List copyReferences = domainClass.getPropertyValue("copy_reference")
        if(copyReferences) {
            reference.addAll(copyReferences)
        }
        for(DefaultGrailsDomainClassProperty prop in domainClass.getPersistentProperties()) {
            if (prop.name in exclude)
                continue

            def oldObj = domainInstanceToClone."${prop.name}"
            if (prop.association) {
                boolean referenceCopy = reference.contains(prop.name);
                if (prop.owningSide) {
                    if (prop.oneToOne) {
                        newDomainInstance."${prop.name}" = referenceCopy ? oldObj : clone(oldObj)
                    } else {
                        oldObj.each { associationInstance ->
                            def newAssociationInstance = referenceCopy ? associationInstance : clone(associationInstance)
                            if (newAssociationInstance) {
                                newDomainInstance."addTo${prop.name.capitalize()}"(newAssociationInstance)
                            }
                        }
                    }
                } else {
                    if (prop.manyToOne || prop.oneToOne) {
                        newDomainInstance."${prop.name}" = referenceCopy ? oldObj : clone(oldObj)
                    } else if (prop.oneToMany) {
                        oldObj.each { associationInstance ->
                            def newAssociationInstance = referenceCopy ? associationInstance : clone(associationInstance)
                            newDomainInstance."addTo${prop.name.capitalize()}"(newAssociationInstance)
                        }
                    } else {
                        /*String fieldName = domainClass.getPropertyValue("non_owning_reference")[prop.name]
                        oldObj.each {
                            it[fieldName].add(newDomainInstance)
                        }*/
                    }
                }
            } else {
                if(oldObj instanceof Collection) {
                    def list = newDomainInstance."${prop.name}" = oldObj instanceof List ? [] : [] as Set
                    oldObj.each {
                        list.add(it)
                    }
                } else {
                    newDomainInstance."${prop.name}" = oldObj
                }
            }
        }
        return newDomainInstance
    }

    public static Map getObjectAsMap(Object domainInst) {
        Map<String, Object> objectAsMap = new HashMap<String, Object>()
        String domainClassName = HibernateProxyHelper.getClassWithoutInitializingProxy(domainInst).getCanonicalName()
        GrailsClass domainClass = Holders.grailsApplication.getDomainClass(domainClassName)
        domainClass.persistentProperties.each {
            def key = it.name
            def value = domainInst[key]
            if (domainInst["id"]){
                objectAsMap["id"] = domainInst["id"]
            }
            objectAsMap[key] = value
        }
        return objectAsMap
    }

    public static Map toMap(Object domainInst, Map config = [:]) {
        Map<String, Object> objectAsMap = new HashMap<String, Object>();
        List exclude = ["created", "updated", "version"] + config.exclude ?: []
        String domainClassName = HibernateProxyHelper.getClassWithoutInitializingProxy(domainInst).getCanonicalName()
        GrailsClass domainClass = Holders.grailsApplication.getDomainClass(domainClassName)
        domainClass.persistentProperties.each {
            def key = it.name
            def value = domainInst[key]
            if (domainInst["id"]){
                objectAsMap["id"] = domainInst["id"]
            }
            if ((!exclude.find { it == key })) {
                GrailsClass refClass = it.getReferencedDomainClass()
                if (value && refClass && value instanceof Collection){
                    String defaultField = config[key]?.default ?:  "id"
                    List defaultValues = value.collect { valueIterator ->
                        return valueIterator[defaultField]
                    }
                    objectAsMap[key] = defaultValues
                } else if(refClass && value) {
                    String defaultField = config[key]?.default ?: "id";
                    objectAsMap[key] = value[defaultField]
                } else {
                    objectAsMap[key] = value
                }
            }
        }
        return objectAsMap;
    }

    public static void populateDomainInst(Object object, Map properties, Map configs = [:]) {
        List exclude = configs.exclude ? configs.exclude + ["id"] : ["id"];
        GrailsClass domainClass = Holders.grailsApplication.getDomainClass(object.class.canonicalName);
        properties.each { fieldName, fieldValue ->
            if (domainClass.hasProperty(fieldName) && (!exclude || !exclude.find { it == fieldName })) {
                GrailsDomainClassProperty classProperty = domainClass.getPersistentProperty(fieldName);
                GrailsClass refDomainClass = classProperty.referencedDomainClass
                if (refDomainClass && (classProperty.oneToMany || classProperty.manyToMany)) {
                    Class refClass = refDomainClass.clazz
                    List values = []
                    if(!fieldValue instanceof List && fieldValue) {
                        values.add(fieldValue)
                    } else if(fieldValue instanceof List || fieldValue instanceof Object[]){
                        values = fieldValue
                    }
                    values.each {
                        def refObject = refClass.get(it);
                        if(refObject) object."addTo${fieldName.capitalize()}"(refObject)
                    }
                } else if(refDomainClass) {
                    Class refClass = refDomainClass.clazz
                    object."${fieldName}" = fieldValue ? refClass.get(fieldValue) : null;
                } else {
                    object."${fieldName}" = fieldValue == null ? null : fieldValue
                }
            }
        }
    }
}