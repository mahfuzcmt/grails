package com.webcommander.tenant

import org.grails.datastore.gorm.jdbc.connections.DataSourceConnectionSource
import org.grails.datastore.gorm.jdbc.connections.DataSourceSettings
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource

import javax.sql.DataSource

/**
 * Created by touhid on 24/08/2016.*/
class SwitchableDataSource extends AbstractRoutingDataSource {
    private static Logger log = LoggerFactory.getLogger(SwitchableDataSource)

    private Map<Object, DataSource> resolvedDataSources
    private SwitchableDataSourceFactory factory
    private DataSourceSettings settings

    SwitchableDataSource(SwitchableDataSourceFactory factory, DataSourceSettings settings) {
        this.factory = factory
        this.settings = settings
    }

    @Override
    void afterPropertiesSet() {
        setPlaceholderDb()
        TenantContext.currentTenant = "placeholderDataSource"
        super.afterPropertiesSet()
        def srcs = AbstractRoutingDataSource.getDeclaredField("resolvedDataSources")
        srcs.accessible = true
        resolvedDataSources = srcs.get(this)
    }

    private boolean isDatasourceReady(String tenantId) {
        return resolvedDataSources.containsKey(tenantId)
    }

    @Override
    protected DataSource determineTargetDataSource() {
        String instanceName = TenantContext.currentTenant
        if (!isDatasourceReady(instanceName)) {
            registerDatasource(instanceName)
        }
        return super.determineTargetDataSource()
    }

    private void registerDatasource(String datasourceName) {
        synchronized (datasourceName) {
            if(isDatasourceReady(datasourceName)) {
                return
            }
            TenantProps instanceInfo = TenantPropsResolver.getTenantPropForId(datasourceName)
            DataSource dataSource = factory.create(settings, instanceInfo)
            DataSourceConnectionSource dataSourceConnectionSource = new DataSourceConnectionSource(datasourceName, dataSource, settings)
            resolvedDataSources.put(datasourceName, dataSourceConnectionSource.getSource())
            log.info "Added datasource for $datasourceName"
        }
    }

    @Override
    protected Object determineCurrentLookupKey() {
        return TenantContext.currentTenant
    }

    void setPlaceholderDb() {
        TenantProps props = new TenantProps(username: settings.username, password: settings.password, server: TenantContext.SINGLE_TENANT_SERVER, hostname: TenantContext.SINGLE_TENANT_HOST, database: TenantContext.SINGLE_TENANT_NAME, dbProperties: new Properties())
        targetDataSources = [placeholderDataSource: factory.create(settings, props)]
    }
}