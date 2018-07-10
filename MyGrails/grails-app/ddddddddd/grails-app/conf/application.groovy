grails {
    gorm {
        failOnError = true
        'default' {
            mapping = {
                autoTimestamp false
                cache true
                version false
                autowire true
                def builder = delegate
                Class entity = Class.forName("grails.util.Holders").grailsApplication.domainClasses.find {it.clazz.name == builder.className}.clazz
                def manyProps = Class.forName("org.grails.datastore.mapping.reflect.ClassPropertyFetcher").getStaticPropertyValuesFromInheritanceHierarchy(entity, "hasMany", java.lang.Object)
                def fetcher = Class.forName("org.grails.datastore.mapping.reflect.ClassPropertyFetcher").newInstance([entity] as Object[])
                List manyPropNames = manyProps*.keySet().flatten()
                List collectionProps = fetcher.getPropertiesAssignableToType(Collection).findAll {it.writeMethodName != null}.name
                manyPropNames.addAll(collectionProps)
                collectionProps.each {
                    "${it}" nullable: true
                }
                manyPropNames.unique().each {
                    "${it}" cache: true
                }
            }
        }
    }
}