package com.webcommander.jobs.newsletter

import com.webcommander.annotations.event.Event
import com.webcommander.annotations.event.EventHandler
import com.webcommander.tenant.TenantContext
import com.webcommander.util.AppUtil
import com.webcommander.webmarketing.NewsletterService
import grails.util.Holders
import org.springframework.stereotype.Component

@Component
@EventHandler
class NewsletterScheduledJob {
    NewsletterService newsletterService

    @Event("clock-hourly-05-trigger")
    def execute() {
        if (!Holders.servletContext || !Holders.servletContext.initialized) {
            return
        }
        TenantContext.eachParallel {
            List schedulerNewsletterList = newsletterService.scheduledNewsletters()
            schedulerNewsletterList.each { newsletter ->
                try {
                    newsletterService.sendNewsletter(newsletter)
                } catch (Exception e) {
                    log.error("Could not send newsletter", e)
                }
            }
        }
    }
}