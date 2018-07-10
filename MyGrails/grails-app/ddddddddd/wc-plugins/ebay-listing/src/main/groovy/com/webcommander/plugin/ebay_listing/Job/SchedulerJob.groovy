package com.webcommander.plugin.ebay_listing.Job

import com.webcommander.plugin.ebay_listing.EbayListingService
import com.webcommander.plugin.ebay_listing.admin.webmarketing.EbayUpdateSchedule
import com.webcommander.plugin.ebay_listing.constants.DomainConstants
import grails.util.Holders
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import com.webcommander.tenant.TenantContext

/**
 * Created by sajedur on 4/8/2015.
 */
class SchedulerJob implements Job {
    private EbayListingService _ebayListingService
    private EbayListingService getEbayListingService() {
        return _ebayListingService ?: (_ebayListingService = Holders.grailsApplication.mainContext.getBean(EbayListingService))
    }

    void execute(JobExecutionContext context) throws JobExecutionException {
        Date date = new Date()
        Calendar calendar = Calendar.getInstance()
        calendar.setTime(date)
        int dateOfMonth = calendar.get(Calendar.DATE)
        int hour = calendar.get(Calendar.HOUR_OF_DAY)
        int minute = calendar.get(Calendar.MINUTE)
        minute = minute - (minute % 5)
        Boolean aBoolean
        TenantContext.eachParallel {
            EbayUpdateSchedule schedule = EbayUpdateSchedule.first()
            if(!schedule) {
                return
            }
            aBoolean = ((schedule.scheduleBy == DomainConstants.SCHEDULE_BY.MONTH && dateOfMonth in schedule.dates) || dateOfMonth in schedule.days) && minute in schedule.minutes && hour in schedule.hours
            if(schedule.enableScheduleListing && aBoolean) {
                ebayListingService.synchronizeInventory()
            }
        }
    }
}