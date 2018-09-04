package com.webcommander.tenant

import grails.util.Holders
import org.grails.datastore.gorm.jdbc.connections.DataSourceSettings
import org.grails.datastore.mapping.core.connections.ConnectionSource
import org.grails.orm.hibernate.connections.HibernateConnectionSourceFactory
import org.grails.orm.hibernate.connections.HibernateConnectionSourceSettings
import org.hibernate.SessionFactory

import javax.sql.DataSource

class SwitchableConnectionSourceFactory extends HibernateConnectionSourceFactory {
    SwitchableConnectionSourceFactory() {
        super(Holders.grailsApplication.domainClasses.collect {it.clazz})
        dataSourceConnectionSourceFactory = new SwitchableDataSourceFactory()
    }

    ConnectionSource<SessionFactory, HibernateConnectionSourceSettings> create(String name, HibernateConnectionSourceSettings settings) {
        DataSourceSettings dataSourceSettings = settings.getDataSource()
        ConnectionSource<DataSource, DataSourceSettings> dataSourceConnectionSource = dataSourceConnectionSourceFactory.create(name, dataSourceSettings)
        return create(name, dataSourceConnectionSource, settings)
    }
}