package com.webcommander.tenant

import org.grails.datastore.gorm.jdbc.DataSourceBuilder
import org.grails.datastore.gorm.jdbc.connections.DataSourceConnectionSource
import org.grails.datastore.gorm.jdbc.connections.DataSourceConnectionSourceFactory
import org.grails.datastore.gorm.jdbc.connections.DataSourceSettings
import org.grails.datastore.mapping.core.connections.ConnectionSource
import javax.sql.DataSource

class SwitchableDataSourceFactory extends DataSourceConnectionSourceFactory {

    @Override
    ConnectionSource<DataSource, DataSourceSettings> create(String name, DataSourceSettings settings) {
        DataSource dataSource
        if(TenantContext.multiTenantEnabled) {
            dataSource = new SwitchableDataSource(this, settings)
            dataSource.afterPropertiesSet()
        } else {
            dataSource = create settings, new TenantProps(username: settings.username, password: settings.password, server: TenantContext.SINGLE_TENANT_SERVER, hostname: TenantContext.SINGLE_TENANT_HOST, database: TenantContext.SINGLE_TENANT_NAME, dbProperties: new Properties())
        }
        return new DataSourceConnectionSource(name, dataSource, settings)
    }

    DataSource create(DataSourceSettings settings, TenantProps props) {
        DataSourceBuilder dataSourceBuilder = new DataSourceBuilder(getClass().getClassLoader())
        dataSourceBuilder.setPooled(settings.isPooled())
        dataSourceBuilder.setReadOnly(settings.isReadOnly())
        String driverClassName = DbUrlPatterns.getDriver props.server
        String username = props.username
        String password = props.password
        Map properties = [:]
        properties.putAll(settings.properties)
        properties.putAll(props.dbProperties)
        String url = DbUrlPatterns.getUrl(props.server, props.hostname, props.database)

        if(properties != null && !properties.isEmpty()) {
            dataSourceBuilder.properties(settings.toProperties())
        }
        dataSourceBuilder.url(url)

        if(driverClassName != null) {
            dataSourceBuilder.driverClassName(driverClassName)
        }
        if(username != null) {
            dataSourceBuilder.username(username)
        }
        if(password != null) {
            dataSourceBuilder.password(password)
        }

        DataSource dataSource = dataSourceBuilder.build()
        return proxy(dataSource, settings)
    }
}
