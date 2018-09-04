package com.webcommander.plugin.awaiting_payment_notification

import com.webcommander.constants.DomainConstants
import com.webcommander.plugin.awaiting_payment_notification.job.NotificationJob
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.Order
import com.webcommander.webcommerce.OrderService
import groovy.sql.Sql
import org.quartz.JobBuilder
import org.quartz.JobDetail
import org.quartz.Scheduler
import org.quartz.SimpleScheduleBuilder
import org.quartz.Trigger
import org.quartz.TriggerBuilder

class AwaitingPaymentEmailService {
    def dataSource
    OrderService orderService
    Scheduler quartzScheduler

    List getOrders() {
        Map config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.AWAITING_PAYMENT);
        def intervalType = ""
        if(config.interval_type == "day") {
            intervalType = "days"
        } else if(config.interval_type == "hr") {
            intervalType = "hours"
        } else {
            intervalType = "minutes"
        }
        def orderList = Order.createCriteria().list {
            ne("paymentStatus", 'paid')
            ne("orderStatus", 'cancelled')
            lt("reminderCount", config.no_of_max_time.toInteger())
        }.findAll{it.lastReminderTime.plus((config.interval.toInteger())."${intervalType}") > new Date().gmt()}

        return orderList
    }

    def sendReminder() {
        try {
            List orderList = getOrders();
            orderList.each { order ->
                AppUtil.initialDummyRequest()
                orderService.sendEmailForOrder(order.id, "awaiting-payment");
                Order.withNewTransaction {
                    Order orderOb = Order.get(order.id)
                    orderOb.lastReminderTime = new Date().gmt();
                    orderOb.reminderCount++
                    orderOb.merge();
                }
            }
        } catch (Exception ex) {
            log.error(ex.message)
        }

    }

    def startScheduler() {
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule().withIntervalInMinutes(4).repeatForever()
        JobDetail job = JobBuilder.newJob(NotificationJob.class).withIdentity("awaitingPaymentNotification", "order").build();
        Trigger trigger = TriggerBuilder
                .newTrigger()
                .withIdentity("awaitingPaymentNotificationTrigger", "order")
                .withSchedule(scheduleBuilder)
                .build();
        quartzScheduler.scheduleJob(job, trigger)
    }
}
