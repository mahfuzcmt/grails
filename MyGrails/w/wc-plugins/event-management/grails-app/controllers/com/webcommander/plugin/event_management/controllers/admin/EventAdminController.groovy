package com.webcommander.plugin.event_management.controllers.admin

import com.webcommander.JSONSerializableList
import com.webcommander.authentication.annotations.License
import com.webcommander.authentication.annotations.Restriction
import com.webcommander.authentication.annotations.Restrictions
import com.webcommander.common.CommonService
import com.webcommander.constants.DomainConstants
import com.webcommander.plugin.event_management.Equipment
import com.webcommander.plugin.event_management.EquipmentInvitation
import com.webcommander.plugin.event_management.EquipmentType
import com.webcommander.plugin.event_management.Event
import com.webcommander.plugin.event_management.EventSession
import com.webcommander.plugin.event_management.EventSessionTopic
import com.webcommander.plugin.event_management.Venue
import com.webcommander.plugin.event_management.VenueLocation
import com.webcommander.plugin.event_management.VenueLocationInvitation
import com.webcommander.plugin.event_management.VenueLocationSection
import com.webcommander.plugin.event_management.EventTicket
import com.webcommander.plugin.event_management.model.EventData
import com.webcommander.plugin.event_management.webmarketing.EventCalendarService
import com.webcommander.plugin.event_management.webmarketing.EventService
import com.webcommander.throwables.AttachmentExistanceException
import com.webcommander.util.AppUtil
import grails.converters.JSON
import org.springframework.web.multipart.MultipartFile

class EventAdminController {

    EventService eventService
    EventCalendarService eventCalendarService
    CommonService commonService

    @License(required = "allow_event_feature")
    @Restriction(permission = "event.view.list")
    def loadAppView() {
        render(view: "/plugins/event_management/admin/appView", model: [d: 0])
    }

    def loadVenueTicketView() {
        Event event
        EventSession eventSession
        Long id = params.eventId ? params.eventId.toLong() : 0
        if(id) {
            event = Event.get(id)
        } else {
            eventSession = EventSession.get(params.eventSessionId.toLong())
        }
        params.max = params.max ?: "10";
        Integer count = eventService.getVenueTicketCount(params, event, eventSession)
        List venueTickets = commonService.withOffset(params.max, params.offset, count) { max, offset, _count->
            params.offset = offset
            return eventService.getVenueTickets(params, event, eventSession)
        }
        render(view: "/plugins/event_management/admin/venueTicket/appView", model: [tickets: venueTickets, count: count])
    }

    def loadTicketReservationSettingView() {
        def config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.TICKET_RESERVATION)
        render (view: "/plugins/event_management/admin/ticketReservationSetting", model: [config: config])
    }

    def loadSessionSettingView() {
        def config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.EVENT_SESSION_SETTING)
        render (view: "/plugins/event_management/admin/sessionSetting", model: [config: config])
    }

    @License(required = "allow_event_feature")
    def loadEventSelectionPanel() {
        params.max = params.max ?: 10
        params.startTime = new Date().gmt()
        Integer count = eventService.getEventsCount(params)
        List<Event> selectedEvents = (List<Event>)commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.offset = offset
            eventService.getEvents(params)
        }
        render(view: "/plugins/event_management/admin/widget/eventSelectionPanel", model: [selectedEvents: selectedEvents, count: count])
    }

    def loadEventsForSelection() {
        params.max = params.max ?: 10
        params.remove("widgetId")
        params.remove("eventIds")
        params.isPublic = true
        params.hasVenueLocation = true
        Integer count = eventService.getEventsCount(params)
        List<Event> events = (List<Event>)commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.offset = offset
            eventService.getEvents(params)
        }
        render(view: "/plugins/event_management/admin/widget/eventSelectionList", model: [events: events, count: count])
    }

    @License(required = "allow_event_feature")
    def loadCalenderView() {
        render(view: "/plugins/event_management/admin/calendar/appView", model: [d:true])
    }

    def loadVenueAsJSON() {
        Map filterMap = [max: -1, offset: 0]
        Collection<Venue> venues = eventService.getVenues(filterMap)
        Map venuesMap = [:]
        venues.each {
            venuesMap."$it.id" = it.name
        }
        render(venuesMap as JSON)
    }

    def loadVenueLocationAsJSON() {
        Long venueId = params.venue.id.toLong(0)
        Collection<VenueLocation> venueLocations = Venue.get(venueId).locations
        Map venueLocationsMap = [:]
        venueLocations.each {
            venueLocationsMap."$it.id" = it.name
        }
        render(venueLocationsMap as JSON)
    }

    def loadEquipmentAsJSON() {
        Collection<Equipment> equipments = eventService.getEquipments(params)
        Map equipmentsMap = [:]
        equipments.each {
            equipmentsMap."$it.id" = it.name
        }
        render(equipmentsMap as JSON)
    }

    def loadAllEventData() {
        String calendarType = params.calendarType
        Long selectedValue = params.selectedValue.toLong(0)
        Venue venueAsParent = Venue.findById(params.venueAsParent.toLong(0))
        List<Event> events = []
        List<EventSession> eventSessions = []
        if(calendarType != "public") {
            List locationIds = []
            List equipmentIds = []
            if(calendarType == "venue") {
                locationIds = Venue.get(selectedValue).locations.id
            } else if(calendarType == "location") {
                if(selectedValue == -1) {
                    locationIds = VenueLocation.where {
                        venue == venueAsParent
                    }.list().id
                } else {
                    locationIds.add(selectedValue)
                }
            } else {
                if(selectedValue == -1) {
                    equipmentIds = Equipment.findAll().id
                } else {
                    equipmentIds.add(selectedValue)
                }
            }
            events = Event.createCriteria().list {
                sizeEq("eventSessions", 0)
                if(locationIds.size()) {
                    inList('venueLocation.id', locationIds)
                }
                if(equipmentIds.size()) {
                    inList('equipment.id', equipmentIds)
                }
            }
            eventSessions = EventSession.createCriteria().list {
                if(locationIds.size()) {
                    inList('venueLocation.id', locationIds)
                }
                if(equipmentIds.size()) {
                    inList('equipment.id', equipmentIds)
                }
            }
        } else {
            params.isPublic = true
            params.hasSession = false
            events = eventService.getEvents(params)
            eventSessions = eventService.getEventSessions(params)
        }
        List<EventData> eventDataList = new JSONSerializableList<EventData>()
        events.each { event ->
            eventDataList.add(new EventData(event))
        }
        eventSessions.each { session ->
            eventDataList.add(new EventData(session))
        }
        render(text: eventDataList.serialize())
    }

    @License(required = "allow_event_feature")
    @Restriction(permission = "event.view.list")
    def loadEventView() {
        params.max = params.max ?: "10";
        Integer count = eventService.getEventsCount(params)
        List<Event> events = commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.offset = offset
            return eventService.getEvents(params)
        }
        List status = eventService.getStatusOfEvents(events)
        List manageSessionStatus = eventService.getStatusOfManageSessionEntry(events)
        List deleteStatus = eventService.getDeleteStatusForEvent(events)
        render(view: "/plugins/event_management/admin/event/appView", model: [events: events,  count: count, status: status, manageSessionStatus: manageSessionStatus, deleteStatus: deleteStatus])
    }

    @Restriction(permission = "event.view.list")
    def loadVenueView() {
        params.max = params.max ?: "10";
        Integer count = eventService.getVenuesCount(params)
        List<Venue> venues = commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.offset = offset
            return eventService.getVenues(params)
        }
        List capacity = eventService.getCapacityOfVenues(venues)
        render(view: "/plugins/event_management/admin/venue/appView", model: [venues: venues, count: count, capacity: capacity])
    }

    def loadEquipmentTypes() {
        params.max = params.max ?: "10";
        Integer count = eventService.getEquipmentTypesCount(params)
        List<EquipmentType> equipmentTypes = commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.offset = offset
            return eventService.getEquipmentTypes(params)
        }
        render(view: "/plugins/event_management/admin/equipment/type/appView", model: [equipmentTypes: equipmentTypes, count: count])
    }

    @Restriction(permission = "event.view.list")
    def loadEquipmentTypeView() {
        params.max = params.max ?: "10";
        Integer count = eventService.getEquipmentTypesCount(params)
        List<EquipmentType> equipmentTypes = commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.offset = offset
            return eventService.getEquipmentTypes(params)
        }
        render(view: "/plugins/event_management/admin/equipment/type/appView", model: [equipmentTypes: equipmentTypes, count: count])
    }

    @Restriction(permission = "event.view.list")
    def loadEquipmentView() {
        params.max = params.max ?: "10";
        Integer count = eventService.getEquipmentsCount(params)
        List<Equipment> equipments = commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.offset = offset
            return eventService.getEquipments(params)
        }
        render(view: "/plugins/event_management/admin/equipment/appView", model: [equipments: equipments, count: count])
    }

    def renderCalendarDom() {
        params.remove("action")
        params.remove("controller")
        String view = params.view ?: "month"
        Date date = eventCalendarService.getDateForCalendar(view, params.date, params.operator)
        def events = eventCalendarService.getEvents(date, view)
        String calendarDom = eventCalendarService."getCalendar${view.capitalize()}View"(date)
        render([status: "success", html: calendarDom, events: events] as JSON)
    }

    //////////////////////////////////////////// Event ////////////////////////////////////////////

    @License(required = "allow_event_feature")
    @Restrictions([
            @Restriction(permission = "event.create", params_not_exist = "id"),
            @Restriction(permission = "event.edit", params_exist = "id", entity_param = "id", domain = Event)
    ])
    def createEvent() {
        Event event = params.id ? Event.get(params.long("id")) : new Event();
        render( view: "/plugins/event_management/admin/event/infoEdit", model: [event: event] )
    }

    @License(required = "allow_event_feature")
    def saveEvent() {
        Boolean success = true
        Long id = eventService.saveEvent(params)
        if (id) {
            List<MultipartFile> file = request.getMultiFileMap().file;
            String removeFile = params["file-remove"]
            success = eventService.saveEventFile(file ? file[0] : null, removeFile, id, null)
            List<Long> removeImgIds = params.list("remove-images")*.toLong();
            List<MultipartFile> images = request.getMultiFileMap().images;
            success = success && eventService.saveEventImages(id, images, removeImgIds);
        }
        if(success) {
            render([status: "success", message: g.message(code: "event.save.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "event.save.error")] as JSON)
        }
    }

    def viewEvent() {
        Event event = Event.get(params.id);
        render(view: "/plugins/event_management/admin/event/infoView", model: [event: event])
    }

    @Restriction(permission = "event.remove", entity_param = "id", domain = Event)
    def deleteEvent() {
        Long id = params.long("id");
        try {
            Boolean result = eventService.deleteEvent(id, params.at2_reply, params.at1_reply);
            if(result) {
                render([status: "success", message: g.message(code: "event.delete.success")] as JSON);
            } else {
                render([status: "error", message: g.message(code: "event.delete.error")] as JSON);
            }
        } catch(AttachmentExistanceException att) {
            render([status: "error", error_code: "attachment.exists", attachments: att.attachmentInfo] as JSON)
        }
    }

    @Restriction(permission = "event.remove", entity_param = "ids", domain = Event)
    def deleteSelectedEvents() {
        def ids = [];
        params.list("ids").each {
            ids.add(it.toLong(0))
        }
        int deleteCount = eventService.deleteSelectedEvents(ids);
        int total = ids.size();
        if (deleteCount == total) {
            render([status: "success", message: g.message(code: "selected.events.delete.success")] as JSON)
        } else if(deleteCount == 0) {
            render([status: "error", message: g.message(code: "selected.events.could.not.delete")] as JSON)
        } else {
            render([status: "warning", message: g.message(code: "selected.not.deleted", args: [total - deleteCount, total, g.message(code: "events")])] as JSON)
        }
    }

    def filterEvent() {
        render(view: "/plugins/event_management/admin/event/filter", model: [params: params])
    }

    def filterEquipment() {
        render(view: "/plugins/event_management/admin/equipment/filter", model: [params: params])
    }

    @License(required = "allow_event_feature")
    def bookVenue() {
        Long eventId = params.id.toLong()
        Long sessionId = params.sessionId.toLong()
        List venues = Venue.list()
        render(view: "/plugins/event_management/admin/event/venueBookingListing", model: [eventId: eventId, sessionId: sessionId, venues: venues])
    }

    def venueLocationForVenue(){
        List venueLocations = eventService.getVenueLocations([:], params.venueId.toLong(0))
        render(view: "/plugins/event_management/admin/event/venueLocationListing", model: [locations: venueLocations])
    }

    def saveVenueBooking() {
        Long eventId = params.eventId.toLong()
        Long sessionId = params.sessionId.toLong()
        Long locationId = params.location.toLong()
        String result = eventService.saveVenueBooking(eventId, sessionId, locationId)
        if(result == "no_section") {
            render([status: "error", message: g.message(code: "venue.location.has.no.section", args: [g.message(code: "venue")])] as JSON)
        } else if(result == "conflicted"){
            render([status: "error", message: g.message(code: "booking.request.conflicted", args: [g.message(code: "venue")])] as JSON)
        } else if(result == "mailSentError") {
            render([status: "success", message: g.message(code: "booking.request.save.successful.mail.sent.error")] as JSON)
        } else if(result == "true") {
            render([status: "success", message: g.message(code: "booking.request.send.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "booking.request.send.error")] as JSON)
        }
    }

    @License(required = "allow_event_feature")
    def bookEquipment() {
        Long eventId = params.eventId.toLong()
        Long sessionId = params.sessionId.toLong()
        List equipments = Equipment.list()
        render(view: "/plugins/event_management/admin/event/equipmentBookingListing", model: [eventId: eventId, sessionId: sessionId, equipments: equipments])
    }

    def saveEquipmentBooking() {
        Long eventId = params.eventId.toLong()
        Long sessionId = params.sessionId.toLong()
        Long equipmentId = params.equipment.toLong()
        String result = eventService.saveEquipmentBooking(eventId, sessionId, equipmentId)
        if(result == "conflicted"){
            render([status: "error", message: g.message(code: "booking.request.conflicted", args: [g.message(code: "equipment")])] as JSON)
        }else if(result == "mailSentError") {
            render([status: "success", message: g.message(code: "booking.request.save.successful.mail.sent.error")] as JSON)
        }else if(result == "true") {
            render([status: "success", message: g.message(code: "booking.request.send.success")] as JSON)
        }else {
            render([status: "error", message: g.message(code: "booking.request.send.error")] as JSON)
        }
    }

    //////////////////////////////////////////// Equipment ////////////////////////////////////////////

    @License(required = "allow_event_feature")
    def editEquipmentType() {
        EquipmentType type = params.id ? EquipmentType.get(params.long("id")) : new EquipmentType();
        render( view: "/plugins/event_management/admin/equipment/type/infoEdit", model: [type: type] )
    }

    @License(required = "allow_event_feature")
    def saveEquipmentType() {
        if(eventService.saveEquipmentType(params)) {
            render([status: "success", message: g.message(code: "equipment.type.save.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "equipment.type.save.error")] as JSON)
        }
    }

    def deleteEquipmentType() {
        Long id = params.long("id")
        try {
            if(eventService.deleteEquipmentType(id, params.at2_reply, params.at1_reply)) {
                render([status: "success", message: g.message(code: "equipment.type.delete.success")] as JSON)
            }else {
                render([status: "error", message: g.message(code: "equipment.type.delete.error")] as JSON)
            }
        } catch (AttachmentExistanceException att) {
            render([status: "error", error_code: "attachment.exists", attachments: att.attachmentInfo] as JSON)
        }
    }

    def deleteSelectedEquipmentTypes() {
        def ids = [];
        params.list("ids").each {
            ids.add(it.toLong(0))
        }
        int deleteCount = eventService.deleteSelectedEquipmentTypes(ids);
        int total = ids.size();
        if (deleteCount == total) {
            render([status: "success", message: g.message(code: "selected.equipment.types.delete.success")] as JSON)
        } else if(deleteCount == 0) {
            render([status: "error", message: g.message(code: "selected.equipment.types.could.not.delete")] as JSON)
        } else {
            render([status: "warning", message: g.message(code: "selected.not.deleted", args: [total - deleteCount, total, g.message(code: "equipment.types")])] as JSON)
        }
    }

    @License(required = "allow_event_feature")
    @Restrictions([
            @Restriction(permission = "event.create", params_not_exist = "id"),
            @Restriction(permission = "event.edit", params_exist = "id", entity_param = "id", domain = Equipment)
    ])
    def editEquipment() {
        Equipment equipment = params.id ? Equipment.get(params.long("id")) : new Equipment();
        List equipmentTypes = EquipmentType.list()
        render( view: "/plugins/event_management/admin/equipment/infoEdit", model: [equipment: equipment, equipmentTypes: equipmentTypes] )
    }

    @License(required = "allow_event_feature")
    def saveEquipment(){
        String result = eventService.saveEquipment(params)
        if(result) {
            render([status: "success", message: g.message(code: "equipment.save.success")] as JSON)
        }else {
            render([status: "error", message: g.message(code: "equipment.save.error")] as JSON)
        }
    }

    @Restriction(permission = "event.remove", entity_param = "id", domain = Equipment)
    def  deleteEquipment(){
        Long id = params.long("id")
        try {
            if(eventService.deleteEquipment(id, params.at2_reply, params.at1_reply)) {
                render([status: "success", message: g.message(code: "equipment.delete.success")] as JSON)
            }else {
                render([status: "error", message: g.message(code: "equipment.delete.error")] as JSON)
            }
        } catch (AttachmentExistanceException att) {
            render([status: "error", error_code: "attachment.exists", attachments: att.attachmentInfo] as JSON)
        }
    }

    @Restriction(permission = "event.remove", entity_param = "ids", domain = Equipment)
    def deleteSelectedEquipments() {
        def ids = [];
        params.list("ids").each {
            ids.add(it.toLong(0))
        }
        int deleteCount = eventService.deleteSelectedEquipments(ids);
        int total = ids.size();
        if (deleteCount == total) {
            render([status: "success", message: g.message(code: "selected.equipments.delete.success")] as JSON)
        } else if(deleteCount == 0) {
            render([status: "error", message: g.message(code: "selected.equipments.could.not.delete")] as JSON)
        } else {
            render([status: "warning", message: g.message(code: "selected.not.deleted", args: [total - deleteCount, total, g.message(code: "equipments")])] as JSON)
        }
    }

    def isEquipmentTypeUnique() {
        if (commonService.isUnique(EquipmentType, params)) {
            render([status: "success", message: g.message(code: "provided.field.available", args: [params.field, params.value])] as JSON)
        } else {
            render([status: "error", message: g.message(code: "provided.field.value.exists", args: [params.field, params.value])] as JSON)
        }
    }

    def isEquipmentUnique() {
        if (commonService.isUnique(Equipment, params)) {
            render([status: "success", message: g.message(code: "provided.field.available", args: [params.field, params.value])] as JSON)
        } else {
            render([status: "error", message: g.message(code: "provided.field.value.exists", args: [params.field, params.value])] as JSON)
        }
    }

    @License(required = "allow_event_feature")
    def approveEquipmentInvitation() {
        EquipmentInvitation invitation = EquipmentInvitation.get(params.long("id"))
        def status = eventService.approveEquipmentInvitation(invitation)
        if (status == "booked") {
            if (invitation.event) {
                render([status: "error", message: g.message(code: "already.booked", args: [g.message(code: "an.equipment"), g.message(code: 'event')])] as JSON)
            } else {
                render([status: "error", message: g.message(code: "already.booked", args: [g.message(code: "an.equipment"), g.message(code: 'event.session')])] as JSON)
            }
        } else if (status) {
            render([status: "success", message: g.message(code: "invitation.approve.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "invitation.approve.error")] as JSON)
        }
    }

    @License(required = "allow_event_feature")
    def rejectEquipmentInvitation() {
        Long id = params.long("id")
        if(eventService.rejectEquipmentInvitation(id)){
            render([status: "success", message: g.message(code: "invitation.reject.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "invitation.reject.error")] as JSON)
        }
    }

    //////////////////////////////////////////////// Venue //////////////////////////////////////////////

    @License(required = "allow_event_feature")
    @Restrictions([
            @Restriction(permission = "event.create", params_not_exist = "id"),
            @Restriction(permission = "event.edit", params_exist = "id", entity_param = "id", domain = Venue)
    ])
    def editVenue() {
        Venue venue = params.id ? Venue.get(params.long("id")) : new Venue();
        render( view: "/plugins/event_management/admin/venue/infoEdit", model: [venue: venue] )
    }

    @License(required = "allow_event_feature")
    def saveVenue(){
        if(eventService.saveVenue(params)) {
            render([status: "success", message: g.message(code: "venue.save.success")] as JSON)
        }else {
            render([status: "error", message: g.message(code: "venue.save.error")] as JSON)
        }
    }

    @Restriction(permission = "event.remove", entity_param = "id", domain = Venue)
    def  deleteVenue(){
        Long id = params.long("id");
        try {
            Boolean result = eventService.deleteVenue(id, params.at2_reply, params.at1_reply);
            if(result) {
                render([status: "success", message: g.message(code: "venue.delete.success")] as JSON);
            } else {
                render([status: "error", message: g.message(code: "venue.delete.error")] as JSON);
            }
        } catch(AttachmentExistanceException att) {
            render([status: "error", error_code: "attachment.exists", attachments: att.attachmentInfo] as JSON)
        }
    }

    @Restriction(permission = "event.remove", entity_param = "ids", domain = Venue)
    def deleteSelectedVenues() {
        def ids = [];
        params.list("ids").each {
            ids.add(it.toLong(0))
        }
        int deleteCount = eventService.deleteSelectedVenues(ids);
        int total = ids.size();
        if (deleteCount == total) {
            render([status: "success", message: g.message(code: "selected.venues.delete.success")] as JSON)
        } else if(deleteCount == 0) {
            render([status: "error", message: g.message(code: "selected.venues.could.not.delete")] as JSON)
        } else {
            render([status: "warning", message: g.message(code: "selected.not.deleted", args: [total - deleteCount, total, g.message(code: "venues")])] as JSON)
        }
    }

    def isVenueUnique() {
        if (commonService.isUnique(Venue, params)) {
            render([status: "success", message: g.message(code: "provided.field.available", args: [params.field, params.value])] as JSON)
        } else {
            render([status: "error", message: g.message(code: "provided.field.value.exists", args: [params.field, params.value])] as JSON)
        }
    }

    def isUniqueVenueLocation() {
        params.compositeValue = params.compositeValue.toLong(0)
        if (commonService.isUnique(VenueLocation, params)) {
            render([status: "success", message: g.message(code: "provided.field.available", args: [params.field, params.value])] as JSON)
        } else {
            render([status: "error", message: g.message(code: "provided.field.value.exists", args: [params.field, params.value])] as JSON)
        }
    }

    def loadVenueLocations() {
        params.max = params.max ?: "10";
        Long venueId = params.long("venueId")
        Integer count = eventService.getVenueLocationsCount(params, venueId);
        List<VenueLocation> venueLocations = commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.offset = offset
            params.max = max
            return eventService.getVenueLocations(params, venueId)
        }
        List capacity = eventService.getCapacityOfLocations(eventService.getVenueLocations(params,venueId))
        render( view: "/plugins/event_management/admin/venue/venueLocationView", model: [locations: venueLocations,count: count, capacity: capacity] )
    }

    def loadVenueLocationSections() {
        params.max = params.max ?: "10";
        Long venueId = params.long("venueId"),
            locationId = params.long("locationId")

        List<VenueLocation> locations = eventService.getVenueLocations([:], venueId)
        Integer count = eventService.getVenueLocationSectionsCount(params, locationId ?: locations[0]?.id);
        List<VenueLocationSection> venueLocationSections = commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.offset = offset
            params.max = max
            return eventService.getVenueLocationSections(params, locationId ?: locations[0]?.id)
        }
        render( view: "/plugins/event_management/admin/venue/venueLocationSectionView", model: [sections: venueLocationSections, locations: locations, count: count] )
    }

    def editLocation() {
        VenueLocation venueLocation = params.id ? VenueLocation.get(params.long("id")) : new VenueLocation();
        def venueId = params.long("venueId")
        render(view: "/plugins/event_management/admin/venue/editLocation", model: [location: venueLocation, venueId: venueId])
    }

    def saveVenueLocation() {
        Long id = eventService.saveVenueLocation(params)
        if (id) {
            List<MultipartFile> images = request.getMultiFileMap().images;
            List<Long> removeImgIds = params.list("remove-images")*.toLong();
            eventService.saveLocationImages(id, images, removeImgIds);
            render([status: "success", message: g.message(code: "location.save.success")] as JSON)
        }else {
            render([status: "error", message: g.message(code: "location.save.error")] as JSON)
        }
    }

    def deleteLocation() {
        try {
            if(eventService.deleteVenueLocation(params.long("id"), params.at2_reply, params.at1_reply)) {
                render([status: "success", message: g.message(code: "location.delete.success")] as JSON)
            }else {
                render([status: "error", message: g.message(code: "location.delete.error")] as JSON)
            }
        } catch (AttachmentExistanceException att) {
            render([status: "error", error_code: "attachment.exists", attachments: att.attachmentInfo] as JSON)
        }
    }

    def deleteSelectedVenueLocations() {
        def ids = [];
        params.list("ids").each {
            ids.add(it.toLong(0))
        }
        int deleteCount = eventService.deleteSelectedVenueLocations(ids);
        int total = ids.size();
        if (deleteCount == total) {
            render([status: "success", message: g.message(code: "selected.venue.locations.delete.success")] as JSON)
        } else if(deleteCount == 0) {
            render([status: "error", message: g.message(code: "selected.venue.locations.could.not.delete")] as JSON)
        } else {
            render([status: "warning", message: g.message(code: "selected.not.deleted", args: [total - deleteCount, total, g.message(code: "venue.locations")])] as JSON)
        }
    }

    def editVenueLocationSection() {
        Long id = params.id.toLong(0)
        Long locationId = params.long("locationId")
        VenueLocationSection venueLocationSection = id ? VenueLocationSection.get(id) : new VenueLocationSection();
        render( view: "/plugins/event_management/admin/venue/editVenueLocationSection",
            model: [section: venueLocationSection, locationId: locationId,
                    ticketExists: venueLocationSection.id ? EventTicket.countBySection(venueLocationSection) > 0 : false] )
    }

    def saveVenueLocationSection() {
        if(eventService.saveVenueLocationSection(params)) {
            render([status: "success", message: g.message(code: "section.save.success")] as JSON)
        }else {
            render([status: "error", message: g.message(code: "section.save.failure")] as JSON)
        }
    }

    def deleteVenueLocationSection() {
        Long sectionId = params.long("id")
        VenueLocationSection section = VenueLocationSection.get(sectionId)
        if(eventService.countLocationSection(sectionId) < 2 && eventService.hasAnyApprovedInvitation(sectionId)) {
            render([status: "error", message: g.message(code: "cannot.remove.all.section.from.booked.location")] as JSON)
            return
        }
        if(section.ticket.size()) {
            render([status: "error", message: g.message(code: "purchased.ticket.exists")] as JSON)
        } else {
            try {
                if(eventService.deleteVenueLocationSection(sectionId, params.at2_reply, params.at1_reply)) {
                    render([status: "success", message: g.message(code: "section.delete.success")] as JSON)
                } else {
                    render([status: "error", message: g.message(code: "section.delete.failure")] as JSON)
                }
            } catch(AttachmentExistanceException att) {
                render([status: "error", error_code: "attachment.exists", attachments: att.attachmentInfo] as JSON)
            }
        }
    }

    def deleteSelectedVenueLocationSections() {
        List<Long> ids = [];
        params.list("ids").each {
            ids.add(it.toLong(0))
        }
        if(ids.size() == eventService.countLocationSection(ids[0]) && eventService.hasAnyApprovedInvitation(ids[0])) {
            render([status: "error", message: g.message(code: "cannot.remove.all.section.from.booked.location")] as JSON)
            return
        }
        int deleteCount = eventService.deleteSelectedVenueLocationSections(ids);
        int total = ids.size();
        if (deleteCount == total) {
            render([status: "success", message: g.message(code: "selected.venue.location.sections.delete.success")] as JSON)
        } else if(deleteCount == 0) {
            render([status: "error", message: g.message(code: "selected.venue.locations.sections.could.not.delete")] as JSON)
        } else {
            render([status: "warning", message: g.message(code: "selected.not.deleted", args: [total - deleteCount, total, g.message(code: "venue.location.sections")])] as JSON)
        }
    }

    def isUniqueVenueLocationSection() {
        params.compositeValue = params.compositeValue.toLong(0)
        if (commonService.isUnique(VenueLocationSection, params)) {
            render([status: "success", message: g.message(code: "provided.field.available", args: [params.field, params.value])] as JSON)
        } else {
            render([status: "error", message: g.message(code: "provided.field.value.exists", args: [params.field, params.value])] as JSON)
        }
    }

    @License(required = "allow_event_feature")
    def approveVenueLocationInvitation() {
        VenueLocationInvitation invitation = VenueLocationInvitation.get(params.long("id"))
        def status = eventService.approveVenueLocationInvitation(invitation)
        if(status == "booked"){
            if(invitation.event) {
                render([status: "error", message: g.message(code: "already.booked", args: [g.message(code: "a.venue"), g.message(code: 'event')])] as JSON)
            }else {
                render([status: "error", message: g.message(code: "already.booked", args: [g.message(code: "a.venue"), g.message(code: 'event.session')])] as JSON)
            }
        }else if(status){
            render([status: "success", message: g.message(code: "invitation.approve.success")] as JSON)
        }else {
            render([status: "error", message: g.message(code: "invitation.approve.error")] as JSON)
        }
    }

    @License(required = "allow_event_feature")
    def rejectVenueLocationInvitation() {
        Long id = params.long("id")
        if(eventService.hasPurchasedTicket(id)) {
            render([status: "error", message: g.message(code: "cannot.remove.some.ticket.already.sold")] as JSON)
        } else {
            if(eventService.rejectVenueLocationInvitation(id)){
                render([status: "success", message: g.message(code: "invitation.reject.success")] as JSON)
            } else {
                render([status: "error", message: g.message(code: "invitation.reject.error")] as JSON)
            }
        }
    }

    //////////////////////////////////////////// EventSession////////////////////////////////////////////

    @License(required = "allow_event_feature")
    def loadSessionAppView() {
        Long eventId = params.eventId ? params.eventId.toLong(0) : 0
        if(eventId) {
            params.max = params.max ?: "10";
            Integer count = eventService.getEventSessionCount(params)
            List<EventSession> eventSessions = (List<EventSession>)commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
                params.offset = offset
                return eventService.getEventSessions(params)
            }
            List deleteStatus = eventService.getDeleteStatusForEventSession(eventSessions)
            render(view: "/plugins/event_management/admin/event/session/appView", model: [eventSessions: eventSessions, count: count, deleteStatus: deleteStatus])
        }
    }

    @License(required = "allow_event_feature")
    def editSession() {
        Long id = params.id.toLong(0)
        EventSession eventSession = id > 0 ? EventSession.get(id) : new EventSession()
        render(view: "/plugins/event_management/admin/event/session/infoEdit", model: [eventSession: eventSession, event: Event.proxy(params.eventId)])
    }

    @License(required = "allow_event_feature")
    def saveSession() {
        Long id = params.id.toLong(0)
        Long eventId = params.eventId.toLong(0)
        boolean success = false;
        id = eventService.saveSession(id, eventId, params);
        if(id) {
            List<MultipartFile> file = request.getMultiFileMap().file;
            String removeFile = params["file-remove"]
            success = eventService.saveEventFile(file ? file[0] : null, removeFile, null, id)
        }
        if(success) {
            render([status: "success", message: g.message(code: "session.save.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "session.save.error")] as JSON)
        }
    }

    def deleteEventSession() {
        Long id = params.id.toLong(0)
        try {
            Boolean result = eventService.deleteEventSession(id, params.at2_reply, params.at1_reply);
            if(result) {
                render([status: "success", message: g.message(code: "event.session.delete.success")] as JSON);
            } else {
                render([status: "error", message: g.message(code: "event.session.delete.error")] as JSON);
            }
        } catch(AttachmentExistanceException att) {
            render([status: "error", error_code: "attachment.exists", attachments: att.attachmentInfo] as JSON)
        }
    }

    def deleteSelectedEventSessions() {
        def ids = [];
        params.list("ids").each {
            ids.add(it.toLong(0))
        }
        int deleteCount = eventService.deleteSelectedEventSessions(ids);
        int total = ids.size();
        if (deleteCount == total) {
            render([status: "success", message: g.message(code: "selected.event.sessions.delete.success")] as JSON)
        } else if(deleteCount == 0) {
            render([status: "error", message: g.message(code: "selected.event.sessions.could.not.delete")] as JSON)
        } else {
            render([status: "warning", message: g.message(code: "selected.not.deleted", args: [total - deleteCount, total, g.message(code: "event.sessions")])] as JSON)
        }
    }

    def isUniqueEventSession() {
        params.compositeValue = params.compositeValue.toLong(0)
        if (commonService.isUnique(EventSession, params)) {
            render([status: "success", message: g.message(code: "provided.field.available", args: [params.field, params.value])] as JSON)
        } else {
            render([status: "error", message: g.message(code: "provided.field.value.exists", args: [params.field, params.value])] as JSON)
        }
    }

    /* ++++++++++++++++++++++++++++++++++++++++ Event Session Topic ++++++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    @License(required = "allow_event_feature")
    def loadTopicAppView() {
        Long sessionId = params.sessionId ? params.sessionId.toLong(0) : 0
        if(sessionId != 0) {
            params.max = params.max ?: "10";
            Integer count = eventService.getSessionTopicCount(params)
            List<EventSessionTopic> topics = (List<EventSessionTopic>)commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
                params.offset = offset
                return eventService.getSessionTopics(params)
            }
            render(view: "/plugins/event_management/admin/event/session/topic/appView", model: [topics: topics, count: count])
        }
    }

    @License(required = "allow_event_feature")
    def editTopic() {
        Long id = params.id.toLong(0)
        Long sessionId = params.sessionId.toLong(0)
        EventSessionTopic topic = id > 0 ? EventSessionTopic.findById(id) : new EventSessionTopic()
        render(view: "/plugins/event_management/admin/event/session/topic/infoEdit", model: [sessionId: sessionId, topic: topic])
    }

    @License(required = "allow_event_feature")
    def saveTopic() {
        if(eventService.saveTopic(params)) {
            render([status: "success", message: g.message(code: "event.session.topic.save.success")] as JSON)
        }else {
            render([status: "error", message: g.message(code: "event.session.topic.save.failed")] as JSON)
        }
    }

    def viewTopic() {
        Long id = params.id.toLong(0)
        EventSessionTopic topic = EventSessionTopic.findById(id)
        render(view: "/plugins/event_management/admin/event/session/topic/infoView", model: [topic: topic])
    }

    def deleteTopic() {
        Long id = params.id.toLong(0)
        try {
            Boolean result = eventService.deleteTopic(id, params.at2_reply, params.at1_reply);
            if(result) {
                render([status: "success", message: g.message(code: "event.session.topic.delete.success")] as JSON);
            } else {
                render([status: "error", message: g.message(code: "event.session.topic.delete.error")] as JSON);
            }
        } catch(AttachmentExistanceException att) {
            render([status: "error", error_code: "attachment.exists", attachments: att.attachmentInfo] as JSON)
        }
    }

    def deleteSelectedTopics() {
        def ids = [];
        params.list("ids").each {
            ids.add(it.toLong(0))
        }
        int deleteCount = eventService.deleteSelectedTopics(ids);
        int total = ids.size();
        if (deleteCount == total) {
            render([status: "success", message: g.message(code: "selected.event.session.topics.delete.success")] as JSON)
        } else if(deleteCount == 0) {
            render([status: "error", message: g.message(code: "selected.event.session.topics.delete.failed")] as JSON)
        } else {
            render([status: "warning", message: g.message(code: "selected.not.deleted", args: [total - deleteCount, total, g.message(code: "event.session.topics")])] as JSON)
        }
    }

    //////////////////////////////////////////// Ticket & Reservation////////////////////////////////////////////

    @License(required = "allow_event_feature")
    def createComplementaryTicket() {
        Long id = params.eventId.toLong()
        List sections
        if(id) {
            Event event = Event.get(id)
            sections = event.venueLocation.sections
        } else {
            EventSession session = EventSession.get(params.eventSessionId.toLong())
            sections = session.venueLocation.sections
        }
        render( view: "/plugins/event_management/admin/venueTicket/createComplementaryTicket", model: [eventId: params.eventId, sessionId:  params.eventSessionId, sections: sections] )
    }

    @License(required = "allow_event_feature")
    def saveComplementaryTicket() {
        eventService.saveComplementaryTicket(params)
        render([status: "success", message: g.message(code: "complementary.ticket.create.success")] as JSON)
    }

    def voidComplementaryTicket() {
        eventService.voidComplementaryTicket(params)
        render([status: "success", message: g.message(code: "ticket.void.success")] as JSON)
    }

    @License(required = "allow_event_feature")
    def loadEventSeatMap() {
        VenueLocation location;
        VenueLocationSection section
        EventSession session
        Event event
        if(params.eventSessionId) {
            session = EventSession.proxy(params.eventSessionId)
            location = session.venueLocation
        } else {
            event = Event.proxy(params.eventId)
            location = event.venueLocation
        }
        if(params.section) {
            section = location.sections.find {
                "" + it.id == params.section
            }
        }
        if(!section) {
            section = location.sections[0]
        }
        def lockedTickets;
        if(section) {
            lockedTickets = eventService.getLockedTickets(section, event, session)
        }
        render( view: "/plugins/event_management/site/printEventSeats", model: [section: section, location: location, lockedTickets: lockedTickets])
    }

    @License(required = "allow_event_feature")
    def loadVenueLocationSeatMap() {
        Venue venue =  Venue.get(params.long("venueId"))
        def locations = venue.locations
        Long id = params.locationId.toLong()
        VenueLocation location = locations ? (id ? locations.find {it.id == id} : locations[0]) : null
        VenueLocationSection section
        if(params.section) {
            section = location.sections.find {
                "" + it.id == params.section
            }
        }
        if(!section) {
            section = location? location.sections[0] : null
        }
        render( view: "/plugins/event_management/site/printLocationSeats", model: [section: section, locations: locations, location: location])
    }

}