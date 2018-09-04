package com.webcommander.plugin.simplified_event_management.controllers.admin

import com.webcommander.JSONSerializableList
import com.webcommander.authentication.annotations.License
import com.webcommander.authentication.annotations.Restriction
import com.webcommander.authentication.annotations.Restrictions

import com.webcommander.plugin.simplified_event_management.SimplifiedEvent
import com.webcommander.common.CommonService
import com.webcommander.plugin.simplified_event_management.SimplifiedEventCheckoutField
import com.webcommander.plugin.simplified_event_management.SimplifiedEventCheckoutFieldsTitle
import com.webcommander.plugin.simplified_event_management.SimplifiedEventCustomFieldData
import com.webcommander.plugin.simplified_event_management.model.SimplifiedEventData
import com.webcommander.plugin.simplified_event_management.webmarketing.SimplifiedEventService
import com.webcommander.util.AppUtil
import grails.converters.JSON
import org.springframework.web.multipart.MultipartFile

class SimplifiedEventAdminController {
    SimplifiedEventService simplifiedEventService
    CommonService commonService

    @License(required = "allow_simplified_event_feature")
    @Restriction(permission = "simplified_event.view.list")
    def loadAppView() {
        render(view: "/plugins/simplified_event_management/admin/appView", model: [d: 0])
    }

    @License(required = "allow_simplified_event_feature")
    @Restriction(permission = "simplified_event.view.list")
    def loadEventView() {
        params.max = params.max ?: "10";
        Integer count = simplifiedEventService.getEventsCount(params)
        List<SimplifiedEvent> events = commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.offset = offset
            return simplifiedEventService.getEvents(params)
        }
        List status = simplifiedEventService.getStatusOfEvents(events)
        render(view: "/plugins/simplified_event_management/admin/event/appView", model: [events: events,  count: count, status : status])
    }

    @License(required = "allow_simplified_event_feature")
    @Restrictions([
            @Restriction(permission = "simplified_event.create", params_not_exist = "id"),
            @Restriction(permission = "simplified_event.edit", params_exist = "id", entity_param = "id", domain = SimplifiedEvent)
    ])
    def createEvent() {
        SimplifiedEvent event = params.id ? SimplifiedEvent.get(params.long("id")) : new SimplifiedEvent();
        render(view: "/plugins/simplified_event_management/admin/event/infoEdit", model: [event: event] )
    }

    @License(required = "allow_simplified_event_feature")
    def saveEvent() {
        Boolean success = true
        Long id = simplifiedEventService.saveEvent(params)
        if (id) {
            List<MultipartFile> file = request.getMultiFileMap().file;
            String removeFile = params["file-remove"]
            success = simplifiedEventService.saveEventFile(file ? file[0] : null, removeFile, id);
            List<Long> removeImgIds = params.list("remove-images")*.toLong();
            List<MultipartFile> images = request.getMultiFileMap().images;
            success = success && simplifiedEventService.saveEventImages(id, images, removeImgIds);
        }
        if(success) {
            render([status: "success", message: g.message(code: "event.save.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "event.save.error")] as JSON)
        }
    }

    @Restriction(permission = "simplified_event.remove", entity_param = "id", domain = SimplifiedEvent)
    def deleteEvent() {
        Long id = params.long("id");
        Boolean result = simplifiedEventService.deleteEvent(id);
        if(result) {
            render([status: "success", message: g.message(code: "event.delete.success")] as JSON);
        } else {
            render([status: "error", message: g.message(code: "event.delete.error")] as JSON);
        }
    }

    @Restriction(permission = "simplified_event.remove", entity_param = "ids", domain = SimplifiedEvent)
    def deleteSelectedEvents() {
        def ids = [];
        params.list("ids").each {
            ids.add(it.toLong(0))
        }
        int deleteCount = simplifiedEventService.deleteSelectedEvents(ids);
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
        render(view: "/plugins/simplified_event_management/admin/event/filter", model: [params: params])
    }

    def loadEventsForSelection() {
        params.max = params.max ?: 10
        params.remove("widgetId")
        params.remove("eventIds")
        params.isPublic = true
        params.startTime = new Date().gmt(AppUtil.session.timezone);
        Integer count = simplifiedEventService.getEventsCount(params)
        List<SimplifiedEvent> events = (List<SimplifiedEvent>)commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.offset = offset
            simplifiedEventService.getEvents(params)
        }
        render(view: "/plugins/simplified_event_management/admin/widget/eventSelectionList", model: [events: events, count: count])
    }

    @License(required = "allow_simplified_event_feature")
    def loadCalenderView() {
        render(view: "/plugins/simplified_event_management/admin/calendar/appView", model: [d:true])
    }

    def loadAllEventData() {
        String calendarType = params.calendarType
        Long selectedValue = params.selectedValue.toLong(0)
        List<SimplifiedEvent> events = []
        params.isPublic = true
        params.hasSession = false
        events = simplifiedEventService.getEvents(params)
        List<SimplifiedEventData> eventDataList = new JSONSerializableList<SimplifiedEventData>()
        events.each { event ->
            eventDataList.add(new SimplifiedEventData(event))
        }
        render(text: eventDataList.serialize())
    }

    def loadCustomFieldAppView() {
        Long eventId = params.eventId?.toLong();
        SimplifiedEvent event = SimplifiedEvent.get(eventId);
        def extraFields = SimplifiedEventCheckoutField.createCriteria().list {
            eq("event.id", eventId)
        };
        Boolean sortDesc = params.dir = "desc";
        render(view: "/plugins/simplified_event_management/admin/event/customFields", model: [event: event, fields: sortDesc ? extraFields.sort {a, b -> b.label <=> a.label} : extraFields.sort {a, b -> a.label <=> b.label}, count: extraFields.size()])
    }

    def editCustomField() {
        SimplifiedEventCheckoutField field;
        if(params.id) {
            field = SimplifiedEventCheckoutField.get(params.id);
        }else {
            field = new SimplifiedEventCheckoutField(event: SimplifiedEvent.proxy(params.eventId));
        }
        render(view: "/plugins/simplified_event_management/admin/event/customFieldInfoEdit", model: [field: field]);
    }

    def saveCustomField() {
        Boolean isFieldSaved = simplifiedEventService.saveCustomField(params);
        if (isFieldSaved) {
            render([status: "success", message: g.message(code: "field.save.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "field.could.not.save")] as JSON)
        }
    }

    def deleteCustomField() {
        Boolean isFieldRemoved = simplifiedEventService.deleteCustomField(params.id.toLong());
        if (isFieldRemoved) {
            render([status: "success", message: g.message(code: "field.remove.success")] as JSON);
        }else {
            render([status: "error", message: g.message(code: "field.remove.error")]);
        }
    }

    def editCustomFieldLabel() {
        SimplifiedEventCheckoutFieldsTitle title = SimplifiedEventCheckoutFieldsTitle.findByEvent(SimplifiedEvent.proxy(params.eventId));
        render(view: "/plugins/simplified_event_management/admin/event/editGroupTitle", model: [title: title, eventId: params.eventId]);
    }

    def saveCustomFieldTitle() {
        def isTitleSaved = simplifiedEventService.saveCustomFieldTitle(params);
        if(isTitleSaved) {
            render([status: "success", message: g.message(code: "title.group.field.set.success")] as JSON)
        }else {
            render([status: "error", message: g.message(code: "title.group.field.could.not.update")] as JSON)
        }
    }

    def loadCustomFieldDataAppView() {
        Long eventId = params.eventId?.toLong();
        def fieldData = SimplifiedEventCustomFieldData.createCriteria().list {
            eq("event.id", eventId)
        }
        Boolean sortDesc = params.dir = "desc";
        render(view: "/plugins/simplified_event_management/admin/event/customFieldData", model: [fieldData: sortDesc ? fieldData.sort {a, b -> b.order.id <=> a.order.id} : fieldData.sort {a, b -> a.order.id <=> b.order.id}, count: fieldData.size()])
    }

}
