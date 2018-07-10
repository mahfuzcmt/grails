package org.grails.orm.hibernate

import com.webcommander.extension.grails.gorm.hibernate.TransactionStatus
import org.grails.orm.hibernate.support.HibernateVersionSupport
import org.hibernate.FlushMode
import org.hibernate.SessionFactory
import org.springframework.orm.hibernate5.HibernateTransactionManager
import org.springframework.orm.hibernate5.SessionHolder
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.DefaultTransactionStatus
import org.springframework.transaction.support.TransactionSynchronizationManager

import javax.sql.DataSource

/**
 * Created by LocalZobair on 19/04/2017.
 * */
class GrailsHibernateTransactionManager extends HibernateTransactionManager {

    final FlushMode defaultFlushMode

    GrailsHibernateTransactionManager(FlushMode defaultFlushMode = FlushMode.AUTO) {
        this.defaultFlushMode = defaultFlushMode
    }

    GrailsHibernateTransactionManager(SessionFactory sessionFactory, FlushMode defaultFlushMode = FlushMode.AUTO) {
        super(sessionFactory)
        this.defaultFlushMode = defaultFlushMode
    }

    GrailsHibernateTransactionManager(SessionFactory sessionFactory, DataSource dataSource, FlushMode defaultFlushMode = FlushMode.AUTO) {
        super(sessionFactory)
        setDataSource(dataSource)
        this.defaultFlushMode = defaultFlushMode
    }

    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) {
        super.doBegin transaction, definition

        if (definition.isReadOnly()) {
            // transaction is HibernateTransactionManager.HibernateTransactionObject private class instance
            // always set to manual; the base class doesn't because the OSIVI has already registered a session

            SessionHolder holder = (SessionHolder)TransactionSynchronizationManager.getResource(sessionFactory)
            HibernateVersionSupport.setFlushMode(holder.getSession(), FlushMode.MANUAL)
        }
        else if(defaultFlushMode != FlushMode.AUTO) {
            SessionHolder holder = (SessionHolder)TransactionSynchronizationManager.getResource(sessionFactory)
            HibernateVersionSupport.setFlushMode(holder.getSession(), defaultFlushMode)
        }
    }

    @Override
    protected DefaultTransactionStatus newTransactionStatus(TransactionDefinition definition, Object transaction, boolean newTransaction, boolean newSynchronization, boolean debug, Object suspendedResources) {
        return new TransactionStatus(super.newTransactionStatus(definition, transaction, newTransaction, newSynchronization, debug, suspendedResources))
    }

    @Override
    protected void doCommit(DefaultTransactionStatus status) {
        super.doCommit(status)
        ((TransactionStatus)status).triggerCommitHandlers()
    }

    @Override
    protected void doRollback(DefaultTransactionStatus status) {
        super.doRollback(status)
        ((TransactionStatus)status).triggerRollbackHandlers()
    }
}