package com.webcommander.plugin.abandoned_cart.job

import com.webcommander.plugin.abandoned_cart.AbandonedCartService
import com.webcommander.tenant.TenantContext
import grails.util.Holders
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException

/**
 * Created by shahin on 9/04/2015.
 */
class ScheduleJob implements Job {

    private AbandonedCartService _abandonedCartService
    private AbandonedCartService getAbandonedCartService() {
        return _abandonedCartService ?: (_abandonedCartService = Holders.grailsApplication.mainContext.getBean(AbandonedCartService))
    }

    @Override
    void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        TenantContext.eachParallel {
            abandonedCartService.sendScheduleEmail()
        }
    }
}