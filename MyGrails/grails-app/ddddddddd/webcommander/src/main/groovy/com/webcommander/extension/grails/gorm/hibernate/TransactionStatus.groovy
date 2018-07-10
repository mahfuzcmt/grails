package com.webcommander.extension.grails.gorm.hibernate

import org.springframework.transaction.support.DefaultTransactionStatus

class TransactionStatus extends DefaultTransactionStatus {
    List<Closure> commitHandlers = []
    List<Closure> rollbackHandlers = []
    private static ThreadLocal<List<TransactionStatus>> _currentStatus = new ThreadLocal<>()

    TransactionStatus(DefaultTransactionStatus status) {
        super(status.transaction, status.newTransaction, status.newSynchronization, status.readOnly, status.debug, status.suspendedResources)
        List statuses = _currentStatus.get()
        if(statuses) {
            statuses << this
        } else {
            _currentStatus.set([this])
        }
    }

    void onCommit(Closure handler) {
        commitHandlers << handler
    }

    void triggerCommitHandlers() {
        commitHandlers*.call()
    }

    void onRollback(Closure handler) {
        rollbackHandlers << handler
    }

    void triggerRollbackHandlers() {
        rollbackHandlers*.call()
    }

    static TransactionStatus getCurrent() {
        _currentStatus.get()?.last()
    }

    @Override
    void setCompleted() {
        super.setCompleted()
        List statusesInThread = _currentStatus.get()
        if(statusesInThread.size() == 1) {
            _currentStatus.remove()
        } else {
            _currentStatus.get().pop()
        }
    }
}
