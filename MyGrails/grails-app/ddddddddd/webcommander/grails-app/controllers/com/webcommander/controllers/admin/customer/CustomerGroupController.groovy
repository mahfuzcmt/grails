package com.webcommander.controllers.admin.customer

import com.webcommander.common.CommonService
import com.webcommander.admin.CustomerGroup
import com.webcommander.admin.CustomerGroupService
import com.webcommander.constants.DomainConstants
import com.webcommander.throwables.AttachmentExistanceException
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.TaxCode
import com.webcommander.webcommerce.TaxService
import grails.converters.JSON

class CustomerGroupController {

    CustomerGroupService customerGroupService
    CommonService commonService
    TaxService taxService

    def loadAppView() {
        Integer count = customerGroupService.getCustomerGroupsCount(params)
        params.max = params.max ?: "10";
        List<CustomerGroup> customerGroups = commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.offset = offset
            customerGroupService.getCustomerGroups(params)
        }
        render view: "/admin/customerGroup/appView", model: [count: count, customerGroups: customerGroups];
    }

    def create () {
        CustomerGroup group = params.id ? CustomerGroup.get(params.long("id")) : new CustomerGroup()

        params.isDefault = (AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.TAX, "configuration_type") == DomainConstants.TAX_CONFIGURATION_TYPE.MANUAL) ? false : true
        List<TaxCode> codes = taxService.getTaxCodes(params)

        render(view: "/admin/customerGroup/infoEdit", model: [group: group, codes: codes])
    }

    def save () {
        params.remove("action")
        params.remove("controller")
        if(customerGroupService.save(params)){
            render([status: "success", message: g.message(code: "customer.group.save.success", args: [])] as JSON)
        } else {
            render([status: "error", message: g.message(code: "customer.group.save.failed")] as JSON)
        }
    }

    def delete() {
        Long id = params.long("id");
        try {
            if (customerGroupService.delete(id, params.at2_reply, params.at1_reply)) {
                render([status: "success", message: g.message(code: "customer.group.delete.success")] as JSON)
            } else {
                render([status: "error", message: g.message(code: "customer.delete.failure")] as JSON)
            }
        } catch(AttachmentExistanceException att) {
            render([status: "error", error_code: "attachment.exists", attachments: att.attachmentInfo] as JSON)
        }
    }

    def advanceFilter() {
        render(view: "/admin/customerGroup/filter", model: [get: 0]);
    }

    def deleteSelected() {
        List<Long> ids = params.list("ids").collect{it.toLong()};
        Integer result = customerGroupService.deleteSelected(ids)
        if(result == ids.size()) {
            render([status: "success", message: g.message(code: "selected.customer.groups.delete.success")] as JSON)
        }else if (result > 0) {
            render([status: "error", message: g.message(code: "selected.not.deleted", args: [ids.size() - result, ids.size(), g.message(code: "customer.groups")])] as JSON)
        } else {
            render([status: "error", message: g.message(code: "selected.customer.groups.could.not.delete")] as JSON)
        }
    }

    def isUnique() {
        customerGroupService.checkCustomerGroupNameForConflict(params.value, params.long("id"));
        render([status: "success", message: g.message(code: "provided.field.available", args: [params.field, params.value])] as JSON)
    }

    def loadCustomerGroupForMultiSelect() {
        params.max = params.max ?: "10"
        params.status = 'A'
        Integer count = customerGroupService.getCustomerGroupsCount(params)
        List<CustomerGroup> customerGroupList = commonService.withOffset(params.max, params.offset, count){max, offset, _count ->
            params.offset = offset;
            customerGroupService.getCustomerGroups(params)
        }
        render(view: "/admin/customerGroup/loadCustomerGroupMultiSelect", model: [count: count, customerGroupList: customerGroupList])
    }

    def loadStatusOption() {
        render view: "/admin/customerGroup/statusOption";
    }

    def changeStatus() {
        List<Long> ids = params.list("id")*.toLong();
        String status = params.status;
        if(customerGroupService.changeStatus(ids, status)) {
            render([status: "success", message: g.message(code: "customer.group.update.success")] as JSON)
        }else {
            render([status: "success", message: g.message(code: "customer.group.update.failed")] as JSON)
        }
    }

    def loadCustomerOption() {
        render view: "/admin/customerGroup/loadCustomerOption";
    }


    def assignCustomer() {
        if(customerGroupService.assignCustomer(params)) {
            render([status: "success", message: g.message(code: "customer.group.update.success")] as JSON)
        }else {
            render([status: "success", message: g.message(code: "customer.group.update.failed")] as JSON)
        }
    }
}
