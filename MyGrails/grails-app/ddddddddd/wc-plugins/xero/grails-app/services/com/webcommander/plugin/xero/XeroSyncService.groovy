package com.webcommander.plugin.xero

import com.webcommander.admin.Country
import com.webcommander.admin.Customer
import com.webcommander.admin.State
import com.webcommander.admin.Zone
import com.webcommander.common.CommonService
import com.webcommander.constants.DomainConstants
import com.webcommander.plugin.xero.constants.LinkComponent
import com.webcommander.plugin.xero.exception.XeroException
import com.webcommander.plugin.xero.manager.XmlManager
import com.webcommander.plugin.xero.models.*
import com.webcommander.plugin.xero.models.invoice.*
import com.webcommander.task.MultiLoggerTask
import com.webcommander.task.Task
import com.webcommander.task.TaskService
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.*
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import org.grails.plugins.web.taglib.ApplicationTagLib
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

@Transactional
class XeroSyncService {
    @Autowired
    @Qualifier("org.grails.plugins.web.taglib.ApplicationTagLib")
    ApplicationTagLib g;
    CommonService commonService
    TaskService taskService
    ProductService productService

    public void importTax(Map<String, String> config, List taxes, MultiLoggerTask task) {
        boolean updateExisting = config.update_tax.toBoolean();
        def currentOrgId = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.XERO, "organisation_id")
        for (JSONObject tax : taxes) {
            XeroTrack.withNewTransaction { status ->
                try {
                    TaxProfile taxProfile;
                    TaxCode taxCode;
                    TaxRule taxRule
                    XeroTrack xeroTrack = XeroTrack.findOrCreateByLinkComponentAndXeroIdAndXeroOrganisationId(LinkComponent.TYPES.TAX, tax.TaxType, currentOrgId);
                    boolean continueImport = true
                    if (xeroTrack.componentId) {
                        taxProfile = TaxProfile.get(xeroTrack.componentId);
                        if (taxProfile && !updateExisting) {
                            continueImport = false;
                        }
                    }
                    if (continueImport) {
                        taxProfile = taxProfile ?: new TaxProfile()
                        taxProfile.name = tax.Name;
                        taxProfile.description = tax.Name;

                        taxRule = taxProfile.rules?.size() > 0 ? taxProfile.rules[0] : new TaxRule();
                        taxRule.name = tax.Name;
                        taxRule.zone = taxRule.zone ?: Zone.get(config.tax_default_zone);

                        taxCode = taxRule.code ?: new TaxCode();
                        taxRule.code = taxCode
                        taxCode.name = tax.Name;
                        //taxCode.label = tax.Type;
                        taxCode.rate = tax.EffectiveRate;
                        taxCode.description = tax.Name

                        [taxCode, taxRule, taxProfile].each {
                            if(it == taxProfile){
                                taxProfile.addToRules(taxRule);
                            }
                            it.save();
                            if (it.hasErrors()) {
                                throw new XeroException("tax.could.not.import")
                            }
                        }
                        xeroTrack.componentId = taxProfile.id;
                        xeroTrack.xeroOrganisationId = currentOrgId
                        xeroTrack.xeroVersion = tax.TaxType
                        xeroTrack.xeroId = tax.TaxType
                        xeroTrack.save()
                        if (xeroTrack.hasErrors()) {
                            throw new XeroException("xero.link.could.not.save")
                        }
                    }

                    task.meta.taxProgress = taskService.countProgress(task.meta.totalTaxCount, ++task.meta.taxComplete as Integer)
                    task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                    task.meta.taxSuccessCount++
                    task.taskLogger.success(taxProfile.name, "tax.import.success");
                } catch (ValidationException ex) {
                    String validationMessage = "";
                    ex.errors?.getFieldErrors().each {
                        validationMessage += it?.objectName?.replace("com.webcommander.webcommerce.", "") + " " + it?.field + " rejected value " + it?.rejectedValue
                    }
                    task.errorCount++
                    task.meta.taxProgress = taskService.countProgress(task.meta.totalTaxCount, ++task.meta.taxComplete as Integer)
                    task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                    task.taskLogger.error("Tax " + ": ${tax?.Name}", validationMessage)
                    task.meta.taxErrorCount++
                    status.setRollbackOnly();

                } catch (Exception ex) {
                    task.errorCount++
                    task.meta.taxProgress = taskService.countProgress(task.meta.totalTaxCount, ++task.meta.taxComplete as Integer)
                    task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                    task.taskLogger.error("Tax" + ": ${tax?.Name}", ex.message)
                    task.meta.taxErrorCount++
                    status.setRollbackOnly();
                }
            }
        }
    }

    public void importProduct(Map<String, String> config, List<Item> items, MultiLoggerTask task) {
        def currentOrgId = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.XERO, "organisation_id")
        items.each { Item item ->
            XeroTrack.withNewTransaction {status ->
                try {
                    XeroTrack track = XeroTrack.findByLinkComponentAndXeroId("product", item.code);
                    Product product = track ? Product.get(track.componentId) : new Product();
                    if ((track && config.update_item_name.toBoolean()) || !track) {
                        product.name = item.name;
                    }
                    if ((track && config.update_item_description.toBoolean()) || !track) {
                        product.description = item.description;
                    }
                    if ((track && config.update_item_base_price.toBoolean()) || !track) {
                        product.basePrice = item.salesDetails.unitPrice;
                    }
                    if ((track && config.update_item_cost_price.toBoolean()) || !track) {
                        product.costPrice = item.purchaseDetails.unitPrice;
                    }
                    if ((track && config.update_item_tax.toBoolean()) || !track) {
                        XeroTrack trackTax = XeroTrack.findByLinkComponentAndXeroId("tax", item.salesDetails.taxType);
                        TaxProfile taxProfile = trackTax ? TaxProfile.get(trackTax.componentId) : null;
                        product.taxProfile = taxProfile;
                    }
                    if (!track) {
                        product.sku = commonService.getSKUForDomain(Product);
                        product.url = commonService.getUrlForDomain(product)
                    }
                    product.productType = DomainConstants.PRODUCT_TYPE.PHYSICAL
                    product.save();
                    if (product.hasErrors()) {
                        throw new XeroException("product.could.not.import")
                    }
                    if (!track) {
                        track = new XeroTrack();
                        track.componentId = product.id;
                        track.linkComponent = LinkComponent.TYPES.PRODUCT
                        track.xeroOrganisationId = currentOrgId
                        track.xeroId = item.code
                        track.xeroVersion = item.updatedDateUTC
                        if (track.hasErrors()){
                            throw new XeroException("xero.track.could.not.save");
                        }
                    }
                    else {
                        track.xeroVersion = item.updatedDateUTC
                    }
                    track.save();
                    task.meta.productProgress = taskService.countProgress(task.meta.totalProductCount, ++task.meta.productComplete as Integer)
                    task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                    task.taskLogger.success(product.name, "product.import.success")
                    task.meta.productSuccessCount++
                }catch (Exception e) {
                    task.errorCount++
                    task.meta.productProgress = taskService.countProgress(task.meta.totalProductCount, ++task.meta.productComplete as Integer)
                    task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                    task.taskLogger.error("Product"  + ": ${item?.name}", e.message)
                    task.meta.productErrorCount++
                    status.setRollbackOnly();
                }
            }
        }
    }

    public void exportProduct(Task task) {
        Map config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.XERO_ITEM);
        Map xeroConfig = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.XERO);
        XeroClient client = new XeroClient(xeroConfig["consumer_key"], xeroConfig["consumer_secret"], xeroConfig["private_key"]);
        List<Product> productList = productService.getProducts([max: "-1", offset: "0"]);
        task.totalRecord = productList.size();
        productList.each {Product product ->
            try {
                XeroTrack track = XeroTrack.findByComponentIdAndResource(product.id, "product")
                Item item = new Item();
                item.code = product.name
                if ((track && config.update_description.toBoolean()) || !track) {
                    item.description = product.description;
                }
                if ((track && config.update_base_price.toBoolean()) || !track) {
                    SalesDetails salesDetails = new SalesDetails();
                    salesDetails.unitPrice = product.basePrice;
                    if(config.default_purchase_details_account) {
                        salesDetails.accountCode = config.default_purchase_details_account
                    }
                    item.salesDetails = salesDetails;
                }
                if ((track && config.update_cost_price.toBoolean()) || !track) {
                    PurchaseDetails purchaseDetails = new PurchaseDetails();
                    purchaseDetails.unitPrice = product.costPrice
                    if (config.default_sales_details_account) {
                        purchaseDetails.accountCode = config.default_sales_details_account
                    }
                    item.purchaseDetails = purchaseDetails
                }
                if (track) {
                    item.code = track.xeroId;
                }
                Item newItem = client.addItem(item);
                if (!track) {
                    track = new XeroTrack();
                    track.componentId = product.id;
                    track.resource = "product";
                    track.xeroId = newItem.code
                    track.save();
                    if (track.hasErrors()){
                        throw new XeroException("xero.track.could.not.save");
                    }
                }
                task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                task.taskLogger.success(product.name, "product.import.success")
                task.meta.successCount++
            } catch (Exception e) {
                task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                task.taskLogger.error("product", e.message)
                task.meta.errorCount++;
            }
        }
    }

    public void resolveCustomerEmailInImport(def config, Customer customer, xeroContact) {
        if(config.email == "PrimaryPerson") {
            customer.address.email = xeroContact."EmailAddress"
        }
        else {
            def xeroContactPersons = xeroContact."ContactPersons"
            JSONArray
            def contactPersonSerial = config.email?.replace("Another", "").toInteger()
            if(xeroContactPersons?.size() >= contactPersonSerial) {
                customer.address.email = xeroContactPersons[contactPersonSerial - 1]."EmailAddress"
            }
        }
    }

    public void importCustomer(Map<String, String> config, List customers, MultiLoggerTask task) {
        boolean updateCustomer = config.update_customer.toBoolean();
        for (JSONObject cus : customers) {
            XeroTrack.withNewTransaction { status ->
                try {
                    XeroClient xeroClient = new XeroClient(config.consumer_key, config.consumer_secret, config.private_key)
                    cus = xeroClient.getContacts(cus.ContactID)?.first()
                    Customer customer;
                    XeroTrack xeroTrack = XeroTrack.findOrCreateByLinkComponentAndXeroIdAndXeroOrganisationId(LinkComponent.TYPES.CUSTOMER, cus.ContactID, XeroClient.getCurrentOrganisation());

                    boolean continueImport = true
                    if (xeroTrack.componentId) {
                        customer = Customer.get(xeroTrack.componentId);
                        continueImport = updateCustomer
                    }
                    if (continueImport) {
                        customer = customer ?: new Customer();
                        customer.isCompany = false;
                        customer.firstName = cus.Name

                        customer.status = DomainConstants.CUSTOMER_STATUS.ACTIVE
                        def addresses = cus."Addresses"
                        ["address", "activeBillingAddress", "activeShippingAddress"].each {def type ->
                            Address address;
                            def addressFromXero = config."${type}" == "Postal" ? addresses.findAll{it.AddressType == "POBOX"}?.first() : addresses.findAll{it.AddressType == "STREET"}?.first()
                            def country = addressFromXero."Country"? Country.findByName(addressFromXero."Country"): null
                            if(type == "address" && !country) {
                                throw new XeroException("address.no.country")
                            }
                            if((addressFromXero."AttentionTo" || addressFromXero."AddressLine1") && country) {
                                def state
                                if(addressFromXero."Region") {
                                    state = State.findByCountryAndName(country, addressFromXero."Region")
                                    state = state ?: State.findByCountryAndCode(country, addressFromXero."Region")
                                }
                                address = new Address()
                                customer."${type}" = address
                                address.firstName = addressFromXero."AttentionTo" ?: customer.firstName
                                address.city = addressFromXero."City"
                                address.addressLine1 = addressFromXero."AddressLine1"?: addressFromXero."AttentionTo"
                                address.addressLine2 = addressFromXero."AddressLine2"
                                address.postCode = addressFromXero."PostalCode"
                                if(type == "address") {
                                    resolveCustomerEmailInImport(config, customer, cus)
                                    if (!customer.address.email) {
                                        throw new XeroException("customer.email.not.found")
                                    }
                                }
                                else if(type == "activeBillingAddress") {
                                    customer?.activeBillingAddress?.email = customer?.address?.email
                                    customer.addToBillingAddresses(customer.activeBillingAddress)
                                }
                                else {
                                    customer?.activeShippingAddress?.email = customer?.address?.email
                                    customer.addToShippingAddresses(customer.activeShippingAddress)
                                }
                                address.country = country
                                address.state = state
                                address.save()
                            }
                        }
                        if (!customer.address) {
                            throw new Exception("No customer address or no address are usable")
                        }

                        def xeroPhones = cus."Phones"
                        ["phone", "mobile", "fax"].each { def type ->
                            if(config."${type}" == "Phone" ) {
                                def xeroPhone = xeroPhones.findAll {it."PhoneType" == "DEFAULT"}.first()
                                customer.address."${type}" = xeroPhone."PhoneCountryCode" + xeroPhone."PhoneAreaCode" + xeroPhone."PhoneNumber"
                            }
                            else if(config."${type}" == "Mobile") {
                                def xeroPhone = xeroPhones.findAll {it."PhoneType" == "MOBILE"}.first()
                                customer.address."${type}" = xeroPhone."PhoneCountryCode" + xeroPhone."PhoneAreaCode" + xeroPhone."PhoneNumber"
                            }
                            else {
                                def xeroPhone = xeroPhones.findAll {it."PhoneType" == "FAX"}.first()
                                customer.address."${type}" = xeroPhone."PhoneCountryCode" + xeroPhone."PhoneAreaCode" + xeroPhone."PhoneNumber"
                            }
                        }

                        if (!customer.activeBillingAddress) {
                            customer.activeBillingAddress = new Address()
                            ["addressLine1", "city", "country", "state", "postCode", "phone", "mobile", "email", "fax", "firstName", "lastName"].each {
                                customer.activeBillingAddress[it] = customer.address[it];
                            }
                            if (!customer.activeBillingAddress.firstName) {
                                customer.activeBillingAddress.firstName = customer.firstName
                            }
                            customer.activeBillingAddress? customer.addToBillingAddresses(customer.activeBillingAddress) : null;
                        }

                        if (!customer.activeShippingAddress) {
                            customer.activeShippingAddress = new Address()
                            ["addressLine1", "city", "country", "state", "postCode", "phone", "mobile", "email", "fax", "firstName", "lastName"].each {
                                customer.activeShippingAddress[it] = customer.address[it];
                            }
                            if (!customer.activeShippingAddress.firstName) {
                                customer.activeShippingAddress.firstName = customer.firstName
                            }
                            customer.activeShippingAddress? customer.addToShippingAddresses(customer.activeShippingAddress): null;
                        }

                        if (!customer.userName) {
                            customer.userName = customer.address.email;
                        }
                        if (!customer.password) {
                            customer.password = "change it".encodeAsMD5();
                        }
                        customer.address.save()
                        customer.activeBillingAddress.save()
                        customer.activeShippingAddress.save()
                        customer.save()
                        if (customer.hasErrors()) {
                            throw new XeroException("customer.could.not.import")
                        }
                        xeroTrack.componentId = customer.id;
                        xeroTrack.xeroId = cus.ContactID
                        xeroTrack.xeroVersion = cus.DateTimeUTC?: cus.UpdatedDateUTC;
                        xeroTrack.save();
                        if (xeroTrack.hasErrors()) {
                            throw new XeroException("xero.track.could.not.save")
                        }
                    }

                    task.meta.customerProgress = taskService.countProgress(task.meta.totalCustomerCount, ++task.meta.customerComplete as Integer)
                    task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                    task.meta.customerSuccessCount++
                    task.taskLogger.success(customer.firstName, "customer.import.success")
                } catch (Exception ex) {
                    task.meta.customerProgress = taskService.countProgress(task.meta.totalCustomerCount, ++task.meta.customerComplete as Integer)
                    task.errorCount++
                    task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                    task.taskLogger.error("Customer" + ": ${cus?.Name}", ex.message)
                    task.meta.customerErrorCount++
                    status.setRollbackOnly();
                }
            }
        }
    }

    public void exportCustomer(Map<String, String> config, List<Customer> customers, XeroClient client, MultiLoggerTask task) {
        boolean updateCustomer = config.update_customer.toBoolean();
        String phoneField = config.customer_phone;
        String mobileField = config.customer_mobile;
        XeroContacts xeroContacts = new XeroContacts()
        xeroContacts.contactList = new ArrayList<XeroContact>()
        for (Customer customer : customers) {
            try {
                customer = Customer.get(customer.id)
                XeroContact xeroContact = new XeroContact();
                XeroTrack xeroTrack = XeroTrack.findOrCreateByLinkComponentAndComponentIdAndXeroOrganisationId(LinkComponent.TYPES.CUSTOMER, customer.id, XeroClient.currentOrganisation);
                boolean continueExport = true;
                if (xeroTrack.xeroId) {
                    xeroContact.contactId = xeroTrack.xeroId;
                    //xeroContact.updatedDateUTC = xeroTrack.xeroVersion;
                    continueExport = updateCustomer;
                }
                if (continueExport) {
                    xeroContact.contactNumber = xeroContact.contactNumber?: UUID.randomUUID().toString()
                    xeroContact.name = customer.fullName()
                    xeroContact.addresses = new Addresses();
                    xeroContact.addresses.addressList = new ArrayList<XeroAddress>();
                    xeroContact.phones = new Phones()
                    xeroContact.phones.phoneList = new ArrayList<Phone>()

                    ["Postal", "Street"].each { def type ->
                        def addressType = config.find {it.value == type}?.key
                        if(addressType) {
                            Address address = customer[addressType]
                            XeroAddress xeroAddress = new XeroAddress();
                            xeroAddress.addressLine1 = address.addressLine1;
                            xeroAddress.addressLine2 = address.addressLine2;

                            xeroAddress.city = address.city;
                            xeroAddress.addressType = type == "Postal"? AddressType.POBOX: AddressType.STREET
                            if (address.country) {
                                xeroAddress.country = address.country.name;
                            }

                            xeroAddress.postalCode = address.postCode

                            xeroAddress.attentionTo = address.firstName + " " + address.lastName;
                            xeroContact.addresses.addressList.add(xeroAddress)
                        }
                    }
                    xeroContact.emailAddress = customer.address?.email
                    xeroContacts.contactList.add(xeroContact)
                    ["Phone", "Mobile", "Fax"].each {def type ->
                        def phoneType = config.find {it.value == type}?.key
                        Phone phone = new Phone()
                        phone.phoneNumber = customer.address?."${type.toLowerCase()}";
                        phone.phoneType = type == "Phone"? PhoneTypeCodeType.DEFAULT: PhoneTypeCodeType.valueOf(type.toUpperCase())
                        xeroContact.phones.phoneList.add(phone);
                    }
                }
                else {
                    task.meta.customerProgress = taskService.countProgress(task.meta.totalCustomerCount, ++task.meta.customerComplete as Integer)
                    task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                    task.taskLogger.success(customer.firstName, "customer.export.success")
                    task.meta.customerSuccessCount++
                }
            } catch (Exception ex) {
                task.meta.customerProgress = taskService.countProgress(task.meta.totalCustomerCount, ++task.meta.customerComplete as Integer)
                task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                task.taskLogger.error("Customer" + ": ${customer?.firstName}", ex.message)
                task.meta.customerErrorCount++
            }
        }

        if (xeroContacts.contactList.size() > 0) {
            def responseContacts
            try {
                responseContacts = client.addContacts(XmlManager.getXml(xeroContacts))
                if(!responseContacts) {
                    throw new XeroException("Error occurred while communicating with xero. All failed")
                }
            }
            catch (Exception ex) {
                task.meta.customerProgress = taskService.countProgress(task.meta.totalCustomerCount, task.meta.totalCustomerCount as Integer)
                task.progress = taskService.countProgress(task.totalRecord, task.meta.totalCustomerCount)
                task.taskLogger.error("Customer", ex.message)
                task.meta.customerErrorCount = task.meta.totalCustomerCount
                //status.setRollbackOnly();
            }
            xeroContacts.contactList.each { XeroContact contact ->
                XeroTrack.withNewTransaction { def status ->
                    try {
                        Customer customer = Customer.createCriteria().get() {
                            createAlias('address', 'a')
                            eq "a.email", contact.emailAddress
                        }
                        XeroTrack xeroTrack = XeroTrack.findOrCreateByLinkComponentAndComponentIdAndXeroOrganisationId(LinkComponent.TYPES.CUSTOMER, customer.id, XeroClient.currentOrganisation);
                        def responseContact = responseContacts.find {it."ContactNumber" == contact.contactNumber }
                        if (responseContact."HasValidationErrors" == true) {
                            task.meta.customerProgress = taskService.countProgress(task.meta.totalCustomerCount, ++task.meta.customerComplete as Integer)
                            task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                            task.taskLogger.error("Customer", responseContact."ValidationErrors".first()?."Message")
                            task.meta.customerErrorCount++
                            status.setRollbackOnly();
                        } else {
                            xeroTrack.componentId = customer.id
                            xeroTrack.xeroId = responseContact.ContactID
                            xeroTrack.xeroVersion = responseContact."UpdatedDateUTC";
                            xeroTrack.save()
                            if (xeroTrack.hasErrors()) {
                                throw new XeroException("xero.link.could.not.save")
                            }
                            task.meta.customerProgress = taskService.countProgress(task.meta.totalCustomerCount, ++task.meta.customerComplete as Integer)
                            task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                            task.taskLogger.success(contact.name, "customer.export.success")
                            task.meta.customerSuccessCount++
                        }
                    }
                    catch (Exception ex) {
                        task.meta.customerProgress = taskService.countProgress(task.meta.totalCustomerCount, ++task.meta.customerComplete as Integer)
                        task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                        task.taskLogger.error("Customer", ex.message)
                        task.meta.customerErrorCount++
                        status.setRollbackOnly();
                    }
                }
            }
        }
    }

    public void exportOrder(Map<String, String> config, List<Order> orders, XeroClient client, MultiLoggerTask task) {
        if(config.order_sync_type == "detail" ) {
            exportOrderDetail(config, orders, client, task)
        } else {
            exportOrderSummary(config, orders, client, task)
        }
    }

    public void exportOrderSummary(Map<String, String> config, List<Order> orders, XeroClient client, MultiLoggerTask task) {
        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        XeroTrack.withNewTransaction { def status ->
            try {
                XeroInvoices xeroInvoices = new XeroInvoices()
                xeroInvoices.invoiceList = new ArrayList<>();
                def defaultAccount =  AppUtil.getConfig( DomainConstants.SITE_CONFIG_TYPES.XERO, "default_account");
                def shippingAccount =  AppUtil.getConfig( DomainConstants.SITE_CONFIG_TYPES.XERO, "shipping_account");
                def defaultCustomer =  AppUtil.getConfig( DomainConstants.SITE_CONFIG_TYPES.XERO, "default_customer");
                def customerLink = defaultCustomer? XeroTrack.findByLinkComponentAndComponentIdAndXeroOrganisationId(LinkComponent.TYPES.CUSTOMER, defaultCustomer?.toLong(), XeroClient.currentOrganisation): null;
                def defaultPaymentAccount =  AppUtil.getConfig( DomainConstants.SITE_CONFIG_TYPES.XERO, "default_payment_account");

                if(!customerLink)
                    throw new XeroException("default.customer.not.selected")

                XeroInvoice xeroInvoice = new XeroInvoice()
                xeroInvoice.lineItems = new ArrayOfLineItem()
                xeroInvoice.lineItems.lineItem = new ArrayList<XeroLineItem>()
                xeroInvoice.status = XeroInvoiceStatus.AUTHORISED
                xeroInvoice.type = XeroInvoiceType.ACCREC
                def summaryId = getSummaryInvoiceId()
                xeroInvoice.invoiceNumber = "WS" + "000000".substring(summaryId.toString().length()) + summaryId
                xeroInvoice.contact = new XeroContact()
                xeroInvoice.contact.contactId = customerLink.xeroId
                Calendar calendar = Calendar.getInstance(timeZone)
                calendar.setTime(new Date())
                xeroInvoice.date = calendar
                calendar = Calendar.getInstance(timeZone)
                calendar.setTime(new Date())
                xeroInvoice.dueDate = calendar

                xeroInvoice.totalTax = 0;
                xeroInvoice.total = 0;
                xeroInvoice.amountDue = 0

                xeroInvoice.lineItems = new ArrayOfLineItem();
                xeroInvoice.lineAmountTypes = ["Inclusive"]




                XeroLineItem line = new XeroLineItem();
                line.quantity = 1;
                line.description = "Summary"
                line.unitAmount = 0
                line.lineAmount = 0
                line.taxAmount = 0
                line.accountCode = defaultAccount

                XeroLineItem shippingLine
                XeroLineItem surchargeLine

                orders.each {Order order ->
                    Order o = Order.get(order.id);
                    xeroInvoice.totalTax += o.totalTax;
                    xeroInvoice.total += o.grandTotal;
                    xeroInvoice.amountDue += o.due.toPrice().toBigDecimal()

                    xeroInvoice.lineItems = new ArrayOfLineItem();
                    xeroInvoice.lineAmountTypes = ["Inclusive"]

                    line.lineAmount += o.total + o.totalTax
                    line.taxAmount += o.totalTax
                    line.unitAmount += o.total + o.totalTax

                    if(order.shippingCost || order.handlingCost) {
                        if(!shippingAccount) {
                            throw new XeroException("shipping.tax.profile.not.found")
                        }
                        if(!shippingLine) {
                            shippingLine = new XeroLineItem()
                            shippingLine.description = "Shipping"
                            shippingLine.quantity = 1;
                            shippingLine.accountCode = shippingAccount
                        }
                        def total = order.shippingCost + order.shippingTax + order.handlingCost

                        shippingLine.unitAmount = shippingLine.unitAmount? shippingLine.unitAmount + total : total;
                        shippingLine.lineAmount = shippingLine.lineAmount? shippingLine.lineAmount + total : total;
                        shippingLine.taxAmount = shippingLine.taxAmount? shippingLine.taxAmount + order.shippingTax : order.shippingTax;
                    }

                    if(order.totalSurcharge > 0) {
                        def enableSurchargeSync = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.XERO, "enable_surcharge_sync");
                        def surchargeAccount
                        if (enableSurchargeSync == "true") {
                            surchargeAccount = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.XERO, "surcharge_account");
                            if(!surchargeAccount) {
                                throw new XeroException("surcharge.account.not.found")
                            }
                        }
                        else {
                            throw new XeroException("order.with.surcharge.not.enabled")
                        }
                        if(!surchargeLine) {
                            surchargeLine = new XeroLineItem();
                            surchargeLine.description = "Surcharge"
                            surchargeLine.quantity = 1
                            surchargeLine.taxAmount = 0
                            surchargeLine.accountCode = surchargeAccount
                        }

                        surchargeLine.lineAmount = surchargeLine.lineAmount ? surchargeLine.lineAmount + order.totalSurcharge : order.totalSurcharge
                        surchargeLine.unitAmount = surchargeLine.unitAmount ? surchargeLine.unitAmount +  order.totalSurcharge : order.totalSurcharge
                    }
                }
                xeroInvoice.lineItems.lineItem.add(line)
                shippingLine ? xeroInvoice.lineItems.lineItem.add(shippingLine) : null
                surchargeLine ? xeroInvoice.lineItems.lineItem.add(surchargeLine) : null
                xeroInvoices.invoiceList.add(xeroInvoice)

                def responseInvoices = client.addInvoices(xeroInvoices)
                def responseInvoice = responseInvoices?.first()
                if (responseInvoice."HasValidationErrors" == true || responseInvoice."HasErrors" == true) {
                    task.meta.orderProgress = taskService.countProgress(task.meta.totalOrderCount, task.meta.totalOrderCount as Integer)
                    task.progress = taskService.countProgress(task.totalRecord, task.meta.totalOrderCount)
                    task.taskLogger.error("Order ", "Summary Export Failed  (${orders.collect() {it.id}.join(', ')}) " + responseInvoice."ValidationErrors".collect{it."Message"}.join(","))
                    task.meta.orderErrorCount = task.meta.totalOrderCount
                }
                else if(responseInvoice) {
                    def payTotal = 0;
                    orders.each { Order order ->
                        order = Order.get(order.id)
                        XeroTrack xeroTrack = XeroTrack.findOrCreateByLinkComponentAndComponentIdAndXeroOrganisationId(LinkComponent.TYPES.ORDER, order.id, XeroClient.currentOrganisation);
                        xeroTrack.componentId = order.id
                        xeroTrack.xeroId = responseInvoice."InvoiceNumber"
                        xeroTrack.xeroVersion = responseInvoice."UpdatedDateUTC";
                        xeroTrack.save()
                        payTotal += order.getPaid()
                    }
                    def responsePayment
                    def allFailed = ""
                    try {
                        XeroPayments xeroPayments = new XeroPayments()
                        XeroPayment xeroPayment = new XeroPayment();
                        List paymentList = new ArrayList()
                        xeroPayment.amount = payTotal.toBigDecimal()
                        XeroAccount account = new XeroAccount()
                        account.setCode(defaultPaymentAccount)
                        xeroPayment.account = account
                        xeroPayment.invoice = new XeroInvoice()
                        xeroPayment.invoice.invoiceID = responseInvoice."InvoiceID"
                        xeroPayment.date = Calendar.getInstance(timeZone).setTime(new Date())
                        paymentList.add(xeroPayment)
                        xeroPayments.paymentList = paymentList
                        def responsePayments = client.addPayments(xeroPayments)
                        responsePayment = responsePayments?.first()
                    }
                    catch (Exception ex) {
                        ex.printStackTrace()
                        allFailed = ex.getMessage()
                    }
                    finally {
                        String paymentErrorMessage = ""
                        def invoiceId = responseInvoice."InvoiceID"
                        def invoiceNumber = responseInvoice."InvoiceNumber"
                        if(responsePayment) {
                            if (responsePayment."HasValidationErrors" == true || responsePayment."HasErrors" == true) {
                                paymentErrorMessage += "Summary Payment" + " Amount: ${responsePayment."Amount"} " + responsePayment."ValidationErrors".collect{it."Message"}.join(",")
                            }
                            else {
                                orders.each {Order order ->
                                    order = Order.get(order.id)
                                    order.payments.findAll(){it.status == "success"}.each {Payment payment ->
                                        XeroTrack xeroTrack = XeroTrack.findOrCreateByLinkComponentAndComponentIdAndXeroOrganisationId(LinkComponent.TYPES.PAYMENT, payment.id, XeroClient.currentOrganisation);
                                        xeroTrack.componentId = payment.id
                                        xeroTrack.xeroId = responsePayment."PaymentID"
                                        xeroTrack.xeroVersion = responsePayment."UpdatedDateUTC";
                                        xeroTrack.save()
                                    }
                                }
                            }
                        }
                        task.meta.orderProgress = taskService.countProgress(task.meta.totalOrderCount, task.meta.totalOrderCount as Integer)
                        task.progress = taskService.countProgress(task.totalRecord, task.meta.totalOrderCount)
                        task.taskLogger.success(invoiceNumber, ("Summary Export Success  (${orders.collect() {it.id}.join(', ')})" + (paymentErrorMessage? " : payment.error: ${paymentErrorMessage}" : "") + allFailed))
                        task.meta.orderSuccessCount = task.meta.totalOrderCount
                    }
                }
                else {
                    task.meta.orderProgress = taskService.countProgress(task.meta.totalOrderCount, task.meta.totalOrderCount as Integer)
                    task.progress = taskService.countProgress(task.totalRecord, task.meta.totalOrderCount)
                    task.taskLogger.error("Order" + ": Summary Export Failed  (${orders.collect() {it.id}.join(', ')})" + "")
                    task.meta.orderErrorCount = task.meta.totalOrderCount
                    status.setRollbackOnly();
                }
            }
            catch (Exception ex) {
                log.error(ex.getMessage())
                task.meta.orderProgress = taskService.countProgress(task.meta.totalOrderCount, task.meta.totalOrderCount as Integer)
                task.progress = taskService.countProgress(task.totalRecord, task.meta.totalOrderCount)
                task.taskLogger.error("Order" + ": Summary Export Failed  (${orders.collect() {it.id}.join(', ')})", ex.message)
                task.meta.orderErrorCount = task.meta.totalOrderCount
                status.setRollbackOnly();
            }
        }


    }

    public void exportOrderDetail(Map<String, String> config, List<Order> orders, XeroClient client, MultiLoggerTask task) {
        XeroInvoices xeroInvoices = new XeroInvoices()
        xeroInvoices.invoiceList = new ArrayList<>();
        def defaultAccount =  AppUtil.getConfig( DomainConstants.SITE_CONFIG_TYPES.XERO, "default_account");
        def shippingAccount =  AppUtil.getConfig( DomainConstants.SITE_CONFIG_TYPES.XERO, "shipping_account");
        def guestCustomerId = AppUtil.getConfig( DomainConstants.SITE_CONFIG_TYPES.XERO, "guest_customer");
        guestCustomerId  = guestCustomerId ? guestCustomerId as Long : null
        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        for (Order order : orders) {
            try {
                order = Order.get(order.id)
                if(order.orderStatus == DomainConstants.ORDER_STATUS.CANCELLED) {
                    throw new XeroException("cancelled.order")
                }
                XeroTrack xeroTrack = XeroTrack.findOrCreateByLinkComponentAndComponentIdAndXeroOrganisationId(LinkComponent.TYPES.ORDER, order.id, XeroClient.currentOrganisation);
                boolean isUpdate = xeroTrack.xeroId ? true : false;

                XeroInvoice xeroInvoice = new XeroInvoice()
                xeroInvoice.lineItems = new ArrayOfLineItem()
                xeroInvoice.lineItems.lineItem = new ArrayList<XeroLineItem>()
                xeroInvoice.status = XeroInvoiceStatus.AUTHORISED
                xeroInvoice.type = XeroInvoiceType.ACCREC
                xeroInvoice.invoiceNumber = "WC" + "000000".substring(order.id.toString().length()) + order.id;

                XeroTrack customerLink = XeroTrack.findByLinkComponentAndComponentIdAndXeroOrganisationId(LinkComponent.TYPES.CUSTOMER, order.customerId ?: guestCustomerId, XeroClient.currentOrganisation);
                if(!customerLink) {
                    def defaultCustomer =  AppUtil.getConfig( DomainConstants.SITE_CONFIG_TYPES.XERO, "default_customer");
                    customerLink = defaultCustomer? XeroTrack.findByLinkComponentAndComponentIdAndXeroOrganisationId(LinkComponent.TYPES.CUSTOMER, defaultCustomer?.toLong(), XeroClient.currentOrganisation): null;
                    if(!customerLink)
                        throw new XeroException("customer.not.linked.with.xero")
                }
                xeroInvoice.contact = new XeroContact()
                xeroInvoice.contact.contactId = customerLink.xeroId
                Calendar calendar = Calendar.getInstance(timeZone)
                calendar.setTime(new Date(order.created.getTime() + TimeZone.default.getOffset(order.created.getTime())))
                xeroInvoice.date = calendar
                calendar = Calendar.getInstance(timeZone)
                Date payDate = order.payments.findAll() {it.status == "success"}.last()?.payingDate
                calendar.setTime(new Date(payDate.getTime() + TimeZone.default.getOffset(payDate.getTime())))
                xeroInvoice.dueDate = calendar

                Order o = Order.get(order.id);
                xeroInvoice.totalTax = o.totalTax;
                xeroInvoice.total = o.grandTotal;
                xeroInvoice.amountDue = o.due.toPrice().toBigDecimal()

                xeroInvoice.lineItems = new ArrayOfLineItem();
                List<XeroTrack> oiLinkList = []
                //xeroInvoice.IsTaxInclusive = true
                xeroInvoice.lineAmountTypes = ["Inclusive"]
                //xeroInvoice.amountPaid = order.paid

                o.items.each { OrderItem orderItem ->
                    if(orderItem.price > 0) {
                        Product product = Product.get(orderItem.productId)
                        //XeroTrack orderItemLink = XeroTrack.findOrCreateByLinkComponentAndComponentIdAndXeroOrganisationId(LinkComponent.TYPES.ORDER_ITEM, orderItem.id, XeroClient.currentOrganisation)
                        XeroTrack taxLink = XeroTrack.findOrCreateByLinkComponentAndComponentIdAndXeroOrganisationId(LinkComponent.TYPES.TAX, product?.taxProfile?.id, XeroClient.currentOrganisation)
                        XeroLineItem line = new XeroLineItem();
                        line.quantity = orderItem.quantity;
                        line.description = product?.description ?: orderItem.productName
                        line.unitAmount = orderItem.price > 0 ?(orderItem.price + orderItem.tax / orderItem.quantity).toPrice().toBigDecimal() : "0".toBigDecimal()
                        line.lineAmount = (orderItem.totalPrice + orderItem.tax).toPrice().toBigDecimal()
                        line.taxAmount = orderItem.tax.toPrice().toBigDecimal()
                        line.discountRate = orderItem.discount > 0 ? (orderItem.discount / orderItem.totalPrice * 100).toPrice().toBigDecimal() : "0".toBigDecimal()
                        line.taxType = taxLink?.xeroId
                        XeroTrack itemLink = XeroTrack.findByLinkComponentAndComponentIdAndXeroOrganisationId(LinkComponent.TYPES.PRODUCT, orderItem.productId, XeroClient.currentOrganisation)
                        if(!itemLink) {
                            def defaultProduct =  AppUtil.getConfig( DomainConstants.SITE_CONFIG_TYPES.XERO, "default_product");
                            defaultProduct  = defaultProduct ? defaultProduct as Long : null
                            itemLink = defaultProduct? XeroTrack.findByLinkComponentAndComponentIdAndXeroOrganisationId(LinkComponent.TYPES.PRODUCT, defaultProduct, XeroClient.currentOrganisation): null;
                            if(!itemLink)
                                throw new XeroException("order.item.not.linked.with.xero", [orderItem.productName])
                        }

                        line.itemCode = itemLink.xeroId;
                        line.accountCode = defaultAccount

                        xeroInvoice.lineItems.lineItem.add(line)
                    }

                    //oiLinkList.add(orderItemLink)
                }

                if(order.shippingCost || order.handlingCost) {
                    if(!shippingAccount) {
                        throw new XeroException("shipping.tax.profile.not.found")
                    }
                    XeroLineItem shippingLine = new XeroLineItem()
                    shippingLine.description = "Shipping"
                    shippingLine.unitAmount =  order.shippingCost + order.shippingTax + order.handlingCost;
                    shippingLine.quantity = 1;
                    shippingLine.accountCode = shippingAccount
                    shippingLine.lineAmount = order.shippingCost + order.shippingTax + order.handlingCost;
                    shippingLine.taxAmount = order.shippingTax;
                    if(order.shippingTax) {
                        TaxProfile profile = TaxProfile.get(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.SHIPPING, "shipping_tax_profile"))
                        XeroTrack taxLink = XeroTrack.findOrCreateByLinkComponentAndComponentIdAndXeroOrganisationId(LinkComponent.TYPES.TAX, profile?.id, XeroClient.currentOrganisation)
                        shippingLine.taxType = taxLink?.xeroId
                    }
                    xeroInvoice.lineItems.lineItem.add(shippingLine)
                }

                if(order.totalSurcharge > 0) {
                    def enableSurchargeSync = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.XERO, "enable_surcharge_sync");
                    def surchargeAccount
                    if (enableSurchargeSync == "true") {
                        surchargeAccount = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.XERO, "surcharge_account");
                        if(!surchargeAccount) {
                            throw new XeroException("surcharge.account.not.found")
                        }
                    }
                    else {
                        throw new XeroException("order.with.surcharge.not.enabled")
                    }
                    XeroLineItem surchargeLine = new XeroLineItem();
                    surchargeLine.description = "Surcharge"
                    surchargeLine.lineAmount = order.totalSurcharge
                    surchargeLine.quantity = 1
                    surchargeLine.taxAmount = 0
                    surchargeLine.unitAmount = order.totalSurcharge
                    surchargeLine.accountCode = surchargeAccount
                    xeroInvoice.lineItems.lineItem.add(surchargeLine)
                }

                xeroInvoices.invoiceList.add(xeroInvoice)

            } catch (Exception ex) {
                log.error(ex.getMessage())
                task.meta.orderProgress = taskService.countProgress(task.meta.totalOrderCount, ++task.meta.orderComplete as Integer)
                task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                task.taskLogger.error("Order" + ": ${order?.id}", ex.message)
                task.meta.orderErrorCount++
            }
        }

        if (xeroInvoices.invoiceList.size() > 0) {
            def responseInvoices
            def orderId = ""
            try {
                responseInvoices = client.addInvoices(xeroInvoices)
                try {
                    orderId = responseInvoices?."InvoiceNumber"?.replaceFirst("WC0*", "")
                }
                catch (Exception ex) {}

                if(responseInvoices instanceof String && responseInvoices.contains("Error")) {
                    throw new XeroException("Error occurred while communicating with xero. All failed " + responseInvoices)
                }
                if(!responseInvoices) {

                }
            }
            catch (Exception ex) {
                task.meta.orderProgress = taskService.countProgress(task.meta.totalOrderCount, task.meta.totalOrderCount as Integer)
                task.progress = taskService.countProgress(task.totalRecord, task.meta.totalOrderCount)
                task.taskLogger.error("Order" + " " + orderId, ex.message)
                task.meta.orderErrorCount = task.meta.totalOrderCount
                return
                //status.setRollbackOnly();
            }
            def successXeroInvoiceList = []
            xeroInvoices.invoiceList.each { XeroInvoice invoice ->
                Order order
                XeroTrack.withNewTransaction { def status ->
                    try {
                        order = Order.createCriteria().get() {
                            eq "id", "${invoice.invoiceNumber.replaceFirst("WC0*", "")}".toLong()
                        }
                        XeroTrack xeroTrack = XeroTrack.findOrCreateByLinkComponentAndXeroIdAndXeroOrganisationId(LinkComponent.TYPES.ORDER, invoice.invoiceNumber, XeroClient.currentOrganisation);
                        xeroTrack = xeroTrack ?: new XeroTrack()
                        def responseInvoice = responseInvoices.find {it."InvoiceNumber" == invoice.invoiceNumber }
                        if (responseInvoice."HasValidationErrors" == true || responseInvoice."HasErrors" == true) {
                            task.meta.orderProgress = taskService.countProgress(task.meta.totalOrderCount, ++task.meta.orderComplete as Integer)
                            task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                            task.taskLogger.error("Order", responseInvoice."ValidationErrors".collect{it."Message"}.join(","))
                            task.meta.orderErrorCount++
                            status.setRollbackOnly();
                        } else {
                            xeroTrack.componentId = order.id
                            xeroTrack.xeroId = invoice.invoiceNumber
                            xeroTrack.xeroVersion = responseInvoice."UpdatedDateUTC";
                            xeroTrack.save()
                            if (xeroTrack.hasErrors()) {
                                throw new XeroException("xero.link.could.not.save")
                            }
                            successXeroInvoiceList.add(responseInvoice)
                        }
                    }
                    catch (Exception ex) {
                        task.meta.orderProgress = taskService.countProgress(task.meta.totalOrderCount, ++task.meta.orderComplete as Integer)
                        task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                        task.taskLogger.error("Order" + ": ${order?.id}", ex.message)
                        task.meta.orderErrorCount++
                        status.setRollbackOnly();
                    }
                }
            }
            exportPayment(successXeroInvoiceList, config, task, client)
        }
    }

    private def exportPayment(def successXeroInvoiceList, def config, MultiLoggerTask task, XeroClient client) {
        XeroPayments xeroPayments = new XeroPayments()
        List<XeroPayment> paymentList = new ArrayList<>();
        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        successXeroInvoiceList.each {def xeroInvoice ->
            try {
                Order order = Order.createCriteria().get() {
                    eq "id", "${xeroInvoice."InvoiceNumber".replaceFirst("WC0*", "")}".toLong()
                }
                List<Payment> wcPaymentList = Payment.findAll() {
                    eq "order.id", order.id
                    eq "status", "success"
                }

                wcPaymentList.each {Payment payment ->
                    def paymentAccountConfig =  AppUtil.getConfig( DomainConstants.SITE_CONFIG_TYPES.XERO, "payment_account_mapping");
                    def defaultPaymentAccount =  AppUtil.getConfig( DomainConstants.SITE_CONFIG_TYPES.XERO, "default_payment_account");
                    def paymentAccountJosnObject = paymentAccountConfig ? JSON.parse(paymentAccountConfig) : null
                    XeroPayment xeroPayment = new XeroPayment();
                    xeroPayment.amount = payment.amount
                    XeroAccount account = new XeroAccount()
                    account.setCode(paymentAccountJosnObject."${payment.gatewayCode}" ?: defaultPaymentAccount)
                    xeroPayment.account =  account
                    xeroPayment.invoice = new XeroInvoice()
                    xeroPayment.invoice.invoiceID = xeroInvoice."InvoiceID"
                    Calendar calendar = Calendar.getInstance(timeZone)
                    calendar.setTime(new Date(payment.payingDate.getTime() + TimeZone.default.getOffset(payment.payingDate.getTime())))
                    xeroPayment.date = calendar
                    paymentList.add(xeroPayment)
                }
                xeroPayments.setPaymentList(paymentList)
            }
            catch (Exception ex) {
                ex.printStackTrace()
            }
        }
        String allfailed = ""
        def responsePayments
        try {
            if(xeroPayments?.paymentList) {
                responsePayments = client.addPayments(xeroPayments)
            }
            if(!responsePayments) {
                allfailed += "All payment failed"
            }
        }
        catch (Exception ex) {
            allfailed += "All payment failed"
        }
        finally {
            successXeroInvoiceList.each {def successInvoice ->
                String paymentErrorMessage = ""
                def invoiceId = successInvoice."InvoiceID"
                def invoiceNumber = successInvoice."InvoiceNumber"
                responsePayments?.findAll {it?.Invoice?."InvoiceID" == invoiceId}.each {def responsePayment ->
                    if (responsePayment."HasValidationErrors" == true || responsePayment."HasErrors" == true) {
                        paymentErrorMessage += "Order : " + "${invoiceNumber.replaceFirst("WC0*", "")}" + " Amount: ${responsePayment."Amount"} " + responsePayment."ValidationErrors".collect{it."Message"}.join(",")
                    }
                }
                task.meta.orderProgress = taskService.countProgress(task.meta.totalOrderCount, ++task.meta.orderComplete as Integer)
                task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                task.taskLogger.success(invoiceNumber, ("Order Export Success" + (paymentErrorMessage? " : payment.error: ${paymentErrorMessage}" : "") + allfailed))
                task.meta.orderSuccessCount++
            }
        }

    }

    private int getSummaryInvoiceId () {
        int lastId = 0
        def allTrack =  XeroTrack.findAll() {
            eq "linkComponent", "order"
            like "xeroId", "WS%"
        }
        if(allTrack) {
            def collectId = allTrack?.findAll {it.xeroId.toString()}
            lastId = collectId?.size() > 0 ? collectId.collect {it.xeroId}.unique().last()?.replaceAll("WS0*", "")?.toInteger() : 0
        }

        return ++lastId
    }
}
