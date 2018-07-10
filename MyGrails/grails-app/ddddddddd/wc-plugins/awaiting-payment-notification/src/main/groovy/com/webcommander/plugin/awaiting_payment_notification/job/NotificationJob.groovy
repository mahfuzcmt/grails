package com.webcommander.plugin.awaiting_payment_notification.job

import com.webcommander.plugin.awaiting_payment_notification.AwaitingPaymentEmailService
import com.webcommander.tenant.TenantContext
import grails.util.Holders
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException

class NotificationJob implements Job {
    private static AwaitingPaymentEmailService _awaitingPaymentEmailService
    private static AwaitingPaymentEmailService getAwaitingPaymentEmailService() {
        _awaitingPaymentEmailService ?: (_awaitingPaymentEmailService = Holders.grailsApplication.mainContext.getBean(AwaitingPaymentEmailService))
    }
    @Override
    void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        TenantContext.eachParallel {
            awaitingPaymentEmailService.sendReminder()
        }
    }
}
