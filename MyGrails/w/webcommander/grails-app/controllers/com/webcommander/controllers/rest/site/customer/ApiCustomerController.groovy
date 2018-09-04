package com.webcommander.controllers.rest.site.customer

import com.webcommander.admin.Customer
import com.webcommander.admin.CustomerService
import com.webcommander.authentication.annotations.RequiresCustomer
import com.webcommander.constants.DomainConstants
import com.webcommander.oauth.OauthProviderService
import com.webcommander.oauth2.OAuthAccess
import com.webcommander.rest.throwable.ApiException
import com.webcommander.util.AppUtil
import com.webcommander.util.RestProcessor
import com.webcommander.util.ValueOrderedTreeMap
import com.webcommander.util.security.InformationEncrypter
import com.webcommander.webcommerce.Address
import com.webcommander.webcommerce.Order
import com.webcommander.webcommerce.OrderService
import grails.converters.JSON
import org.apache.commons.httpclient.HttpStatus

class ApiCustomerController extends RestProcessor {
    CustomerService customerService
    OrderService orderService
    OauthProviderService oauthProviderService

    def verify() {
        Customer customer;
        Map verificationInfo = null;
        if(params.credentials_type == "social_media_token") {
            verificationInfo = customerService.verifyCustomerByToken(params.media, params.token)
        } else {
            verificationInfo = customerService.verifyCustomer(params.email, params.password)
        }
        if(verificationInfo.status == "verified") {
            customer = verificationInfo.customer
        }
        if(customer) {
            Long clientId = request.getAttribute(DomainConstants.REQUEST_ATTR_KEYS.API_CLIENT);
            OAuthAccess access = oauthProviderService.generateToken(clientId, null, customer.id)
            rest status: 'success', access_token: access.accessToken, refresh_token: access.refreshToken
        } else {
            rest status: "error", message: g.message(code: verificationInfo.message), code: 401
        }
    }

    def register(){
        params.status = DomainConstants.CUSTOMER_STATUS.ACTIVE
        Customer customer = customerService.save(params)
        if(customer) {
            rest status: "success", id: customer.id
        } else {
            rest status: "error"
        }
    }

    def passwordReset() {
        String message, status = "error", email = params.email;
        Customer customer = null
        if(email && (customer = Customer.findByUserName(email))) {
            InformationEncrypter rsa = new InformationEncrypter();
            rsa.hideInfo("" + customer.id);
            rsa.hideInfo("" + customer.userName);
            def resetPasswordLink = app.baseUrl() + "customer/resetPassword?token=" + rsa.toString().encodeAsURL();
            try {
                customerService.sendResetPasswordMail(customer, resetPasswordLink)
                message = g.message(code: 'email.with.password.reset.link.sent')
                status = "success"
            } catch (Exception e) {
                status = g.message(code: 'could.not.send.mail.try.later')
            }
        } else {
            message = g.message(code: 'email.not.found')
        }
        rest([status: status, message: message])
    }

    @RequiresCustomer
    def billingList() {
        Customer customer = Customer.get(AppUtil.loggedCustomer);
        Map data = [active_billing: customer.activeBillingAddress, billings: customer.billingAddresses]
        rest data
    }

    @RequiresCustomer
    def info() {
        Customer customer = Customer.get(AppUtil.loggedCustomer);
        rest([customer: customer], [address: [details: true], billingAddresses: [details: true], shippingAddresses: [details: true]])
    }

    @RequiresCustomer
    def updateInfo() {
        params.status = DomainConstants.CUSTOMER_STATUS.ACTIVE
        Customer customer = Customer.get(AppUtil.loggedCustomer);
        params.email = customer.userName
        Boolean result = customerService.updateAccountDetails(customer, params)
        if(result) {
            rest status: "success", id: customer.id
        } else {
            rest status: "error"
        }
    }

    @RequiresCustomer
    def billingCreate() {
        Customer customer = Customer.get(AppUtil.loggedCustomer)
        params.addressType = "billing"
        Address address = customerService.saveAddress(customer, params);
        rest(status: "success", id: address.id)
    }

    @RequiresCustomer
    def billingDelete() {
        Customer customer = Customer.get(AppUtil.loggedCustomer)
        Long addressId = params.id ? params.id.toLong(0) : 0
        Address address = customer.activeBillingAddress
        if(address.id == addressId) {
            throw new ApiException("active.address.cannot.delete", HttpStatus.SC_FORBIDDEN)
        } else {
            Boolean result = addressId > 0 &&  customerService.deleteAddress(customer, addressId, true)
            if(result) {
                rest([status: "success", message: g.message(code: "address.delete.success")])
            } else {
                rest([status: "error", message: g.message(code: "address.delete.failed")])
            }
        }
    }

    @RequiresCustomer
    def billingSetDefault() {
        Customer customer = Customer.get(AppUtil.loggedCustomer)
        customerService.changeActiveAddress(customer, params.long("id"), true)
        rest status: "success"
    }

    @RequiresCustomer
    def shippingDelete() {
        Customer customer = Customer.get(AppUtil.loggedCustomer)
        Long addressId = params.id ? params.id.toLong(0) : 0
        Address address = customer.activeShippingAddress
        if(address.id == addressId) {
            throw new ApiException("active.address.cannot.delete", HttpStatus.SC_FORBIDDEN)
        } else {
            Boolean result = addressId > 0 && customerService.deleteAddress(customer, addressId, false)
            if(result) {
                rest([status: "success", message: g.message(code: "address.delete.success")])
            } else {
                rest([status: "error", message: g.message(code: "address.delete.failed")])
            }
        }
    }

    @RequiresCustomer
    def shippingList() {
        Customer customer = Customer.get(AppUtil.loggedCustomer);
        Map data = [active_shipping: customer.activeShippingAddress, shippings: customer.shippingAddresses]
        rest data
    }

    @RequiresCustomer
    def shippingCreate() {
        Customer customer = Customer.get(AppUtil.loggedCustomer)
        params.addressType = "shipping"
        Address address = customerService.saveAddress(customer, params);
        rest(status: "success", id: address.id)
    }

    @RequiresCustomer
    def shippingSetDefault() {
        Customer customer = Customer.get(AppUtil.loggedCustomer)
        customerService.changeActiveAddress(customer, params.long("id"), false)
        rest status: "success"
    }

    @RequiresCustomer
    def activeRegistrationFields() {
        def registrationConfigs = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.CUSTOMER_REGISTRATION_SETTINGS);
        def fieldConfigs = new ValueOrderedTreeMap({ a, b ->
            a.order <=> b.order
        } as Comparator);
        registrationConfigs.each {
            if(it.key.endsWith("_active") && it.value == "activated") {
                def base = it.key.substring(0, it.key.length() - 7)
                String order = base + "_order";
                String required = base + "_required";
                fieldConfigs.put(base, [required: registrationConfigs.get(required).toBoolean(true), order: registrationConfigs.get(order).toInteger(19)])
            }
        }
        if(registrationConfigs){
            render([status: "SUCCESS", fieldConfigs: fieldConfigs] as JSON);
        }
        else{
            render([status: "SUCCESS", error: "error"] as JSON);
        }
    }

    @RequiresCustomer
    def orderList() {
        params.customerId = AppUtil.loggedCustomer
        List<Order> orders = orderService.getOrders(params);
        Map config = [
            items: [
                details: true,
                marshallerExclude: ["order", "totalPriceConsideringConfiguration"]
            ]
        ]
        rest orders: orders, config
    }

    @RequiresCustomer
    def orderInfo() {
        Long id = params.long("id");
        Order order = Order.createCriteria().get {
            eq("id", id)
            eq("customerId", AppUtil.loggedCustomer)
        }
        if(!order) {
            throw new ApiException("order.not.found", HttpStatus.SC_NOT_FOUND)
        }
        Map config = [
            items: [
                details: true,
                marshallerExclude: ["order", "totalPriceConsideringConfiguration"]
            ],
            billing: [details: true],
            shipping: [details: true]
        ]
        rest order: order, config
    }

    def isExists() {
        Customer customer = Customer.findByUserName(params.email)
        if(customer) {
            rest  is_exist: true, id: customer.id
        } else {
            rest is_exist: false
        }
    }

    @RequiresCustomer()
    def sendStoreCreditRequest() {
        Customer customer = Customer.get(AppUtil.loggedCustomer)
        String message = params.message
        if(customerService.sendStoreCreditRequestMail(customer, message)) {
            rest([status: "success", message: g.message(code: "store.credit.request.send")])
        } else {
            throw new ApiException("store.credit.request.send.fail")
        }
    }
}
