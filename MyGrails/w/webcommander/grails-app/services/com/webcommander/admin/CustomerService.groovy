package com.webcommander.admin

import com.webcommander.annotations.Initializable
import com.webcommander.beans.SiteMessageSource
import com.webcommander.common.CommanderMailService
import com.webcommander.common.CommonService
import com.webcommander.config.StoreDetail
import com.webcommander.constants.DomainConstants
import com.webcommander.events.AppEventManager
import com.webcommander.manager.HookManager
import com.webcommander.tenant.Thread
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.throwables.AttachmentExistanceException
import com.webcommander.util.AppUtil
import com.webcommander.util.StringUtil
import com.webcommander.util.TrashUtil
import com.webcommander.webcommerce.Address
import com.webcommander.webcommerce.Order
import com.webcommander.webcommerce.OrderComment
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.transaction.NotTransactional
import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.methods.GetMethod
import org.grails.plugins.web.taglib.ApplicationTagLib
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import javax.servlet.http.HttpSession

@Transactional
@Initializable
class CustomerService {
    CommanderMailService commanderMailService
    CommonService commonService
    TrashService trashService
    SiteMessageSource siteMessageSource
    @Autowired
    @Qualifier("org.grails.plugins.web.taglib.ApplicationTagLib")
    ApplicationTagLib g

    final static ArrayList<Map> restrictedRegistrationNames = [
        [firstName: "guest", lastName: "customer", logic: "and"],
        [firstName: "admin", lastName: "admin", logic: "or"],
        [firstName: "administrator", lastName: "administrator", logic: "or"],
        [firstName: "guest customer", lastName: "", logic: "and"]
    ];


    static void initialize() {
        HookManager.register("customer-group-delete-at2-count", {response, id ->
            Integer count = Customer.createCriteria().count{
                groups {
                    eq("id", id)
                }
            }
            if (count) {
                response."customer" = count
            }
            return response;
        })

        HookManager.register("customer-group-delete-at2-list", {response, id ->
            List customers = Customer.createCriteria().list{
                groups {
                    eq("id", id)
                }
            }
            if (customers.size() > 0) {
                response."customer" = customers.collect { it.fullName }
            }
            return response;
        })

        AppEventManager.on("before-customer-group-delete", {id ->
            Customer.createCriteria().list {
                groups {
                    eq("id", id)
                }
            }.each { customer ->
                CustomerGroup customerGroup = CustomerGroup.get(id)
                customerGroup.removeFromCustomers(customer)
            }
        })
    }

    private Closure getCriteriaClosure(Map params) {
        def session = AppUtil.session;
        Closure closure = {
            if (params.searchText) {
                or {
                    ilike("firstName", "%${params.searchText.trim().encodeAsLikeText()}%")
                    ilike("lastName", "%${params.searchText.trim().encodeAsLikeText()}%")
                    ilike("userName", "%${params.searchText.trim().encodeAsLikeText()}%")
                    sqlRestriction "CONCAT(this_.first_name, ' ', this_.last_name) like '%${params.searchText.trim().encodeAsLikeText()}%'"
                }
            }
            if (params.name) {
                or {
                    ilike("firstName", "%${params.name.trim().encodeAsLikeText()}%")
                    ilike("lastName", "%${params.name.trim().encodeAsLikeText()}%")
                    sqlRestriction "CONCAT(this_.first_name, ' ', this_.last_name) like '%${params.name.trim().encodeAsLikeText()}%'"
                }
            }
            if(params.email) {
                ilike("userName", "%${params.email.trim().encodeAsLikeText()}%")
            }
            if(params.isCompany) {
                Boolean isCompany = params.isCompany == "true" ? true : false;
                eq("isCompany", isCompany)
            }
            if(params.city || params.postCode || params.phone || params.fax || params.mobile || params.country || params.address) {
                address {
                    if(params.city) {
                        ilike("city", "%${params.city.trim().encodeAsLikeText()}%")
                    }
                    if(params.address) {
                        or {
                            ilike("addressLine1", "%${params.address.trim().encodeAsLikeText()}%")
                            ilike("addressLine2", "%${params.address.trim().encodeAsLikeText()}%")
                        }
                    }
                    if(params.postCode) {
                        ilike("postCode", "%${params.postCode.trim().encodeAsLikeText()}%")
                    }
                    if(params.phone) {
                        ilike("phone", "%${params.phone.trim().encodeAsLikeText()}%")
                    }
                    if(params.fax) {
                        ilike("fax", "%${params.fax.trim().encodeAsLikeText()}%")
                    }
                    if(params.mobile) {
                        ilike("mobile", "%${params.mobile.trim().encodeAsLikeText()}%")
                    }
                    if(params.state) {
                       eq("state.id", params.state.toLong())
                    }
                    if(params.country) {
                        if(params.country) {
                            eq("country.id", params.country.toLong())
                        }
                    }

                }
            }
            if (params.status) {
                eq("status", params.status)
            }
            if(params.storeCreditFrom) {
                Double storeCreditFrom = Double.parseDouble(params.storeCreditFrom);
                ge("storeCredit", storeCreditFrom)
            }
            if(params.storeCreditTo) {
                Double storeCreditFrom = Double.parseDouble(params.storeCreditTo);
                le("storeCredit", storeCreditFrom)
            }
            if (params.createdFrom) {
                Date date = params.createdFrom.dayStart.gmt(session.timezone);
                ge("created", date);
            }
            if (params.createdTo) {
                Date date = params.createdTo.dayEnd.gmt(session.timezone);
                le("created", date);
            }
            if (params.updatedFrom) {
                Date date = params.updatedFrom.dayStart.gmt(session.timezone);
                ge("updated", date);
            }
            if (params.updatedTo) {
                Date date = params.updatedTo.dayEnd.gmt(session.timezone);
                le("updated", date);
            }
            if(params.ids) {
                inList("id", params.list("ids").collect {it.toLong()})
            }
            eq("isInTrash", false);
        }
        return closure;
    }

    List<Customer> getCustomers (Map params) {
        def listMap = [max: params.max, offset: params.offset];
        return Customer.createCriteria().list(listMap) {
            and getCriteriaClosure(params);
            order(params.sort ?: "firstName", params.dir ?: "asc");
        }
    }

    private Closure getCriteriaClosureForTrash(Map params) {
        def session = AppUtil.session;
        Closure closure = {
            if (params.searchText) {
                ilike("firstName", "%${params.searchText.trim().encodeAsLikeText()}%")
            }
            if (params.deletedFrom) {
                Date date = params.deletedFrom.dayStart.gmt(session.timezone);
                ge("updated", date);
            }
            if (params.deletedTo) {
                Date date = params.deletedTo.dayEnd.gmt(session.timezone);
                le("updated", date);
            }
            eq("isInTrash", true);
        }
        return closure;
    }

    Customer getCustomer(Long id) {
        return Customer.get(id)
    }
   
    Integer getCustomerCount (Map params) {
        return Customer.createCriteria().get {
            and getCriteriaClosure(params);
            projections {
                rowCount();
            }
        }
    }

    def save (Map params) {
        String registrationStatus = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.CUSTOMER_REGISTRATION_SETTINGS, "registration_status_type")
        Boolean sendMail = false;
        Boolean sendApprovalMailToCustomer = false;
        Boolean sendApproveCustomerMailToAdmin = false;
        Long id = params.id.toLong(0)
        checkCustomerUserNameForConflict(params.email, id);
        checkCustomerNameForRestrictionConstraints(params.firstName, params.lastName);
        Customer customer = id ? Customer.get(id) : new Customer();

        customer.firstName = params.firstName
        customer.lastName = params.lastName
        customer.userName = params.email
        if(params.isCompany == "false") {
            customer.sex = params.sex
        } else {
            customer.abn = params.abn
            customer.companyName = params.companyName
        }
        customer.abnBranch = params.abnBranch
        if(params.storeCredit) {
            customer.storeCredit = params.storeCredit.toDouble()
        }
        if(customer.status ==  DomainConstants.CUSTOMER_STATUS.APPROVAL_AWAITING && params.status == DomainConstants.CUSTOMER_STATUS.ACTIVE) {
            sendApprovalMailToCustomer = true;
        }
        customer.isCompany = params.isCompany == "true" ? true : false
        if (id) {
            customer.address = saveCustomerAddress(params, customer.address.id)
            customer.status = params.status ?: customer.status
        } else {
            if(params.password){
                customer.password = params.password.encodeAsMD5();
            } else {
                sendMail = true;
                customer.password = "change it".encodeAsMD5();
            }
            if(registrationStatus == DomainConstants.CUSTOMER_REG_TYPE.AWAITING_APPROVAL && (params.isAdmin != true || !AppUtil.getLoggedOperator())){
                customer.status = DomainConstants.CUSTOMER_STATUS.APPROVAL_AWAITING
                sendApproveCustomerMailToAdmin = true
                sendMail = false
            } else {
                customer.status = params.status ?: DomainConstants.CUSTOMER_STATUS.ACTIVE
            }
            customer.address = saveCustomerAddress(params)
            customer.activeBillingAddress = customer.activeBillingAddress ?: saveCustomerAddress(params)
            customer.activeShippingAddress = customer.activeShippingAddress ?: saveCustomerAddress(params)
            customer.addToBillingAddresses(customer.activeBillingAddress)
            customer.addToShippingAddresses(customer.activeShippingAddress)
        }

        customer.defaultTaxCode = params.defaultTaxCode

        AppEventManager.fire("before-customer-create", [customer])
        customer.save();

        if(!customer.hasErrors()) {
            AppEventManager.fire("customer-create", [customer])
            try {
                if (sendApproveCustomerMailToAdmin) {
                    commanderMailService.sendApproveCustomerMailToAdmin(customer)
                }
                if (sendApprovalMailToCustomer) {
                    commanderMailService.sendApprovalMailToCustomer(customer)
                }
                if(sendMail) {
                    commanderMailService.sendCreateCustomerMail(customer);
                }
            } catch (Exception e) {
            }
            return customer;
        }
        return null
    }

    def saveCustomerAddress(Map params, Long id = 0) {
        Address address = id ? Address.get(id) : new Address();
        address.firstName = params.firstName
        address.lastName = params.lastName
        address.addressLine1 = params.addressLine1
        address.addressLine2 = params.addressLine2
        address.postCode = params.postCode
        address.phone = params.phone
        address.mobile = params.mobile
        address.fax = params.fax
        address.email = params.email
        address.country = Country.get(params.country.id)
        address.state = State.get(params.state ? params.state.id : 0)
        address.city = params.city
        address.save();
        return address;
    }

    public boolean saveAddress(Address address, Map params) {
        address.firstName = params.firstName
        address.lastName = params.lastName
        address.addressLine1 = params.addressLine1
        address.addressLine2 = params.addressLine2
        address.postCode = params.postCode
        address.phone = params.phone
        address.mobile = params.mobile
        address.fax = params.fax
        address.email = params.email
        address.country = Country.get(params.countryId.toString().toLong(0))
        address.state = State.get(params.state ? params.state.id : 0)
        address.city = params.city
        if(address.id) {
            address.merge()
        } else {
            address.save();
        }
        return !address.hasErrors()
    }

    public boolean updateAccountDetails(Customer customer, Map params) {
        Address address = customer.address ?: new Address()
        if(saveAddress(address, params)) {
            customer.address = address
            customer.userName = params.email
            customer.firstName = params.firstName
            if(params.lastName) {
                customer.lastName = params.lastName
            }
            customer.merge()
        }
        return !customer.hasErrors() && !address.hasErrors()
    }

    public Address saveAddress(Customer customer, Map params) {
        boolean isBilling = params.addressType != "shipping"
        Address address = params.id ? Address.get(params.id.toString().toLong(0)) : new Address()
        if(saveAddress(address, params)) {
            if(!params.id) {
                isBilling ? customer.billingAddresses.add(address) : customer.shippingAddresses.add(address)
                customer.merge()
            }
        }
        if(!customer.hasErrors() && !address.hasErrors()) {
            return address
        }
        return null
    }

    void saveAddress(Customer customer, Address address, String type) {
        address.save()
        if(type == "shipping") {
            customer.addToShippingAddresses(address)
        } else {
            customer.addToBillingAddresses(address)
        }
        customer.merge()
    }

    public boolean deleteAddress(Customer customer, Long addressId, boolean isBilling) {
        Address addressToDelete = Address.get(addressId)
        List addresses = isBilling ? customer.billingAddresses : customer.shippingAddresses
        Long countBeforeDelete = addresses.size()
        if(addresses.contains(addressToDelete)) {
            isBilling ? customer.removeFromBillingAddresses(addressToDelete) : customer.removeFromShippingAddresses(addressToDelete)
            customer.merge(flush: true)
            Address.findById(addressId)?.delete()
            Long countAfterDelete = isBilling ? customer.billingAddresses.size() : customer.shippingAddresses.size()
            return countAfterDelete == countBeforeDelete - 1
        }
        return false
    }

    public boolean changeActiveAddress(Customer customer, Long addressId, isBilling) {
        Address addressToActive = Address.get(addressId)
        if(isBilling) {
            customer.activeBillingAddress = addressToActive
        } else {
            customer.activeShippingAddress = addressToActive
        }
        customer.merge()
        return !customer.hasErrors()
    }

    public boolean sendStoreCreditRequestMail(Customer customer, String msg) {
        def storeDetail = StoreDetail.first()
        Map macrosAndTemplate = commanderMailService.getMacrosAndTemplateByIdentifier("store-credit-request")
        Map refinedMacros = macrosAndTemplate.commonMacros
        macrosAndTemplate.macros.each {
            switch (it.key.toString()) {
                case "customer_name" :
                    refinedMacros[it.key] = customer.firstName + " " + customer.lastName
                    break;
                case "customer_email" :
                    refinedMacros[it.key] = customer.address.email
                    break;
                case "message" :
                    refinedMacros[it.key] = msg?.encodeAsBMHTML()
                    break;
            }
        }
        commanderMailService.sendMail(macrosAndTemplate.emailTemplate, macrosAndTemplate.activeHtml, macrosAndTemplate.activeText, refinedMacros, storeDetail.address.email, customer.address.email)
    }

    def deleteCustomer(Long id) {
        Customer customer = Customer.proxy(id);
        try {
            List<Long> ids = customer.billingAddresses.id + customer.shippingAddresses.id + customer.address.id
            customer.delete(flush: true);
            if(ids.size()) {
                Address.where {
                    id in ids
                }.deleteAll()
            }
        } catch (Throwable t) {
            return false;
        }
        return true;
    }

    public boolean putCustomerInTrash(Long id, boolean force = false) {
        if(force) {
            return putCustomerInTrash(id, "yes", "with_children");
        } else {
            return putCustomerInTrash(id, null, null);
        }
    }

    public boolean putCustomerInTrash(Long id, String at2_reply, String at1_reply) {
        TrashUtil.preProcessPutInTrash("customer", id, at2_reply != null, at1_reply != null)
        Customer customer = Customer.get(id);
        return trashService.putObjectInTrash("customer", customer, at1_reply)
    }

    def deleteSelected(List<Long> ids) {
        boolean deleted = true;
        ids.find{ id->
            if (!deleteCustomer(id)) {
                deleted = false;
                return true
            }
        }
        return deleted;
    }

    int putSelectedCustomersInTrash(List<Long> ids) {
        int removeCount = 0;
        ids.each { id ->
            try {
                if(putCustomerInTrash(id, true)) {
                    removeCount++;
                }
            } catch(AttachmentExistanceException att) {
            }
        }
        return removeCount;
    }

    public Long countCustomersInTrash() {
        return Customer.createCriteria().count {
            eq "isInTrash", true
        }
    }

    public Map getCustomersInTrash(int offset, int max, String sort, String dir) {
        String currentSort = (sort == "name") ? "firstName" : sort
        return [Customer: Customer.createCriteria().list(offset: offset, max: max) {
            eq "isInTrash", true
            order(currentSort, dir?:"asc")
        }.collect {
            [id: it.id, name: it.fullName, updated: it.updated]
        }]
    }

    public Long countCustomersInTrash(Map params){
        return Customer.createCriteria().count {
            and getCriteriaClosureForTrash(params)
        }
    }

    public Map getCustomersInTrash(Map params) {
        String currentSort = (params.sort == "name") ? "firstName" : params.sort
        def listMap = [ offset: params.offset, max: params.max];
        return [Customer: Customer.createCriteria().list() {
            and getCriteriaClosureForTrash(params)
            order(currentSort, params.dir ?: "asc")
        }.collect{
            [id: it.id, name: it.fullName, updated: it.updated]
        }]
    }

    public boolean restoreCustomerFromTrash(Long id) {
        Customer customer = Customer.get(id);
        if (!customer) {
            return false
        }
        customer.isInTrash = false
        customer.merge()
        return !customer.hasErrors()
    }

    public Long restoreCustomerFromTrash(String field,String value){
        Customer customer = Customer.createCriteria().get {
            eq(field, value)
        }
        customer.isInTrash = false;
        customer.merge();
        return customer.id;
    }

    public boolean deleteTrashItemAndSaveCurrent(String userName){
        Customer customer = Customer.createCriteria().get {
            eq("userName", userName)
        }
        if(customer) {
            AppEventManager.fire("before-customer-delete", [customer.id])
            deleteCustomer(customer.id);
            AppEventManager.fire("customer-delete", [customer.id])
        }
        return !customer.hasErrors();
    }

    void checkCustomerUserNameForConflict(String userName, Long id = 0) {
        Integer count = Customer.createCriteria().count {
            if (id) {
                ne("id", id)
            }
            eq("userName", userName)
        }
        if (count > 0) {
            throw new ApplicationRuntimeException("email.already.registered");
        }
    }

    void checkCustomerNameForRestrictionConstraints(String firstName, String lastName) {
        for (int iterator = 0; iterator < restrictedRegistrationNames.size(); iterator++){
            def restriction = restrictedRegistrationNames[iterator];
            String fname =  firstName.trim();
            String lname;
            if(lastName) {
                lname = lastName.trim();
            } else {
                lname = ""
            }
            int firstNameMatched = fname.compareToIgnoreCase(restriction.firstName);
            int lastNameMatched = lname.compareToIgnoreCase(restriction.lastName);
            boolean restrictionRequired = false;
            if (restriction.logic == "and" && (firstNameMatched == 0 && lastNameMatched == 0)) {
                restrictionRequired = true;
            } else if (restriction.logic == "or" && (firstNameMatched == 0 || lastNameMatched == 0)) {
                restrictionRequired = true;
            }
            if (restrictionRequired) {
                throw new ApplicationRuntimeException("customer.name.restricted.pick.different");
            }
        }
    }

    def verifyCustomer (String userName, String password) {
        String status = "verified";
        String message;
        Customer customer = Customer.findByUserNameAndPasswordAndIsInTrash(userName, password.encodeAsMD5(), false);
        if(customer == null) {
            status = "invalid";
            message = "login.failed.invalid.id.or.password";
        } else if (customer.status != 'A') {
            status = "invalid";
            message = "customer.account.inactive.message";
        }
        return([status: status, message: message, customer: customer])
    }

    def clearCustomerSession(HttpSession session) {
        session.customer = null;
    }

    boolean updatePassword(Long id, String password) {
        return Customer.where {
            id == id
        }.updateAll(
            password: password.encodeAsMD5()
        ) > 0
    }

    Map registerCustomerBySocialInfo(Map params) {
        def registrationConfigs = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.CUSTOMER_REGISTRATION_SETTINGS)
        params.country = [id:  AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "default_country")];
        params.password = StringUtil.uuid
        params.addressLine1 = ""
        Customer customer = save(params)
        if(registrationConfigs.registration_status_type == DomainConstants.CUSTOMER_REG_TYPE.OPEN) {
            Thread.start {
                AppUtil.initialDummyRequest()
                commanderMailService.sendCustomerRegistrationMail(customer.id)
            }
        }
        if(registrationConfigs.registration_status_type == DomainConstants.CUSTOMER_REG_TYPE.AWAITING_APPROVAL) {
            return [message: siteMessageSource.convert(registrationConfigs.restricted_registration_message, [])]
        }
        return [customer: customer]
    }

    Map verifyCustomerByToken(String media, String token) {
        try {
            Map userInfo = null;
            if(media == "fb") {
                userInfo = getFbUserInfo(token)
            } else {
                userInfo = getGoolgeUserInfo(token)
            }
            String status = "verified";
            String message = null;
            Customer customer = Customer.findByUserName(userInfo.email)
            if(customer == null) {
                Map result = registerCustomerBySocialInfo(userInfo)
                customer = result.customer
                status = result.message ? "invalid" : status
                message = result.message
            }
            if (customer && customer.status != 'A') {
                status = "invalid";
                message = g.message(code: "customer.account.inactive.message");
            }
            return([status: status, message: message, customer: customer])
        } catch (Throwable t) {
            return([status: "invalid", message: g.message(code: "invalid.token")])

        }

    }

    Map getFbUserInfo(String token) {
        String url = "https://graph.facebook.com/v2.8/me?" + AppUtil.getQueryStringFromMap([
                access_token: token,
                fields: 'id,last_name,first_name,email'
        ])
        GetMethod getMethod = new GetMethod(url)
        HttpClient client = new HttpClient()
        client.executeMethod(getMethod)
        String responseText = getMethod.responseBodyAsString
        def result = JSON.parse(responseText)
        return [
            firstName: result.first_name,
            lastName: result.last_name,
            email: result.email
        ]
    }

    Map getGoolgeUserInfo(String token) {
        String url = "https://www.googleapis.com/oauth2/v3/tokeninfo?id_token=${token}"
        GetMethod getMethod = new GetMethod(url)
        HttpClient client = new HttpClient()
        client.executeMethod(getMethod)
        String responseText = getMethod.responseBodyAsString
        def result = JSON.parse(responseText)
        return [
           firstName: result.name,
           lastName: "",
           email: result.email
        ]
    }

    @NotTransactional
    Address copyAddress(Map params) {
        Address address = new Address();
        address.firstName = params.firstName
        address.lastName = params.lastName
        address.addressLine1 = params.addressLine1
        address.addressLine2 = params.addressLine2
        address.postCode = params.postCode
        address.phone = params.phone
        address.mobile = params.mobile
        address.fax = params.fax
        address.email = params.email
        address.country = Country.get(params.country.id)
        address.state = State.get(params.state ? params.state.id : 0)
        address.city = params.city
        return address
    }

    Integer getStoreCreditHistoryCount(Map params) {
        Long id = params.long("id")
        return StoreCreditHistory.createCriteria().count {
            eq("customer.id", id)
        }
    }

    List<StoreCreditHistory> getStoreCreditHistory(Map params) {
        Long id = params.long("id")
        Map listMap = [max: params.max, offset: params.offset]
        return StoreCreditHistory.createCriteria().list(listMap) {
            eq("customer.id", id)
            order("id", "desc")
        }
    }

    def updateStoreCredit(Map params) {
        Customer customer = Customer.get(params.long("id"))
        Boolean add = params.add.toBoolean(true)
        Double deltaAmount = params.deltaAmount.toDouble()
        Operator createdBy = Operator.get(AppUtil.session.admin)
        StoreCreditHistory history = new StoreCreditHistory(customer: customer, deltaAmount: add ? deltaAmount : (-1 * deltaAmount), createdBy: createdBy, note: params.adjustNote)
        history.save()
        Double storeCredit = (customer.storeCredit? customer.storeCredit.round(2) : 0.00)
        def proposed = add ? storeCredit + deltaAmount.round(2) : storeCredit - deltaAmount.round(2);
        if(!history.hasErrors()) {
            customer.addToStoreCreditHistories(history)
            customer.storeCredit = (proposed < 0 ? 0.00 : proposed);
            customer.merge()
        } else {
            return false
        }

        return !customer.hasErrors()
    }

    def sendResetPasswordMail(Customer recipient, String resetPasswordLink) {
        def storeDetail = StoreDetail.first()
        Map macrosAndTemplate = commanderMailService.getMacrosAndTemplateByIdentifier("customer-reset-password")
        if (!macrosAndTemplate.emailTemplate.active) {
            return;
        }
        Map refinedMacros = macrosAndTemplate.commonMacros
        macrosAndTemplate.macros.each {
            switch (it.key.toString()) {
                case "customer_first_name" :
                    refinedMacros[it.key] = recipient.firstName
                    break;
                case "customer_last_name" :
                    refinedMacros[it.key] = recipient.lastName ?: ""
                    break;
                case "customer_email" :
                    refinedMacros[it.key] = recipient.userName
                    break;
                case "password_reset_link" :
                    refinedMacros[it.key] = resetPasswordLink
                    break;
                case "store_name" :
                    refinedMacros[it.key] = storeDetail.name
                    break;
            }
        }
        commanderMailService.sendMail(macrosAndTemplate.emailTemplate, macrosAndTemplate.activeHtml, macrosAndTemplate.activeText, refinedMacros, recipient.address.email)
    }

    public OrderComment sendOderComment(Order order, Map params) {
        if(!order) {
            return false
        }
        OrderComment comment = new OrderComment(order: order, isAdmin: false)
        comment.content = params.message;
        comment.save()
        sendOrderCommentMail(order, params)
        return comment
    }

    def sendOrderCommentMail(Order order, Map params) {
        def storeDetail = StoreDetail.first()
        Address billingAddress = order.billing
        Map macrosAndTemplate = commanderMailService.getMacrosAndTemplateByIdentifier("customer-order-comment-notification")
        if (!macrosAndTemplate.emailTemplate.active) {
            return;
        }
        Map refinedMacros = macrosAndTemplate.commonMacros
        macrosAndTemplate.macros.each {
            switch (it.key.toString()) {
                case "customer_name" :
                    refinedMacros[it.key] = order.customerName
                    break;
                case "order_id" :
                    refinedMacros[it.key] = order.id
                    break;
            }
        }
        commanderMailService.sendMail(macrosAndTemplate.emailTemplate, macrosAndTemplate.activeHtml, macrosAndTemplate.activeText, refinedMacros, storeDetail.address?.email, billingAddress.email)
    }

    @Transactional
    Integer saveBasicBulkProperties(params) {
        List<Long> ids = params.list("id").collect { it.toLong() }
        int count = 0

        ids.each {
            Customer customer = Customer.get(it)
            if(params.status) {
                customer.status = params.status
            }

            if(params.deltaAmount) {
                Boolean add = params.add.toBoolean(true)
                Double deltaAmount = params.deltaAmount.toDouble()
                String note = params.noteText
                Operator createdBy = Operator.get(AppUtil.session.admin)
                StoreCreditHistory history = new StoreCreditHistory(customer: customer, deltaAmount: add ? deltaAmount : (-1 * deltaAmount), createdBy: createdBy, note: note)
                history.save()
                Double storeCredit = (customer.storeCredit? customer.storeCredit.round(2) : 0.00)
                Double proposed = add ? storeCredit + deltaAmount.round(2) : storeCredit - deltaAmount.round(2);
                if(!history.hasErrors()) {
                    customer.addToStoreCreditHistories(history)
                    customer.storeCredit = (proposed < 0 ? 0.00 : proposed);
                    customer.merge()
                } else {
                    return false
                }
            }

            customer.save()
            if (!customer.hasErrors()) {
                AppEventManager.fire("customer-update", [params.id])
                count++
            }
        }
        return count
    }

}
