package com.webcommander

import com.webcommander.events.AppEventManager
import grails.util.Holders

class SchedulerJob {
    def sessionRequired = false

    static long interval = Holders.config.webcommander.scheduler.tick

    static triggers = {
        simple startDelay: 2*60*1000, repeatInterval: interval
    }

    def execute() {
        Date trigDate = new Date(((long) (new Date().gmt().time / interval)) * interval)
        String trigTime = trigDate.format("HH-mm")
        String trigMinute = trigDate.format("mm")
        String trigDateTime = trigDate.format("yyyy-MM-dd-HH-mm")
        try {
            AppEventManager.fire "clock-slotted-tick-trigger", [trigDate]
        } catch (Throwable k) {
            log.error "'clock-slotted-tick-trigger' failed on " + trigDate, k
        }
        try {
            AppEventManager.fire "clock-daily-" + trigTime + "-trigger", [trigDate]
        } catch (Throwable k) {
            log.error "'clock-daily-trigger' failed on " + trigDate, k
        }
        try {
            AppEventManager.fire "clock-hourly-" + trigMinute + "-trigger", [trigDate]
        } catch (Throwable k) {
            log.error "'clock-hourly-trigger' failed on " + trigDate, k
        }
        try {
            AppEventManager.fire "clock-" + trigDateTime + "-trigger", [trigDate]
        } catch (Throwable k) {
            log.error "'clock-trigger' failed on " + trigDate, k
        }
    }
}