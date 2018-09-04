package com.webcommander.plugin.order_custom_fields.controllers.admin

import com.webcommander.authentication.annotations.License
import com.webcommander.common.CommonService
import com.webcommander.plugin.order_custom_fields.OrderCheckoutFields
import com.webcommander.plugin.order_custom_fields.OrderCheckoutFieldsTitle
import com.webcommander.plugin.order_custom_fields.OrderCustomFieldsService
import grails.converters.JSON

class CustomFieldsAdminController {

    OrderCustomFieldsService orderCustomFieldsService
    CommonService commonService

    @License(required = "allow_order_custom_fields_feature")
    def loadAppView() {
        params.max = params.max ?: "10";
        Integer count = orderCustomFieldsService.getOrderCheckoutFieldsCount(params);
        List<OrderCheckoutFields> fields = commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.max = max;
            params.offset = offset;
            orderCustomFieldsService.getOrderCheckoutFields(params);
        }
        render(view: "/plugins/order_custom_fields/loadAppView", model: [fields: fields, count: count])
    }

    @License(required = "allow_order_custom_fields_feature")
    def createField() {
        OrderCheckoutFields field
        List<String> custom = []
        if(params.id) {
            field = OrderCheckoutFields.get(params.id)
            custom = field?.validation?.split(" ")?.toList()
            String maxlength = ""
            custom.each {
                if(it.indexOf("maxlength[") >= 0) {
                    maxlength = it
                }
            }
            custom?.removeAll(['email', 'alphanumeric', 'alphabetic', 'number', 'phone', 'required', maxlength])
        } else {
            field = new OrderCheckoutFields()
        }
        render(view: "/plugins/order_custom_fields/fieldInfoEdit", model: [field: field, custom: custom?.join(" ")])
    }

    @License(required = "allow_order_custom_fields_feature")
    def editOrderFieldLabel() {
        String title = ""
        if(OrderCheckoutFieldsTitle.count() > 0) {
            title = OrderCheckoutFieldsTitle.list().get(0).title
        }
        render(view: "/plugins/order_custom_fields/editGroupTitle", model: [title: title])
    }

    @License(required = "allow_order_custom_fields_feature")
    def saveField() {
        def result = orderCustomFieldsService.saveFields(params)
        if (result) {
            render([status: "success", message: g.message(code: "field.save.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "field.could.not.save")] as JSON)
        }
    }

    @License(required = "allow_order_custom_fields_feature")
    def saveFieldTitle() {
        def result = orderCustomFieldsService.saveFieldsGroupTitle(params)
        if (result) {
            render([status: "success", message: g.message(code: "title.group.field.set.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "title.group.field.could.not.update")] as JSON)
        }
    }

    def delete() {
        def result = orderCustomFieldsService.deleteFields(params.long("id"))
        if (result) {
            render([status: "success", message: g.message(code: "field.delete.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "field.could.not.delete")] as JSON)
        }
    }

    def deleteSelected() {
        List<Long> ids = params.list("ids").collect {it.toLong()}
        def result = orderCustomFieldsService.deleteSelectedFields(ids)
        if (result) {
            render([status: "success", message: g.message(code: "field.delete.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "field.could.not.delete")] as JSON)
        }
    }

}
