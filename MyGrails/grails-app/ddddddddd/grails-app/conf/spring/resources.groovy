package spring

import com.webcommander.extension.grails.gorm.beans.DomainDataBinder
import com.webcommander.spring.ExtendedUrlMappingHolderFactory
import com.webcommander.tenant.SwitchableConnectionSourceFactory
import org.grails.spring.beans.factory.HotSwappableTargetSourceFactoryBean

beans = {
    urlMappingsTargetSource(HotSwappableTargetSourceFactoryBean) {
        target = bean(ExtendedUrlMappingHolderFactory)
    }
    grailsWebDataBinder(DomainDataBinder, ref("grailsApplication"))
    hibernateConnectionSourceFactory(SwitchableConnectionSourceFactory)
}