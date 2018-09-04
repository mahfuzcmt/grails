package com.webcommander.plugin.ebay_listing.admin.webmarketing

import com.webcommander.events.AppEventManager
import com.webcommander.plugin.ebay_listing.constants.DomainConstants
import com.webcommander.plugin.ebay_listing.constants.NamedConstants

class EbayUpdateSchedule {

    Boolean enableScheduleListing = false
    String scheduleBy = DomainConstants.SCHEDULE_BY.MONTH

    Collection<Integer> months = []
    Collection<Integer> days = []
    Collection<Integer> dates = []
    Collection<Integer> hours = []
    Collection<Integer> minutes = []

    static hasMany = [months: Integer, days: Integer, dates: Integer, hours: Integer, minutes: Integer]

    public static initialize() {
        if(!EbayUpdateSchedule.count()) {
            new EbayUpdateSchedule().save();
        }
    }
}
