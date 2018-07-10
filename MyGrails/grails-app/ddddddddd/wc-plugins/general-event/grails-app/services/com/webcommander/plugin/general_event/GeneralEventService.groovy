package com.webcommander.plugin.general_event

import com.webcommander.JSONSerializableList
import com.webcommander.admin.Customer
import com.webcommander.admin.CustomerGroup
import com.webcommander.annotations.Initializable
import com.webcommander.common.CommanderMailService
import com.webcommander.common.CommonService
import com.webcommander.common.ImageService
import com.webcommander.common.MetaTag
import com.webcommander.constants.NamedConstants
import com.webcommander.events.AppEventManager
import com.webcommander.manager.CartManager
import com.webcommander.manager.HookManager
import com.webcommander.manager.PathManager
import com.webcommander.models.Cart
import com.webcommander.plugin.general_event.constants.DomainConstants
import com.webcommander.plugin.general_event.manager.VenueTicketCartManager
import com.webcommander.plugin.general_event.model.CartGeneralEventTicket
import com.webcommander.plugin.general_event.model.GeneralEventData
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.throwables.AttachmentExistanceException
import com.webcommander.throwables.UnconfiguredWidgetExceptions
import com.webcommander.util.AppUtil
import com.webcommander.util.TrashUtil
import com.webcommander.webcommerce.Order
import com.webcommander.webcommerce.TaxProfile
import com.webcommander.widget.Widget
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.util.Holders
import org.apache.commons.io.FilenameUtils
import grails.web.databinding.DataBindingUtils
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.multipart.MultipartFile

@Initializable
@Transactional
class GeneralEventService {

    CommonService commonService
    ImageService imageService
    CommanderMailService commanderMailService
    int eventRepetition = 500

    static void initialize() {
        AppEventManager.on("before-general-event-delete", { id ->
            RecurringEvents.createCriteria().list {
                eq("parentEvent.id", id)
            }.each { value ->
                TrashUtil.preProcessFinalDelete("recurring-event", value.id, true, true)
                AppEventManager.fire("before-recurring-event-delete", [value.id])
                value.delete()
                AppEventManager.fire("recurring-event-delete", [value.id])
            }
        })

        HookManager.register("venue-location-section-delete-veto") { response, id ->
            int ticketCount = TicketInventoryAdjustment.createCriteria().count {
                eq("section.id", id)
            }
            if(ticketCount) {
                def entry = [tickets: [list: false, count: ticketCount]]
                if(response.has) {
                    response.has << entry
                } else {
                    response.has = entry
                }
            }
            return response
        }

        HookManager.register("venueLocation-delete-veto-list") { response, id ->
            List<VenueLocationSection> sectionList = VenueLocation.get(id).sections
            int ticketList = 0;
            for(int i = 0; i < sectionList.size(); i++) {
                ticketList += TicketInventoryAdjustment.createCriteria().count() {
                    eq("section.id", sectionList[i].id)
                }
            }
            if(ticketList) {
                response."tickets" = ["Total Tickets : ${ticketList}"]
            }
            return response
        }

        HookManager.register("general-event-delete-veto") { response, id ->
            int ticketCount = TicketInventoryAdjustment.createCriteria().count {
                eq("generalEvent.id", id)
            }
            if(ticketCount && ticketCount > 1) {
                response.Tickets = ticketCount
            }else if(ticketCount) {
                response.Ticket = ticketCount
            }
            return response
        }

        HookManager.register("general-event-delete-veto-list") { response, id ->
            int ticketCount = TicketInventoryAdjustment.createCriteria().count {
                eq("generalEvent.id", id)
            }
            if(ticketCount && ticketCount > 1) {
                response.Tickets = ticketCount
            }else if(ticketCount) {
                response.Ticket = ticketCount
            }
            return response
        }

        HookManager.register("recurring-event-delete-veto") { response, id ->
            int ticketCount = TicketInventoryAdjustment.createCriteria().count {
                eq("recurringEvent.id", id)
            }
            if(ticketCount && ticketCount > 1) {
                response.Tickets = ticketCount
            }else if(ticketCount) {
                response.Ticket = ticketCount
            }
            return response
        }

        HookManager.register("recurring-event-delete-veto-list") { response, id ->
            int ticketCount = TicketInventoryAdjustment.createCriteria().count {
                eq("recurringEvent.id", id)
            }
            if(ticketCount && ticketCount > 1) {
                response.Tickets = ticketCount
            }else if(ticketCount) {
                response.Ticket = ticketCount
            }
            return response
        }

        HookManager.register("venue-delete-veto") { response, id ->
            List locations = VenueLocation.createCriteria().list {
                eq("venue.id", id)
            }.id
            if(locations.size()) {
                locations.each { locationId ->
                    Map vetos = HookManager.hook("venue-location-delete-veto", [:], id)
                    response.putAll(vetos)
                }
            }
            return response
        }

        AppEventManager.on("before-venue-delete", { id ->
            VenueLocation.createCriteria().list {
                eq("venue.id", id)
            }.each {
                TrashUtil.preProcessFinalDelete("venue-location", id, true, true)
                AppEventManager.fire("before-venue-location-delete", [it.id, "include"])
                it.delete()
                AppEventManager.fire("venue-location-delete", [it.id])
            }
        })

        AppEventManager.on("before-venue-location-delete", { id ->
            VenueLocationImage.createCriteria().list {
                eq("venueLocation.id", id)
            }*.delete()
        })

        HookManager.register("venue-location-delete-veto") { response, id ->
            List sectionIds = VenueLocationSection.createCriteria().list{
                eq("venueLocation.id", id)
            }.id
            int sectionCount = sectionIds.size()
            if(sectionCount) {
                sectionIds.each {sectionId->
                    try {
                        TrashUtil.preProcessFinalDelete("venue-location-section", sectionId, true, true)
                    } catch (AttachmentExistanceException att) {
                        if(response.has) {
                            att.attachmentInfo["at3"].has.tickets.count += response.has.tickets.count
                        }
                        response.putAll(att.attachmentInfo["at3"])
                    }
                }
                response.sections = sectionCount
            }
            return response
        }
        HookManager.register("venueLocation-delete-veto-list") { response, id ->
            VenueLocation location = VenueLocation.get(id)
            if(location.sections) {
                response."sections" = location.sections.collect {it.name.encodeAsBMHTML()}
            }
            return response
        }

        AppEventManager.on("before-venue-location-delete", { id ->
            VenueLocationSection.createCriteria().list {
                eq("venueLocation.id", id)
            }.each {
                AppEventManager.fire("before-venue-location-section-delete", [it.id])
                it.delete()
                AppEventManager.fire("venue-location-section-delete", [it.id])
            }
        })

        HookManager.register("taxProfile-delete-at2-count") { response, id ->
            int eventCount = GeneralEvent.where {
                taxProfile.id == id
            }.count()
            if(eventCount) {
                response.events = eventCount
            }
            return response;
        }
        HookManager.register("taxProfile-delete-at2-list") { response, id ->
            List events = GeneralEvent.createCriteria().list {
                projections {
                    property("name")
                }
                eq("taxProfile.id", id)
            }
            if(events.size()) {
                response.events = events
            }
            return response;
        }
        AppEventManager.on("before-taxProfile-delete", { id ->
            TaxProfile profile = TaxProfile.proxy(id);
            GeneralEvent.where {
                taxProfile == profile
            }.updateAll([taxProfile: null])
        })
        HookManager.register("general-equipment-delete-veto") { response, id ->
            int count = GeneralEvent.createCriteria().count {
                eq("equipment.id", id)
            }
            if(count) {
                response.events = count
            }
            return response
        }
        HookManager.register("general-equipment-delete-veto-list") { response, id ->
            int count = GeneralEvent.createCriteria().count {
                eq("equipment.id", id)
            }
            if(count) {
                response.events = count
            }
            return response
        }
        HookManager.register("venue-location-delete-veto") { response, id ->
            int count = GeneralEvent.createCriteria().count {
                eq("venueLocation.id", id)
            }
            if(count && count > 1) {
                response.Events = count
            }else if(count) {
                response.Event = count
            }
            return response
        }
        HookManager.register("venueLocation-delete-veto-list") { response, id ->
            int count = GeneralEvent.createCriteria().count {
                eq("venueLocation.id", id)
            }
            if(count && count > 1) {
                response.Events = count
            }else if(count) {
                response.Event = count
            }
            return response
        }

        AppEventManager.on("before-general-event-delete", { id ->
            GeneralEventCheckoutField.createCriteria().list {
                eq("event.id", id)
            }*.delete()
        })

        AppEventManager.on("before-general-event-delete", { id ->
            GeneralEventCheckoutFieldTitle.createCriteria().list {
                eq("event.id", id)
            }*.delete()
        })

        AppEventManager.on("before-general-event-delete", { id ->
            GeneralEventCustomFieldData.createCriteria().list {
                eq("generalEvent.id", id)
            }*.delete()
        })
        AppEventManager.on("before-recurring-event-delete", { id ->
            GeneralEventCustomFieldData.createCriteria().list {
                eq("recurringEvent.id", id)
            }*.delete()
        })

        AppEventManager.on("before-general-event-delete", { id ->
            GeneralEventImage.createCriteria().list {
                eq("event.id", id)
            }*.delete()
        })
    }

    public List timeList() {
        List<String> list = new ArrayList<String>();
        String postfix = ' AM'
        int count = 1;
        for(int i = 0; i < 24; i++) {
            int timeGap = 0
            for(int j = 1; timeGap < 60 ; j++) {
                list.add(String.format("%02d", count) + ':' + String.format("%02d", timeGap) + postfix)
                timeGap += 5
            }
            count++
            if(count == 13) {
                count = 1
                postfix = ' PM'
            }
        }
        return list
    }

    //////////////////////////////////////////////// ADD TICKET TO CART SECTION ////////////////////////////////////////

    static {

        AppEventManager.on("order-create order-update", {Long orderId ->
            def updateStockConfig = AppUtil.getConfig(com.webcommander.constants.DomainConstants.SITE_CONFIG_TYPES.E_COMMERCE, "update_general_event_ticket_stock")
            if (updateStockConfig == com.webcommander.constants.DomainConstants.UPDATE_STOCK.AFTER_ORDER) {
                GeneralEventService _this = Holders.grailsApplication.mainContext.getBean(GeneralEventService)
                _this.adjustTicketSoldQuantity(orderId)
            }
        });
        AppEventManager.on("paid-for-cart", { Collection<Cart> carts ->
            def updateStockConfig = AppUtil.getConfig(com.webcommander.constants.DomainConstants.SITE_CONFIG_TYPES.E_COMMERCE, "update_general_event_ticket_stock")
            if (updateStockConfig == com.webcommander.constants.DomainConstants.UPDATE_STOCK.AFTER_PAYMENT) {
                GeneralEventService _this = Holders.grailsApplication.mainContext.getBean(GeneralEventService)
                carts.each {Cart cart->
                    _this.adjustTicketSoldQuantity(cart.orderId)
                }
            }
        });
        AppEventManager.on("before-order-update", {Long orderId ->
            GeneralEventService _this = Holders.grailsApplication.mainContext.getBean(GeneralEventService)
            _this.reAdjustTicketSoldQuantity(orderId)
        });

        AppEventManager.on("paid-for-order", { Order order->
            GeneralEventService _this = Holders.grailsApplication.mainContext.getBean(GeneralEventService)
            _this.adjustTicketSoldQuantity(order.id)
        });

        AppEventManager.on("order-cancelled", { orderId ->
            GeneralEventService _this = Holders.grailsApplication.mainContext.getBean(GeneralEventService)
            _this.reAdjustTicketSoldQuantity(orderId)
        });

        HookManager.register("resolveCartObject-general_event_ticket", { object, cartItem ->
            return new CartGeneralEventTicket(cartItem.itemId);
        });

        HookManager.register("addToCart-general_event_ticket", {def cartItem ->
            CartGeneralEventTicket cartGeneralEventTicket = new CartGeneralEventTicket(cartItem.itemId);
            CartManager.addToCart(AppUtil.session.id, cartGeneralEventTicket, cartItem.quantity);
        });
    }

    @Transactional
    void adjustTicketSoldQuantity(Long orderId) {
        Order order = Order.get(orderId);
        List<TicketInventoryAdjustment> adjustments = TicketInventoryAdjustment.findAllByOrder(order)
        if(adjustments) {
            return
        }
        def event
        order.items.each {
            if(it.productType ==  NamedConstants.CART_OBJECT_TYPES.GENERAL_EVENT_TICKET && GeneralEvent.get(it.productId)) {
                event = GeneralEvent.get(it.productId)
                event.totalSoldTicket += it.quantity;
                String message = "After order# " + orderId
                TicketInventoryAdjustment adjustment = new TicketInventoryAdjustment(changeQuantity: -1 * it.quantity, order: order, generalEvent: event, note: message)
                adjustment.save()
                event.save()
            }else if(it.productType ==  NamedConstants.CART_OBJECT_TYPES.RECURRING_EVENT_TICKET && RecurringEvents.get(it.productId)) {
                event = RecurringEvents.get(it.productId)
                event.totalSoldTicket += it.quantity
                String message = "After order# " + orderId
                TicketInventoryAdjustment adjustment = new TicketInventoryAdjustment(changeQuantity: -1 * it.quantity, order: order, recurringEvent: event, note: message)
                adjustment.save()
                event.save()
            }else if(it.productType ==  NamedConstants.CART_OBJECT_TYPES.GENERAL_EVENT_VENUE_TICKET ) {
                String[] variations = it.variations[0].split('#')
                event = GeneralEvent.get(variations[2].toLong())
                event.totalSoldTicket += it.quantity;
                String message = "After order# " + orderId
                TicketInventoryAdjustment adjustment = new TicketInventoryAdjustment(changeQuantity: -1 * it.quantity, order: order, ticketNumber: variations[0].trim(), generalEvent: event, section: VenueLocationSection.get(it.productId), note: message)
                variations[1].split(',').toList().each { value->
                    adjustment.seatNumber.add(value.toInteger())
                }
                adjustment.save();
                event.save();
            }else if(it.productType ==  NamedConstants.CART_OBJECT_TYPES.RECURRING_EVENT_VENUE_TICKET ) {
                String[] variations = it.variations[0].split('#')
                event = RecurringEvents.get(variations[2].toLong())
                event.totalSoldTicket += it.quantity
                String message = "After order# " + orderId
                TicketInventoryAdjustment adjustment = new TicketInventoryAdjustment(changeQuantity: -1 * it.quantity, order: order, ticketNumber: variations[0].trim(), recurringEvent: event, section: VenueLocationSection.get(it.productId), note: message);
                variations[1].split(',').toList().each { value->
                    adjustment.seatNumber.add(value.toInteger())
                }
                adjustment.save()
                event.save()
            }
        }
    }

    @Transactional
    void reAdjustTicketSoldQuantity(Long orderId) {
        Order order = Order.get(orderId);
        List<TicketInventoryAdjustment> adjustments = TicketInventoryAdjustment.findAllByOrder(order)
        adjustments.each {
            if(it.generalEvent) {
                it.generalEvent.totalSoldTicket += it.changeQuantity;
            }else {
                it.recurringEvent.totalSoldTicket += it.changeQuantity;
            }
            it.save()
        }
        adjustments*.delete()
    }

    ////////////////////////////////////////////// EVENT SECTION /////////////////////////////////////////////////////////

    def saveEvent(Map params) {
        Long id = params.id ? params.id.toLong(0) : null;
        if(id) {
            Integer soldTickets = GeneralEvent.proxy(id).totalSoldTicket?.toInteger();
            if(soldTickets && soldTickets > params.maxTicket?.toInteger()) {
                throw new ApplicationRuntimeException("event.update.error", [soldTickets]);
            }
        }
        GeneralEvent event = id ? GeneralEvent.get(id) : new GeneralEvent();
        def session = AppUtil.session;
        event.name = params.name
        event.heading = params.heading
        event.title = params.title
        event.isPublic = params.isPublic.toBoolean()
        event.isRecurring = params.isRecurring.toBoolean()
        if(params.isRecurring.toBoolean()) {
            event.eventStartTime = params.eventStartTime
            event.eventEndTime = params.eventEndTime
            event.recurrencePattern = params.recurrencePattern
            event.startDateTime = params.startDateTime[1]?.toDate()?.gmt(AppUtil.session.timezone)
            event.recurrenceEndType = params.recurrenceEndType
            if(params.recurrencePattern == DomainConstants.RECURRENCE_PATTERN.DAILY && params.dailyRecurrenceType == DomainConstants.DAILY_RECURRENCE_TYPE.EVERY)
            {
                event.dailyRecurrenceType = params.dailyRecurrenceType
                event.recurrenceEndType = params.recurrenceEndType
                if(event.dailyEvents) {
                    event.dailyEvents.clear()
                }
                params.list("dailyEvents").each {
                    event.dailyEvents.add(it.toInteger());
                }
                if(params.recurrenceEndType == DomainConstants.RECURRENCE_END_TYPE.END_BY_DATE) {
                    event.endDateTime = params.endDateTime[1]?.toDate()?.gmt(AppUtil.session.timezone)
                    event.occurrencesOfRecurrence = countRepetition(event.startDateTime, event.endDateTime, DomainConstants.DAILY_RECURRENCE_TYPE.EVERY, event.dailyEvents)
                }else if(params.recurrenceEndType == DomainConstants.RECURRENCE_END_TYPE.END_AFTER_REPETITION) {
                    event.occurrencesOfRecurrence = params.occurrencesOfRecurrence.toInteger()
                    event.endDateTime = getRecurringEndTime(event.startDateTime, DomainConstants.DAILY_RECURRENCE_TYPE.EVERY, event.dailyEvents, event.occurrencesOfRecurrence)
                }else {
                    event.endDateTime = getRecurringEndTime(event.startDateTime, DomainConstants.DAILY_RECURRENCE_TYPE.EVERY, event.dailyEvents)
                }
            }
            else if(params.recurrencePattern == DomainConstants.RECURRENCE_PATTERN.DAILY && params.dailyRecurrenceType == DomainConstants.DAILY_RECURRENCE_TYPE.EVERYDAY)
            {
                event.dailyRecurrenceType = params.dailyRecurrenceType
                if(params.recurrenceEndType == DomainConstants.RECURRENCE_END_TYPE.END_BY_DATE) {
                    event.endDateTime = params.endDateTime[1]?.toDate()?.gmt(AppUtil.session.timezone)
                    event.occurrencesOfRecurrence = countRepetition(event.startDateTime, event.endDateTime, DomainConstants.DAILY_RECURRENCE_TYPE.EVERYDAY)
                }else if(params.recurrenceEndType == DomainConstants.RECURRENCE_END_TYPE.END_AFTER_REPETITION) {
                    event.occurrencesOfRecurrence = params.occurrencesOfRecurrence.toInteger()
                    event.endDateTime = getRecurringEndTime(event.startDateTime, DomainConstants.DAILY_RECURRENCE_TYPE.EVERYDAY, null, event.occurrencesOfRecurrence)
                }else {
                    event.endDateTime = getRecurringEndTime(event.startDateTime, DomainConstants.DAILY_RECURRENCE_TYPE.EVERYDAY)
                }
            }
            else if(params.recurrencePattern == DomainConstants.RECURRENCE_PATTERN.WEEKLY)
            {
                if(event.weekDays) {
                    event.weekDays.clear();
                }
                params.list("weekDays").each {
                    if(it) {
                        event.weekDays.add(it.toInteger());
                    }
                }
                if(params.recurrenceEndType == DomainConstants.RECURRENCE_END_TYPE.END_BY_DATE) {
                    event.endDateTime = params.endDateTime[1]?.toDate()?.gmt(AppUtil.session.timezone)
                    event.occurrencesOfRecurrence = countRepetition(event.startDateTime, event.endDateTime, DomainConstants.RECURRENCE_PATTERN.WEEKLY, event.weekDays)
                }else if(params.recurrenceEndType == DomainConstants.RECURRENCE_END_TYPE.END_AFTER_REPETITION) {
                    event.occurrencesOfRecurrence = params.occurrencesOfRecurrence.toInteger()
                    event.endDateTime = getRecurringEndTime(event.startDateTime, DomainConstants.RECURRENCE_PATTERN.WEEKLY, event.weekDays, event.occurrencesOfRecurrence)
                }else {
                    event.endDateTime = getRecurringEndTime(event.startDateTime, DomainConstants.RECURRENCE_PATTERN.WEEKLY, event.weekDays)
                }
            }
            else if(params.recurrencePattern == DomainConstants.RECURRENCE_PATTERN.MONTHLY && params.monthlyRecurrenceType == DomainConstants.MONTHLY_RECURRENCE_TYPE.DAY)
            {
                event.monthlyRecurrenceType = params.monthlyRecurrenceType
                event.dateOfMonthlyRecurring = params.dateOfMonthlyRecurring.toInteger()
                event.monthNameOfMonthlyRecurring = params.monthNameOfMonthlyRecurring.toInteger()
                List<Integer> list = new ArrayList<Integer>()
                list.add(event.monthNameOfMonthlyRecurring)
                list.add(event.dateOfMonthlyRecurring)
                if(params.recurrenceEndType == DomainConstants.RECURRENCE_END_TYPE.END_BY_DATE) {
                    event.endDateTime = params.endDateTime[1]?.toDate()?.gmt(AppUtil.session.timezone)
                    event.occurrencesOfRecurrence = countRepetition(event.startDateTime, event.endDateTime, DomainConstants.MONTHLY_RECURRENCE_TYPE.DAY, list)
                }else if(params.recurrenceEndType == DomainConstants.RECURRENCE_END_TYPE.END_AFTER_REPETITION) {
                    event.occurrencesOfRecurrence = params.occurrencesOfRecurrence.toInteger()
                    event.endDateTime = getRecurringEndTime(event.startDateTime, DomainConstants.MONTHLY_RECURRENCE_TYPE.DAY, list, event.occurrencesOfRecurrence)
                }else {
                    event.endDateTime = getRecurringEndTime(event.startDateTime, DomainConstants.MONTHLY_RECURRENCE_TYPE.DAY, list)
                }
            }
            else if(params.recurrencePattern == DomainConstants.RECURRENCE_PATTERN.MONTHLY && params.monthlyRecurrenceType == DomainConstants.MONTHLY_RECURRENCE_TYPE.SELECTED)
            {
                event.monthlyRecurrenceType = params.monthlyRecurrenceType
                event.selectedWeekOfMonthlyRecurring = params.selectedWeekOfMonthlyRecurring.toInteger()
                event.selectedDayOfMonthlyRecurring = params.selectedDayOfMonthlyRecurring.toInteger()
                event.selectedMonthNameOfMonthlyRecurring = params.selectedMonthNameOfMonthlyRecurring.toInteger()
                List<Integer> list = new ArrayList<Integer>()
                list.add(event.selectedMonthNameOfMonthlyRecurring)
                list.add(event.selectedWeekOfMonthlyRecurring)
                list.add(event.selectedDayOfMonthlyRecurring)
                if(params.recurrenceEndType == DomainConstants.RECURRENCE_END_TYPE.END_BY_DATE) {
                    event.endDateTime = params.endDateTime[1]?.toDate()?.gmt(AppUtil.session.timezone)
                    event.occurrencesOfRecurrence = countRepetition(event.startDateTime, event.endDateTime, DomainConstants.MONTHLY_RECURRENCE_TYPE.SELECTED, list)
                }else if(params.recurrenceEndType == DomainConstants.RECURRENCE_END_TYPE.END_AFTER_REPETITION) {
                    event.occurrencesOfRecurrence = params.occurrencesOfRecurrence.toInteger()
                    event.endDateTime = getRecurringEndTime(event.startDateTime, DomainConstants.MONTHLY_RECURRENCE_TYPE.SELECTED, list, event.occurrencesOfRecurrence)
                }else {
                    event.endDateTime = getRecurringEndTime(event.startDateTime, DomainConstants.MONTHLY_RECURRENCE_TYPE.SELECTED, list)
                }
            }
            else if(params.recurrencePattern == DomainConstants.RECURRENCE_PATTERN.YEARLY && params.yearlyRecurrenceType == DomainConstants.YEARLY_RECURRENCE_TYPE.ON)
            {
                event.yearlyRecurrenceType = params.yearlyRecurrenceType
                event.recurringYear = params.recurringYear.toInteger()
                event.dateOfYearlyRecurring = params.dateOfYearlyRecurring.toInteger()
                event.monthNameOfYearlyRecurring = params.monthNameOfYearlyRecurring.toInteger()
                List<Integer> list = new ArrayList<Integer>()
                list.add(event.monthNameOfYearlyRecurring)
                list.add(event.dateOfYearlyRecurring)
                if(params.recurrenceEndType == DomainConstants.RECURRENCE_END_TYPE.END_BY_DATE) {
                    event.endDateTime = params.endDateTime[1]?.toDate()?.gmt(AppUtil.session.timezone)
                    event.occurrencesOfRecurrence = countRepetition(event.startDateTime, event.endDateTime, DomainConstants.YEARLY_RECURRENCE_TYPE.ON, list, event.recurringYear)
                }else if(params.recurrenceEndType == DomainConstants.RECURRENCE_END_TYPE.END_AFTER_REPETITION) {
                    event.occurrencesOfRecurrence = params.occurrencesOfRecurrence.toInteger()
                    event.endDateTime = getRecurringEndTime(event.startDateTime, DomainConstants.YEARLY_RECURRENCE_TYPE.ON, list, event.occurrencesOfRecurrence, event.recurringYear)
                }else {
                    event.endDateTime = getRecurringEndTime(event.startDateTime, DomainConstants.YEARLY_RECURRENCE_TYPE.ON, list, eventRepetition, event.recurringYear)
                }
            }
            else if(params.recurrencePattern == DomainConstants.RECURRENCE_PATTERN.YEARLY && params.yearlyRecurrenceType == DomainConstants.YEARLY_RECURRENCE_TYPE.ON_THE)
            {
                event.yearlyRecurrenceType = params.yearlyRecurrenceType
                event.recurringYear = params.recurringYear.toInteger()
                event.selectedWeekOfYearlyRecurring = params.selectedWeekOfYearlyRecurring.toInteger()
                event.selectedDayOfYearlyRecurring = params.selectedDayOfYearlyRecurring.toInteger()
                event.selectedMonthNameOfYearlyRecurring = params.selectedMonthNameOfYearlyRecurring.toInteger()
                List<Integer> list = new ArrayList<Integer>()
                list.add(event.selectedMonthNameOfYearlyRecurring)
                list.add(event.selectedWeekOfYearlyRecurring)
                list.add(event.selectedDayOfYearlyRecurring)
                if(params.recurrenceEndType == DomainConstants.RECURRENCE_END_TYPE.END_BY_DATE) {
                    event.endDateTime = params.endDateTime[1]?.toDate()?.gmt(AppUtil.session.timezone)
                    event.occurrencesOfRecurrence = countRepetition(event.startDateTime, event.endDateTime, DomainConstants.YEARLY_RECURRENCE_TYPE.ON_THE, list, event.recurringYear)
                }else if(params.recurrenceEndType == DomainConstants.RECURRENCE_END_TYPE.END_AFTER_REPETITION) {
                    event.occurrencesOfRecurrence = params.occurrencesOfRecurrence.toInteger()
                    event.endDateTime = getRecurringEndTime(event.startDateTime, DomainConstants.YEARLY_RECURRENCE_TYPE.ON_THE, list, event.occurrencesOfRecurrence, event.recurringYear)
                }else {
                    event.endDateTime = getRecurringEndTime(event.startDateTime, DomainConstants.YEARLY_RECURRENCE_TYPE.ON_THE, list, eventRepetition, event.recurringYear)
                }
            }
        }
        else
        {
            event.startDateTime =  params.startDateTime[0]?.toDate()?.gmt(AppUtil.session.timezone)
            event.endDateTime = params.endDateTime[0]?.toDate()?.gmt(AppUtil.session.timezone)
        }
        event.isTicketPurchaseEnabled = params.isTicketPurchaseEnabled.toBoolean()
        if(params.isTicketPurchaseEnabled.toBoolean()) {
            event.taxProfile = params.taxProfile ? TaxProfile.proxy(params.taxProfile) : null
            event.ticketPrice = params.ticketPrice.toDouble()
            event.maxTicket = params.maxTicket.toInteger()
            event.maxTicketPerCustomer = params.maxTicketPerCustomer? params.maxTicketPerCustomer.toInteger() : null
            event.ticketAvailability = params.ticketAvailability
            if (params.ticketAvailability == 'selected') {
                event.availableToCustomers = []
                event.availableToCustomerGroups = []
                if(params.customer) {
                    event.availableToCustomers = Customer.where {
                        id in params.list("customer").collect { it.toLong() }
                    }.list();
                }
                if(params.customerGroup) {
                    event.availableToCustomerGroups = CustomerGroup.where {
                        id in params.list("customerGroup").collect { it.toLong() }
                    }.list()
                }
            } else {
                event.availableToCustomers = [];
                event.availableToCustomerGroups = [];
            }
        }
        event.isVenueEnabled = params.isVenueEnabled.toBoolean()
        if(event.isVenueEnabled && params.location) {
            VenueLocation location = VenueLocation.proxy(params.location)
            if(!location.sections) {
                throw new ApplicationRuntimeException('no.location.section')
            }
            event.venueLocation = location
        }else if(event.isVenueEnabled && params.oldVenueRemoved) {
            event.venueLocation = null
        }else if(!event.isVenueEnabled) {
            event.generalAddress = params.generalAddress
            event.showGoogleMap = params.showGoogleMap.toBoolean()
            if(params.showGoogleMap.toBoolean()) {
                event.latitude = params.latitude.toDouble()
                event.longitude = params.longitude.toDouble()
            }
        }
        event.isEquipmentEnabled = params.isEquipmentEnabled.toBoolean()
        if(event.isEquipmentEnabled && params.equipment) {
            event.equipment = Equipment.proxy(params.equipment)
        }else if(event.isEquipmentEnabled && params.oldEquipmentRemoved) {
            event.equipment = null
        }
        event.summary = params.summary
        event.description = params.description
        event.metaTags*.delete()
        event.metaTags = [];
        def tag_names = params.list("tag_name");
        def tag_values = params.list("tag_content");
        for (int i = 0; i < tag_names.size(); i++) {
            MetaTag metaTag = new MetaTag(name: tag_names[i], value: tag_values[i]);
            metaTag.save()
            event.metaTags.add(metaTag)
        }
        event.save()
        if(event.isRecurring.toBoolean()) {
            RecurringEvents.findAllByParentEvent(event)*.delete()
            List<Date> list = getRecurringEventDateList(event)
            for(int i = 0; i < list.size(); i++) {
                Map timeMap = getEventStartNEndTime(list.get(i), event.eventStartTime, event.eventEndTime)
                RecurringEvents recurringEvent = new RecurringEvents(start: timeMap.start.gmt(AppUtil.session.timezone), end: timeMap.end.gmt(AppUtil.session.timezone), parentEvent: event)
                recurringEvent.save()
                event.addToEvents(recurringEvent)
            }
        }
        if(!event.hasErrors()){
            return event.id
        } else {
            return null
        }
    }

    boolean saveEventFile(MultipartFile inputFile, String removeFile, Long eventId) {
        GeneralEvent event = GeneralEvent.get(eventId);
        String filePath
        filePath = processFilePath(PathManager.getResourceRoot("general-event/event-${eventId}/personalized"));

        File dir = new File(filePath)

        if(removeFile) {
            dir.traverse {file ->
                file.delete()
                event.file = null
            }
        }
        if(inputFile) {
            if (!dir.exists()) {
                dir.mkdirs()
            }else {
                dir.traverse {file ->
                    file.delete()
                }
            }
            String name = processImageName(filePath, inputFile.originalFilename)
            String originalFilePath = filePath + File.separator + name;
            OutputStream out = new FileOutputStream(originalFilePath);
            InputStream uploadedStream = inputFile.inputStream
            out << uploadedStream;
            out.close();
            uploadedStream.close();

            event.file = name
            event.merge()
            return !event.hasErrors();
        }else {
            return true
        }

    }

    boolean saveEventImages(Long id, List<MultipartFile> images, List removeImgIds) {
        GeneralEvent event = GeneralEvent.get(id);
        List<String> names = removeImgIds.size() > 0 ? GeneralEventImage.where {
            id in removeImgIds
        }.list().name : []

        boolean success = removeEventImages(removeImgIds)

        if(images?.size()) {
            String filePath = processFilePath( PathManager.getResourceRoot("general-event/event-${event?.id}/images") );
            images.each {
                String name = processImageName(filePath, it.originalFilename)
                GeneralEventImage image = new GeneralEventImage(name: name, event: event, idx: getIndexForNewImage(event))
                imageService.uploadImage(it, NamedConstants.IMAGE_RESIZE_TYPE.EVENT_IMAGE, image)
                image.save()
                event.addToImages(image)
            }
        }
        event.save()
        return !event.hasErrors()
    }

    @Transactional
    Boolean removeEventImages(List<Long> ids) {
        boolean success
        if(ids.size() > 0) {
            def criteria = GeneralEventImage.where {
                id in ids
            }
            List<GeneralEventImage> eventImages = criteria.list()
            success = criteria.deleteAll() > 0
            eventImages*.afterDelete()
        } else {
            success = true
        }
        return success
    }

    public String processFilePath(String filePath) {
        File dir = new File(filePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return filePath
    }

    public String processImageName(String filePath, String originalFilename) {
        String name = FilenameUtils.getBaseName(originalFilename);
        String attempt = name;
        Integer tryCount = 0;
        String extension = FilenameUtils.getExtension(originalFilename);
        while(true) {
            File targetFile = new File(filePath, attempt + "." + extension)
            if(!targetFile.exists()) {
                break;
            }
            attempt = name + "_" + (++tryCount);
        }
        return attempt + "." + extension;
    }

    Integer getEventsCount(Map params) {
        return GeneralEvent.createCriteria().count {
            and getEventCriteriaClosure(params)
        }
    }

    List<GeneralEvent> getEvents(Map params) {
        Map listMap = [max: params.max, offset: params.offset]
        return GeneralEvent.createCriteria().list(listMap) {
            and getEventCriteriaClosure(params)
            order(params.sort ?: "name", params.dir ?: "asc");
        }
    }

    private Closure getEventCriteriaClosure(Map params) {
        def session = AppUtil.session;
        return {
            if (params.searchText) {
                ilike("name", "%${params.searchText.trim().encodeAsLikeText()}%");
            }
            if (params.name) {
                ilike("name", "%${params.name.trim().encodeAsLikeText()}%");
            }
            if(params.isPublic != null) {
                eq("isPublic", params.isPublic)
            }
            if(params.startDateTime) {
                Date date = params.startDateTime.dayStart.gmt(session.timezone);
                ge("startDateTime", date)
            }
            if(params.endDateTime) {
                Date date = params.endDateTime.dayEnd.gmt(session.timezone)
                le("endDateTime", date)
            }
            if(params.eventIds != null) {
                if(params.eventIds.size() == 0) {
                    eq("id", 0L)
                } else {
                    inList("id", params.eventIds)
                }
            }
        }
    }

    List getStatusOfEvents(List<GeneralEvent> events) {
        List status = []
        String tempStatus
        events.each { event ->
            tempStatus = isEventCompleted(event) ? "complete" : (isEventActive(event) ? "active" : "unconfirmed")
            status.add(tempStatus)
        }
        return status
    }

    boolean isEventCompleted(GeneralEvent event) {
        Calendar calendar = Calendar.getInstance()
        Date currentTime = calendar.getTime().gmt(AppUtil.session.timezone)
        if(event.isRecurring) {
            if(currentTime > event.endDateTime.gmt(AppUtil.session.timezone)) {
                return true
            }
        }else if(currentTime > event.endDateTime.gmt(AppUtil.session.timezone)) {
            return true
        }
    }

    boolean isEventActive(GeneralEvent event) {
        if(event.isPublic) {
            return true
        }
    }

    Date getRecurringEndTime(Date start, String endType, List<Integer> eventList = null, Integer repetition = eventRepetition, Integer recurringYears = 1) {
        Calendar c = Calendar.getInstance();
        c.setTime(start)
        int count = 0
        if(endType == DomainConstants.DAILY_RECURRENCE_TYPE.EVERYDAY) {
            c.add(Calendar.DATE, repetition - 1)
            return c.getTime()
        }else if(endType == DomainConstants.DAILY_RECURRENCE_TYPE.EVERY) {
            eventList.each {
                if(it < c.get(Calendar.DAY_OF_MONTH))
                    count++
            }
            int months = Math.ceil((repetition + count ) / eventList.size()) - 1
            int endDate = (repetition + count ) % eventList.size()
            c.add(Calendar.MONTH, months)
            if(endDate) {
                c.set(Calendar.DATE, eventList.sort().get(endDate - 1))
            }else {
                c.set(Calendar.DATE, eventList.sort().last())
            }
            return c.getTime()
        }else if(endType == DomainConstants.RECURRENCE_PATTERN.WEEKLY) {
            eventList.each {
                if(it < c.get(Calendar.DAY_OF_WEEK))
                    count++
            }
            int weeks = Math.ceil((repetition + count) / eventList.size()) - 1
            int endDate = (repetition + count ) % eventList.size()
            c.add(Calendar.WEEK_OF_YEAR, weeks)
            if(endDate) {
                c.set(Calendar.DAY_OF_WEEK, eventList.sort().get(endDate - 1))
            }else {
                c.set(Calendar.DAY_OF_WEEK, eventList.sort().last())
            }
            return c.getTime()
        }else if(endType == DomainConstants.MONTHLY_RECURRENCE_TYPE.DAY || endType == DomainConstants.YEARLY_RECURRENCE_TYPE.ON) {
            int month = eventList.get(0)
            int day = eventList.get(1)
            if(month < c.get(Calendar.MONTH) || day < c.get(Calendar.DATE)) {
                count++
            }
            int years = (repetition * recurringYears) + count - 1
            c.add(Calendar.YEAR, years)
            c.set(Calendar.MONTH, month)
            c.set(Calendar.DATE, day)
            return c.getTime()

        }else if(endType == DomainConstants.MONTHLY_RECURRENCE_TYPE.SELECTED || endType == DomainConstants.YEARLY_RECURRENCE_TYPE.ON_THE) {
            int month = eventList.get(0)
            int week = eventList.get(1)
            int day = eventList.get(2)
            if(month < c.get(Calendar.MONTH) || week < c.get(Calendar.WEEK_OF_MONTH) || day < c.get(Calendar.DAY_OF_WEEK)) {
                count++
            }
            int years = (repetition * recurringYears) + count - 1
            c.add(Calendar.YEAR, years)
            c.set(Calendar.MONTH, month)
            c.set(Calendar.WEEK_OF_MONTH, week)
            c.set(Calendar.DAY_OF_WEEK, day)
            return c.getTime()
        }
    }

    Integer countRepetition(Date startDate, Date endDate, String endType, List<Integer> eventList = null, Integer recurringYear = 1) {
        Calendar start = Calendar.getInstance()
        start.setTime(startDate)
        Calendar end = Calendar.getInstance()
        end.setTime(endDate)
        int count = 0
        int repetition = 0
        if(endType == DomainConstants.DAILY_RECURRENCE_TYPE.EVERYDAY) {
            return (end - start) + 1
        }else if(endType == DomainConstants.DAILY_RECURRENCE_TYPE.EVERY) {
            while (true) {
                eventList.each {
                    if(it >= start.get(Calendar.DAY_OF_MONTH))
                        repetition++
                }
                start.add(Calendar.MONTH, 1)
                start.set(Calendar.DAY_OF_MONTH, 1)
                start.clear(Calendar.HOUR)
                if(start > end)
                    break
            }
            eventList.each {
                if(it > end.get(Calendar.DAY_OF_MONTH))
                    repetition--
            }
            return  repetition
        }else if(endType == DomainConstants.RECURRENCE_PATTERN.WEEKLY) {
            while(true) {
                eventList.each {
                    if(it >= start.get(Calendar.DAY_OF_WEEK))
                        repetition++
                }
                start.add(Calendar.WEEK_OF_YEAR, 1)
                start.set(Calendar.DAY_OF_WEEK, 1)
                if(start > end)
                    break
            }
            eventList.each {
                if(it > end.get(Calendar.DAY_OF_WEEK))
                    repetition--
            }
            return  repetition
        }else if(endType == DomainConstants.MONTHLY_RECURRENCE_TYPE.DAY || endType == DomainConstants.YEARLY_RECURRENCE_TYPE.ON) {
            int month = eventList.get(0)
            int day = eventList.get(1)
            while(true) {
                if(month > start.get(Calendar.MONTH) || (month == start.get(Calendar.MONTH) && day >= start.get(Calendar.DATE))) {
                    repetition++
                }
                start.add(Calendar.YEAR, recurringYear)
                start.set(Calendar.MONTH, 0)
                start.set(Calendar.DAY_OF_MONTH, 1)
                if(start > end)
                    break
            }
            if(month > end.get(Calendar.MONTH) || (month == end.get(Calendar.MONTH) && day > end.get(Calendar.DATE))) {
                repetition--
            }
            return repetition
        }else if(endType == DomainConstants.MONTHLY_RECURRENCE_TYPE.SELECTED || endType == DomainConstants.YEARLY_RECURRENCE_TYPE.ON_THE) {
            int month = eventList.get(0)
            int week = eventList.get(1)
            int day = eventList.get(2)
            while(true) {
                if(month > start.get(Calendar.MONTH) || (month == start.get(Calendar.MONTH) && week > start.get(Calendar.WEEK_OF_MONTH)) || (month == start.get(Calendar.MONTH) && week == start.get(Calendar.WEEK_OF_MONTH) && day >= start.get(Calendar.DAY_OF_WEEK))) {
                    repetition++
                }
                start.add(Calendar.YEAR, recurringYear)
                start.set(Calendar.MONTH, 0)
                start.set(Calendar.WEEK_OF_MONTH, 1)
                start.set(Calendar.DAY_OF_WEEK, 1)
                if(start > end)
                    break
            }
            if(month > end.get(Calendar.MONTH) || (month == end.get(Calendar.MONTH) && week > end.get(Calendar.WEEK_OF_MONTH)) || (month == start.get(Calendar.MONTH) && week == start.get(Calendar.WEEK_OF_MONTH) && day > start.get(Calendar.DAY_OF_WEEK))) {
                repetition--
            }
            return repetition
        }
    }

    @Transactional
    boolean deleteEvent(Long id, String at2_reply, String at1_reply){
        TrashUtil.preProcessFinalDelete("general-event", id, at2_reply != null, at1_reply != null)
        AppEventManager.fire("before-general-event-delete", [id])
        GeneralEvent event = GeneralEvent.get(id)
        event.delete()
        AppEventManager.fire("general-event-delete", [id])
        return !event.hasErrors()
    }

    @Transactional
    Integer deleteSelectedEvents(List ids) {
        int removeCount = 0;
        ids.each { id ->
            try {
                GeneralEvent.withNewSession { session ->
                    if(deleteEvent(id, "yes", "yes")) {
                        removeCount++;
                    }
                    session.flush()
                }

            } catch(AttachmentExistanceException att) {
            }
        }
        return removeCount
    }

    ////////////////////////////////////////////// EVENT WIDGET RENDERING SECTION ////////////////////////////////////////////////

    public Map renderEventWidget(Widget widget) {
        def config
        if (widget.params) {
            config = JSON.parse(widget.params)
        } else {
            throw new UnconfiguredWidgetExceptions()
        }
        String view = "/plugins/general_event/widget/event"
        Map model = [widget: widget, config: config]

        GrailsParameterMap _rparams = RequestContextHolder.currentRequestAttributes().params;
        Map widgetParams = [:]
        widgetParams.isPublic = true
        if(config.selectionType != "all") {
            List<Long> eventIds = widget.widgetContent.contentId.collect { it.toLong() }
            widgetParams.eventIds = eventIds
        }
        widgetParams.offset = 0
        widgetParams.max = -1
        widgetParams.startTime = new Date().gmt()
        List<GeneralEvent> events = getEvents(widgetParams)
        List<GeneralEventData> eventDataList = new JSONSerializableList<GeneralEventData>()
        events.each { event ->
            if(event.isRecurring.toBoolean()) {
                event.events.each {
                    eventDataList.add(new GeneralEventData(event, it))
                }
            }else {
                eventDataList.add(new GeneralEventData(event))
            }
        }
        model += [events: events, eventDataList: eventDataList.serialize()]
        view += "/calendarView"
        return [view: view, model: model]
    }

    List<Date> getRecurringEventDateList(GeneralEvent event) {
        int repetition = event.recurrenceEndType == DomainConstants.RECURRENCE_END_TYPE.NO_END_DATE ? eventRepetition : event.occurrencesOfRecurrence
        List<Integer> eventList = new ArrayList<Integer>()
        String endType = null
        Integer recurringYears = 1
        if(event.recurrencePattern == DomainConstants.RECURRENCE_PATTERN.DAILY) {
            endType = event.dailyRecurrenceType == DomainConstants.DAILY_RECURRENCE_TYPE.EVERY ? DomainConstants.DAILY_RECURRENCE_TYPE.EVERY : DomainConstants.DAILY_RECURRENCE_TYPE.EVERYDAY
            eventList = endType == DomainConstants.DAILY_RECURRENCE_TYPE.EVERY ? event.dailyEvents : null
        }else if(event.recurrencePattern == DomainConstants.RECURRENCE_PATTERN.WEEKLY) {
            endType = DomainConstants.RECURRENCE_PATTERN.WEEKLY
            eventList = event.weekDays
        }else if(event.recurrencePattern == DomainConstants.RECURRENCE_PATTERN.MONTHLY) {
            endType = event.monthlyRecurrenceType == DomainConstants.MONTHLY_RECURRENCE_TYPE.DAY ? DomainConstants.MONTHLY_RECURRENCE_TYPE.DAY : DomainConstants.MONTHLY_RECURRENCE_TYPE.SELECTED
            if(endType == DomainConstants.MONTHLY_RECURRENCE_TYPE.DAY){
                eventList.add(event.monthNameOfMonthlyRecurring)
                eventList.add(event.dateOfMonthlyRecurring)
            }else {
                eventList.add(event.selectedMonthNameOfMonthlyRecurring)
                eventList.add(event.selectedWeekOfMonthlyRecurring)
                eventList.add(event.selectedDayOfMonthlyRecurring)
            }
        }else if(event.recurrencePattern == DomainConstants.RECURRENCE_PATTERN.YEARLY) {
            endType = event.yearlyRecurrenceType == DomainConstants.YEARLY_RECURRENCE_TYPE.ON ? DomainConstants.YEARLY_RECURRENCE_TYPE.ON : DomainConstants.YEARLY_RECURRENCE_TYPE.ON_THE
            recurringYears = event.recurringYear
            if(endType == DomainConstants.YEARLY_RECURRENCE_TYPE.ON){
                eventList.add(event.monthNameOfYearlyRecurring)
                eventList.add(event.dateOfYearlyRecurring)
            }else {
                eventList.add(event.selectedMonthNameOfYearlyRecurring)
                eventList.add(event.selectedWeekOfYearlyRecurring)
                eventList.add(event.selectedDayOfYearlyRecurring)
            }
        }
        List<Date> dateList = getEventDates(event.startDateTime, endType, eventList, repetition, recurringYears)
        return dateList
    }

    List<Date> getEventDates(Date start, String endType, List<Integer> eventList = null, int repetition = eventRepetition, Integer recurringYears = 1) {
        List<Date> dateList = new ArrayList<Date>()
        Calendar c = Calendar.getInstance()
        c.setTime(start)
        Boolean flag = true
        if(endType == DomainConstants.DAILY_RECURRENCE_TYPE.EVERY) {
            for(int i = 0; ; i++) {
                eventList.each {
                    if(it < c.get(Calendar.DAY_OF_MONTH) && i == 0) {}
                    else if(flag && it <= c.getActualMaximum(Calendar.DATE)) {
                        c.set(Calendar.DATE, it)
                        dateList.add(c.getTime())
                    }
                    if(dateList.size() == repetition) {
                        flag = false
                    }
                }
                if(!flag)
                    break
                c.add(Calendar.MONTH, 1)
            }
        }else if(endType == DomainConstants.DAILY_RECURRENCE_TYPE.EVERYDAY) {
            for(int i = 0; i < repetition ; i++) {
                dateList.add(c.getTime())
                c.add(Calendar.DATE, 1)
            }
        }else if(endType == DomainConstants.RECURRENCE_PATTERN.WEEKLY) {
            for(int i = 0; ; i++) {
                eventList.each {
                    if(it < c.get(Calendar.DAY_OF_WEEK) && i == 0) {}
                    else if(flag) {
                        c.set(Calendar.DAY_OF_WEEK, it)
                        dateList.add(c.getTime())
                    }
                    if(dateList.size() == repetition) {
                        flag = false
                    }
                }
                if(!flag)
                    break
                c.add(Calendar.WEEK_OF_YEAR, 1)
            }
        }
        else if(endType == DomainConstants.MONTHLY_RECURRENCE_TYPE.DAY || endType == DomainConstants.YEARLY_RECURRENCE_TYPE.ON) {
            int month = eventList.get(0)
            int day = eventList.get(1)
            for(int i = 0; ; i++) {
                if((month < c.get(Calendar.MONTH) || day < c.get(Calendar.DATE)) && i == 0) {}
                else {
                    c.set(Calendar.MONTH, month)
                    c.set(Calendar.DATE, day)
                    dateList.add(c.getTime())
                    if(dateList.size() == repetition) {
                        break
                    }
                }
                c.add(Calendar.YEAR, recurringYears)
            }
        }else if(endType == DomainConstants.MONTHLY_RECURRENCE_TYPE.SELECTED || endType == DomainConstants.YEARLY_RECURRENCE_TYPE.ON_THE) {
            int month = eventList.get(0)
            int week = eventList.get(1)
            int day = eventList.get(2)
            for(int i = 0; ; i++) {
                if((month < c.get(Calendar.MONTH) || week < c.get(Calendar.WEEK_OF_MONTH) || day < c.get(Calendar.DAY_OF_WEEK)) && i == 0) {}
                else {
                    c.set(Calendar.MONTH, month)
                    c.set(Calendar.WEEK_OF_MONTH, week)
                    c.set(Calendar.DAY_OF_WEEK, day)
                    dateList.add(c.getTime())
                    if(dateList.size() == repetition) {
                        break
                    }
                }
                c.add(Calendar.YEAR, recurringYears)
            }
        }
        return dateList
    }

    public static Map getEventStartNEndTime(Date date, String start, String end) {
        Map map = [:]
        Calendar c = Calendar.getInstance()
        c.setTime(date)
        c.clear(Calendar.SECOND)
        if(start.contains('AM')) {
            c.set(Calendar.AM_PM, Calendar.AM)
            c.set(Calendar.HOUR, start.substring(0, start.indexOf(':')).toInteger())
            c.set(Calendar.MINUTE, start.substring(start.indexOf(':') + 1, start.indexOf(' AM')).toInteger())
        }else {
            c.set(Calendar.AM_PM, Calendar.PM)
            c.set(Calendar.HOUR, start.substring(0, start.indexOf(':')).toInteger())
            c.set(Calendar.MINUTE, start.substring(start.indexOf(':') + 1, start.indexOf(' PM')).toInteger())
        }
        map.start = c.getTime()
        if(end.contains('AM')) {
            c.set(Calendar.AM_PM, Calendar.AM)
            c.set(Calendar.HOUR, end.substring(0, end.indexOf(':')).toInteger())
            c.set(Calendar.MINUTE, end.substring(end.indexOf(':') + 1, end.indexOf(' AM')).toInteger())
        }else {
            c.set(Calendar.AM_PM, Calendar.PM)
            c.set(Calendar.HOUR, end.substring(0, end.indexOf(':')).toInteger())
            c.set(Calendar.MINUTE, end.substring(end.indexOf(':') + 1, end.indexOf(' PM')).toInteger())
        }
        map.end = c.getTime()
        return map
    }

    public Collection<GeneralEvent> getEventsForWidgetContent(List<Long> contentIds) {
        Collection<GeneralEvent> events = GeneralEvent.createCriteria().list {
            and getEventCriteriaClosure([eventIds: contentIds])
        }
        return events
    }

    public String ticketPriceWithCurrency(GeneralEvent event) {
        return AppUtil.session.currency?.symbol?:AppUtil.baseCurrency.symbol + " " + (getLowestTicketPrice(event) == getHighestTicketPrice(event) ?
                getLowestTicketPrice(event).toCurrency().toPrice() : getLowestTicketPrice(event).toCurrency().toPrice() + "-" + getHighestTicketPrice(event).toCurrency().toPrice())
    }

    public Double getLowestTicketPrice(GeneralEvent event) {
        Double price = 0d
        price = event.ticketPrice ?: price
        return price
    }

    public Double getHighestTicketPrice(GeneralEvent event) {
        Double price = 0d
        price = event.ticketPrice ?: price
        return price
    }

    /////////////////////////////////////////// EQUIPMENT SECTION ////////////////////////////////////////////////////////

    Integer getEquipmentsCount(Map params) {
        return Equipment.createCriteria().count {
            and getEquipmentCriteriaClosure(params)
        }
    }

    List<Equipment> getEquipments (Map params) {
        Map listMap = [max: params.max, offset: params.offset]
        return Equipment.createCriteria().list(listMap) {
            and getEquipmentCriteriaClosure(params)
            order(params.sort ?: "name", params.dir ?: "asc");
        }
    }

    private getEquipmentCriteriaClosure(Map params) {
        def session = AppUtil.session
        Long admin = session.admin
        return {
            if (params.searchText) {
                ilike("name", "%${params.searchText.trim().encodeAsLikeText()}%");
            }
            if (params.name) {
                ilike("name", "%${params.name.trim().encodeAsLikeText()}%")
            }
            if(params.type) {
                eq("type.id", params.type.toLong())
            }
        }
    }

    Boolean saveEquipment(Map params) {
        Long id = params.id? params.id.toLong() : null;
        Equipment equipment = id? Equipment.get(id) : new Equipment();
        equipment.name = params.name;
        equipment.description = params.description;
        equipment.save();
        return !equipment.hasErrors();
    }

    @Transactional
    boolean deleteEquipment(Long id, String at2_reply, String at1_reply) {
        TrashUtil.preProcessFinalDelete("general-equipment", id, at2_reply != null, at1_reply != null)
        AppEventManager.fire("before-general-equipment-delete", [id])
        Equipment equipment = Equipment.get(id)
        equipment.delete()
        AppEventManager.fire("general-equipment-delete", [id])
        return !equipment.hasErrors()
    }

    @Transactional
    def deleteSelectedEquipments(List ids) {
        int removeCount = 0;
        ids.each { id ->
            try {
                if(deleteEquipment(id, "yes", "yes")) {
                    removeCount++;
                }
            } catch(AttachmentExistanceException att) {
            }
        }
        return removeCount
    }

    //////////////////////////////////////////////////// VENUE SECTION /////////////////////////////////////////////////

    private Boolean checkVenueUrlForConflict(Long id, String url){
        return Venue.createCriteria().count{
            if(id != 0){
                ne("id", id)
            }
            eq("url", url)
        } > 0
    }

    Long createVenue(Map params) {
        Long venueId = 0;
        if(!commonService.isUnique(Venue, [field: "name", value: params.name])) {
            throw new ApplicationRuntimeException("profile.name.already.exists")
        }
        Venue venue = new Venue(name: params.name);
        venue.save();
        if(!venue.hasErrors()) {
            venueId = venue.id;
        }
        return venueId
    }

    Long saveVenue(GrailsParameterMap params) {
        Venue venue
        venue = params.id ? Venue.get(params.id.toLong()) : new Venue();
        venue.name = params.name
        venue.siteUrl = params.siteUrl
        venue.url = (params.id && !checkVenueUrlForConflict(params.id.toLong(), venue.url)) ? venue.url : commonService.getUrlForDomain(venue);
        venue.generalAddress = params.generalAddress
        venue.showGoogleMap = params.showGoogleMap.toBoolean()
        if(venue.showGoogleMap) {
            venue.latitude = params.latitude.toDouble()
            venue.longitude = params.longitude.toDouble()
        }
        List locationIds = params.list("venueLocations");
        venue.locations = locationIds.collect {VenueLocation.proxy(it)}
        venue.save()
        if(!venue.hasErrors()) {
            if(params.id) {
                AppEventManager.fire("venue-update", [params.id])
            }
            return venue.id
        }
        return 0;
    }

    @Transactional
    boolean deleteVenue(Long id, String at2_reply, String at1_reply) {
        TrashUtil.preProcessFinalDelete("venue", id, at2_reply != null, at1_reply != null)
        AppEventManager.fire("before-venue-delete", [id])
        Venue venue = Venue.get(id)
        venue.delete()
        AppEventManager.fire("venue-delete", [id])
        return !venue.hasErrors()
    }

    @Transactional
    Long saveVenueLocation (Map params) {
        VenueLocation location = params.id ? VenueLocation.get(params.id.toLong()) : new VenueLocation()
        location.name = params.name
        location.url = (params.id && !checkVenueUrlForConflict(params.id.toLong(), location.url)) ? location.url : commonService.getUrlForDomain(location);
        location.description = params.description
        location.venue = Venue.proxy(params.long("venueId"))
        location.save()
        if( !location.hasErrors()) {
            return location.id
        }else {
            return null
        }
    }

    @Transactional
    boolean saveLocationImages (Long id, List<MultipartFile> images, List removeImgIds) {
        VenueLocation location = VenueLocation.get(id)
        List<String> names = removeImgIds.size() > 0 ? VenueLocationImage.where {
            id in removeImgIds
        }.list().name : []

        boolean success = removeVenueLocationImages(removeImgIds)

        if(images) {
            String filePath = processFilePath( PathManager.getResourceRoot("general-event-venue-location/location-${location?.id}") );
            images.each {
                String name = processImageName(filePath, it.originalFilename)
                VenueLocationImage image = new VenueLocationImage(name: name, venueLocation: location, idx: getIndexForNewImage(location))
                imageService.uploadImage(it, NamedConstants.IMAGE_RESIZE_TYPE.LOCATION_IMAGE, image)
                image.save()
                location.addToImages(image)
            }
        }
        location.save()
        return !location.hasErrors()
    }

    @Transactional
    Boolean removeVenueLocationImages(List<Long> ids) {
        boolean success
        if(ids.size() > 0) {
            def criteria = VenueLocationImage.where {
                id in ids
            }
            List<VenueLocationImage> venueLocationImages = criteria.list()
            success = criteria.deleteAll() > 0
            venueLocationImages*.afterDelete()
        } else {
            success = true
        }
        return success
    }

    @Transactional
    def deleteVenueLocation(Long id, String at2_reply, String at1_reply) {
        TrashUtil.preProcessFinalDelete("venue-location", id, at2_reply != null, at1_reply != null)
        AppEventManager.fire("before-venue-location-delete",[id])
        VenueLocation location = VenueLocation.get(id)
        location.delete()
        AppEventManager.fire("venue-location-delete", [id])
        if(!location.hasErrors()) {
            File resDir = new File(PathManager.getResourceRoot("general-event-venue-location/location-${location?.id}"))
            if (resDir.exists()) {
                resDir.deleteDir()
            }
        }
        return !location.hasErrors()
    }

    @Transactional
    boolean saveVenueLocationSection (Map params) {
        VenueLocationSection section = params.id ? VenueLocationSection.get(params.id.toLong()) : new VenueLocationSection()

        section.name = params.name
        section.ticketPrice = params.ticketPrice.toDouble()
        section.ticketName = params.ticketName

        section.rowCount = params.rowCount ? params.rowCount.toInteger(0) : section.rowCount
        section.columnCount = params.columnCount ? params.columnCount.toInteger(0) : section.columnCount

        section.rowPrefixType = params.rowPrefixType ?: section.rowPrefixType
        section.rowPrefixOrder = params.rowPrefixOrder ?: section.rowPrefixOrder
        section.rowPrefixStartsAt = params.rowPrefixStartsAt ?: section.rowPrefixStartsAt
        section.columnPrefixType = params.columnPrefixType ?: section.columnPrefixType
        section.columnPrefixOrder = params.columnPrefixOrder ?: section.columnPrefixOrder
        section.columnPrefixStartsAt = params.columnPrefixStartsAt ?: section.columnPrefixStartsAt

        section.rowAccessBetween = params.rowAccessBetween ? params.rowAccessBetween.toInteger(0) : section.rowAccessBetween
        section.columnAccessBetween = params.columnAccessBetween ? params.columnAccessBetween.toInteger(0) : section.columnAccessBetween

        section.venueLocation = VenueLocation.proxy(params.long("locationId") ?: 0)

        section.save()
        return !section.hasErrors()
    }

    @Transactional
    def deleteVenueLocationSection (Long id, String at2_reply, String at1_reply) {
        TrashUtil.preProcessFinalDelete("venue-location-section", id, at2_reply != null, at1_reply != null)
        AppEventManager.fire("before-venue-location-section-delete", [id])
        VenueLocationSection section = VenueLocationSection.get(id)
        section.delete()
        AppEventManager.fire("venue-location-section-delete",[id])
        return !section.hasErrors()
    }

    //////////////////////////////////////////// VENUE TICKET ADD TO CART SECTION //////////////////////////////////////

    private int alphabetToDecimal(String target) {
        int res = 0;
        char[] numChar = new StringBuilder(target).reverse().toString().toCharArray();
        for(int i = numChar.length - 1; i >= 0; i--) {
            res += Math.pow(26, i) * (numChar[i] - ('A' as char) + 1);
        }
        return res;
    }

    public String decimalToAlphabet(int target) {
        StringBuilder res = new StringBuilder();
        while(target > 0) {
            target--;
            int rem = target % 26;
            res.insert(0, (char)(rem + ('A' as char)));
            target = (target - rem) / 26;
        }
        return res.toString();
    }

    public Integer getStartRowNumber(VenueLocationSection section) {
        return section.rowPrefixType == "alphabetic" ? alphabetToDecimal(section.rowPrefixStartsAt) : section.rowPrefixStartsAt.toInteger()
    }

    public Integer getStartColumnNumber(VenueLocationSection section) {
        return section.columnPrefixType == "alphabetic" ? alphabetToDecimal(section.columnPrefixStartsAt) : section.columnPrefixStartsAt.toInteger()
    }

    Long getNumberOfTicketPurchased(VenueLocationSection section) {
        return TicketInventoryAdjustment.createCriteria().count {
            eq "section", section
        }
    }

    TreeSet<Integer> getAllPurchasedTicket(VenueLocationSection section, def event) {
        List<Integer> allPurchased = TicketInventoryAdjustment.createCriteria().list {
            eq("section.id", section.id)
            if(event instanceof GeneralEvent) {
                eq("generalEvent.id", event.id)
            }else {
                eq("recurringEvent.id", event.id)
            }
        }
        Set tickets = new TreeSet<Integer>();
        allPurchased.each {
            tickets.addAll(it.seatNumber)
        }
        return tickets
    }

    synchronized
    public TreeSet getLockedTickets(VenueLocationSection section, def event) {
        Set withCarts = new TreeSet<Integer>();
        withCarts = getAllPurchasedTicket(section, event)
        Set inCarts = VenueTicketCartManager.seatsInCart["$section.id#${event?.id}"]
        if(inCarts) {
            withCarts.addAll(inCarts)
        }
        return withCarts
    }

    boolean isTicketAvailable(Integer seat, def event, VenueLocationSection section) {
        Boolean purchased = getAllPurchasedTicket(section, event).contains(seat)
        if(purchased) {
            return false
        }
        Set inCarts = VenueTicketCartManager.seatsInCart["$section.id#${event.id}"]
        if(inCarts) {
            return !inCarts.contains(seat)
        }
        return true
    }

    public List<String> seatNumberToName(VenueLocationSection section, List<Integer> seatNumbers) {
        Collections.sort(seatNumbers)
        int columnCount = section.columnCount
        List convertedString = []
        int lastStackRow = -1
        int lastStackColumn = -1
        boolean consecutive = false
        Integer rowNumber = getStartRowNumber(section)
        Integer columnNumber = getStartColumnNumber(section)
        boolean rowAsc = section.rowPrefixOrder == 'ascending'
        boolean columnAsc = section.columnPrefixOrder == 'ascending'
        boolean rowAlpha = section.rowPrefixType == 'alphabetic'
        boolean columnAlpha = section.columnPrefixType == 'alphabetic'
        int rowIndexMultiplier = rowAsc ? 1 : -1
        int columnIndexMultiplier = columnAsc ? 1 : -1
        seatNumbers.each { seat ->
            Integer row = Math.floor((seat - 1)/columnCount)
            Integer column = seat - row * columnCount - 1
            if(lastStackRow == row && lastStackColumn + 1 == column) {
                lastStackColumn = column
                consecutive = true
            } else {
                if(consecutive) {
                    int rowActualNumber = rowNumber + rowIndexMultiplier * lastStackRow
                    int columnActualNumber = columnNumber + columnIndexMultiplier * lastStackColumn
                    String name = (rowAlpha ? decimalToAlphabet(rowActualNumber) : "" + rowActualNumber) + (columnAlpha ? decimalToAlphabet(columnActualNumber) : columnActualNumber)
                    convertedString[convertedString.size() - 1] += "-" + name
                    consecutive = false
                }
                int rowActualNumber = rowNumber + rowIndexMultiplier * row
                int columnActualNumber = columnNumber + columnIndexMultiplier * column
                String name = (rowAlpha ? decimalToAlphabet(rowActualNumber) : "" + rowActualNumber) + (columnAlpha ? decimalToAlphabet(columnActualNumber) : columnActualNumber)
                convertedString.add(name)
                lastStackRow = row
                lastStackColumn = column
            }
        }
        if(consecutive) {
            int seat = seatNumbers[seatNumbers.size() - 1]
            Integer row = Math.floor((seat - 1)/columnCount)
            Integer column = seat - row * columnCount - 1
            int rowActualNumber = rowNumber + rowIndexMultiplier * row
            int columnActualNumber = columnNumber + columnIndexMultiplier * column
            String name = (rowAlpha ? decimalToAlphabet(rowActualNumber) : "" + rowActualNumber) + (columnAlpha ? decimalToAlphabet(columnActualNumber) : columnActualNumber)
            convertedString[convertedString.size() - 1] += "-" + name
        }
        return convertedString
    }

    /////////////////////////////////////////////// CUSTOM FIELD SECTION ///////////////////////////////////////////////

    @Transactional
    Boolean saveCustomField(Map params) {
        GeneralEventCheckoutField field;
        if(params.id) {
            field = GeneralEventCheckoutField.get(params.id);
            field.options.clear();
        }else {
            field = new GeneralEventCheckoutField();
        }
        DataBindingUtils.bindObjectToInstance(field, params, null, ["id", "options"], null);
        params.list("options").each { option ->
            if(option) { //preventing addition of empty string
                field.options.add(option)
            }
        }
        field.save();
        return !field.hasErrors();
    }

    Boolean deleteCustomField(Long id) {
        GeneralEventCheckoutField field = GeneralEventCheckoutField.get(id);
        field.delete();
        return !field.hasErrors();
    }

    Boolean saveCustomFieldTitle(Map params) {
        def entity = GeneralEvent.proxy(params.eventId);
        def title = GeneralEventCheckoutFieldTitle.findByEvent(entity);
        if(title && !params.title) {
            title.delete()
        } else if(params.title) {
            if(!title) {
                title = new GeneralEventCheckoutFieldTitle();
                title["event"] = entity
            }
            title.title = params.title
            title.save()
        } else {
            return true
        }
        return !title.hasErrors()
    }

    def getFieldsOrTitle(Long eventId, Boolean getTitle = false) {
        if(getTitle) {
            return GeneralEventCheckoutFieldTitle.findByEvent(GeneralEvent.get(eventId))?.title;
        }else {
            List<GeneralEventCheckoutField> fields = GeneralEventCheckoutField.createCriteria().list {
                eq("event.id", eventId)
            }
            return [fields: fields.unique {a, b -> a.name <=> b.name}]
        }
    }

    Integer getIndexForNewImage(GeneralEvent event) {
        def idx = GeneralEventImage.createCriteria().list {
            projections {
                max("idx")
            }
            eq("event", event)
        }
        return idx[0] != null ? idx[0] + 1 : 1
    }

    Integer getIndexForNewImage(VenueLocation location) {
        def idx = VenueLocationImage.createCriteria().list {
            projections {
                max("idx")
            }
            eq("venueLocation", location)
        }
        return idx[0] != null ? idx[0] + 1 : 1
    }
}
