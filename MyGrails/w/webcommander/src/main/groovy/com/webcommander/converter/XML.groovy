package com.webcommander.converter

import grails.util.Holders
import groovy.xml.StreamingMarkupBuilder
import grails.core.GrailsApplication
import grails.util.GrailsClassUtils
import grails.core.GrailsDomainClass
import org.hibernate.proxy.HibernateProxyHelper

import java.util.concurrent.ConcurrentHashMap

/**
 * Created by zobair on 04/02/2015.
 */
class XML {
    static Map default_list_entry_names = new ConcurrentHashMap([
        products: "product",
        categories: "category"
    ])

    private String converted;

    public XML() {
    }

    /**
     * to be used as as operator
     * @param map
     */
    public XML(Map map) {
        converted = XML.fromMap(map, [:])
    }

    public XML(Map map, Map config) {
        converted = XML.fromMap(map, [:], config)
    }

    public XML(Map map, Map config, Map option) {
        converted = XML.fromMap(map, option, config)
    }
    /**
     * @param p
     * @param options root, <key map name for list entries>
     * @return
     */
    public static fromMap(Map p, Map options, Map config = [:]) {
        StreamingMarkupBuilder builder = new StreamingMarkupBuilder()
        builder.encoding = "utf-8"
        Closure addTag
        builder.bind {
            addTag = { k, v, Map conf = [:], Map listEntryName = [:] ->
                if(conf == null) {
                    conf = [:]
                }
                def value;
                GrailsApplication grailsApp = Holders.grailsApplication;
                if(v instanceof Map) {
                    value = {
                        v.each {mk, mv ->
                            addTag mk, mv, conf
                        }
                    }
                } else if(v instanceof List) {
                    def tag = listEntryName[k] ?: default_list_entry_names[k] ?: k
                    value = {
                        v.eachWithIndex { def o, int i ->
                            addTag tag, o, conf
                        }
                    }
                } else if (v && grailsApp.isDomainClass(v.class)) {
                    value = {
                        List marshallerExclude = GrailsClassUtils.getStaticPropertyValue(v.class, "marshallerExclude") ?: [];
                        Map marshallerNameMapping = GrailsClassUtils.getStaticPropertyValue(v.class, "marshallerNameMapping") ?: [:];
                        Map marshallerListEntryNames = GrailsClassUtils.getStaticPropertyValue(v.class, "marshallerListEntryNames") ?: [:];
                        if(conf.marshallerExclude instanceof List) {
                            marshallerExclude = marshallerExclude + conf.marshallerExclude
                        }
                        if(conf.marshallerNameMapping instanceof Map) {
                            marshallerNameMapping = marshallerNameMapping + conf.marshallerNameMapping
                        }
                        String domainClassName = HibernateProxyHelper.getClassWithoutInitializingProxy(v).getCanonicalName();
                        GrailsDomainClass domainClass = Holders.grailsApplication.getDomainClass(domainClassName);
                        domainClass.properties.each {
                            def key = it.name;
                            if(!marshallerExclude.find { it == key }) {
                                String keyName = marshallerNameMapping[key] ?: key
                                def fieldValue = v[key];
                                GrailsDomainClass aClass = it.getReferencedDomainClass();
                                if(aClass && fieldValue instanceof Collection && conf[key]?.details) {
                                    fieldValue.each {
                                        addTag keyName, fieldValue, conf[key]
                                    }
                                } else if(fieldValue && aClass && conf[key]?.details) {
                                    addTag(keyName, fieldValue,  conf[key])
                                } else if(aClass && fieldValue && fieldValue instanceof Collection) {
                                    List defaultFields = conf[key]?.default ?: aClass.hasPersistentProperty("name") ? ["id", "name"] : ["id"];
                                    List defaultValues = fieldValue.collect { valueIterator ->
                                        Map defaultValue = [:]
                                        defaultFields.each {fieldName ->
                                            defaultValue[fieldName] = valueIterator[fieldName]
                                        }
                                        return defaultValue
                                    }
                                    addTag keyName, defaultValues, conf, marshallerListEntryNames
                                } else if(aClass && fieldValue) {
                                    List defaultFields = conf[key]?.default ?: aClass.hasPersistentProperty("name") ? ["id", "name"] : ["id"];
                                    Map defaultValue = [:]
                                    defaultFields.each {fieldName ->
                                        defaultValue[fieldName] = fieldValue[fieldName]
                                    }
                                    addTag keyName, defaultValue, conf
                                } else {
                                    addTag keyName, fieldValue, conf
                                }
                            }
                        }
                    }

                } else {
                    value = v ? v.toString().encodeAsPrintableUTF() : "";
                }
                "$k" value
            }
            mkp.xmlDeclaration()
            if(p.size() == 1) {
                p.each {k, v ->
                    addTag k, v, config
                }
            } else {
                def root = options.root ?: "root"
                addTag root, p, config
            }
        }.toString()
    }

    public static fromMap(Map p) {
        fromMap(p, [:])
    }

    @Override
    String toString() {
        return converted
    }
}
