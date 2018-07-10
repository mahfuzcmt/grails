package com.webcommander.controllers.rest.admin.webcommerce

import com.webcommander.admin.Customer
import com.webcommander.admin.CustomerService
import com.webcommander.authentication.annotations.Restriction
import com.webcommander.authentication.annotations.Restrictions
import com.webcommander.constants.DomainConstants
import com.webcommander.rest.throwable.ApiException
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.util.RestProcessor
import org.apache.http.HttpStatus

class ApiCustomerAdminController extends RestProcessor {
    CustomerService customerService
    static allowedMethods = [create: "POST"]

    @Restriction(permission = "customer.view.list")
    def list() {
        params.max = params.max ?: "-1"
        params.offset = params.offset ?: "0"
        List<Customer> customers = customerService.getCustomers(params)
        rest customers: customers
    }

    @Restriction(permission = "customer.view.list")
    def count() {
        Integer count = customerService.getCustomerCount(params)
        rest count: count
    }

    @Restriction(permission = "customer.view.list")
    def info() {
        Customer customer = Customer.get(params.id);
        if(!customer) {
            new ApiException("customer.not.found", HttpStatus.SC_NOT_FOUND)
        }
        rest customer: customer
    }

    @Restrictions([
            @Restriction(permission = "customer.create", params_not_exist = "id"),
            @Restriction(permission = "customer.edit.properties", entity_param = "id", domain = Customer)
    ])
    def create() {
        Map customerData = request.getAttribute(DomainConstants.REQUEST_ATTR_KEYS.REQUEST_BODY);
        try {
            Customer customer = customerService.save(customerData)
            rest customerId: customer.id
        } catch (ApplicationRuntimeException ex) {
            throw ApiException(ex.message)
        }
    }
}
