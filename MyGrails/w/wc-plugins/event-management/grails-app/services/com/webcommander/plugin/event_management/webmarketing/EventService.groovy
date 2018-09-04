package com.webcommander.plugin.event_management.webmarketing

import com.webcommander.JSONSerializableList
import com.webcommander.admin.Operator
import com.webcommander.admin.RoleService
import com.webcommander.annotations.Initializable
import com.webcommander.common.CommanderMailService
import com.webcommander.common.CommonService
import com.webcommander.common.ImageService
import com.webcommander.common.MetaTag
import com.webcommander.constants.NamedConstants
import com.webcommander.events.AppEventManager
import com.webcommander.manager.HookManager
import com.webcommander.manager.PathManager
import com.webcommander.models.RestrictionPolicy
import com.webcommander.plugin.event_management.*
import com.webcommander.plugin.event_management.manager.CartTicketManager
import com.webcommander.plugin.event_management.model.CartEventTicket
import com.webcommander.plugin.event_management.model.EventData
import com.webcommander.tenant.Thread
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.throwables.AttachmentExistanceException
import com.webcommander.throwables.UnconfiguredWidgetExceptions
import com.webcommander.util.AppUtil
import com.webcommander.util.StringUtil
import com.webcommander.util.TrashUtil
import com.webcommander.webcommerce.Order
import com.webcommander.webcommerce.TaxProfile
import com.webcommander.widget.Widget
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.util.Holders
import grails.web.servlet.mvc.GrailsParameterMap
import org.apache.commons.io.FilenameUtils
import org.hibernate.SessionFactory
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.multipart.MultipartFile

import javax.servlet.http.HttpSession
import java.util.regex.Matcher
import java.util.regex.Pattern

@Initializable
class EventService {
    SessionFactory sessionFactory
    CommonService commonService
    CommanderMailService commanderMailService
    ImageService imageService
    RoleService roleService

    static {
        AppEventManager.on "paid-for-cart", { carts ->
            carts.each { cart ->
                cart.cartItemList.each { item ->
                    if(item.object instanceof CartEventTicket) {
                        Thread.start {
                            AppUtil.initialDummyRequest()
                            Order.withNewSession {
                                CartEventTicket cartTicket = item.object
                                VenueLocationSection section = VenueLocationSection.get(cartTicket.section)
                                Event event = Event.proxy(cartTicket.event)
                                EventSession eventSession = EventSession.proxy(cartTicket.session)
                                String groupId = StringUtil.uuid
                                cartTicket.seats.each { number ->
                                    EventTicket ticket = new EventTicket(isHonorable: false)
                                    ticket.orderRef = cart.orderId
                                    ticket.ticketNumber = groupId
                                    ticket.seatNumber = number
                                    ticket.event = event
                                    ticket.session = eventSession
                                    ticket.section = section
                                    ticket.purchased = new Date().gmt()
                                    ticket.save()
                                }
                                Order order = Order.get(cart.orderId);
                                EventService service = Holders.applicationContext.getBean(EventService)
                                service.sendNewPurchaseTicketEmail(event, section, order.billing.email, eventSession, cartTicket.seats)
                            }
                        }
                    }
                }
            }
        }

        AppEventManager.on("payment-pending-for-cart", { carts ->
            carts.each { cart ->
                cart.cartItemList.each { item ->
                    if(item.object instanceof CartEventTicket) {
                        CartEventTicket cartTicket = item.object
                        VenueLocationSection section = VenueLocationSection.get(cartTicket.section)
                        Event event = Event.proxy(cartTicket.event)
                        EventSession eventSession = EventSession.proxy(cartTicket.session)
                        String groupId = StringUtil.uuid
                        cartTicket.seats.each { number ->
                            EventTicket ticket = new EventTicket(isHonorable: false)
                            ticket.orderRef = cart.orderId
                            ticket.ticketNumber = groupId
                            ticket.seatNumber = number
                            ticket.event = event
                            ticket.session = eventSession
                            ticket.section = section
                            ticket.purchased = new Date().gmt()
                            ticket.isReserved = true;
                            ticket.save()
                        }
                    }
                }
            }

        });

        AppEventManager.on('paid-for-order', { Order order ->
            Thread.start {
                AppUtil.initialDummyRequest()
                List<EventTicket> eventTickets = EventTicket.findAllByOrderRef(order.id)
                eventTickets.each {
                    it.isReserved = false
                    it.merge();
                }
                if(eventTickets.size()) {
                    EventTicket eventTicket = eventTickets[0];
                    Order.withNewSession {
                        EventService service = Holders.applicationContext.getBean(EventService)
                        service.sendNewPurchaseTicketEmail(eventTicket.event, eventTicket.section, order.billing.email, eventTicket.session, eventTickets.seatNumber.toArray())
                    }
                }
            }

        })

        HookManager.register("beforeManageUserPermission beforeSaveUserPermission", { Map response, Map params ->
            if(params.type == "event") {
                Long admin = AppUtil.session.admin
                response.deniedPolicy = new RestrictionPolicy(type: "event", permission: "edit.permission")
                response.allowed = RoleService.getInstance().isPermitted(admin, response.deniedPolicy, params)
            }
            return response
        })
    }

    static void initialize() {
        HookManager.register("equipment-type-delete-veto") {response, id ->
            List<Equipment> equipments = Equipment.createCriteria().list {
                eq("type.id", id)
            }
            if(equipments) {
                response.equipments = equipments.size()
            }
            return response
        }
        HookManager.register("equipmentType-delete-veto-list") { response, id ->
            List<Equipment> equipments = Equipment.createCriteria().list {
                eq("type.id", id)
            }
            if(equipments) {
                response.equipments = equipments.collect {it.name}
            }
            return response
        }

        AppEventManager.on("before-event-delete", {id ->
            EquipmentInvitation.createCriteria().list {
                eq("event.id", id)
            }.each {
                AppEventManager.fire("before-equipment-invitation-delete", [it.id])
                it.delete()
                AppEventManager.fire("equipment-invitation-delete", [it.id])
            }
        })
        AppEventManager.on("before-event-session-delete", {id ->
            EquipmentInvitation.createCriteria().list {
                eq("eventSession.id", id)
            }.each {
                AppEventManager.fire("before-equipment-invitation-delete", [it.id])
                it.delete()
                AppEventManager.fire("equipment-invitation-delete", [it.id])
            }
        })
        HookManager.register("equipment-delete-veto") { response, id ->
            int invitationCount = EquipmentInvitation.createCriteria().count {
                eq("equipment.id", id)
                ne("status", "rejected")
                not {
                    eq("status", "rejected")
                }
            }
            if(invitationCount) {
                response.invitations = invitationCount
            }
            return response
        }
        HookManager.register("equipment-delete-veto-list") { response, id ->
            List<EquipmentInvitation> invitationList = Equipment.get(id).invitation
            if(invitationList) {
                int approved = 0;
                int pending = 0;
                int rejected = 0;
                for(int i = 0; i < invitationList.size(); i++) {
                    if(invitationList[i].status == "approved") {
                        approved += 1;
                    }
                    else if(invitationList[i].status == "rejected") {
                        rejected += 1;
                    }
                    else {
                        pending += 1;
                    }
                }
                response."invitations" = ["Total ${invitationList.size() > 1?'Invitations:':'Invitation:'} ${invitationList.size()}\n Approved: ${approved}\n Rejected: ${rejected}\n Pending: ${pending}"]
            }
            return response
        }

        HookManager.register("venue-delete-veto") { response, id ->
            int eventCount = Event.createCriteria().count {
                venueLocation {
                    eq("venue.id", id)
                }
            }
            if(eventCount) {
                response.events = eventCount
            }
            return response
        }
        HookManager.register("venue-delete-veto-list") { response, id ->
            List<Event> eventList = Event.createCriteria().list {
                venueLocation {
                    eq("venue.id", id)
                }
            }
            if(eventList.size()) {
                response.events = eventList.collect { it.name }
            }
            return response
        }
        HookManager.register("taxProfile-delete-at2-count") { response, id ->
            int eventCount = Event.where {
                taxProfile.id == id
            }.count()
            if(eventCount) {
                response.events = eventCount
            }
            return response;
        }
        HookManager.register("taxProfile-delete-at2-list") { response, id ->
            List events = Event.createCriteria().list {
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
            Event.where {
                taxProfile == profile
            }.updateAll([taxProfile: null])
        });

        HookManager.register("venue-delete-veto") { response, id ->
            int sessionCount = EventSession.createCriteria().count{
                venueLocation {
                    eq("venue.id", id)
                }
            }
            if(sessionCount) {
                response.sessions = sessionCount
            }
            return response
        }
        HookManager.register("venue-delete-veto-list") { response, id ->
            List<Event> sessionList = EventSession.createCriteria().list {
                venueLocation {
                    eq("venue.id", id)
                }
            }
            if(sessionList.size()) {
                response.sessions = sessionList.collect { it.name }
            }
            return response
        }
        AppEventManager.on("before-event-delete", {id ->
            EventSession.createCriteria().list {
                eq("event.id", id)
            }.each {
                AppEventManager.fire("before-event-session-delete", [it.id])
                it.delete()
                AppEventManager.fire("event-session-delete", [it.id])
            }
        })


        HookManager.register("event-session-delete-veto") { response, id ->
            EventSession session = EventSession.get(id)
            int topicCount = EventSessionTopic.createCriteria().count {
                or {
                    eq("eventSession.id", id)
                }
            }
            if(topicCount) {
                response.topics = topicCount
            }
            return response
        }
        HookManager.register("eventSession-delete-veto-list") { response, id ->
            List<EventSessionTopic> sessionTopicList = EventSessionTopic.createCriteria().list {
                eq("eventSession.id", id)
            }
            if(sessionTopicList) {
                response.topics = sessionTopicList.collect {it.name}
            }
        }

        HookManager.register("venue-location-section-delete-veto") { response, id ->
            int ticketCount = EventTicket.createCriteria().count {
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
                ticketList += EventTicket.createCriteria().count() {
                    eq("section.id", sectionList[i].id)
                }
            }
            if(ticketList) {
                response."tickets" = ["Total Tickets : ${ticketList}"]
            }
            return response
        }
        HookManager.register("event-delete-veto") { response, id ->
            Event event = Event.get(id)
            int ticketCount = EventTicket.createCriteria().count {
                or {
                    eq("event.id", id)
                    if(event.eventSessions) {
                        inList("session.id", event.eventSessions.id)
                    }
                }
            }
            if(ticketCount) {
                response.tickets = ticketCount
            }
            return response
        }
        HookManager.register("event-session-delete-veto") { response, id ->
            int ticketCount = EventTicket.createCriteria().count {
                or {
                    eq("session.id", id)
                }
            }
            if(ticketCount) {
                response.tickets = ticketCount
            }
            return response
        }
        AppEventManager.on("order-cancelled", { orderId ->
            List eventTickets = EventTicket.findAllByOrderRef(orderId);
            eventTickets*.delete();
        })
        HookManager.register("populate-summary-n-images") { response, product ->
            EventTicket ticket = EventTicket.load(product.productId)
            if(ticket) {
                Event event = ticket.event
                if(event) {
                    product.summary = event.summary
                    if(event.images) {
                        product.image = 'resources/event/event-' + event.id + "/images/100-" + event.images[0].name;
                    }
                }
            }
            if(!product.image) {
                product.image = 'resources/event/default/100-default.png'
            }
            product.name = product.name + " - " + product.variation
        }
    }

    private getCriteriaClosureForVenueLocation(Map params) {
        def session = AppUtil.session
        Long admin = session.admin
        return {
            if(!roleService.isPermitted(admin, new RestrictionPolicy(type: "event_location", permission: "view.others"), params)){
                eq("organiser.id", admin)
            }
            if (params.searchText) {
                ilike("name", "%${params.searchText.trim().encodeAsLikeText()}%")
            }
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
            if(params.hasSession != null) {
                if(params.hasSession) {
                    sizeNe("eventSessions", 0)
                } else {
                    sizeEq("eventSessions", 0)
                }
            }
            if(params.startTime) {
                Date date = params.startTime.dayStart.gmt(session.timezone);
                ge("startTime", date)
            }
            if(params.endTime) {
                Date date = params.endTime.dayEnd.gmt(session.timezone)
                le("endTime", date)
            }
            if(params.ids) {
                inList("id", params.list("ids").collect {it.toLong()})
            }
            if(params.eventIds != null) {
                if(params.eventIds.size() == 0) {
                    eq("id", 0L)
                } else {
                    inList("id", params.eventIds)
                }
            }
            if(params.locationIds != null) {
                if(params.locationIds.size() == 0) {
                    eq("id", 0L)
                } else {
                    inList("venueLocation.id", params.locationIds)
                }
            } else if(params.hasVenueLocation) {
                or {
                    isNotNull("venueLocation")
                    sqlRestriction("exists (select esn.id from event_session esn inner join venue_location vln on esn.venue_location_id = vln.id where esn.event_id = this_.id)")
                }
            }
        }
    }

    private Closure getEventSessionCriteriaClosure(Map params) {
        return {
            if (params.searchText) {
                ilike("name", "%${params.searchText.trim().encodeAsLikeText()}%");
            }
            if (params.name) {
                ilike("name", "%${params.name.trim().encodeAsLikeText()}%");
            }
            if(params.isPublic) {
                event {
                    eq("isPublic", params.isPublic)
                }
            }
            if(params.startTime || params.endTime) {
                if(params.startTime) {
                    Date date = params.startTime
                    ge("startTime", date)
                }
                if(params.endTime) {
                    Date date = params.endTime
                    le("endTime", date)
                }
            }
            if(params.eventId) {
                eq("event.id", params["eventId"].toLong())
            }
            if(params.sessionIds) {
                if(params.sessionIds.size() == 0) {
                    inList("id", [0L])
                } else {
                    inList("id", params.sessionIds)
                }
            }
            if(params.eventIds) {
                if(params.eventIds.size() == 0) {
                    inList("event.id", [0L])
                } else {
                    inList("event.id", params.eventIds)
                }
            }
            if(params.locationIds) {
                if(params.locationIds.size() == 0) {
                    inList("venueLocation.id", [0L])
                } else {
                    inList("venueLocation.id", params.locationIds)
                }
            } else if(params.hasVenueLocation) {
                or {
                    isNotNull("venueLocation")
                    sqlRestriction("exists (select esn.id from event_session esn inner join venue_location vln on esn.venue_location_id = vln.id where esn.event_id = this_.id)")
                }
            }
        }
    }

    private Closure getSessionTopicCriteriaClosure(Map params) {
        return {
            if (params.searchText) {
                ilike("name", "%${params.searchText.trim().encodeAsLikeText()}%");
            }
            if (params.name) {
                ilike("name", "%${params.name.trim().encodeAsLikeText()}%");
            }
            if(params.sessionId) {
                eq("eventSession.id", params.sessionId.toLong())
            }
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
            if(!roleService.isPermitted(admin, new RestrictionPolicy(type: "event_equipment", permission: "view.others"), params)) {
                eq("organiser.id", admin)
            }
        }
    }

    private getVenueLocationSectionCriteriaClosure(Map params) {
        return {
            if (params.searchText) {
                ilike("name", "%${params.searchText.trim().encodeAsLikeText()}%")
            }
        }
    }

    private getEquipmentTypeCriteriaClosure(Map params) {
        return {
            if (params.searchText) {
                ilike("name", "%${params.searchText.trim().encodeAsLikeText()}%")
            }
        }
    }

    private getVenueCriteriaClosure(Map params) {
        def session = AppUtil.session
        Long admin = session.admin
        return {
            if (params.searchText) {
                ilike("name", "%${params.searchText.trim().encodeAsLikeText()}%");
            }
            if (params.name) {
                ilike("name", "%${params.name.trim().encodeAsLikeText()}%")
            }
            if(!roleService.isPermitted(admin, new RestrictionPolicy(type: "event_venue", permission: "view.others"), params)){
                or {
                    eq("manager.id", admin)
                }
            }
        }
    }

    Integer getVenueTicketCount(Map params,  Event event, EventSession session) {
        return EventTicket.createCriteria().get {
            projections {
                countDistinct("ticketNumber")
            }
            if(session) {
                eq("session", session)
            } else {
                eq("event", event)
            }
        }
    }

    List getVenueTickets(Map params, Event event, EventSession session) {
        List<String> ticketNumber = EventTicket.createCriteria().list(max: params.max, offset: params.offset) {
            if(session) {
                eq("session", session)
            } else {
                eq("event", event)
            }
            groupProperty("ticketNumber")
        }
        if(ticketNumber.size()) {
            def ticketRows = EventTicket.createCriteria().list {
                inList("ticketNumber", ticketNumber)
            }
            def groupedRows = [:]
            ticketRows.each {
                String hash = it.ticketNumber + "#" + it.section.id
                if(!groupedRows[hash]) {
                    groupedRows[hash] = [ticketNumber: it.ticketNumber, seats: [], purchased: it.purchased, section: it.section, isHonorable: it.isHonorable, isReserved: it.isReserved, orderRef: it.orderRef]
                }
                groupedRows[hash].seats.add(it.seatNumber)
            }
            return groupedRows.collect {
                List<String> seatName = seatNumberToName(it.value.section, it.value.seats)
                String key = it.key
                [ticketNumber: key.substring(0, key.length() - 2), purchased: it.value.purchased, section: it.value.section, seats: seatName, isHonorable: it.value.isHonorable, isReserved: it.value.isReserved, orderRef: it.value.orderRef]
            }
        }
        return []
    }

    Integer getEventsCount(Map params) {
        return Event.createCriteria().count {
            and getEventCriteriaClosure(params)
        }
    }

    Integer getEquipmentsCount(Map params) {
        return Equipment.createCriteria().count {
            and getEquipmentCriteriaClosure(params)
        }
    }

    Integer getEquipmentTypesCount(Map params) {
        return EquipmentType.createCriteria().count {
            and getEquipmentTypeCriteriaClosure(params)
        }
    }

    List<EquipmentType> getEquipmentTypes(Map params) {
        Map listMap = [max: params.max, offset: params.offset]
        return EquipmentType.createCriteria().list(listMap) {
            and getEquipmentTypeCriteriaClosure(params)
            order(params.sort ?: "name", params.dir ?: "asc");
        }
    }

    Integer getVenuesCount(Map params) {
        return Venue.createCriteria().count {
            and getVenueCriteriaClosure(params)
        }
    }

    Integer getVenueLocationsCount(Map params, Long venueId) {
        return VenueLocation.createCriteria().count {
            eq("venue.id", venueId)
            and getCriteriaClosureForVenueLocation(params)
        }
    }

    Integer getVenueLocationSectionsCount (Map params, Long locationId) {
        return VenueLocationSection.createCriteria().count {
            eq("venueLocation.id", locationId)
            and getVenueLocationSectionCriteriaClosure(params)
        }
    }

    List<Event> getEvents(Map params) {
        Map listMap = [max: params.max, offset: params.offset]
        return Event.createCriteria().list(listMap) {
            and getEventCriteriaClosure(params)
            order(params.sort ?: "name", params.dir ?: "asc");
        }
    }

    boolean isEventSessionCompleted(EventSession session) {
        Calendar calendar = Calendar.getInstance()
        Date currentTime = calendar.getTime().gmt(AppUtil.session.timezone)
        if(currentTime > session.endTime.gmt(AppUtil.session.timezone)) {
            return true
        }
    }

    boolean isEventSessionActive(EventSession session) {
        if(session.venueLocation && session.equipment) {
            return true
        }
    }

    boolean isEventCompleted(Event event) {
        Calendar calendar = Calendar.getInstance()
        Date currentTime = calendar.getTime().gmt(AppUtil.session.timezone)
        if(currentTime > event.endTime.gmt(AppUtil.session.timezone)) {
            return true
        }
    }

    boolean isEventActive(Event event) {
        if(event.venueLocation && event.equipment && event.isPublic) {
            return true
        }
    }

    List getStatusOfEvents(List<Event> events) {
        List status = []
        List sessionStatus = []
        String tempStatus
        events.each { event ->
            if(event.eventSessions.size()) {
                event.eventSessions.each { session->
                    tempStatus = isEventSessionCompleted(session) ? "complete" : (isEventSessionActive(session) ? "active" : "unconfirmed")
                    sessionStatus.add(tempStatus)
                }
                if("unconfirmed" in sessionStatus) {
                    status.add("unconfirmed")
                } else if ("active" in sessionStatus) {
                    status.add("active")
                } else {
                    status.add("complete")
                }
            } else {
                tempStatus = isEventCompleted(event) ? "complete" : (isEventActive(event) ? "active" : "unconfirmed")
                status.add(tempStatus)
            }
        }
        return status
    }

    boolean hasEquipmentBooking(Event event) {
        Integer count = EquipmentInvitation.createCriteria().count {
            eq("event", event)
            eq("status", "approved")
        }
        if(count > 0) {return true}
    }

    boolean hasVenueBooking(Event event) {
        Integer count = VenueLocationInvitation.createCriteria().count {
            eq("event", event)
            eq("status", "approved")
        }
        if(count > 0) {return true}
    }

    List getStatusOfManageSessionEntry (List events) {
        List status = []
        events.each { event->
            if(hasVenueBooking(event) || hasEquipmentBooking(event)) {
                status.add("disable-session")
            } else {
                status.add("")
            }
        }
        return status
    }

    boolean isTicketPurchasedForEvent(Event event) {
        Integer count = EventTicket.createCriteria().count {
            eq("event", event)
        }
        if(count > 0) {return true}
    }

    List getDeleteStatusForEvent (List events) {
        List status = []
        events.each { event->
            if(event.eventSessions.size()) {
                status.add("disable-delete")
            } else {
                if(isTicketPurchasedForEvent(event)) {
                    status.add("disable-delete")
                }else {
                    status.add("")
                }
            }
        }
        return status
    }

    boolean isTicketPurchesedForEventSession(EventSession session) {
        Integer count = EventTicket.createCriteria().count {
            eq("session", session)
        }
        if(count > 0) {return true}
    }

    List getDeleteStatusForEventSession (List sessions) {
        List status = []
        sessions.each { session->
            if(isTicketPurchesedForEventSession(session)) {
                status.add("disable-delete")
            }else {
                status.add("")
            }
        }
        return status
    }

    public Collection<Event> getEventsForWidgetContent(List<Long> contentIds) {
        Collection<Event> events = Event.createCriteria().list {
            and getEventCriteriaClosure([eventIds: contentIds])
        }
        return events
    }

    List<Equipment> getEquipments (Map params) {
        Map listMap = [max: params.max, offset: params.offset]
        return Equipment.createCriteria().list(listMap) {
            and getEquipmentCriteriaClosure(params)
            order(params.sort ?: "name", params.dir ?: "asc");
        }
    }

    List<Venue> getVenues (Map params) {
        Map listMap = [max: params.max, offset: params.offset]
        return Venue.createCriteria().list(listMap) {
            and getVenueCriteriaClosure(params)
            order(params.sort ?: "name", params.dir ?: "asc");
        }
    }

    List<VenueLocation> getVenueLocations (Map params, Long venueId) {
        Map listMap = [max: params.max, offset: params.offset]
        return VenueLocation.createCriteria().list(listMap) {
            eq("venue.id", venueId)
            and getCriteriaClosureForVenueLocation(params)
            order(params.sort ?: "name", params.dir ?: "asc");
        }
    }

    List<VenueLocationSection> getVenueLocationSections (Map params, Long locationId) {
        Map listMap = [max: params.max, offset: params.offset]
        VenueLocation venueLocation = VenueLocation.proxy(locationId)
        return VenueLocationSection.createCriteria().list(listMap) {
            eq("venueLocation", venueLocation)
            and getVenueLocationSectionCriteriaClosure(params)
            order(params.sort ?: "name", params.dir ?: "asc");
        }
    }

    @Transactional
    def saveEvent(Map params) {
        Long id = params.id ? params.id.toLong(0) : null;
        Event event = id ? Event.get(id) : new Event()
        def session = AppUtil.session;
        event.createdBy = Operator.proxy(session.admin)
        event.name = params.name
        event.heading = params.heading
        event.title = params.title
        event.disableGooglePageTracking = params.disableTracking.toBoolean(false)
        event.organiser = Operator.proxy(params.long("organiser"))
        event.isPublic = params.isPublic.toBoolean()
        event.isPurchasable = params.isPurchasable.toBoolean()
        if(event.isPublic && event.isPurchasable) {
            event.taxProfile = params.taxProfile ? TaxProfile.proxy(params.taxProfile) : null
        } else {
            event.taxProfile = null
        }
        event.summary = params.summary
        if(params.description) {
            event.description = params.description
        }
        if(params.startTime) {
            event.startTime =  params.startTime?.toDate()?.gmt(AppUtil.session.timezone)
        }
        if(params.endTime) {
            event.endTime = params.endTime?.toDate()?.gmt(AppUtil.session.timezone)
        }
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
        if(!event.hasErrors()){
            return event.id
        } else {
            return null
        }
    }

    @Transactional
    boolean saveEquipmentType(Map params) {
        Long id = params.id ? params.id.toLong(0) : null;
        EquipmentType type = id ? EquipmentType.get(id) : new EquipmentType()
        type.name = params.name
        type.description = params.description
        type.save()
        return !type.hasErrors()
    }

    @Transactional
    boolean saveEquipment (Map params) {
        Long id = params.id ? params.id.toLong(0) : null;
        Equipment equipment = id ? Equipment.get(id) : new Equipment()
        equipment.type = EquipmentType.proxy(params.type.toLong())
        equipment.name = params.name
        equipment.autoAccept= params.autoAccept.toBoolean()
        equipment.organiser = Operator.proxy(params.long("organiser"))
        equipment.description = params.description
        equipment.save()
        return !equipment.hasErrors()
    }

    private Boolean checkVenueUrlForConflict(Long id, String url){
        return Venue.createCriteria().count{
            if(id != 0){
                ne("id", id)
            }
            eq("url", url)
        } > 0
    }

    @Transactional
    boolean saveVenue (Map params) {
        Long id = params.id ? params.id.toLong(0) : null;
        Venue venue = id ? Venue.get(id) : new Venue()
        venue.name = params.name
        venue.url = (id && !checkVenueUrlForConflict(id, venue.url)) ? venue.url : commonService.getUrlForDomain(venue);
        venue.description = params.description
        venue.manager = Operator.proxy(params.long("manager"))
        venue.address = params.address
        venue.siteUrl = params.siteUrl
        venue.latitude = params.latitude.toDouble(null)
        venue.longitude = params.longitude.toDouble(null)
        venue.showGoogleMap = params.showGoogleMap.toBoolean()

        venue.save()
        return !venue.hasErrors()
    }

    @Transactional
    Long saveVenueLocation (Map params) {
        Long id = params.id ? params.id.toLong(0) : null;
        VenueLocation location = id ? VenueLocation.get(id) : new VenueLocation()

        location.name = params.name
        location.url = (id && !checkVenueUrlForConflict(id, location.url)) ? location.url : commonService.getUrlForDomain(location);
        location.description = params.description
        location.venue = Venue.proxy(params.long("venueId"))
        location.organiser = Operator.proxy(params.long("organiser"))

        location.save()
        if( !location.hasErrors()) {
            return location.id
        }else {
            return null
        }
    }

    @Transactional
    boolean saveVenueLocationSection (Map params) {
        Long id = params.id ? params.id.toLong(0) : null;
        VenueLocationSection section = id ? VenueLocationSection.get(id) : new VenueLocationSection()

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
    boolean deleteEvent(Long id, String at2_reply, String at1_reply){
        TrashUtil.preProcessFinalDelete("event", id, at2_reply != null, at1_reply != null)
        AppEventManager.fire("before-event-delete", [id])
        Event event = Event.get(id)
        event.delete()
        AppEventManager.fire("event-delete", [id])
        return !event.hasErrors()
    }

    @Transactional
    Integer deleteSelectedEvents(List ids) {
        int removeCount = 0;
        ids.each { id ->
            try {
                Event.withNewSession { session ->
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

    @Transactional
    boolean deleteEventSession (Long id, String at2_reply, String at1_reply){
        TrashUtil.preProcessFinalDelete("event-session", id, at2_reply != null, at1_reply != null)
        AppEventManager.fire("before-event-session-delete", [id])
        EventSession session = EventSession.get(id)
        session.delete()
        AppEventManager.fire("event-session-delete", [id])
        return !session.hasErrors()
    }

    @Transactional
    Integer deleteSelectedEventSessions(List ids) {
        int removeCount = 0;
        ids.each { id ->
            try {
                if(deleteEventSession(id, "yes", "yes")) {
                    removeCount++;
                }
            } catch(AttachmentExistanceException att) {
            }
        }
        return removeCount
    }

    @Transactional
    boolean deleteEquipmentType(Long id, String at2_reply, String at1_reply) {
        TrashUtil.preProcessFinalDelete("equipment-type", id, at2_reply != null, at1_reply != null)
        AppEventManager.fire("before-equipment-type-delete", [id])
        EquipmentType type = EquipmentType.get(id)
        type.delete()
        AppEventManager.fire("equipment-type-delete", [id])
        return !type.hasErrors()
    }

    @Transactional
    Integer deleteSelectedEquipmentTypes(List ids) {
        int removeCount = 0;
        ids.each { id ->
            try {
                EquipmentType.withNewSession { session ->
                    if(deleteEquipmentType(id, "yes", "yes")) {
                        removeCount++;
                    }
                    session.flush()
                }
            } catch(AttachmentExistanceException att) {
            }
        }
        return removeCount
    }

    @Transactional
    boolean deleteEquipment(Long id, String at2_reply, String at1_reply){
        TrashUtil.preProcessFinalDelete("equipment", id, at2_reply != null, at1_reply != null)
        AppEventManager.fire("before-equipment-delete", [id])
        Equipment equipment = Equipment.get(id)
        equipment.delete()
        AppEventManager.fire("equipment-delete", [id])
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

    @Transactional
    boolean saveEventImages(Long id, List<MultipartFile> images, List removeImgIds) {
        Event event = Event.get(id);
        List<String> names = removeImgIds.size() > 0 ? EventImage.where {
            id in removeImgIds
        }.list().name : []

        boolean success = removeEventImages(removeImgIds)
        if(images?.size()) {
            String filePath = processFilePath( PathManager.getResourceRoot("event/event-${event?.id}/images") );
            images.each {
                String name = processImageName(filePath, it.originalFilename)
                EventImage image = new EventImage(name: name, event: event, idx: getIndexForNewImage(event))
                imageService.uploadImage(it, NamedConstants.IMAGE_RESIZE_TYPE.EVENT_IMAGE, image)
                image.save()
                event.addToImages(image)
            }
        }
        event.merge()
        return !event.hasErrors()
    }

    @Transactional
    Boolean removeEventImages(List<Long> ids) {
        boolean success
        if(ids.size() > 0) {
            def criteria = EventImage.where {
                id in ids
            }
            List<EventImage> eventImages = criteria.list()
            success = criteria.deleteAll() > 0
            eventImages*.afterDelete()
        } else {
            success = true
        }
        return success
    }

    @Transactional
    boolean saveEventFile(MultipartFile inputFile, String removeFile, Long eventId, Long sessionId) {
        Event event = Event.get(eventId);
        EventSession eventSession = sessionId ? EventSession.get(sessionId) : new EventSession() ;
        String filePath
        if(eventId) {
            filePath = processFilePath( PathManager.getResourceRoot("event/event-${eventId}/personalized"));
        }else {
            filePath = processFilePath( PathManager.getResourceRoot("event/event-session-${sessionId}/personalized"));
        }
        File dir = new File(filePath)

        if(removeFile) {
            dir.traverse {file ->
                file.delete()
                if(event){event.file = null}else {eventSession.file = null}
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

            if(eventId) {
                event.file = name
                event.merge()
                return !event.hasErrors()
            } else {
                eventSession.file = name
                eventSession.save()
                return !eventSession.hasErrors()
            }
        }else {
            return true
        }

    }

    @Transactional
    boolean saveLocationImages (Long id, List<MultipartFile> images, List removeImgIds) {
        VenueLocation location = VenueLocation.get(id);
        List<String> names = removeImgIds.size() > 0 ? VenueLocationImage.where {
            id in removeImgIds
        }.list().name : [];

        boolean success
        if (removeImgIds.size() > 0) {
            success = VenueLocationImage.where {
                id in removeImgIds
            }.deleteAll() > 0
            if (success) {
                def namePrefix = ["", "100-", "300-"]
                names.each { name ->
                    namePrefix.each {
                        File imgFile = new File(PathManager.getResourceRoot("venue-location/location-${location?.id}/${it + name}"))
                        imgFile.delete();
                    }
                }
            }
        }
        if(images) {
            String filePath = processFilePath( PathManager.getResourceRoot("venue-location/location-${location?.id}") );
            images.each {
                String name = processImageName(filePath, it.originalFilename)
                VenueLocationImage image = new VenueLocationImage(name: name, venueLocation: location)
                imageService.uploadImage(it, NamedConstants.IMAGE_RESIZE_TYPE.LOCATION_IMAGE, image)
                image.save()
                location.addToImages(image)
            }
        }
        location.merge()
        return !location.hasErrors()
    }

    List getCapacityOfLocations(List<VenueLocation> locations) {
        List capacity = []
        locations.eachWithIndex {location, i ->
            Long sum = 0
            location.sections.each {
                sum +=  it.rowCount * it.columnCount
            }
            capacity[i] = (sum)
        }
        return capacity
    }

    List getCapacityOfVenues(List<Venue> venues) {
        List capacity = []
        venues.eachWithIndex {venue, i->
            List capacityOfLocations = getCapacityOfLocations(venue.locations.toList())
            Long sum = 0
            capacityOfLocations.each { cap ->
                sum  += cap
            }
            capacity[i] = sum
        }
        return capacity
    }

    @Transactional
    def approveVenueLocationInvitation(VenueLocationInvitation invitation) {
        if(invitation.event) {
            if(invitation.event.venueLocation) {return "booked"}
        } else {
            if(invitation.eventSession.venueLocation) {return "booked"}
        }
        invitation.status = "approved"
        invitation.merge()

        if(!invitation.hasErrors()) {
            if(invitation.event) {
                invitation.event.venueLocation = invitation.location
            } else {
                invitation.eventSession.venueLocation = invitation.location
            }
            try {
                sendLocationInvitationApprovedEmail(invitation, invitation.event, invitation.eventSession)
            } catch (Exception e) {
                return "mailSentError"
            }
            sessionFactory.cache.evictQueryRegions()
            return true
        } else {
            return false
        }
    }

    boolean hasPurchasedTicket(Long invitationId) {
        boolean hasPurchasedTicket = false
        VenueLocationInvitation invitation = VenueLocationInvitation.get(invitationId)
        List<VenueLocationSection> sections = invitation.location.sections
        sections.each {
            if(it.ticket) {
                hasPurchasedTicket = true
                return true
            }
        }
        return hasPurchasedTicket
    }

    @Transactional
    boolean rejectVenueLocationInvitation(Long id) {
        VenueLocationInvitation invitation = VenueLocationInvitation.get(id)
        def prevStatus = invitation.status
        invitation.status = "rejected"
        invitation.merge()
        if(!invitation.hasErrors()) {
            if(prevStatus == "approved") {
                if(invitation.event) {
                    invitation.event.venueLocation = null
                }else {
                    invitation.eventSession.venueLocation = null
                }
            }
            return true
        } else {
            return false
        }
    }

    @Transactional
    def approveEquipmentInvitation(EquipmentInvitation invitation) {
        if(invitation.event) {
            if(invitation.event.equipment) {return "booked"}
        }else {
            if(invitation.eventSession.equipment) {return "booked"}
        }
        invitation.status = "approved"
        invitation.merge()
        if(!invitation.hasErrors()) {
            if(invitation.event) {
                invitation.event.equipment = invitation.equipment
            }else {
                invitation.eventSession.equipment = invitation.equipment
            }
            try {
                sendEquipmentInvitationApprovedEmail(invitation, invitation.event, invitation.eventSession)
            } catch (Exception e) {
                return "mailSentError"
            }
            return true
        } else {
            return false
        }
    }

    @Transactional
    boolean rejectEquipmentInvitation(Long id) {
        EquipmentInvitation invitation = EquipmentInvitation.get(id)
        String prevStatus = invitation.status
        invitation.status = "rejected"
        invitation.merge()
        if(!invitation.hasErrors()) {
            if(prevStatus == "approved") {
                if(invitation.event) {
                    invitation.event.equipment = null
                }else {
                    invitation.eventSession.equipment = null
                }
            }
            return true
        } else {
            return false
        }
    }

    def sendEquipmentInvitationApprovedEmail( EquipmentInvitation invitation, Event event, EventSession session) {
        Map macrosAndTemplate = commanderMailService.getMacrosAndTemplateByIdentifier("equipment-invitation-approved")
        if(!macrosAndTemplate.emailTemplate.active) {
            return;
        }
        Map refinedMacros = macrosAndTemplate.commonMacros
        macrosAndTemplate.macros.each {
            switch (it.key.toString()) {
                case "event_name":
                    refinedMacros[it.key] = event ? event.name : session.name
                    break;
                case "start_date_of_event":
                    refinedMacros[it.key] = event ? event.startTime.toEmailFormat() : session.startTime.toEmailFormat()
                    break;
                case "end_date_of_event":
                    refinedMacros[it.key] = event ? event.endTime.toEmailFormat() : session.endTime.toEmailFormat()
                    break;
                case "event_organiser_name" :
                    refinedMacros[it.key] = event ? event.organiser.fullName : session.event.organiser.fullName;
                    break;
            }
        }
        String recipient = event ? event.organiser.email : session.event.organiser.email
        Thread.start {
            commanderMailService.sendMail(macrosAndTemplate.emailTemplate, macrosAndTemplate.activeHtml, macrosAndTemplate.activeText, refinedMacros, recipient)
        }
    }

    def sendLocationInvitationApprovedEmail( VenueLocationInvitation invitation, Event event, EventSession session) {
        Map macrosAndTemplate = commanderMailService.getMacrosAndTemplateByIdentifier("location-invitation-approved")
        if(!macrosAndTemplate.emailTemplate.active) {
            return;
        }
        Map refinedMacros = macrosAndTemplate.commonMacros
        macrosAndTemplate.macros.each {
            switch (it.key.toString()) {
                case "event_name":
                    refinedMacros[it.key] = event ? event.name : session.name
                    break;
                case "start_date_of_event":
                    refinedMacros[it.key] = event ? event.startTime.toEmailFormat() : session.startTime.toEmailFormat()
                    break;
                case "end_date_of_event":
                    refinedMacros[it.key] = event ? event.endTime.toEmailFormat() : session.endTime.toEmailFormat()
                    break;
                case "venue_organiser_name":
                    refinedMacros[it.key] = event ? event.venueLocation.organiser.fullName : session.venueLocation.organiser.fullName
                    break;
                case "event_organiser_name" :
                    refinedMacros[it.key] = event ? event.organiser.fullName : session.event.organiser.fullName;
                    break;
                case "venue_location_name":
                    refinedMacros[it.key] = invitation.location.venue.name;
                    break;
            }
        }
        String recipient = event ? event.organiser.email : session.event.organiser.email
        Thread.start {
            commanderMailService.sendMail(macrosAndTemplate.emailTemplate, macrosAndTemplate.activeHtml, macrosAndTemplate.activeText, refinedMacros, recipient)
        }

    }

    @Transactional
    String saveVenueBooking(Long eventId, Long sessionId, Long locationId) {
        Event event = Event.get(eventId)
        EventSession session = EventSession.get(sessionId)
        VenueLocationInvitation invitation

        VenueLocation location = VenueLocation.get(locationId)
        if(!location.sections.size()) {
            return "no_section"
        }
        boolean conflicted = false
        location.venueLocationInvitation.find { inv ->
            if(inv.status != "rejected") {
                if(event) {
                    if(inv.event) {
                        if( (event.startTime >= inv.event.startTime && event.startTime <= inv.event.endTime) || (event.endTime >= inv.event.startTime && event.endTime <= inv.event.endTime)){
                            conflicted = true
                            return true
                        }
                    }else {
                        if( (event.startTime >= inv.eventSession.startTime && event.startTime <= inv.eventSession.endTime)
                                || (event.endTime >= inv.eventSession.startTime && event.endTime <= inv.eventSession.endTime)){
                            conflicted = true
                            return true
                        }
                    }
                }else {
                    if(inv.event) {
                        if( (session.startTime >= inv.event.startTime && session.startTime <= inv.event.endTime) || (session.endTime >= inv.event.startTime && session.endTime <= inv.event.endTime)){
                            conflicted = true
                            return true
                        }
                    } else {
                        if( (session.startTime >= inv.eventSession.startTime && session.startTime <= inv.eventSession.endTime)
                                || (session.endTime >= inv.eventSession.startTime && session.endTime <= inv.eventSession.endTime)){
                            conflicted = true
                            return true
                        }
                    }
                }
            }
        }
        if(conflicted){
            return "conflicted"
        } else {
            if(event){
                invitation = new VenueLocationInvitation(status: "pending", event: event, eventSession: null, location: location);
            }else {
                invitation = new VenueLocationInvitation(status: "pending", event: null, eventSession: session, location: location);
            }
            invitation.save()
            location.addToVenueLocationInvitation(invitation)
            location.merge();

            if(!location.hasErrors()) {
                try {
                    sendLocationInvitationMail(location, event, session)
                } catch (Exception e) {
                    return "mailSentError"
                }
                return "true"
            }else {
                return "false"
            }
        }
    }

    @Transactional
    String saveEquipmentBooking(Long eventId, Long sessionId, Long equipmentId) {
        Event event = Event.get(eventId)
        EventSession session = EventSession.get(sessionId)
        EquipmentInvitation invitation
        Equipment equipment = Equipment.get(equipmentId)
        Boolean conflicted = false

        if(equipment.autoAccept){
            List equipmentInvitation = EquipmentInvitation.list()
            equipmentInvitation.each { inv ->
                if(inv.equipment.id == equipmentId) {
                    if(event) {
                        if(inv.event) {
                            if( (event.startTime >= inv.event.startTime && event.startTime <= inv.event.endTime) || (event.endTime >= inv.event.startTime && event.endTime <= inv.event.endTime)){
                                conflicted = true
                                return true
                            }
                        }else {
                            if( (event.startTime >= inv.eventSession.startTime && event.startTime <= inv.eventSession.endTime)
                                    || (event.endTime >= inv.eventSession.startTime && event.endTime <= inv.eventSession.endTime)){
                                conflicted = true
                                return true
                            }
                        }
                    }else {
                        if(inv.event) {
                            if( (session.startTime >= inv.event.startTime && session.startTime <= inv.event.endTime) || (session.endTime >= inv.event.startTime && session.endTime <= inv.event.endTime)){
                                conflicted = true
                                return true
                            }
                        }else {
                            if( (session.startTime >= inv.eventSession.startTime && session.startTime <= inv.eventSession.endTime)
                                    || (session.endTime >= inv.eventSession.startTime && session.endTime <= inv.eventSession.endTime)){
                                conflicted = true
                                return true
                            }
                        }
                    }
                }
            }
        }
        if(conflicted) {
            return "conflicted"
        } else {
            if(event){
                invitation = new EquipmentInvitation(status: "pending", event: event, eventSession: null, equipment: equipment);
            }else {
                invitation = new EquipmentInvitation(status: "pending", event: null, eventSession: session, equipment: equipment);
            }
            invitation.save()
            equipment.addToInvitation(invitation)
            equipment.merge();
            if(!equipment.hasErrors()) {
                if(equipment.autoAccept) {
                    invitation.status = "approved"
                    if(event) {
                        event.equipment = equipment
                    } else {
                        session.equipment = equipment
                    }
                }
                try {
                    sendEquipmentInvitationMail(equipment, event, session)
                } catch (e) {
                    return "mailSentError"
                }
                return "true"
            }else {
                return "false"
            }
        }
    }

    def sendEquipmentInvitationMail(Equipment equipment, Event event, EventSession session) {
        Map macrosAndTemplate = commanderMailService.getMacrosAndTemplateByIdentifier("new-equipment-invitation-request")
        if(!macrosAndTemplate.emailTemplate.active) {
            return;
        }
        Map refinedMacros = macrosAndTemplate.commonMacros
        macrosAndTemplate.macros.each {
            switch (it.key.toString()) {
                case "equipment_name" :
                    refinedMacros[it.key] = equipment.name;
                    break;
                case "equipment_organiser_name" :
                    refinedMacros[it.key] = equipment.organiser.fullName;
                    break;
                case "event_name" :
                    refinedMacros[it.key] = event ? event.name : session.name;
                    break;
                case "event_organiser_name" :
                    refinedMacros[it.key] = event ? event.organiser.fullName : session.event.organiser.fullName;
                    break;
                case "start_date_of_event" :
                    refinedMacros[it.key] = event ? event.startTime.toEmailFormat() : session.startTime.toEmailFormat();
                    break;
            }
        }
        String recipient = equipment.organiser.email
        Thread.start {
            commanderMailService.sendMail(macrosAndTemplate.emailTemplate, macrosAndTemplate.activeHtml, macrosAndTemplate.activeText, refinedMacros, recipient)
        }
    }

    def sendLocationInvitationMail(VenueLocation location, Event event, EventSession session) {
        Map macrosAndTemplate = commanderMailService.getMacrosAndTemplateByIdentifier("new-venue-location-invitation")
        if(!macrosAndTemplate.emailTemplate.active) {
            return;
        }
        Map refinedMacros = macrosAndTemplate.commonMacros
        macrosAndTemplate.macros.each {
            switch (it.key.toString()) {
                case "organiser" :
                    refinedMacros[it.key] = location.organiser.fullName;
                    break;
                case "venue_name" :
                    refinedMacros[it.key] = location.venue.name
                    break;
                case "venue_location_name" :
                    refinedMacros[it.key] = location.name
                    break;
                case "venue_organiser_name" :
                    refinedMacros[it.key] = location.venue.manager.fullName
                    break;
                case "event_name":
                    refinedMacros[it.key] = event ? event.name : session.name
                    break;
                case "start_date_of_event":
                    refinedMacros[it.key] = event ? event.startTime.toEmailFormat() : session.startTime.toEmailFormat()
                    break;
                case "end_date_of_event":
                    refinedMacros[it.key] = event ? event.endTime.toEmailFormat() : session.endTime.toEmailFormat()
                    break;
                case "event_organiser_name":
                    refinedMacros[it.key] = event ? event.organiser.fullName : session.event.organiser.fullName
                    break;
            }
        }
        String recipient = location.organiser.email
        Thread.start {
            commanderMailService.sendMail(macrosAndTemplate.emailTemplate, macrosAndTemplate.activeHtml, macrosAndTemplate.activeText, refinedMacros, recipient)
        }
    }

    boolean isTicketAvailable(Integer seat, Long event, Long session, Long section) {
        Boolean purchased = EventTicket.createCriteria().count {
            eq("seatNumber", seat)
            eq("section.id", section)
            if(session) {
                eq("session.id", session)
            } else {
                eq("event.id", event)
            }
        } > 0
        if(purchased) {
            return false
        }
        Set inCarts = CartTicketManager.seatsInCart["$section#${session}#${event}"]
        if(inCarts) {
            return !inCarts.contains(seat)
        }
        return true
    }

    @Transactional
    def saveComplementaryTicket(GrailsParameterMap params) {
        VenueLocationSection section = VenueLocationSection.get(params.section.toLong())
        def numbers = new TreeSet()
        def allSeats = params.list("seat")
        def add = { number ->
            if(number == -1) {
                throw new ApplicationRuntimeException("invalid.seat.provided")
            }
            if(isTicketAvailable(number, params.eventId.toLong(null), params.sessionId.toLong(null), params.section.toLong())) {
                numbers.add(number)
            } else {
                throw new ApplicationRuntimeException("unavailable.seat.provided")
            }
        }
        allSeats.each { seat ->
            def seats
            if(seat.indexOf("-") > -1) {
                seats = seat.split "-"
                def number1 = seatNameToNumber(section, seats[0])
                def number2 = seatNameToNumber(section, seats[1])
                for(number in number1..number2) {
                    add(number)
                }
            } else {
                def number = seatNameToNumber(section, seat)
                add(number)
            }
        }

        Event event = params.eventId ? Event.proxy(params.eventId) : null
        EventSession session = params.sessionId ? EventSession.proxy(params.sessionId) : null
        String groupId = StringUtil.uuid
        numbers.each { number ->
            EventTicket ticket = new EventTicket(isHonorable: true)
            ticket.ticketNumber = groupId
            ticket.seatNumber = number
            ticket.event = event
            ticket.session = session
            ticket.section = section
            ticket.purchased = new Date().gmt()
            ticket.save()
        }
        if(params.sendMail.toBoolean()) {
            sendComplementaryTicketEmail(event, section, params.email, session, numbers)
        }

    }

    def sendComplementaryTicketEmail(Event event, VenueLocationSection section, String recipient, EventSession session, def numbers) {
        Map macrosAndTemplate = commanderMailService.getMacrosAndTemplateByIdentifier("new-complementary-ticket")
        if(!macrosAndTemplate.emailTemplate.active) {
            return;
        }
        Map refinedMacros = macrosAndTemplate.commonMacros
        macrosAndTemplate.macros.each {
            switch (it.key.toString()) {
                case "event_name":
                    refinedMacros[it.key] = event ? event.name.encodeAsBMHTML() : session.name.encodeAsBMHTML()
                    break;
                case "event_organiser_name":
                    refinedMacros[it.key] = event ? event.organiser.fullName : session.event.organiser.fullName
                    break;
                case "section_name":
                    refinedMacros[it.key] = section.name.encodeAsBMHTML()
                    break;
                case "venue_location_name":
                    refinedMacros[it.key] = section.venueLocation.name.encodeAsBMHTML()
                    break;
                case "venue_name" :
                    refinedMacros[it.key] = section.venueLocation.venue.name.encodeAsBMHTML()
                    break;
                case "ticket_number" :
                    refinedMacros[it.key] = seatNumberToName(section, numbers.toList()).join(",");
                    break;
                case "event_date" :
                    refinedMacros[it.key] = session ? session.startTime.toEmailFormat() : event.startTime.toEmailFormat();
                    break;
            }
        }
        commanderMailService.sendMail(macrosAndTemplate.emailTemplate, macrosAndTemplate.activeHtml, macrosAndTemplate.activeText, refinedMacros, recipient)
    }

    @Transactional
    def voidComplementaryTicket(Map params) {
        List tickets = EventTicket.findAllByTicketNumber(params.ticketNumber)
        tickets*.delete()
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

    @Transactional
    def deleteSelectedVenueLocationSections(List ids) {
        int removeCount = 0;
        ids.each { id ->
            try {
                if(deleteVenueLocationSection(id, "yes", "yes")) {
                    removeCount++;
                }
            } catch(AttachmentExistanceException att) {
            }
        }
        return removeCount
    }

    Long countLocationSection(Long sectionId) {
        VenueLocationSection section = VenueLocationSection.get(sectionId)
        return VenueLocationSection.createCriteria().count {
            eq("venueLocation", section.venueLocation)
        }
    }

    boolean hasAnyApprovedInvitation(Long sectionId) {
        boolean hasAnyApprovedInvitation = false
        VenueLocationSection section = VenueLocationSection.get(sectionId)
        for(VenueLocationInvitation invitation : section.venueLocation.venueLocationInvitation) {
            if(invitation.status == "approved") {
                hasAnyApprovedInvitation = true
                break
            }
        }
        return hasAnyApprovedInvitation
    }

    @Transactional
    def deleteVenueLocation(Long id, String at2_reply, String at1_reply) {
       TrashUtil.preProcessFinalDelete("venue-location", id, at2_reply != null, at1_reply != null)
       AppEventManager.fire("before-venue-location-delete",[id])
       VenueLocation location = VenueLocation.get(id)
       location.delete()
       AppEventManager.fire("venue-location-deleted", [id])
       if(!location.hasErrors()) {
           File resDir = new File(PathManager.getResourceRoot("venue-location/location-${location?.id}"))
           if (resDir.exists()) {
               resDir.deleteDir()
           }
       }
       return !location.hasErrors()
    }

    @Transactional
    def deleteSelectedVenueLocations(List ids) {
        int removeCount = 0;
        ids.each { id ->
            try {
                if(deleteVenueLocation(id, "yes", "yes")) {
                    removeCount++;
                }
            } catch(AttachmentExistanceException att) {
            }
        }
        return removeCount
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
    Integer deleteSelectedVenues(List ids) {
        int removeCount = 0;
        ids.each { id ->
            try {
                Venue.withNewSession { session ->
                    if(deleteVenue(id, "yes", "yes")) {
                        removeCount++;
                    }
                    session.flush()
                }
            } catch(AttachmentExistanceException att) {
            }
        }
        return removeCount
    }

    @Transactional
    public Long saveSession(Long id, Long eventId, Map params) {
        HttpSession session = AppUtil.session
        Event event = Event.get(eventId)

        EventSession eventSession = id != 0 ? EventSession.get(id) : new EventSession()
        eventSession.name = params.name
        eventSession.startTime = params.startTime.toDate().gmt(session.timezone)
        eventSession.endTime = params.endTime.toDate().gmt(session.timezone)
        eventSession.description = params.description
        if(id == 0) {
            eventSession.event = event
            eventSession.createdBy = Operator.get(session.admin)
            eventSession.save()
        } else {
            eventSession.merge()
        }
        if(!eventSession.hasErrors()) {
            return eventSession.id
        }
        return null
    }

    public Long getEventSessionCount(Map params) {
        return EventSession.createCriteria().count {
            and getEventSessionCriteriaClosure(params)
        }
    }

    public Collection<EventSession> getEventSessions(Map params) {
        Map listMap = [max: params.max, offset: params.offset]
        return EventSession.createCriteria().list(listMap) {
            and getEventSessionCriteriaClosure(params)
            order(params.sort ?: "name", params.dir ?: "asc");
        }
    }

    @Transactional
    public boolean saveTopic(Map params) {
        def session = AppUtil.session
        Long id = params.id.toLong(0)
        EventSessionTopic topic = id > 0 ? EventSessionTopic.findById(id) : new EventSessionTopic()
        topic.name = params.name
        topic.description = params.description
        if(id > 0) {
            topic.updated = new Date().gmt(session.timezone)
            topic.merge()
        } else {
            Long sessionId = params.sessionId.toLong(0)
            if(sessionId < 1) return false
            EventSession eventSession = EventSession.findById(sessionId)
            topic.eventSession = eventSession
            topic.created = new Date().gmt(session.timezone)
            topic.updated = topic.created
            topic.save()
        }
        return !topic.hasErrors()
    }

    public Long getSessionTopicCount(Map params) {
        return EventSessionTopic.createCriteria().count {
            and getSessionTopicCriteriaClosure(params)
        }
    }

    public Collection<EventSessionTopic> getSessionTopics(Map params) {
        Map listMap = [max: params.max, offset: params.offset]
        return EventSessionTopic.createCriteria().list(listMap) {
            and getSessionTopicCriteriaClosure(params)
            order(params.sort ?: "name", params.dir ?: "asc");
        }
    }

    @Transactional
    boolean deleteTopic (Long id, String at2_reply, String at1_reply){
        TrashUtil.preProcessFinalDelete("event-session-topic", id, at2_reply != null, at1_reply != null)
        AppEventManager.fire("before-event-session-topic-delete", [id])
        EventSessionTopic topic = EventSessionTopic.findById(id)
        topic.delete()
        AppEventManager.fire("event-session-topic-delete", [id])
        return !topic.hasErrors()
    }

    @Transactional
    Integer deleteSelectedTopics(List ids) {
        int removeCount = 0;
        ids.each { id ->
            try {
                if(deleteTopic(id, "yes", "yes")) {
                    removeCount++;
                }
            } catch(AttachmentExistanceException att) {
            }
        }
        return removeCount
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

    private int alphabetToDecimal(String target) {
        int res = 0;
        char[] numChar = new StringBuilder(target).reverse().toString().toCharArray();
        for(int i = numChar.length - 1; i >= 0; i--) {
            res += Math.pow(26, i) * (numChar[i] - ('A' as char) + 1);
        }
        return res;
    }

    public Integer getStartRowNumber(VenueLocationSection section) {
        return section.rowPrefixType == "alphabetic" ? alphabetToDecimal(section.rowPrefixStartsAt) : section.rowPrefixStartsAt.toInteger()
    }

    public Integer getStartColumnNumber(VenueLocationSection section) {
        return section.columnPrefixType == "alphabetic" ? alphabetToDecimal(section.columnPrefixStartsAt) : section.columnPrefixStartsAt.toInteger()
    }

    public Map renderEventWidget(Widget widget) {
        def config
        if (widget.params) {
            config = JSON.parse(widget.params)
        } else {
            throw new UnconfiguredWidgetExceptions()
        }
        String view = "/plugins/event_management/widget/event"
        Map model = [widget: widget, config: config]

        GrailsParameterMap _rparams = RequestContextHolder.currentRequestAttributes().params;
        Map widgetParams = [:]
        widgetParams.isPublic = true
        widgetParams.hasVenueLocation = true
        if(config.selectionType != "all") {
            if(config.selectionType == "venueLocation") {
                List<Long> locationIds = widget.widgetContent.contentId.collect { it.toLong() }
                widgetParams.locationIds = locationIds
            } else {
                List<Long> eventIds = widget.widgetContent.contentId.collect { it.toLong() }
                widgetParams.eventIds = eventIds
            }
        }

        if(config.displayType == "basic-calendar" || config.displayType == "advance-calendar") {
            widgetParams.offset = 0
            widgetParams.max = -1
            widgetParams.startTime = new Date().gmt()
            widgetParams.hasSession = false
            List<Event> events = getEvents(widgetParams)
            List<EventSession> eventSessions = getEventSessions(widgetParams)
            List<EventData> eventDataList = new JSONSerializableList<EventData>()
            events.each { event ->
                eventDataList.add(new EventData(event))
            }
            eventSessions.each { session ->
                eventDataList.add(new EventData(session))
            }
            model += [events: events, eventDataList: eventDataList.serialize()]
        } else {
            Date startTime = new Date().gmt()
            Collection<Event> allActiveEvents = []
            Collection<Event> allEvents
            if(widgetParams.eventIds != null) {
                allEvents = widgetParams.eventIds ? Event.findAllByIdInList(widgetParams.eventIds) : []
            } else if(widgetParams.locationIds != null) {
                List venueLocations = widgetParams.locationIds ? VenueLocation.findAllByIdInList(widgetParams.locationIds) : []
                allEvents = venueLocations ? Event.findAllByVenueLocationInList(venueLocations) : []
            } else {
                allEvents = Event.findAll()
            }
            allEvents.each { event ->
                if(event.isPublic) {
                    if(event.eventSessions?.size() > 0) {
                        Collection<EventSession> sessions = event.eventSessions
                        event.startTime = sessions.sort {
                            it.startTime
                        }[0].startTime
                        for(EventSession session : sessions) {
                            if(session.venueLocation && session.startTime >= startTime) {
                                allActiveEvents.add(event)
                                break;
                            }
                        }
                        event.endTime = sessions.sort {
                            it.endTime
                        }[sessions.size() - 1].endTime
                    } else {
                        if(event.venueLocation && event.startTime >= startTime) {
                            allActiveEvents.add(event)
                        }
                    }
                }
            }
            allActiveEvents.sort {
                it.startTime
            }
            if(config.listViewType == "paginated") {
                Map paginationProps = [offset: 0, max: 0, url_prefix: "evwd-" + widget.id]
                paginationProps.offset = _rparams.int(paginationProps.url_prefix + "-offset") ?: 0
                config.itemsPerPage = config.itemsPerPage ?: "10"
                paginationProps.max = _rparams.int(paginationProps.url_prefix + "-max") ?: config.itemsPerPage.toInteger(0)
                model += paginationProps
                _rparams.max = widgetParams.max = (_rparams[paginationProps.url_prefix + "-max"] ?: paginationProps.max).toInteger()
                _rparams.offset = widgetParams.offset = (_rparams[paginationProps.url_prefix + "-offset"] ?: paginationProps.offset).toInteger()
                def count = allActiveEvents.size()
                Collection<Event> events = []
                if(count > 0) {
                    if(widgetParams.max < 1) {
                        widgetParams.max = count
                        widgetParams.offset = 0
                    }
                    Integer lastIndex = count < (widgetParams.offset + widgetParams.max) ? count : widgetParams.offset + widgetParams.max
                    events = allActiveEvents[widgetParams.offset .. (lastIndex - 1)]
                }
                model += [events: events, count: count]
            } else {
                model += [events: allActiveEvents]
            }
        }
        view += config.displayType.toString().indexOf('-calendar') != -1 ? "/calendarView" : "/listView"
        return [view: view, model: model]
    }

    public List getUpcomingSessions(Event event) {
        List<EventSession> sessions = []
        Date now = new Date().gmt()
        event.eventSessions.each {
            if(it.startTime > now) {
                sessions.add(it)
            }
        }
        return sessions
    }

    public Double getLowestTicketPrice(Event event) {
        Double price = 0d
        Collection<VenueLocation> locations = []
        if(event.eventSessions?.size() > 0) {
            event.eventSessions.each {
                if(it.venueLocation) {
                    locations.add(it.venueLocation)
                }
            }
        } else {
            if(event.venueLocation) {
                locations.add(event.venueLocation)
            }
        }

        Collection<VenueLocationSection> sections = []
        locations.each {
            sections.addAll(it.sections)
        }
        sections.eachWithIndex { section, i ->
            if(i == 0) {
                price = section.ticketPrice
            }
            price = price > section.ticketPrice ? section.ticketPrice : price
        }
        return price
    }

    public Double getHighestTicketPrice(Event event) {
        Double price = 0d
        Collection<VenueLocation> locations = []
        if(event.eventSessions?.size() > 0) {
            event.eventSessions.each {
                if(it.venueLocation) {
                    locations.add(it.venueLocation)
                }
            }
        } else {
            if(event.venueLocation) {
                locations.add(event.venueLocation)
            }
        }

        Collection<VenueLocationSection> sections = []
        locations.each {
            sections.addAll(it.sections)
        }
        sections.each {
            price = price < it.ticketPrice ? it.ticketPrice : price
        }
        return price
    }

    public String ticketPriceWithCurrency(Event event) {
        return AppUtil.session.currency?.symbol?:AppUtil.baseCurrency.symbol + " " + (getLowestTicketPrice(event) == getHighestTicketPrice(event) ?
            getLowestTicketPrice(event).toCurrency().toPrice() : getLowestTicketPrice(event).toCurrency().toPrice() + "-" + getHighestTicketPrice(event).toCurrency().toPrice())
    }

    public Double getLowestTicketPrice(EventSession session) {
        Double price = 0d
        VenueLocation location = session.venueLocation
        if(location) {
            location.sections.eachWithIndex { section, i ->
                if(i == 0) {
                    price = section.ticketPrice
                }
                price = price > section.ticketPrice ? section.ticketPrice : price
            }
        }
        return price
    }

    public Double getHighestTicketPrice(EventSession session) {
        Double price = 0d
        VenueLocation location = session.venueLocation
        if(location) {
            location.sections.each {
                price = price < it.ticketPrice ? it.ticketPrice : price
            }
        }
        return price
    }

    public String ticketPriceWithCurrency(EventSession session) {
        return AppUtil.baseCurrency.symbol + " " + (getLowestTicketPrice(session) == getHighestTicketPrice(session) ? getLowestTicketPrice(session) :
            getLowestTicketPrice(session) + "-" + getHighestTicketPrice(session))
    }

    public Long countUpcomingEventSessions(Event event) {
        Map params = [:]
        params.startTime = new Date().gmt()
        Long count = EventSession.createCriteria().count {
            getEventSessionCriteriaClosure(params)
        }
        return count
    }

    public Date getStartTimeFromUpcomingSessions(Event event) {
        Map params = [:]
        params.startTime = new Date().gmt()
        Collection<EventSession> sessions = EventSession.createCriteria().list {
            getEventSessionCriteriaClosure(params)
        }
        sessions.sort {
            it.startTime
        }
        return sessions[0].startTime
    }

    public boolean sendPersonalizedProgram(String email, Event event, String customerName) {
        Map macrosAndTemplate = commanderMailService.getMacrosAndTemplateByIdentifier("personalized-program")
        if(!macrosAndTemplate.emailTemplate.active) {
            return;
        }
        Map refinedMacros = macrosAndTemplate.commonMacros
        macrosAndTemplate.macros.each {
            switch (it.key.toString()) {
                case "customer_name":
                    refinedMacros[it.key] = customerName
                    break;
                case "event_name":
                    refinedMacros[it.key] = event.name
                    break;
                case "event_start_date":
                    refinedMacros[it.key] = event.startTime.toEmailFormat()
                    break;
                case "organiser":
                    refinedMacros[it.key] = event.organiser.fullName
                    break;
                case "venue_name" :
                    refinedMacros[it.key] = event.venueLocation?.name ?: ""
                    break;
            }
        }
        List<File> attachments = []
        try {
            if(event.file) {
                String path = PathManager.getResourceRoot("event/event-${event.id}/personalized/${event.file}")
                attachments.add(new File(path))
                commanderMailService.sendMail(macrosAndTemplate.emailTemplate, macrosAndTemplate.activeHtml, macrosAndTemplate.activeText, refinedMacros, attachments, email)
            } else {
                commanderMailService.sendMail(macrosAndTemplate.emailTemplate, macrosAndTemplate.activeHtml, macrosAndTemplate.activeText, refinedMacros, email)
            }
        } catch(Exception e) {
            return false
        }
        return true
    }

    Long getNumberOfTicketPurchased(VenueLocationSection section, Event event, EventSession session) {
        return EventTicket.createCriteria().count {
            eq "section", section
            if(session) {
                eq "session", session
            } else {
                eq "event", event
            }
        }
    }

    synchronized
    public TreeSet getLockedTickets(VenueLocationSection section, Event event, EventSession session) {
        List<Integer> allPurchased = EventTicket.createCriteria().list {
            projections {
                property("seatNumber")
            }
            eq("section.id", section.id)
            if(session) {
                eq("session.id", session.id)
            } else {
                eq("event.id", event.id)
            }
        }

        Set withCarts = new TreeSet<Integer>();
        withCarts.addAll(allPurchased)
        Set inCarts = CartTicketManager.seatsInCart["$section.id#${session?.id}#${event?.id}"]
        if(inCarts) {
            withCarts.addAll(inCarts)
        }

        return withCarts
    }

    synchronized
    public TreeSet getFirstNAvailableTickets(VenueLocationSection section, Event event, EventSession session, Integer ticketCount) {
        Set withCarts = getLockedTickets(section, event, session)
        withCarts.add(section.rowCount * section.columnCount + 1)

        Set availables = new TreeSet<Integer>();
        def iterator = withCarts.iterator()
        Integer last = 0
        Integer addedCount = 0;
        for(def n in 1..ticketCount) {
            Integer number = iterator.next()
            if(number == null) {
                break
            }
            if(number > last + 1) {
                for(def s in last+1..number-1) {
                    availables.add(s)
                    if(addedCount == ticketCount) {
                        break;
                    }
                }
            }
            if(addedCount == ticketCount) {
                break;
            }
        }
        if(addedCount != ticketCount) {
            throw new ApplicationRuntimeException("quantity.ticket.not.available", [ticketCount])
        }
    }

    public Integer seatNameToNumber(VenueLocationSection section, String seatName) {
        String expressionForMatching
        if(section.rowPrefixType == "alphabetic") {
            expressionForMatching = "^([A-Z]{1,2})(\\d+)\$"
        } else {
            expressionForMatching = "^(\\d+)([A-Z]{1,2})\$"
        }
        Pattern pattern = Pattern.compile(expressionForMatching)
        Matcher matcher = pattern.matcher(seatName);
        matcher.find()
        String row = matcher.group(1)
        String column = matcher.group(2)
        Integer rowIndex
        Integer columnIndex
        if(section.rowPrefixType == "alphabetic") {
            rowIndex = section.rowPrefixOrder == "ascending" ? alphabetToDecimal(row) - alphabetToDecimal(section.rowPrefixStartsAt) + 1 : alphabetToDecimal(section
                    .rowPrefixStartsAt) - alphabetToDecimal(row) + 1
            columnIndex = section.columnPrefixOrder == "ascending" ? column.toInteger() - section.columnPrefixStartsAt.toInteger() + 1 : section.columnPrefixStartsAt.toInteger() -
                    column.toInteger() + 1
        } else {
            columnIndex = section.columnPrefixOrder == "ascending" ? alphabetToDecimal(column) - alphabetToDecimal(section.columnPrefixStartsAt) + 1 : alphabetToDecimal(section
                    .columnPrefixStartsAt) - alphabetToDecimal(column) + 1
            rowIndex = section.rowPrefixOrder == "ascending" ? row.toInteger() - section.rowPrefixStartsAt.toInteger() + 1 : section.rowPrefixStartsAt.toInteger() - row.toInteger() + 1
        }
        if(rowIndex < 1 || rowIndex > section.rowCount || columnIndex < 1 || columnIndex > section.columnCount) {
            return -1;
        }
        return (rowIndex - 1) * section.columnCount + columnIndex;
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

    def sendNewPurchaseTicketEmail(Event event, VenueLocationSection section, String recipient, EventSession session, def numbers) {
       event.attach();
       section.attach();
        if (session) {
            session = session.attach()
        }
        Map macrosAndTemplate = commanderMailService.getMacrosAndTemplateByIdentifier("new-purchase-ticket")
        if(!macrosAndTemplate.emailTemplate.active) {
            return;
        }
        Map refinedMacros = macrosAndTemplate.commonMacros
        macrosAndTemplate.macros.each {
            switch (it.key.toString()) {
                case "event_name":
                    refinedMacros[it.key] = event ? event.name.encodeAsBMHTML() : session.name.encodeAsBMHTML()
                    break;
                case "event_organiser_name":
                    refinedMacros[it.key] = event ? event.organiser.fullName : session.event.organiser.fullName
                    break;
                case "section_name":
                    refinedMacros[it.key] = section.name.encodeAsBMHTML()
                    break;
                case "venue_location_name":
                    refinedMacros[it.key] = section.venueLocation.name.encodeAsBMHTML()
                    break;
                case "venue_name" :
                    refinedMacros[it.key] = section.venueLocation.venue.name.encodeAsBMHTML()
                    break;
                case "ticket_number" :
                    refinedMacros[it.key] = seatNumberToName(section, numbers.toList()).join(",");
                    break;
                case "event_date" :
                    refinedMacros[it.key] = session ? session.startTime.toEmailFormat() : event.startTime.toEmailFormat();
                    break;
            }
        }
        commanderMailService.sendMail(macrosAndTemplate.emailTemplate, macrosAndTemplate.activeHtml, macrosAndTemplate.activeText, refinedMacros, recipient)
    }

    Integer getIndexForNewImage(Event event) {
        def idx = EventImage.createCriteria().list {
            projections {
                max("idx")
            }
            eq("event", event)
        }
        return idx[0] != null ? idx[0] + 1 : 1
    }
}
