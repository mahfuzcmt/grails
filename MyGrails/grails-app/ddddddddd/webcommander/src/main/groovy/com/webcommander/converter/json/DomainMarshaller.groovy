package com.webcommander.converter.json

import grails.core.GrailsClass
import grails.util.GrailsClassUtils
import grails.util.Holders
import org.grails.core.artefact.DomainClassArtefactHandler
import org.hibernate.proxy.HibernateProxyHelper

/**
 * Created by sajedur on 18-02-2015.
 */
class DomainMarshaller {

    public static void writeJSONString(Object ob, Writer out, Map config = [:]) {
        if(config == null) {
            config = [:]
        }
        List marshallerExclude = GrailsClassUtils.getStaticPropertyValue(ob.class, "marshallerExclude") ?: [];
        List marshallerInclude = GrailsClassUtils.getStaticPropertyValue(ob.class, "marshallerInclude") ?: [];
        Map marshallerNameMapping = GrailsClassUtils.getStaticPropertyValue(ob.class, "marshallerNameMapping") ?: [:];
        Map fieldMarshaller = GrailsClassUtils.getStaticPropertyValue(ob.class, "fieldMarshaller") ?: [:];
        if(config.marshallerExclude instanceof List) {
            marshallerExclude = marshallerExclude + config.marshallerExclude
        }
        if(config.marshallerInclude instanceof List) {
            marshallerInclude = marshallerInclude + config.marshallerInclude
        }
        if(config.marshallerNameMapping instanceof Map) {
            marshallerNameMapping = marshallerNameMapping + config.marshallerNameMapping
        }
        boolean first = true;
        String domainClassName = HibernateProxyHelper.getClassWithoutInitializingProxy(ob).getCanonicalName();
        GrailsClass domainClass = Holders.grailsApplication.getArtefact(DomainClassArtefactHandler.TYPE, domainClassName)
        out.write('{');
        domainClass.propertyMap.each {
            def key = it.key
            def prop = it.value
            String keyName = marshallerNameMapping[key] ?: key
            def value = ob[key];
            if(!marshallerExclude.find { it == key }) {
                if(first) {
                    first = false;
                } else {
                    out.write(',');
                }
                out.write('\"');
                out.write(keyName);
                out.write('\"');
                out.write(':');
                GrailsClass aClass = prop.getReferencedDomainClass()
                if(value && aClass && config[key]?.details) {
                    writeJSONString(value, out, config[key])
                } else if(fieldMarshaller[key]) {
                    def retrieved = fieldMarshaller[key].call(ob)
                    JSON.writeJSONString(retrieved, out)
                } else if (value && aClass && value instanceof Collection){
                    List defaultFields = config[key]?.default ?: aClass.hasPersistentProperty("name") ? ["id", "name"] : ["id"];
                    List defaultValues = value.collect { valueIterator ->
                        Map defaultValue = [:]
                        defaultFields.each {fieldName ->
                            defaultValue[fieldName] = valueIterator[fieldName]
                        }
                        return defaultValue
                    }
                    JSON.writeJSONString(defaultValues, out)
                } else if(aClass && value) {
                    List defaultFields = config[key]?.default ?: aClass.hasPersistentProperty("name") ? ["id", "name"] : ["id"];
                    Map defaultValue = [:]
                    defaultFields.each {fieldName ->
                        defaultValue[fieldName] = value[fieldName]
                    }
                    JSON.writeJSONString(defaultValue, out)
                } else {
                    JSON.writeJSONString(value, out)
                }
            }
        }
        marshallerInclude.each { key ->
            if(fieldMarshaller[key]) {
                out.write(',');
                out.write('\"');
                out.write(key);
                out.write('\"');
                out.write(':');
                def retrieved = fieldMarshaller[key].call(ob)
                JSON.writeJSONString(retrieved, out, config[key])
            }
        }
        out.write('}');
    }

    public static void writeJSONString(Collection value, Writer writer, Map config = [:]) {
        ListMarshaller.writeJSONString(value, writer, config)
    }
}

