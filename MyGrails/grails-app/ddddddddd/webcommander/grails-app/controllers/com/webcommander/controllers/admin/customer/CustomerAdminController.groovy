package com.webcommander.controllers.admin.customer

import com.webcommander.admin.*
import com.webcommander.authentication.annotations.License
import com.webcommander.authentication.annotations.Restriction
import com.webcommander.authentication.annotations.Restrictions
import com.webcommander.common.CommonService
import com.webcommander.common.Email
import com.webcommander.constants.DomainConstants
import com.webcommander.manager.HookManager
import com.webcommander.throwables.AttachmentExistanceException
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.Address
import com.webcommander.webcommerce.TaxCode
import com.webcommander.webcommerce.TaxService
import grails.converters.JSON
import groovy.json.JsonSlurper

class CustomerAdminController {
    CustomerService customerService
    CommonService commonService
    AdministrationService administrationService
    TaxService taxService

    @Restriction(permission = "customer.view.list")
    def loadAppView() {
        Integer count = customerService.getCustomerCount(params)
        params.max = params.max ?: "10";
        List<Customer> customers = commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.offset = offset
            customerService.getCustomers(params)
        }
        render view: "/admin/customer/appView", model: [count: count, customers: customers];
    }

    def loadCustomerBulkEditor() {
        JsonSlurper jsonSlurper = new JsonSlurper()
        List<Long> ids = jsonSlurper.parseText(params.ids).collect{it.toLong()}
        render(view: "/admin/customer/bulkEdit/bulkEditor", model: [customerIds: ids]);
    }

    def loadCustomerBulkProperties() {
        JsonSlurper jsonSlurper = new JsonSlurper()
        params.ids = jsonSlurper.parseText(params.ids).collect{it.toLong()}
        List<Customer> customers = params.ids ? Customer.findAllByIdInList(params.ids) : []
        switch (params.property) {
            case "basic":
                render(view: "/admin/customer/bulkEdit/basic", model: [customers: customers, count: customers.size()])
                break;
        }
    }

    def saveBasicBulkProperties() {
        def save = customerService.saveBasicBulkProperties(params);
        if(save) {
            render([status: "success", message: g.message(code: "customer.bulk.update.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "customer.could.not.bulk.update")] as JSON)
        }
    }

    @Restrictions([
            @Restriction(permission = "customer.create", params_not_exist = "id"),
            @Restriction(permission = "customer.edit.properties", entity_param = "id", domain = Customer)
    ])
    def create() {
        Customer customer = params.id ? Customer.get(params.long("id")) : new Customer();
        Address address = customer.address
        Long defaultCountryId = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "default_country").toLong();
        def states = administrationService.getStatesForCountry(address ? address.country.id : defaultCountryId)

        params.isDefault = (AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.TAX, "configuration_type") == DomainConstants.TAX_CONFIGURATION_TYPE.MANUAL) ? false : true
        List<TaxCode> codes = taxService.getTaxCodes(params)

        render view: "/admin/customer/infoEdit", model:[customer: customer, address : address, defaulCountryId: defaultCountryId, states: states, codes: codes];
    }

    @Restrictions([
            @Restriction(permission = "customer.create", params_not_exist = "id"),
            @Restriction(permission = "customer.edit.properties", entity_param = "id", domain = Customer)
    ])
    def save() {
        params.remove("action")
        params.remove("controller")
        if(params.deleteTrashItem){
            customerService.deleteTrashItemAndSaveCurrent(params.email);
        }
        params.isAdmin = true
        Customer customer = customerService.save(params)
        if(customer) {
            render([status: "success", message: g.message(code: "customer.save.success"), id: customer.id] as JSON)
        } else {
            render([status: "error", message: g.message(code: "customer.save.failed")] as JSON)
        }
    }

    def view() {
        Customer customer = Customer.get(params.long("id"));
        render view: "/admin/customer/view", model: [customer: customer];
    }

    def resetPassPopup() {
        Customer customer = Customer.get(params.long("id"));
        render(view: "/admin/customer/resetPassPopup", model: [customer: customer])
    }

    def changePass() {
        if (customerService.updatePassword(params.long("id"), params.password)) {
            render([status: "success", message: g.message(code: "customer.reset.password.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "customer.reset.password.failure")] as JSON)
        }
    }

    @Restriction(permission = "customer.remove", entity_param = "id", domain = Customer)
    def delete() {
        Long id = params.long("id");
        try {
            if (customerService.putCustomerInTrash(id, params.at2_reply, params.at1_reply)) {
                render([status: "success", message: g.message(code: "customer.delete.success")] as JSON)
            } else {
                render([status: "error", message: g.message(code: "customer.delete.failure")] as JSON)
            }
        } catch(AttachmentExistanceException att) {
            render([status: "error", error_code: "attachment.exists", attachments: att.attachmentInfo] as JSON)
        }
    }

    def advanceFilter() {
        params.stateName = "state"
        render(view: "/admin/customer/filter", model: [get: 0]);
    }

    @Restriction(permission = "customer.remove", entity_param = "id", domain = Customer)
    def deleteSelected() {
        List ids = params.list("ids").collect { it.toLong() }
        int deleteCount = customerService.putSelectedCustomersInTrash(ids)
        int total = ids.size()
        if (deleteCount == total) {
            render([status: "success", message: g.message(code: "selected.customers.delete.success")] as JSON)
        } else if(deleteCount == 0) {
            render([status: "error", message: g.message(code: "selected.customers.could.not.delete")] as JSON)
        } else {
            render([status: "warning", message: g.message(code: "selected.not.deleted", args: [total - deleteCount, total, g.message(code: "customers")])] as JSON)
        }
    }

    def isUnique() {
        render(commonService.responseForUniqueField(Customer, params.long("id"), params.field, params.value, params.customName) as JSON)
    }

    def loadCustomerForMultiSelect() {
        params.max = params.max ?: "10"
        params.offset = params.offset ?: "0"
        params.status = DomainConstants.CUSTOMER_STATUS.ACTIVE
        Integer count = customerService.getCustomerCount(params)
        List<Customer> customerList = commonService.withOffset(params.max, params.offset, count){max, offset, _count ->
            params.offset = offset;
            customerService.getCustomers(params)
        }
        render(view: "/admin/customer/loadCustomerMultiSelect", model: [count: count, customerList: customerList])
    }

    def selectCustomerAndGroups() {
        /*TODO: find better solution to get customer and group list for special rule without hook*/
        Map response = [success: false]
        HookManager.hook("customerAndGroupListForSpecialRule", response, params)
        if(response.success) {
            params.customer = response.customer
            params.customerGroup = response.customerGroup
        }
        List<Long> customerIds = params.customer ? params.list("customer").collect { it.toLong() } : []
        List<Long> customerGroupIds = params.customerGroup ? params.list("customerGroup").collect { it.toLong() } : []
        List<Customer> customers = customerIds ? Customer.where {
            id in customerIds
        }.list() : []
        List<CustomerGroup> customerGroups = customerGroupIds ? CustomerGroup.where {
            id in customerGroupIds
        }.list() : []
        render(view: "/admin/common/customerAndGroupSelection", model: [customers: customers, customerGroups: customerGroups, params: params])
    }

    def loadRecipientSelector() {
        List<Long> customerIds = params.list("customer").collect { it.toLong(0) }
        List<Long> customerGroupIds = params.list("customerGroup").collect { it.toLong(0) }
        List<String> recipientEmails = params.list("recipientEmail")
        List<String> recipientNames = params.list("recipientName")
        List<Customer> customers = customerIds ? Customer.createCriteria().list {
            inList("id", customerIds)
        } : []
        List<CustomerGroup> customerGroups = customerGroupIds ? CustomerGroup.createCriteria().list {
            inList("id", customerGroupIds)
        } : []
        List<Email> emailRecipients = []
        recipientEmails.eachWithIndex { email, idx ->
            emailRecipients.add(new Email(email: email, name: recipientNames[idx]))
        }
        String submitButtonText = params.submitButtonText ?: "done"
        render(view: "/admin/common/recipientSelector", model: [customers: customers, customerGroups: customerGroups, emailRecipients: emailRecipients,
                                                                submitButtonText: submitButtonText])
    }

    def restoreFromTrash(){
        def value = params.value;
        Long id = customerService.restoreCustomerFromTrash("userName",value);
        if(id){
            render([status: "success", message: g.message(code: "restored.successfully", args: ["Customer"]), type: "customer", id: id] as JSON)
        }
    }

    @License(required = "allow_store_credit_feature")
    @Restriction(permission = "customer.adjust.store.credit", entity_param = "id", domain = Customer)
    def loadStoreCredit() {
        Long id = params.long("id")
        Customer customer = Customer.get(id)
        render(view: "/admin/customer/storeCreditInfoEdit", model:[customer: customer])
    }

    @License(required = "allow_store_credit_feature")
    @Restriction(permission = "customer.adjust.store.credit", entity_param = "id", domain = Customer)
    def updateStoreCredit() {
        if(customerService.updateStoreCredit(params)) {
            render([status: "success", message: g.message(code: "store.credit.update.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "store.credit.update.failed")] as JSON)
        }
    }

    @Restriction(permission = "customer.view.store.credit.history", entity_param = "id", domain = StoreCreditHistory, owner_field = "createdBy")
    def loadStoreCreditHistory() {
        params.max = "5"
        params.offset = params.offset ?: "0"
        Integer count = customerService.getStoreCreditHistoryCount(params)
        List<StoreCreditHistory> histories = customerService.getStoreCreditHistory(params)
        render(view: "/admin/customer/storeCreditHistory", model: [histories: histories, count: count, max: params.max, offset: params.offset])
    }

    def loadStatusOption() {
        render view: "/admin/customer/statusOption";
    }

    def changeStatus() {
        List<Long> ids = params.list("id")*.toLong();
        String status = params.status;
        if(customerService.changeStatus(ids, status)) {
            render([status: "success", message: g.message(code: "customer.update.success")] as JSON)
        }else {
            render([status: "success", message: g.message(code: "customer.update.failed")] as JSON)
        }
    }

}
