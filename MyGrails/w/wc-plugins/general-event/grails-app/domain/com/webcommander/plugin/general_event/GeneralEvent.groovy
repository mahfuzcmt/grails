package com.webcommander.plugin.general_event

import com.webcommander.admin.Customer
import com.webcommander.admin.CustomerGroup
import com.webcommander.common.MetaTag
import com.webcommander.events.AppEventManager
import com.webcommander.manager.HookManager
import com.webcommander.webcommerce.TaxProfile
import com.webcommander.plugin.general_event.VenueLocation
import com.webcommander.plugin.general_event.Equipment
import com.webcommander.plugin.general_event.constants.DomainConstants
import grails.util.Holders

class GeneralEvent {

    Long id

    String name
    String summary
    String description
    String file
    String title
    String heading
    String generalAddress
    String eventStartTime
    String eventEndTime
    String recurrencePattern = DomainConstants.RECURRENCE_PATTERN.DAILY
    String recurrenceEndType = DomainConstants.RECURRENCE_END_TYPE.NO_END_DATE
    String ticketAvailability = DomainConstants.TICKET_AVAILABILITY.EVERYONE
    String dailyRecurrenceType = DomainConstants.DAILY_RECURRENCE_TYPE.EVERYDAY
    String monthlyRecurrenceType = DomainConstants.MONTHLY_RECURRENCE_TYPE.DAY
    String yearlyRecurrenceType = DomainConstants.YEARLY_RECURRENCE_TYPE.ON

    Boolean isPublic
    Boolean isRecurring
    Boolean showGoogleMap
    Boolean isTicketPurchaseEnabled
    Boolean isVenueEnabled
    Boolean isEquipmentEnabled

    Double latitude
    Double longitude
    Double ticketPrice = 0.0
    Integer occurrencesOfRecurrence
    Integer maxTicket
    Integer maxTicketPerCustomer
    Integer totalSoldTicket = 0
    Integer recurringYear = 1
    Integer monthNameOfYearlyRecurring = 1
    Integer dateOfYearlyRecurring = 1
    Integer selectedWeekOfYearlyRecurring = 1
    Integer selectedDayOfYearlyRecurring = 1
    Integer selectedMonthNameOfYearlyRecurring = 1
    Integer monthNameOfMonthlyRecurring = 1
    Integer dateOfMonthlyRecurring = 1
    Integer selectedWeekOfMonthlyRecurring = 1
    Integer selectedDayOfMonthlyRecurring = 1
    Integer selectedMonthNameOfMonthlyRecurring = 1

    Date startDateTime
    Date endDateTime
    Date created
    Date updated

    VenueLocation venueLocation
    Equipment equipment
    TaxProfile taxProfile

    Collection<GeneralEventImage> images = []
    Collection<RecurringEvents> events = []
    Collection<MetaTag> metaTags = []
    Collection<Integer> dailyEvents = []
    Collection<String> weekDays = []
    Collection<Customer> availableToCustomers = []
    Collection<CustomerGroup> availableToCustomerGroups = []

    static hasMany = [images: GeneralEventImage, events: RecurringEvents, metaTags: MetaTag, availableToCustomers: Customer, availableToCustomerGroups: CustomerGroup, dailyEvents: Integer, weekDays: Integer]

    static constraints = {
        summary(nullable: true, maxSize: 500)
        description(nullable: true)
        title(nullable: true, maxSize: 200)
        heading(nullable: true, maxSize: 200)
        eventStartTime(nullable: true)
        eventEndTime(nullable: true)
        startDateTime(nullable: true)
        endDateTime(nullable: true)
        occurrencesOfRecurrence(nullable: true)
        file(nullable: true)
        generalAddress(nullable: true, maxSize: 500)
        showGoogleMap(nullable: true)
        latitude(nullable: true)
        longitude(nullable: true)
        taxProfile(nullable: true)
        ticketPrice(nullable: true)
        recurrenceEndType(nullable: true)
        maxTicket(nullable: true)
        maxTicketPerCustomer(nullable: true)
        venueLocation(nullable: true)
        equipment(nullable: true)
        dailyRecurrenceType(nullable: true)
        dailyEvents(nullable: true)
        weekDays(nullable: true)
        monthlyRecurrenceType(nullable: true)
        yearlyRecurrenceType(nullable: true)
        recurringYear(nullable: true)
        monthNameOfYearlyRecurring(nullable: true)
        dateOfYearlyRecurring(nullable: true)
        selectedWeekOfYearlyRecurring(nullable: true)
        selectedDayOfYearlyRecurring(nullable: true)
        selectedMonthNameOfYearlyRecurring(nullable: true)
        monthNameOfMonthlyRecurring(nullable: true)
        dateOfMonthlyRecurring(nullable: true)
        selectedWeekOfMonthlyRecurring(nullable: true)
        selectedDayOfMonthlyRecurring(nullable: true)
        selectedMonthNameOfMonthlyRecurring(nullable: true)
    }

    static mapping = {
        dailyEvents joinTable:[name: "general_event_daily_events", key: "event_id", column: "daily_events", type: "bigint(20)"]
        weekDays joinTable:[name: "general_event_week_days", key: "event_id", column: "week_days", type: "int(1)"]
        description type: "text"
    }

    def beforeValidate() {
        if (!this.created) {
            this.created = new Date().gmt()
        }
        if (!this.updated) {
            this.updated = new Date().gmt()
        }
    }

    def beforeUpdate() {
        this.updated = new Date().gmt()
    }

    public String ticketPriceWithCurrency() {
        GeneralEventService generalEventService = Holders.grailsApplication.mainContext.getBean(GeneralEventService)
        return generalEventService.ticketPriceWithCurrency(this)
    }

}
