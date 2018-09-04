package com.webcommander.plugin.general_event.controllers.admin

import com.webcommander.JSONSerializableList
import com.webcommander.authentication.annotations.License
import com.webcommander.plugin.general_event.GeneralEventCheckoutField
import com.webcommander.plugin.general_event.GeneralEventCheckoutFieldTitle
import com.webcommander.plugin.general_event.GeneralEventCustomFieldData
import com.webcommander.plugin.general_event.TicketInventoryAdjustment
import com.webcommander.plugin.general_event.VenueLocation
import com.webcommander.plugin.general_event.Equipment
import com.webcommander.plugin.general_event.GeneralEvent
import com.webcommander.plugin.general_event.GeneralEventService
import com.webcommander.common.CommonService
import com.webcommander.plugin.general_event.Venue
import com.webcommander.plugin.general_event.VenueLocationSection
import com.webcommander.plugin.general_event.model.GeneralEventData
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.throwables.AttachmentExistanceException
import com.webcommander.util.AppUtil
import grails.converters.JSON
import org.springframework.web.multipart.MultipartFile

class GeneralEventAdminController {

    GeneralEventService generalEventService
    CommonService commonService

    /////////////////////////////////////////////////// EVENT SECTION ///////////////////////////////////////////////////

    def loadEventAppView() {
        params.max = params.max ?: "10";
        Integer count = generalEventService.getEventsCount(params)
        List<GeneralEvent> events = commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.offset = offset
            return generalEventService.getEvents(params)
        }
        List status = generalEventService.getStatusOfEvents(events)
        render(view: '/plugins/general_event/admin/event/appView', model: [events: events,  count: count, status : status]);
    }

    def editEvent() {
        GeneralEvent event = params.id? GeneralEvent.get(params.id.toLong()) : new GeneralEvent();
        if(event.isRecurring) {
            throw new ApplicationRuntimeException("recurring.event.edit.fail");
        }else if(event.totalSoldTicket) {
            throw new ApplicationRuntimeException("event.update.error", [event.totalSoldTicket]);
        }
        render(view: '/plugins/general_event/admin/event/infoEdit', model: [event: event, timeList: generalEventService.timeList()]);
    }

    @License(required = "allow_general_event_feature")
    def saveEvent() {
        Boolean success = true
        Long id = generalEventService.saveEvent(params)
        if (id) {
            List<MultipartFile> file = request.getMultiFileMap().file;
            String removeFile = params["file-remove"]
            success = generalEventService.saveEventFile(file ? file[0] : null, removeFile, id);
            List<Long> removeImgIds = params.list("remove-images")*.toLong();
            List<MultipartFile> images = request.getMultiFileMap().images;
            success = success && generalEventService.saveEventImages(id, images, removeImgIds);
        }
        if(success) {
            render([status: "success", message: g.message(code: "event.save.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "event.save.error")] as JSON)
        }
    }

    def loadEventsForSelection() {
        params.max = params.max ?: 10
        params.remove("widgetId")
        params.remove("eventIds")
        params.isPublic = true
        params.startDateTime = new Date().gmt(AppUtil.session.timezone);
        Integer count = generalEventService.getEventsCount(params)
        List<GeneralEvent> events = (List<GeneralEvent>)commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.offset = offset
            generalEventService.getEvents(params)
        }
        render(view: "/plugins/general_event/admin/widget/eventSelectionList", model: [events: events, count: count])
    }

    def addVenueView() {
        render(view: "/plugins/general_event/admin/event/addVenueList", model: [venues: Venue.all])
    }

    def venueLocationForVenue(){
        List venueLocations = VenueLocation.findAllWhere(venue: Venue.get(params.venueId.toLong()))
        render(view: "/plugins/general_event/admin/event/addVenueLocationList", model: [locations: venueLocations])
    }

    def addEquipmentView() {
        render(view: "/plugins/general_event/admin/event/addEquipmentList", model: [equipments: Equipment.all])
    }

    def deleteEvent() {
        Long id = params.long("id");
        try {
            Boolean result = generalEventService.deleteEvent(id, params.at2_reply, params.at1_reply);
            if(result) {
                render([status: "success", message: g.message(code: "event.delete.success")] as JSON);
            } else {
                render([status: "error", message: g.message(code: "event.delete.error")] as JSON);
            }
        } catch(AttachmentExistanceException att) {
            render([status: "error", error_code: "attachment.exists", attachments: att.attachmentInfo] as JSON)
        }
    }

    def deleteSelectedEvents() {
        def ids = [];
        params.list("ids").each {
            ids.add(it.toLong(0))
        }
        int deleteCount = generalEventService.deleteSelectedEvents(ids);
        int total = ids.size();
        if (deleteCount == total) {
            render([status: "success", message: g.message(code: "selected.events.delete.success")] as JSON)
        } else if(deleteCount == 0) {
            render([status: "error", message: g.message(code: "selected.events.could.not.delete")] as JSON)
        } else {
            render([status: "warning", message: g.message(code: "selected.not.deleted", args: [total - deleteCount, total, g.message(code: "events")])] as JSON)
        }
    }

    def advanceFilter() {
        render(view: "/plugins/general_event/admin/event/filter", model: [params: params])
    }

    ///////////////////////////////////////////////////// CALENDAR SECTION ///////////////////////////////////////////////

    def loadCalenderView() {
        render(view: "/plugins/general_event/admin/calendar/appView", model: [d:true])
    }

    def loadAllEventData() {
        List<GeneralEvent> events = []
        params.isPublic = true
        params.hasSession = false
        events = generalEventService.getEvents(params)
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
        render(text: eventDataList.serialize())
    }

    //////////////////////////////////////////////////// EQUIPMENT SECTION //////////////////////////////////////////////

    def loadEquipmentAppView() {
        params.max = params.max ?: "10";
        Integer count = generalEventService.getEquipmentsCount(params)
        List<Equipment> equipments = commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.offset = offset
            return generalEventService.getEquipments(params)
        }
        render(view: "/plugins/general_event/admin/equipment/appView", model: [equipments: equipments, count: count])
    }

    def editEquipment() {
        Equipment equipment = params.id ? Equipment.get(params.long("id")) : new Equipment();
        render( view: "/plugins/general_event/admin/equipment/infoEdit", model: [equipment: equipment] )
    }

    def isEquipmentUnique() {
        if (commonService.isUnique(Equipment, params)) {
            render([status: "success", message: g.message(code: "provided.field.available", args: [params.field, params.value])] as JSON)
        } else {
            render([status: "error", message: g.message(code: "provided.field.value.exists", args: [params.field, params.value])] as JSON)
        }
    }

    def saveEquipment() {
        Boolean isSaved = generalEventService.saveEquipment(params);
        if(isSaved) {
            render([status: 'success', message: g.message(code: 'equipment.create.success')] as JSON);
        }else {
            render([status: 'error', message: g.message(code: 'equipment.create.error')] as JSON);
        }
    }

    def deleteEquipment() {
        Long id = params.long("id")
        try {
            if(generalEventService.deleteEquipment(id, params.at2_reply, params.at1_reply)) {
                render([status: "success", message: g.message(code: "equipment.delete.success")] as JSON)
            }else {
                render([status: "error", message: g.message(code: "equipment.delete.error")] as JSON)
            }
        } catch (com.webcommander.throwables.AttachmentExistanceException att) {
            render([status: "error", error_code: "attachment.exists", attachments: att.attachmentInfo] as JSON)
        }
    }

    def deleteSelectedEquipments() {
        def ids = [];
        params.list("ids").each {
            ids.add(it.toLong(0))
        }
        int deleteCount = generalEventService.deleteSelectedEquipments(ids);
        int total = ids.size();
        if (deleteCount == total) {
            render([status: "success", message: g.message(code: "selected.equipments.delete.success")] as JSON)
        } else if(deleteCount == 0) {
            render([status: "error", message: g.message(code: "selected.equipments.could.not.delete")] as JSON)
        } else {
            render([status: "warning", message: g.message(code: "selected.not.deleted", args: [total - deleteCount, total, g.message(code: "equipments")])] as JSON)
        }
    }

    /////////////////////////////////////////////// VENUE SECTION //////////////////////////////////////////////////////

    def loadVenueAppView() {
        List<Venue> venues = Venue.all?.sort {-it.id};
        render(view: '/plugins/general_event/admin/venue/appView', model: [venues: venues, selected: venues? venues.first().id : 0])
    }

    def loadLeftVenuePanel() {
        List<Venue> venueList = Venue.all?.sort {-it.id};
        Long selected = params.long("selected");
        selected = selected ?: (venueList ? venueList.first().id : 0)
        render(view: "/plugins/general_event/admin/venue/leftPanel", model: [venues: venueList, selected: selected]);
    }

    def loadRightVenuePanel() {
        Venue venue = Venue.get(params.selected);
        def count = Venue.count;
        venue = venue ?: (count ? venue.first() : new Venue());
        render(view: "/plugins/general_event/admin/venue/rightPanel", model: [venue: venue]);
    }

    def createVenue() {
        Long venueId = generalEventService.createVenue(params);
        if(venueId) {
            render([status: "success", message: g.message(code: "venue.save.success"), id: venueId] as JSON);
        } else {
            render([status: "error", message: g.message(code: "venue.save.failure")] as JSON);
        }
    }

    def saveVenue() {
        Long id = generalEventService.saveVenue(params)
        if (id) {
            render([status: "success", message: g.message(code: "venue.save.success"), id: id] as JSON)
        } else {
            render([status: "error", message: g.message(code: "venue.save.failure")] as JSON)
        }
    }

    def  deleteVenue(){
        Long id = params.long("id");
        try {
            Boolean result = generalEventService.deleteVenue(id, params.at2_reply, params.at1_reply);
            if(result) {
                render([status: "success", message: g.message(code: "venue.delete.success")] as JSON);
            } else {
                render([status: "error", message: g.message(code: "venue.delete.error")] as JSON);
            }
        } catch(AttachmentExistanceException att) {
            render([status: "error", error_code: "attachment.exists", attachments: att.attachmentInfo] as JSON)
        }
    }

    def editVenueLocation() {
        VenueLocation location = params.id? VenueLocation.get(params.id.toLong()) : new VenueLocation();
        render(view: '/plugins/general_event/admin/venue/location/infoEdit', model: [venueId: params.venueId, location: location])
    }

    def saveVenueLocation() {
        Long id = generalEventService.saveVenueLocation(params)
        if (id) {
            List<MultipartFile> images = request.getMultiFileMap().images;
            List<Long> removeImgIds = params.list("remove-images")*.toLong();
            generalEventService.saveLocationImages(id, images, removeImgIds);
            render([status: "success", message: g.message(code: "location.save.success")] as JSON)
        }else {
            render([status: "error", message: g.message(code: "location.save.error")] as JSON)
        }
    }

    def deleteLocation() {
        try {
            if(generalEventService.deleteVenueLocation(params.long("id"), params.at2_reply, params.at1_reply)) {
                render([status: "success", message: g.message(code: "location.delete.success")] as JSON)
            }else {
                render([status: "error", message: g.message(code: "location.delete.error")] as JSON)
            }
        } catch (AttachmentExistanceException att) {
            render([status: "error", error_code: "attachment.exists", attachments: att.attachmentInfo] as JSON)
        }
    }

    def editSection() {
        VenueLocationSection section = params.id? VenueLocationSection.get(params.id.toLong()) : new VenueLocationSection();
        render(view: '/plugins/general_event/admin/venue/section/infoEdit', model: [section: section, locationId: params.locationId, ticketExists: section.id ? TicketInventoryAdjustment.countBySection(section) > 0 : false])
    }

    def isUniqueVenueLocationSection() {
        params.compositeValue = params.compositeValue.toLong(0)
        if (commonService.isUnique(VenueLocationSection, params)) {
            render([status: "success", message: g.message(code: "provided.field.available", args: [params.field, params.value])] as JSON)
        } else {
            render([status: "error", message: g.message(code: "provided.field.value.exists", args: [params.field, params.value])] as JSON)
        }
    }

    def saveVenueLocationSection() {
        if(generalEventService.saveVenueLocationSection(params)) {
            render([status: "success", message: g.message(code: "section.save.success")] as JSON)
        }else {
            render([status: "error", message: g.message(code: "section.save.failure")] as JSON)
        }
    }

    def deleteVenueLocationSection() {
        Long sectionId = params.long("id")
        VenueLocationSection section = VenueLocationSection.get(sectionId)
        if(section.ticket.size()) {
            render([status: "error", message: g.message(code: "purchased.ticket.exists")] as JSON)
        }else {
            try {
                if(generalEventService.deleteVenueLocationSection(sectionId, params.at2_reply, params.at1_reply)) {
                    render([status: "success", message: g.message(code: "section.delete.success")] as JSON)
                } else {
                    render([status: "error", message: g.message(code: "section.delete.failure")] as JSON)
                }
            } catch(AttachmentExistanceException att) {
                render([status: "error", error_code: "attachment.exists", attachments: att.attachmentInfo] as JSON)
            }
        }
    }

    def loadLocationView() {
        render(view: '/plugins/general_event/admin/venue/location/appView', model: [locations: VenueLocation.all])
    }

    def loadSectionView() {
        render(view: '/plugins/general_event/admin/venue/section/appView', model: [sections: VenueLocationSection.all])
    }

    def loadSeatMapView() {
        Venue venue = params.venueId ? Venue.get(params.long("venueId")) : Venue.first();
        def locations = venue?.locations
        Long id = params.locationId.toLong()
        VenueLocation location = locations ? (id ? locations.find {it.id == id} ?: locations[0] : locations[0]) : null
        VenueLocationSection section
        if(params.section) {
            section = location?.sections.find {
                "" + it.id == params.section
            }
        }
        if(!section) {
            section = location? location.sections[0] : null
        }
        render( view: "/plugins/general_event/site/printLocationSeats", model: [section: section, locations: locations, location: location, venue: venue, venues: Venue.all])
    }

    //////////////////////////////////////////////// CUSTOM FIELDS SECTION /////////////////////////////////////////////

    def loadCustomFieldAppView() {
        Long eventId = params.eventId?.toLong();
        def extraFields = GeneralEventCheckoutField.createCriteria().list {
            eq("event.id", eventId)
        };
        Boolean sortDesc = params.dir = "desc";
        render(view: "/plugins/general_event/admin/event/customFields", model: [fields: sortDesc ? extraFields.sort {a, b -> b.label <=> a.label} : extraFields.sort {a, b -> a.label <=> b.label}, count: extraFields.size()])
    }

    def editCustomField() {
        GeneralEventCheckoutField field;
        if(params.id) {
            field = GeneralEventCheckoutField.get(params.id);
        }else {
            field = new GeneralEventCheckoutField(event: GeneralEvent.proxy(params.eventId));
        }
        render(view: "/plugins/general_event/admin/event/customFieldInfoEdit", model: [field: field]);
    }

    def saveCustomField() {
        Boolean isFieldSaved = generalEventService.saveCustomField(params);
        if (isFieldSaved) {
            render([status: "success", message: g.message(code: "event.custom.field.save.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "field.could.not.save")] as JSON)
        }
    }

    def deleteCustomField() {
        Boolean isFieldRemoved = generalEventService.deleteCustomField(params.id.toLong());
        if (isFieldRemoved) {
            render([status: "success", message: g.message(code: "field.remove.success")] as JSON);
        }else {
            render([status: "error", message: g.message(code: "field.remove.error")]);
        }
    }

    def editCustomFieldLabel() {
        GeneralEventCheckoutFieldTitle title = GeneralEventCheckoutFieldTitle.findByEvent(GeneralEvent.proxy(params.eventId));
        render(view: "/plugins/general_event/admin/event/editGroupTitle", model: [title: title, eventId: params.eventId]);
    }

    def saveCustomFieldTitle() {
        def isTitleSaved = generalEventService.saveCustomFieldTitle(params);
        if(isTitleSaved) {
            render([status: "success", message: g.message(code: "title.group.field.set.success")] as JSON)
        }else {
            render([status: "error", message: g.message(code: "title.group.field.could.not.update")] as JSON)
        }
    }

    def loadAttendeeDetailsView() {
        Long eventId = params.eventId?.toLong();
        def fieldData = GeneralEventCustomFieldData.createCriteria().list {
            if(params.isRecurring.toBoolean()) {
                eq("recurringEvent.id", eventId)
            } else{
                eq("generalEvent.id", eventId)
            }
        }
        Boolean sortDesc = params.dir = "desc";
        render(view: "/plugins/general_event/admin/event/attendeeDetails", model: [fieldData: sortDesc ? fieldData.sort {a, b -> b.order.id <=> a.order.id} : fieldData.sort {a, b -> a.order.id <=> b.order.id}, count: fieldData.size()])
    }

}
