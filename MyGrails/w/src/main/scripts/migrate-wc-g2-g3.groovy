description( "WebCommander Grails 2 to Grails 3 Migration." ) {
    usage "grails migrate-wc-g2-g3"
}


int i = 0
new File("wc-plugins/blog").traverse() { file ->
    file.name.lastIndexOf(".").with {
        String extension = file.name[it..(file.name.size() - 1)]
        if (extension.equals(".groovy") || extension.equals(".gsp") || extension.equals(".java")){
            def oldContent = file.text
            def content = oldContent
            println(file.name)
            content = content.replaceAll("org.codehaus.groovy.grails.web.binding.DataBindingUtils","grails.web.databinding.DataBindingUtils")
            content = content.replaceAll("org.codehaus.groovy.grails.web.pages.FastStringWriter","org.grails.buffer.FastStringWriter")
            content = content.replaceAll("org.codehaus.groovy.grails.web.util.TypeConvertingMap","grails.util.TypeConvertingMap")
            content = content.replaceAll("org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib","org.grails.plugins.web.taglib.ApplicationTagLib")
            content = content.replaceAll("org.codehaus.groovy.grails.plugins.web.taglib.RenderTagLib","org.grails.plugins.web.taglib.RenderTagLib")
            content = content.replaceAll("org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap","grails.web.servlet.mvc.GrailsParameterMap")
            content = content.replaceAll("org.codehaus.groovy.grails.web.util.WebUtils","org.grails.plugins.web.taglib.ApplicationTagLib")
            content = content.replaceAll("org.codehaus.groovy.grails.web.servlet.mvc.GrailsHttpSession","grails.web.servlet.mvc.GrailsHttpSession")
            content = content.replaceAll("org.codehaus.groovy.grails.web.pages.discovery.GrailsConventionGroovyPageLocator","org.grails.web.gsp.io.GrailsConventionGroovyPageLocator")
            content = content.replaceAll("org.codehaus.groovy.grails.web.util.StreamCharBuffer","org.grails.buffer.StreamCharBuffer")
            content = content.replaceAll("org.codehaus.groovy.grails.web.pages.GroovyPagesTemplateEngine","org.grails.gsp.GroovyPagesTemplateEngine")
            content = content.replaceAll("org.codehaus.groovy.grails.web.pages.TagLibraryLookup","org.grails.taglib.TagLibraryLookup")
            content = content.replaceAll("org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass","org.grails.core.DefaultGrailsDomainClass")
            content = content.replaceAll("org.codehaus.groovy.grails.commons.GrailsDomainClassProperty","grails.core.GrailsDomainClassProperty")
            content = content.replaceAll("org.codehaus.groovy.grails.commons.GrailsApplication","grails.core.GrailsApplication")
            content = content.replaceAll("org.codehaus.groovy.grails.commons.GrailsClassUtils","grails.util.GrailsClassUtils")
            content = content.replaceAll("org.codehaus.groovy.grails.commons.GrailsDomainClass","grails.core.GrailsDomainClass")
            content = content.replaceAll("org.codehaus.groovy.grails.commons.GrailsClass","grails.core.GrailsClass")
            content = content.replaceAll("org.codehaus.groovy.grails.commons.GrailsUrlMappingsClass","grails.core.GrailsUrlMappingsClass")
            content = content.replaceAll("org.codehaus.groovy.grails.commons.UrlMappingsArtefactHandler","org.grails.core.artefact.UrlMappingsArtefactHandler")
            content = content.replaceAll("org.codehaus.groovy.grails.plugins.GrailsPluginManager","grails.plugins.GrailsPluginManager")
            content = content.replaceAll("org.codehaus.groovy.grails.web.mapping.DefaultUrlMappingEvaluator","org.grails.web.mapping.DefaultUrlMappingEvaluator")
            content = content.replaceAll("org.codehaus.groovy.grails.web.mapping.DefaultUrlMappings","org.grails.web.mapping.DefaultUrlMappings")
            content = content.replaceAll("org.codehaus.groovy.grails.web.mapping.UrlMappingsHolderFactoryBean","org.grails.web.mapping.UrlMappingsHolderFactoryBean")
            content = content.replaceAll("org.codehaus.groovy.grails.web.mapping.UrlMappings","grails.web.mapping.UrlMappings")
            content = content.replaceAll("org.codehaus.groovy.grails.web.mapping.UrlMapping","grails.web.mapping.UrlMapping")
            content = content.replaceAll("org.codehaus.groovy.grails.orm.hibernate.query.AbstractHibernateQuery","org.grails.orm.hibernate.query.AbstractHibernateQuery")
            content = content.replaceAll("org.codehaus.groovy.grails.orm.hibernate.query.HibernateCriterionAdapter","org.grails.orm.hibernate.query.HibernateCriterionAdapter")
            content = content.replaceAll("org.codehaus.groovy.grails.orm.hibernate.query.HibernateProjectionAdapter","org.grails.orm.hibernate.query.HibernateProjectionAdapter")
            content = content.replaceAll("org.codehaus.groovy.grails.orm.hibernate.query.HibernateQuery","org.grails.orm.hibernate.query.HibernateQuery")
            content = content.replaceAll("org.codehaus.groovy.grails.commons.DomainClassArtefactHandler","org.grails.core.artefact.DomainClassArtefactHandler")
            content = content.replaceAll("org.codehaus.groovy.grails.domain.GrailsDomainClassMappingContext","org.grails.datastore.gorm.config.GrailsDomainClassMappingContext")
            content = content.replaceAll("org.codehaus.groovy.grails.orm.hibernate.cfg.Mapping","org.grails.orm.hibernate.cfg.Mapping")
            content = content.replaceAll("org.codehaus.groovy.grails.commons.DefaultGrailsDomainClassProperty","org.grails.core.DefaultGrailsDomainClassProperty")
            content = content.replaceAll("org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass","org.grails.core.DefaultGrailsDomainClass")
            content = content.replaceAll("org.codehaus.groovy.grails.context.support.PluginAwareResourceBundleMessageSource","org.grails.spring.context.support.PluginAwareResourceBundleMessageSource")
            content = content.replaceAll("org.codehaus.groovy.grails.plugins.web.mimes.FormatInterceptor","org.grails.plugins.web.mime.FormatInterceptor")
            content = content.replaceAll("org.codehaus.groovy.grails.commons.GrailsControllerClass","grails.core.GrailsControllerClass")
            content = content.replaceAll("org.codehaus.groovy.grails.commons.CodecArtefactHandler","org.grails.commons.CodecArtefactHandler")
            content = content.replaceAll("org.codehaus.groovy.grails.commons.ControllerArtefactHandler","org.grails.core.artefact.ControllerArtefactHandler")
            content = content.replaceAll("org.codehaus.groovy.grails.commons.GrailsTagLibClass","grails.core.GrailsTagLibClass")
            content = content.replaceAll("org.codehaus.groovy.grails.commons.ServiceArtefactHandler","org.grails.core.artefact.ServiceArtefactHandler")
            content = content.replaceAll("org.codehaus.groovy.grails.commons.TagLibArtefactHandler","org.grails.core.artefact.TagLibArtefactHandler")
            content = content.replaceAll("org.codehaus.groovy.grails.orm.hibernate.validation.HibernateDomainClassValidator","org.grails.orm.hibernate.validation.HibernateDomainClassValidator")
            content = content.replaceAll("org.codehaus.groovy.grails.web.json.JSONArray","org.grails.web.json.JSONArray")
            content = content.replaceAll("org.codehaus.groovy.grails.web.servlet.GrailsDispatcherServlet","org.grails.web.servlet.mvc.GrailsDispatcherServlet")
            content = content.replaceAll("org.codehaus.groovy.grails.web.pages.discovery.CachingGrailsConventionGroovyPageLocator","org.grails.web.gsp.io.CachingGrailsConventionGroovyPageLocator")
            content = content.replaceAll("org.codehaus.groovy.grails.web.pages.discovery.GroovyPageCompiledScriptSource","org.grails.gsp.io.GroovyPageCompiledScriptSource")
            content = content.replaceAll("org.codehaus.groovy.grails.web.pages.discovery.GroovyPageScriptSource","org.grails.gsp.io.GroovyPageScriptSource")
            content = content.replaceAll("org.codehaus.groovy.grails.web.servlet.FlashScope","grails.web.mvc.FlashScope")
            content = content.replaceAll("org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest","org.grails.web.servlet.mvc.GrailsWebRequest")
            content = content.replaceAll("import org.codehaus.groovy.grails.web.json.JSONObject","")
            content = content.replaceAll("com.bitmascot","com.webcommander")
            content = content.replaceAll("JSONObject.NULL","null")
            content = content.replaceAll("grails.transaction.Transactional","grails.gorm.transactions.Transactional")

            if (!oldContent.equals(content)){
                file.delete()
                file << content
            }
            i++
        }
    }
}

println(i)