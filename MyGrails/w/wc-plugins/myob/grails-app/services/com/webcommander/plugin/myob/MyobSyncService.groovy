package com.webcommander.plugin.myob

import com.webcommander.admin.ConfigService
import com.webcommander.admin.Country
import com.webcommander.admin.Customer
import com.webcommander.admin.State
import com.webcommander.admin.Zone
import com.webcommander.common.CommonService
import com.webcommander.constants.DomainConstants
import com.webcommander.plugin.myob.constants.LinkComponent
import com.webcommander.plugin.myob.constants.MYOB
import com.webcommander.plugin.myob.exceptions.MyOBException
import com.webcommander.task.MultiLoggerTask
import com.webcommander.task.TaskService
import com.webcommander.util.AppUtil
import com.webcommander.util.StringUtil
import com.webcommander.webcommerce.*
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import org.grails.plugins.web.taglib.ApplicationTagLib
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

import java.text.SimpleDateFormat

@Transactional
class MyobSyncService {
    @Autowired()
    @Qualifier("org.grails.plugins.web.taglib.ApplicationTagLib")
    ApplicationTagLib g;
    TaskService taskService
    CommonService commonService
    ProductImportService productImportService
    ConfigService configService
    final static String IMAGE_SOURCE = "/pub/product-import/images";

    public void importTax(Map<String, String> config, List taxes, MultiLoggerTask task) {
        boolean updateExisting = config.update_tax.toBoolean();
        for (JSONObject tax : taxes) {
            MyobLink.withNewTransaction { status ->
                try {
                    TaxProfile taxProfile;
                    TaxCode taxCode;
                    TaxRule taxRule
                    MyobLink myobLink = MyobLink.findOrCreateByLinkComponentAndUid(LinkComponent.TYPES.TAX, tax.UID);
                    boolean continueImport = true
                    if (myobLink.componentId) {
                        taxProfile = TaxProfile.get(myobLink.componentId);
                        if (taxProfile && !updateExisting) {
                            continueImport = false;
                        }
                    }
                    if (continueImport) {
                        taxProfile = taxProfile ?: new TaxProfile()
                        taxProfile.name = tax.Code;
                        taxProfile.description = tax.Description;

                        taxRule = taxProfile.rules?.size() > 0 ? taxProfile.rules[0] : new TaxRule();
                        taxRule.name = tax.Code;
                        taxRule.zone = taxRule.zone ?: Zone.get(config.tax_default_zone);

                        taxCode = taxRule.code ?: new TaxCode();
                        taxRule.code = taxCode
                        taxCode.name = tax.Code;
                        taxCode.label = tax.Type;
                        taxCode.rate = tax.Rate;
                        taxCode.description = tax.Description

                        [taxCode, taxRule, taxProfile].each {
                            if(it == taxProfile){
                                taxProfile.addToRules(taxRule);
                            }
                            it.save();
                            if (it.hasErrors()) {
                                throw new MyOBException("tax.could.not.import")
                            }
                        }
                        myobLink.componentId = taxProfile.id;
                        myobLink.myobVersion = tax.RowVersion
                        myobLink.save()
                        if (myobLink.hasErrors()) {
                            throw new MyOBException("myob.link.could.not.save")
                        }
                    }
                    task.meta.taxProgress = taskService.countProgress(task.meta.totalTaxCount, ++task.meta.taxComplete as Integer)
                    task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                    task.meta.taxSuccessCount++
                    task.taskLogger.success(taxProfile.name, "tax.import.success");
                } catch (ValidationException ex) {
                    String validationMessage = "";
                    ex.errors.getFieldErrors().each {
                        validationMessage += it.objectName.replace("com.webcommander.webcommerce.", "") + " " + it.field + " rejected value " + it.rejectedValue
                    }
                    task.errorCount++
                    task.meta.taxProgress = taskService.countProgress(task.meta.totalTaxCount, ++task.meta.taxComplete as Integer)
                    task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                    task.taskLogger.error("Tax", "${tax? 'Myob Code(' + tax.Code + ') ': ''}"  + validationMessage)
                    task.meta.taxErrorCount++
                    status.setRollbackOnly();
                } catch (Exception ex) {
                    task.errorCount++
                    task.meta.taxProgress = taskService.countProgress(task.meta.totalTaxCount, ++task.meta.taxComplete as Integer)
                    task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                    task.taskLogger.error("Tax", "${tax? 'Myob Code(' + tax.Code + ') ': ''}" + ex.message)
                    task.meta.taxErrorCount++
                    status.setRollbackOnly();
                }
            }
        }
    }

    private void mapCustomAttr(Product product, String field, customAttr, Map config, Map myobCategories) {
        if(myobCategories.containsKey(field)) {
            def value = customAttr?.Value
            myobCategories[field] = value instanceof String ? value : null
            return;
        }
        if(field == LinkComponent.CUSTOME_ATTR_MAPPING.BRAND) {
            product.brand = Brand.findByName(customAttr?.Value)
        } else if (field == LinkComponent.CUSTOME_ATTR_MAPPING.MANUFACTURER) {
            product.manufacturer = !config.update_product_manufacturer.toBoolean() && !product.id ? product.manufacturer : Manufacturer.findByName(customAttr?.Value)
        } else if (field == LinkComponent.CUSTOME_ATTR_MAPPING.HEIGHT) {
            product.height = customAttr?.Value?.toDouble(0) ?: 0.0;
        } else if (field == LinkComponent.CUSTOME_ATTR_MAPPING.WEIGHT) {
            product.weight = customAttr?.Value?.toDouble(0) ?: 0.0;
        }else if (field == LinkComponent.CUSTOME_ATTR_MAPPING.WIDTH) {
            product.width = customAttr?.Value?.toDouble(0) ?: 0.0;
        } else if (field == LinkComponent.CUSTOME_ATTR_MAPPING.LENGHT) {
            product.length = customAttr?.Value?.toDouble(0) ?: 0.0;
        } else if(field == LinkComponent.CUSTOME_ATTR_MAPPING.MODEL) {
            product.model = customAttr?.Value
        }
    }

    private void mapProductCategories(Product product, Map myobCategories) {
        product.parent = null
        List toRemove = new ArrayList(product.parents);
        toRemove.each {
            product.removeFromParents(it)
        }
        Category root = null
        List keys = ["category_level_1", "category_level_2", "category_level_3"];
        for (String key: keys) {
            String name = myobCategories[key]
            if(name) {
                Category category = Category.findByNameAndParent(name, root)
                if (!category) {
                    category = new Category(name: name, sku:  commonService.getSKUForDomain(Category), parent: root);
                    category.url = commonService.getUrlForDomain(category)
                    category.save()
                }
                product.addToParents(category)
                root = category
            } else {
                break
            }
        }
        product.parent = root
    }

    public void importProduct(Map<String, String> config, List products, MultiLoggerTask task) {
        boolean updateName = config.update_product_name.toBoolean();
        boolean updateDesc = config.update_product_description.toBoolean();
        boolean updateTax = config.update_product_tax.toBoolean();
        boolean updateBasePrice = config.update_product_baseprice.toBoolean();
        boolean updateCostPrice = config.update_product_costprice.toBoolean();
        boolean updateCategory = config.update_product_category.toBoolean();
        boolean updateManufacturer = config.update_product_manufacturer.toBoolean();
        boolean updateStock = config.update_product_stock.toBoolean();
        boolean updateInventory = config.update_product_inventory.toBoolean();
        boolean updateImage = config.update_product_image.toBoolean();
        for (JSONObject prod : products) {
            MyobLink.withNewTransaction { status ->
                try {
                    Product product;
                    MyobLink myobLink = MyobLink.findOrCreateByLinkComponentAndUid(LinkComponent.TYPES.PRODUCT, prod.UID);
                    Map myobCategories = [category_level_1: null, category_level_2: null, category_level_3: null]
                    boolean continueImport = true;
                    if (myobLink.componentId) {
                        product = Product.get(myobLink.componentId);
                        continueImport = [updateImage, updateName, updateDesc, updateTax, updateBasePrice, updateCostPrice, updateCategory, updateManufacturer, updateStock, updateInventory].any {
                            return it
                        }
                    }
                    if (continueImport) {
                        boolean isUpdate = product ? true : false;
                        product = isUpdate ? product : new Product(sku: commonService.getSKUForDomain(Product));
                        product.sku = prod.Number;
                        product.productType = product.productType ?: DomainConstants.PRODUCT_TYPE.PHYSICAL
                        if (!isUpdate || updateName) {
                            product.name = prod.Name
                        }
                        if (!isUpdate || updateDesc) {
                            product.description = prod.Description
                        }
                        if (!isUpdate || updateBasePrice) {
                            product.basePrice = prod.BaseSellingPrice
                        }
                        if (!isUpdate || updateCostPrice) {
                            try {
                                product.costPrice = prod.BuyingDetails.LastPurchasePrice
                            } catch (Exception ex) {
                                product.costPrice = 0;
                            }
                        }
                        if (!isUpdate || updateTax) {
                            try {
                                MyobLink taxLink = MyobLink.findByLinkComponentAndUid(LinkComponent.TYPES.TAX, prod.SellingDetails.TaxCode.UID);
                                product.taxProfile = TaxProfile.get(taxLink.componentId)
                            } catch (Exception ex) {
                                product.taxProfile = null
                            }
                        }
                        if (!isUpdate || updateStock) {
                            product.availableStock = prod.QuantityOnHand;
                            try {
                                product.lowStockLevel = Integer.parseInt(prod.BuyingDetails.RestockingInformation.MinimumLevelForRestockingAlert)
                            } catch (Exception ex) {
                                product.lowStockLevel = 0;
                            }

                        }
                        if (!isUpdate || updateInventory) {
                            product.isInventoryEnabled = prod.IsInventoried
                        }
                        if (product.isInventoryEnabled == null) {
                            product.isInventoryEnabled = false;
                        }

                        if (!product.url) {
                            product.url = commonService.getUrlForDomain(product);
                        }
                        if(config.mapping_custom_list_1) {
                            mapCustomAttr(product, config.mapping_custom_list_1, prod.CustomList1, config, myobCategories)
                        }
                        if(config.mapping_custom_list_2) {
                            mapCustomAttr(product, config.mapping_custom_list_2, prod.CustomList2, config, myobCategories)
                        }
                        if(config.mapping_custom_list_3) {
                            mapCustomAttr(product, config.mapping_custom_list_3, prod.CustomList3, config, myobCategories)
                        }
                        if(config.mapping_custom_field_1) {
                            mapCustomAttr(product, config.mapping_custom_field_1, prod.CustomField1, config, myobCategories)
                        }
                        if(config.mapping_custom_field_2) {
                            mapCustomAttr(product, config.mapping_custom_field_2, prod.CustomField2, config, myobCategories)
                        }
                        if(config.mapping_custom_field_3) {
                            mapCustomAttr(product, config.mapping_custom_field_3, prod.CustomField3, config, myobCategories)
                        }
                        product.save()
                        if(!isUpdate || updateCategory) {
                            mapProductCategories(product, myobCategories)
                        }
                        if(!isUpdate || updateImage) {
                            productImportService.saveProductImages(product, product.sku + ".jpg,"  + product.sku + ".png", IMAGE_SOURCE, task, product.name)
                        }
                        product.save()
                        if (product.hasErrors()) {
                            throw new MyOBException("product.could.not.import")
                        }
                        myobLink.componentId = product.id;
                        myobLink.myobVersion = prod.RowVersion;
                        myobLink.save();
                        if (myobLink.hasErrors()) {
                            throw new MyOBException("myob.link.could.not.save");
                        }
                    }

                    task.meta.productProgress = taskService.countProgress(task.meta.totalProductCount, ++task.meta.productComplete as Integer)
                    task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                    task.meta.productSuccessCount++
                    task.taskLogger.success(product.name, "product.import.success")
                } catch (Exception ex) {
                    task.meta.productProgress = taskService.countProgress(task.meta.totalProductCount, ++task.meta.productComplete as Integer)
                    task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                    task.taskLogger.error("Product ${prod && prod.Name? '(' + prod.Name + ')': '' }", "${prod && prod.Number? 'Myob Number(' + prod.Number + ') ': ''}"  + ex.message)
                    task.meta.productErrorCount++
                    status.setRollbackOnly();
                }
            }
        }
    }

    public void exportProduct(Map<String, String> config, List<Product> products, MyOBClient client, MultiLoggerTask task) {
        String defaultTaxCode = config.product_default_tax
        for (Product product : products) {
            MyobLink.withNewTransaction { status ->
                try {
                    JSONObject jsonObject = new JSONObject();
                    MyobLink myobLink = MyobLink.findOrCreateByLinkComponentAndComponentId(LinkComponent.TYPES.PRODUCT, product.id);
                    boolean isUpdate = false;
                    boolean continueExport = true;
                    if (myobLink.uid) {
                        isUpdate = true;
                        jsonObject.UID = myobLink.uid;
                        jsonObject.RowVersion = myobLink.myobVersion;
                    }
                    jsonObject.Number = product.sku;
                    jsonObject.Name = product.name
                    jsonObject.Description = product.description;
                    jsonObject.IsSold = true;
                    jsonObject.SellingDetails = new JSONObject()
                    jsonObject.SellingDetails.BaseSellingPrice = product.basePrice
                    if (product.costPrice) {
                        try {
                            jsonObject.BuyingDetails = new JSONObject();
                            jsonObject.IsBought = true
                        } catch (Exception ex) {

                        }
                    }
                    try {
                        Product p = product;
                        MyobLink taxLink = MyobLink.findByLinkComponentAndComponentId(LinkComponent.TYPES.TAX, product.taxProfileId);
                        if(!taxLink) {
                            String defaultTaxProfile = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.TAX, "default_tax_profile");
                            taxLink = defaultTaxProfile ? MyobLink.findByLinkComponentAndComponentId(LinkComponent.TYPES.TAX, defaultTaxProfile.toLong()) : null;
                        }
                        if (!jsonObject.SellingDetails) {
                            jsonObject.SellingDetails = new JSONObject()
                        }
                        jsonObject.SellingDetails.TaxCode = new JSONObject()
                        jsonObject.SellingDetails.TaxCode.UID = taxLink ? taxLink.uid : defaultTaxCode
                        if (!jsonObject.BuyingDetails) {
                            jsonObject.BuyingDetails = new JSONObject()
                        }
                        jsonObject.BuyingDetails.TaxCode = new JSONObject()
                        jsonObject.BuyingDetails.TaxCode.UID = taxLink ? taxLink.uid : defaultTaxCode

                    } catch (Exception ex) {

                    }
                    try {
                        if (product.lowStockLevel) {
                            jsonObject.BuyingDetails.RestockingInformation = new JSONObject()
                            jsonObject.BuyingDetails.RestockingInformation.MinimumLevelForRestockingAlert = product.lowStockLevel
                        }
                    } catch (Exception ex) {

                    }

                    jsonObject.IsInventoried = product.isInventoryEnabled;
                    product.isInventoryEnabled = false;
                    if (product.parentId) {
                        try {
                            jsonObject.CustomField1 = new JSONObject()
                            jsonObject.CustomField1.Value = product.parentId;
                        } catch (Exception ex) {

                        }
                    }
                    if (product.manufacturerId) {
                        try {
                            jsonObject.CustomField2 = new JSONObject()
                            jsonObject.CustomField2.Value = product.manufacturerId;
                        } catch (Exception ex) {

                        }
                    }


                    if (jsonObject.IsBought && !jsonObject.IsInventoried) {
                        jsonObject.ExpenseAccount = new JSONObject()
                        jsonObject.ExpenseAccount.UID = config.product_expense_account
                    }
                    if (jsonObject.IsSold && jsonObject.IsInventoried) {
                        jsonObject.CostOfSalesAccount = new JSONObject()
                        jsonObject.CostOfSalesAccount.UID = config.product_costofsale_account
                    }
                    if (jsonObject.IsInventoried) {
                        jsonObject.AssetAccount = new JSONObject()
                        jsonObject.AssetAccount.UID = config.product_asset_account
                    }
                    if (jsonObject.IsSold) {
                        jsonObject.IncomeAccount = new JSONObject()
                        jsonObject.IncomeAccount.UID = config.product_income_account
                    }


                    MYOB operation = myobLink.uid ? MYOB.UPDATE : MYOB.CREATE;
                    client.performOperation(operation, MyobService.tabs.product.endpoint, myobLink.uid, jsonObject)
                    JSONObject responseObject = client.performQuery(MyobService.tabs.product.endpoint, "\$filter=Number eq '${product.sku}'").Items[0]

                    myobLink.uid = responseObject.UID;
                    myobLink.myobVersion = responseObject.RowVersion;
                    myobLink.save();

                    Product p = Product.get(product.id);
                    p.inventoryAdjustments.each { ProductInventoryAdjustment adj ->
                        MyobLink adjLink = MyobLink.findOrCreateByLinkComponentAndComponentId(LinkComponent.TYPES.PRODUCT_ADJUSTMENT, adj.id);
                        boolean adjUpdate = adjLink.uid;
                        if(isUpdate) {
                            return;
                        }
                        JSONObject adjustment = new JSONObject();
                        adjustment.Memo = adj.id
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        adjustment.Date = sdf.format(adj.created);
                        adjustment.Lines = new JSONArray();
                        JSONObject line = new JSONObject()
                        line.Account = new JSONObject();
                        line.Account.UID = config.product_purchase_account;
                        line.Item = new JSONObject()
                        line.Item.UID = responseObject.UID;
                        line.Quantity = adj.changeQuantity;
                        line.Memo = adj.note;
                        if (p.costPrice) {
                            line.UnitCost = p.costPrice
                            line.Amount = p.costPrice * adj.changeQuantity
                        }
                        adjustment.Lines.add(line)
                        if (adjUpdate) {
                            adjustment.UID = adjLink.uid
                            adjustment.RowVersion = adjLink.myobVersion
                        }
                        client.performOperation(MYOB.CREATE, "Inventory/Adjustment", null, adjustment)
                        JSONObject respAdj = client.performQuery("Inventory/Adjustment", "\$filter=Memo eq '${adj.id}'").Items[0]
                        adjLink.uid = respAdj.UID;
                        adjLink.myobVersion = respAdj.RowVersion;
                        adjLink.save()
                    }
                    if (myobLink.hasErrors()) {
                        throw new MyOBException("myob.link.could.not.save");
                    }
                    task.meta.productProgress = taskService.countProgress(task.meta.totalProductCount, ++task.meta.productComplete as Integer)
                    task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                    task.taskLogger.success(product.name, "product.export.success")
                    task.meta.productSuccessCount++
                } catch (Exception ex) {
                    task.meta.productProgress = taskService.countProgress(task.meta.totalProductCount, ++task.meta.productComplete as Integer)
                    task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                    task.taskLogger.error("product ${': ' + product?.name}", ex.message)
                    task.meta.productErrorCount++
                    status.setRollbackOnly();
                }
            }
        }
    }

    public Address getAddressFromMyOBAddress(JSONObject myObAddress, Address address, Customer customer, Map configs) {
        String phoneField = configs.customer_phone;
        String mobileField = configs.customer_mobile;
        address.addressLine1 = myObAddress.Street;
        address.city = myObAddress.City
        address.country = Country.findByName(myObAddress.Country)
        address.state = State.findByCountryAndName(address.country, myObAddress.State) ?: State.findByCountryAndCode(address.country, myObAddress.State)
        address.postCode = myObAddress.PostCode;
        address.phone = myObAddress[phoneField]
        address.mobile = myObAddress[mobileField]
        address.email = myObAddress.Email;
        address.fax = myObAddress.Fax
        if (myObAddress.ContactName) {
            String contactName = myObAddress.ContactName;
            String[] contactNames = contactName.split(" ");
            address.firstName = contactNames.first();
            address.lastName = contactNames.last();
        }
        address.firstName = address.firstName ?: customer.firstName
        return address
    }

    public void importCustomer(Map<String, String> config, List customers, MultiLoggerTask task) {
        boolean updateCustomer = config.update_customer.toBoolean();

        for (JSONObject cus : customers) {
            MyobLink.withNewTransaction { status ->
                try {
                    Customer customer;
                    MyobLink myobLink = MyobLink.findOrCreateByLinkComponentAndUid(LinkComponent.TYPES.CUSTOMER, cus.UID);

                    boolean continueImport = true
                    if (myobLink.componentId) {
                        customer = Customer.get(myobLink.componentId);
                        continueImport = updateCustomer
                    }
                    if (continueImport) {
                        customer = customer ?: new Customer();
                        if (cus.IsIndividual) {
                            customer.isCompany = false;
                            customer.firstName = cus.FirstName
                            customer.companyName = null
                        } else {
                            customer.isCompany = true;
                            customer.firstName = cus.CompanyName
                            customer.companyName = cus.CompanyName
                        }
                        customer.lastName = cus.LastName;
                        customer.firstName = customer.firstName?: customer.lastName
                        customer.status = cus.IsActive ? DomainConstants.CUSTOMER_STATUS.ACTIVE : DomainConstants.CUSTOMER_STATUS.INACTIVE;
                        JSONArray addresses = cus.Addresses;
                        int addri = 0
                        def addressesToDelete = []
                        List addrFields = ["activeBillingAddress", "activeShippingAddress", "address"]
                        for (JSONObject addr : addresses) {
                            Address address = getAddressFromMyOBAddress(addr, customer[addrFields[addri]] ?: new Address(), customer, config)
                            if(!address.validate()) {
                                if(customer[addrFields[addri]]) {
                                    addressesToDelete.add(customer[addrFields[addri]].id)
                                    customer[addrFields[addri]].discard()
                                    customer[addrFields[addri]] = null;
                                }
                                continue;
                            }
                            address.save()
                            customer[addrFields[addri]] = address;
                            if (addri < 2) {
                                String addrListField = addrFields[addri].substring(6);
                                customer."addTo${addrListField}es"(customer[addrFields[addri]])
                            }
                            addri++;
                            if(addri >= 3) {
                                break
                            }
                        }
                        if (!customer.address && customer.activeBillingAddress) {
                            customer.address = new Address()
                            ["addressLine1", "city", "country", "state", "postCode", "phone", "mobile", "email", "fax", "firstName", "lastName"].each {
                                customer.address[it] = customer.activeBillingAddress[it];
                            }
                            if (!customer.address.firstName) {
                                customer.address.firstName = customer.firstName;
                            }
                        }
                        if(customer.address) {
                            if (!customer.address.email) {
                                throw new MyOBException("customer.email.not.found")
                            }
                            customer.address.save()
                        }
                        if (!customer.activeShippingAddress && customer.activeBillingAddress) {
                            customer.activeShippingAddress = new Address()
                            ["addressLine1", "city", "country", "state", "postCode", "phone", "mobile", "email", "fax", "firstName", "lastName"].each {
                                customer.activeShippingAddress[it] = customer.activeBillingAddress[it];
                            }
                            if (!customer.activeBillingAddress.firstName) {
                                customer.activeBillingAddress.firstName = customer.firstName
                            }
                            customer.activeShippingAddress.save()
                            customer.addToShippingAddresses(customer.activeShippingAddress);

                        }
                        if (!customer.activeShippingAddress) {
                            throw new Exception("No customer address or no address are usable")
                        }

                        if (!customer.userName) {
                            customer.userName = customer.address.email;
                        }
                        if (!customer.password) {
                            customer.password = "change it".encodeAsMD5();
                        }
                        customer.abn = cus.SellingDetails.ABN;
                        customer.abnBranch = cus.SellingDetails.ABNBranch;

                        if(customer.id) {
                            customer.merge()
                        }
                        else
                            customer.save(true)
                        if (customer.hasErrors()) {
                            throw new MyOBException("customer.could.not.import")
                        }
                        myobLink.componentId = customer.id;
                        myobLink.myobVersion = cus.RowVersion;
                        myobLink.save();
                        if (myobLink.hasErrors()) {
                            throw new MyOBException("myob.link.could.not.save")
                        }
                        addressesToDelete.each { def id ->
                            Address.get(id).delete(flush: true)
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
                    def name = cus?.LastName?: cus?.CompanyName
                    task.taskLogger.error("Customer ${name? '(' + name + ')': '' }}","${cus && cus.DisplayID? 'Myob Card Id (' + cus.DisplayID + ') ': ''}" + ex.message)
                    task.meta.customerErrorCount++
                    status.setRollbackOnly();
                }
            }
        }
    }

    public void exportCustomer(Map<String, String> config, List<Customer> customers, MyOBClient client, MultiLoggerTask task) {
        boolean updateCustomer = config.update_customer.toBoolean();
        String phoneField = config.customer_phone;
        String mobileField = config.customer_mobile;

        for (Customer customer : customers) {
            MyobLink.withNewTransaction { status ->
                try {
                    JSONObject cus = new JSONObject();
                    MyobLink myobLink = MyobLink.findOrCreateByLinkComponentAndComponentId(LinkComponent.TYPES.CUSTOMER, customer.id);
                    boolean continueExport = true;
                    if (myobLink.uid) {
                        cus.UID = myobLink.uid;
                        cus.RowVersion = myobLink.myobVersion;
                        continueExport = updateCustomer;
                    }
                    if (continueExport) {
                        cus.IsIndividual = !customer.isCompany;
                        if(customer.isCompany) {
                            cus.CompanyName = customer.fullName().length() > 50 ? customer.fullName().substring(0, 50): customer.fullName()
                        } else {
                            cus.FirstName = customer.firstName.length() > 30 ? customer.firstName.substring(0, 30): customer.firstName;
                            if(customer.lastName) {
                                cus.LastName = customer.lastName.length() > 20 ? customer.lastName.substring(0, 20): customer.lastName;
                            } else {
                                cus.LastName = cus.FirstName
                            }
                        }
                        cus.IsActive = customer.status == DomainConstants.CUSTOMER_STATUS.ACTIVE ? true : false;
                        cus.Addresses = new JSONArray();
                        List addrFields = ["activeBillingAddress", "activeShippingAddress", "address"]
                        for (int addri = 0; addri < 3; addri++) {
                            Address address;
                            if (!customer."${addrFields[addri]}Id") {
                                continue;
                            } else {
                                address = Address.get(customer."${addrFields[addri]}Id");
                            }

                            JSONObject addr = new JSONObject();
                            addr.Street = address.addressLine1;
                            addr.City = address.city;
                            if (address.country) {
                                addr.Country = address.country.name;
                            }
                            if (address.state) {
                                addr.State = address.state.name
                            }
                            addr.PostCode = address.postCode
                            addr[phoneField] = address.phone;
                            addr[mobileField] = address.mobile;
                            addr.Email = address.email;
                            addr.Fax = address.fax;

                            addr.ContactName = address.firstName + " " + address.lastName;
                            addr.ContactName = addr.ContactName?.toString()?.length() > 25 ? addr.ContactName?.toString()?.substring(0, 25) : addr.ContactName
                            addr.Location = addri + 1;
                            cus.Addresses.add(addr);
                        }

                        cus.SellingDetails = new JSONObject();
                        cus.SellingDetails.ABN = customer.abn;
                        cus.SellingDetails.ABNBranch = customer.abnBranch;
                        if (config.customer_tax) {
                            cus.SellingDetails.TaxCode = new JSONObject();
                            cus.SellingDetails.TaxCode.UID = config.customer_tax;
                            cus.SellingDetails.FreightTaxCode = new JSONObject();
                            cus.SellingDetails.FreightTaxCode.UID = config.customer_tax;
                        }
                        if (!myobLink.uid) {
                            cus.CustomField3 = new JSONObject();
                            cus.CustomField3.Value = customer.id
                        }

                        MYOB operation = myobLink.uid ? MYOB.UPDATE : MYOB.CREATE;
                        cus.DisplayID = "WC-" + StringUtil.getUuid().substring(0, 12)
                        String objectLocationUri = client.performOperation(operation, MyobService.tabs.customer.endpoint, myobLink.uid, cus)
                        JSONObject responseObject = client.performOperation(MYOB.READ, null, null, null, objectLocationUri)
                        myobLink.uid = responseObject.UID
                        myobLink.myobVersion = responseObject.RowVersion;
                        myobLink.save()
                        if (myobLink.hasErrors()) {
                            throw new MyOBException("myob.link.could.not.save")
                        }
                    }

                    task.meta.customerProgress = taskService.countProgress(task.meta.totalCustomerCount, ++task.meta.customerComplete as Integer)
                    task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                    task.taskLogger.success(customer.fullName, "customer.export.success")
                    task.meta.customerSuccessCount++
                } catch (Exception ex) {
                    task.meta.customerProgress = taskService.countProgress(task.meta.totalCustomerCount, ++task.meta.customerComplete as Integer)
                    task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                    task.taskLogger.error("Customer ${':' + customer?.fullName}", ex.message)
                    task.meta.customerErrorCount++
                    status.setRollbackOnly();
                }
            }
        }
    }


    public void exportOrder(Map<String, String> config, List<Order> orders, MyOBClient client, MultiLoggerTask task) {
        if(config.order_sync_type == "detail" ) {
            exportOrderDetail(config, orders, client, task)
        } else {
            exportOrderSummary(config, orders, client, task)
        }
    }

    public void exportOrderSummary(Map<String, String> config, List<Order> orders, MyOBClient client, MultiLoggerTask task) {
        MyobLink.withNewTransaction { status ->
            try {
                def defaultAccount =  AppUtil.getConfig( DomainConstants.SITE_CONFIG_TYPES.MYOB, "default_account");
                def defaultCustomer =  AppUtil.getConfig( DomainConstants.SITE_CONFIG_TYPES.MYOB, "default_customer");
                def customerLink = defaultCustomer? MyobLink.findByLinkComponentAndComponentId(LinkComponent.TYPES.CUSTOMER, defaultCustomer?.toLong()): null;
                def defaultPaymentAccount =  AppUtil.getConfig( DomainConstants.SITE_CONFIG_TYPES.MYOB, "default_payment_account");
                def defaultProduct =  AppUtil.getConfig( DomainConstants.SITE_CONFIG_TYPES.MYOB, "default_product");
                MyobLink itemLink = defaultProduct? MyobLink.findByLinkComponentAndComponentId(LinkComponent.TYPES.PRODUCT, defaultProduct?.toLong()): null;
                MyobLink taxLink = MyobLink.findByLinkComponentAndComponentId(LinkComponent.TYPES.TAX, Product.get(itemLink.componentId.toLong()).taxProfileId)
                TaxProfile profile = TaxProfile.get(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.SHIPPING, "shipping_tax_profile"))
                def defaultSurchargeProduct =  AppUtil.getConfig( DomainConstants.SITE_CONFIG_TYPES.MYOB, "default_surcharge_line_product");

                if(!profile) {
                    throw new MyOBException("shipping.tax.profile.not.found")
                }
                if(!taxLink) {
                    String defaultTaxProfile = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.TAX, "default_tax_profile");
                    taxLink = defaultTaxProfile ? MyobLink.findByLinkComponentAndComponentId(LinkComponent.TYPES.TAX, defaultTaxProfile.toLong()) : null;
                }
                if(!taxLink) {
                    throw new MyOBException("product.tax.code.not.linked.with.myob")
                }

                if(!itemLink)
                    throw new MyOBException("default.item.not.linked.with.myob")

                if(!customerLink)
                    throw new MyOBException("default.customer.not.selected")

                TaxProfile shippingTaxProfile = TaxProfile.get(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.SHIPPING, "shipping_tax_profile"))
                if(!shippingTaxProfile) {
                    throw new MyOBException("shipping.tax.profile.not.found")
                }

                JSONObject invoice = new JSONObject()
                def summaryId = getSummaryInvoiceId()
                invoice.Number = "WS" + "000000".substring(summaryId.toString().length()) + summaryId
                invoice.Customer = new JSONObject();
                invoice.Customer.UID = customerLink.uid;
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                invoice.Date = sdf.format(new Date());

                invoice.TotalTax = 0;
                invoice.TotalAmount = 0;
                invoice.BalanceDueAmountDecimal = 0
                invoice.IsTaxInclusive = true

                invoice.JournalMemo = "Summary"

                invoice.Freight = 0;

                JSONObject shippingLine
                JSONObject surchargeLine

                JSONObject line = new JSONObject();
                line.ShipQuantity = 1;
                line.description = "Summary"
                line.UnitPrice = 0
                line.Total = 0

                line.TaxCode = new JSONObject()
                line.TaxCode.UID = taxLink.uid

                line.Item = new JSONObject();
                line.Item.UID = itemLink.uid
                invoice.Lines = new JSONArray();
                invoice.Lines.add(line);
                orders.each { Order order ->
                    if(order.orderStatus == DomainConstants.ORDER_STATUS.CANCELLED) {
                        throw new MyOBException("cancelled.order")
                    }
                    MyobLink myobLink = MyobLink.findOrCreateByLinkComponentAndComponentId(LinkComponent.TYPES.ORDER, order.id);
                    boolean isUpdate = myobLink.uid ? true : false;

                    invoice.Customer = new JSONObject();
                    invoice.Customer.UID = customerLink.uid;

                    invoice.Date = sdf.format(order.created);

                    Order o = Order.get(order.id);
                    invoice.TotalTax += o.totalTax;
                    invoice.TotalAmount += o.grandTotal;
                    invoice.BalanceDueAmountDecimal += o.due.toPrice()

                    line.UnitPrice += o.total + o.totalTax
                    line.Total += o.total + o.totalTax

                    if(order.shippingCost || order.handlingCost) {
                        invoice.Freight += order.shippingCost + order.shippingTax + order.handlingCost;
                    }
                    if(order.shippingTax && !invoice.FreightTaxCode?.UID) {
                        taxLink = MyobLink.findByLinkComponentAndComponentId(LinkComponent.TYPES.TAX, shippingTaxProfile.id)
                        if(!taxLink) {
                            throw new MyOBException("shipping.tax.code.not.linked.with.myob")
                        }
                        invoice.FreightTaxCode = new JSONObject()
                        invoice.FreightTaxCode.UID = taxLink.uid
                    }
                    if(order.totalSurcharge > 0) {
                        if(!surchargeLine) {
                            def enableSurchargeSync = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.MYOB, "enable_surcharge_sync");
                            if (enableSurchargeSync == "true" && !defaultSurchargeProduct) {
                                throw new MyOBException("surcharge.line.product.required")
                            }
                            else {
                                throw new MyOBException("order.with.surcharge.not.enabled")
                            }

                            itemLink = defaultSurchargeProduct? MyobLink.findByLinkComponentAndComponentId(LinkComponent.TYPES.PRODUCT, defaultSurchargeProduct.toLong()): null;

                            taxLink = MyobLink.findByLinkComponentAndComponentId(LinkComponent.TYPES.TAX, Product.get(defaultSurchargeProduct.toLong()).taxProfileId)
                            if(!taxLink) {
                                String defaultTaxProfile = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.TAX, "default_tax_profile");
                                taxLink = defaultTaxProfile ? MyobLink.findByLinkComponentAndComponentId(LinkComponent.TYPES.TAX, defaultTaxProfile.toLong()) : null;
                            }
                            if(!taxLink) {
                                throw new MyOBException("surcharge.product.tax.code.not.linked.with.myob")
                            }

                            surchargeLine = new JSONObject();
                            surchargeLine.Description = "Surcharge"
                            surchargeLine.Total = order.totalSurcharge
                            surchargeLine.TaxTotal = 0
                            surchargeLine.Item = new JSONObject();
                            surchargeLine.Item.UID = itemLink.uid
                            surchargeLine.TaxCode = new JSONObject()
                            surchargeLine.TaxCode.UID = taxLink.uid
                            surchargeLine.ShipQuantity = 1
                            invoice.Lines.add(line);
                        }
                        else {
                            surchargeLine.Total = surchargeLine.Total + order.totalSurcharge
                        }
                    }
                }
                shippingLine ? invoice.Lines.add(shippingLine) : null
                surchargeLine ? invoice.Lines.add(surchargeLine) : null


                String objectLocationUri = client.performOperation(MYOB.CREATE, MyobService.tabs.order.endpoint, null, invoice);
                JSONObject responseInvoice = client.performOperation(MYOB.READ, null, null, null, objectLocationUri)

                if(responseInvoice) {
                    def totalPaid = 0
                    orders.each { Order order ->
                        order = Order.get(order.id)
                        MyobLink myobLink = MyobLink.findOrCreateByLinkComponentAndComponentId(LinkComponent.TYPES.ORDER, order.id);
                        myobLink.componentId = order.id
                        myobLink.uid = responseInvoice."UID"
                        myobLink.myobVersion = responseInvoice."RowVersion";
                        myobLink.save()
                        configService.update([[type: "myob", configKey: "last_sync_summary_id", value: responseInvoice.Number]])
                        totalPaid += order.getPaid()
                    }
                    def responsePayment
                    def allFailed = ""
                    try {
                        JSONObject customerPayment = new JSONObject()
                        customerPayment."AmountReceived" = totalPaid
                        customerPayment."Customer" = new JSONObject()
                        customerPayment."Customer".UID = responseInvoice.Customer.UID
                        customerPayment.Account = new JSONObject()
                        customerPayment.Account.UID = defaultPaymentAccount
                        customerPayment.Invoices = new JSONArray()
                        invoice = new JSONObject()
                        invoice.UID = responseInvoice.UID
                        invoice.AmountApplied = totalPaid
                        invoice.Type = "Invoice"
                        customerPayment.Invoices.add(invoice)

                        objectLocationUri = client.performOperation(MYOB.CREATE, "Sale/CustomerPayment", null, customerPayment);
                        responsePayment = client.performOperation(MYOB.READ, null, null, null, objectLocationUri)
                        if(responsePayment) {
                            orders.each {Order order ->
                                order = Order.get(order.id)
                                order.payments.findAll(){it.status == "success"}.each {Payment payment ->
                                    MyobLink myobLink = MyobLink.findOrCreateByLinkComponentAndComponentId(LinkComponent.TYPES.PAYMENT, payment.id);
                                    myobLink.componentId = payment.id
                                    myobLink.uid = responsePayment."UID"
                                    myobLink.myobVersion = responsePayment."RowVersion";
                                    myobLink.save()
                                }
                            }
                        }
                    }
                    catch (Exception ex) {
                        ex.printStackTrace()
                        allFailed = ex.getMessage()
                    }
                    finally {
                        task.meta.orderProgress = taskService.countProgress(task.meta.totalOrderCount, task.meta.totalOrderCount as Integer)
                        task.progress = taskService.countProgress(task.totalRecord, task.meta.totalOrderCount)
                        task.taskLogger.success("Order (${responseInvoice?.Number})", ("Summary Export Success  (${orders.collect() {it.id}.join(', ')})"  + allFailed))
                        task.meta.orderSuccessCount = task.meta.totalOrderCount
                    }
                }
                else {
                    task.meta.orderProgress = taskService.countProgress(task.meta.totalOrderCount, task.meta.totalOrderCount as Integer)
                    task.progress = taskService.countProgress(task.totalRecord, task.meta.totalOrderCount)
                    task.taskLogger.error("Order" + ": Summary Export Failed   (${orders.collect() {it.id}.join(', ')})" + "")
                    task.meta.orderErrorCount = task.meta.totalOrderCount
                    status.setRollbackOnly();
                }
            }
            catch (Exception ex) {
                log.error(ex.getMessage())
                task.meta.orderProgress = taskService.countProgress(task.meta.totalOrderCount, task.meta.totalOrderCount as Integer)
                task.progress = taskService.countProgress(task.totalRecord, task.meta.totalOrderCount)
                task.taskLogger.error("Order" + ": Summary Export Failed   (${orders.collect() {it.id}.join(', ')})", ex.message)
                task.meta.orderErrorCount = task.meta.totalOrderCount
                status.setRollbackOnly();
            }
        }


    }

    public void exportOrderDetail(Map<String, String> config, List<Order> orders, MyOBClient client, MultiLoggerTask task) {
        def guestCustomerId = AppUtil.getConfig( DomainConstants.SITE_CONFIG_TYPES.MYOB, "guest_customer");
        guestCustomerId  = guestCustomerId ? guestCustomerId as Long : null
        for (Order order : orders) {
            MyobLink.withNewTransaction { status ->
                try {
                    if(order.orderStatus == DomainConstants.ORDER_STATUS.CANCELLED) {
                        throw new MyOBException("cancelled.order")
                    }
                    MyobLink myobLink = MyobLink.findOrCreateByLinkComponentAndComponentId(LinkComponent.TYPES.ORDER, order.id);
                    boolean isUpdate = myobLink.uid ? true : false;

                    JSONObject invoice = new JSONObject()
                    invoice.Number = "WC" + "000000".substring(order.id.toString().length()) + order.id;

                    MyobLink customerLink = MyobLink.findByLinkComponentAndComponentId(LinkComponent.TYPES.CUSTOMER, order.customerId ?: guestCustomerId);
                    if(!customerLink) {
                        def defaultCustomer =  AppUtil.getConfig( DomainConstants.SITE_CONFIG_TYPES.MYOB, "default_customer");
                        customerLink = defaultCustomer? MyobLink.findByLinkComponentAndComponentId(LinkComponent.TYPES.CUSTOMER, defaultCustomer?.toLong()): null;
                        if(!customerLink)
                            throw new MyOBException("customer.not.linked.with.myob")
                    }
                    invoice.Customer = new JSONObject();
                    invoice.Customer.UID = customerLink.uid;

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    invoice.Date = sdf.format(order.created);

                    Order o = Order.get(order.id);
                    invoice.TotalTax = o.totalTax;
                    invoice.TotalAmount = o.grandTotal;
                    invoice.BalanceDueAmountDecimal = o.due.toPrice()
                    if(order.shippingCost || order.handlingCost) {
                        invoice.Freight = order.shippingCost + order.shippingTax + order.handlingCost;
                    }
                    if(order.shippingTax) {
                        TaxProfile profile = TaxProfile.get(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.SHIPPING, "shipping_tax_profile"))
                        if(!profile) {
                            throw new MyOBException("shipping.tax.profile.not.found")
                        }
                        MyobLink taxLink = MyobLink.findByLinkComponentAndComponentId(LinkComponent.TYPES.TAX, profile.id)
                        if(!taxLink) {
                            throw new MyOBException("shipping.tax.code.not.linked.with.myob")
                        }
                        invoice.FreightTaxCode = new JSONObject()
                        invoice.FreightTaxCode.UID = taxLink.uid
                    }
                    invoice.Lines = new JSONArray();
                    List<MyobLink> oiLinkList = []
                    invoice.IsTaxInclusive = true
                    o.items.each { OrderItem orderItem ->
                        MyobLink orderItemLink = MyobLink.findOrCreateByLinkComponentAndComponentId(LinkComponent.TYPES.ORDER_ITEM, orderItem.id)
                        JSONObject line = new JSONObject();
                        line.ShipQuantity = orderItem.quantity;
                        line.UnitPrice = orderItem.price > 0? (orderItem.price + orderItem.tax / orderItem.quantity).toPrice() : 0
                        line.Total = (orderItem.totalPrice + orderItem.tax).toPrice()
                        line.DiscountPercent = orderItem.discount > 0 ? (orderItem.discount / orderItem.totalPrice * 100).toPrice() : 0
                        MyobLink itemLink = MyobLink.findByLinkComponentAndComponentId(LinkComponent.TYPES.PRODUCT, orderItem.productId)
                        if(!itemLink) {
                            def defaultProduct =  AppUtil.getConfig( DomainConstants.SITE_CONFIG_TYPES.MYOB, "default_product");
                            itemLink = defaultProduct? MyobLink.findByLinkComponentAndComponentId(LinkComponent.TYPES.PRODUCT, defaultProduct?.toLong()): null;
                            if(!itemLink)
                                throw new MyOBException("order.item.not.linked.with.myob", [orderItem.productName])
                        }

                        line.Item = new JSONObject();
                        line.Item.UID = itemLink.uid

                        MyobLink taxLink = MyobLink.findByLinkComponentAndComponentId(LinkComponent.TYPES.TAX, Product.get(orderItem.productId).taxProfileId)
                        if(!taxLink) {
                            String defaultTaxProfile = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.TAX, "default_tax_profile");
                            taxLink = defaultTaxProfile ? MyobLink.findByLinkComponentAndComponentId(LinkComponent.TYPES.TAX, defaultTaxProfile.toLong()) : null;
                        }
                        if(!taxLink) {
                            throw new MyOBException("product.tax.code.not.linked.with.myob")
                        }
                        line.TaxCode = new JSONObject()
                        line.TaxCode.UID = taxLink.uid

                        if (isUpdate) {
                            line.RowID = orderItemLink.uid;
                            line.RowVersion = orderItemLink.myobVersion;
                        }
                        invoice.Lines.add(line);
                        oiLinkList.add(orderItemLink)
                    }
                    if(order.totalSurcharge > 0) {
                        def enableSurchargeSync = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.MYOB, "enable_surcharge_sync");
                        def defaultSurchargeProduct
                        if (enableSurchargeSync == "true") {
                            defaultSurchargeProduct =  AppUtil.getConfig( DomainConstants.SITE_CONFIG_TYPES.MYOB, "default_surcharge_line_product");
                            if(!defaultSurchargeProduct)
                                throw new MyOBException("surcharge.line.product.required")
                        }
                        else {
                            throw new MyOBException("order.with.surcharge.not.enabled")
                        }

                        def itemLink = defaultSurchargeProduct? MyobLink.findByLinkComponentAndComponentId(LinkComponent.TYPES.PRODUCT, defaultSurchargeProduct.toLong()): null;

                        MyobLink taxLink = MyobLink.findByLinkComponentAndComponentId(LinkComponent.TYPES.TAX, Product.get(defaultSurchargeProduct.toLong()).taxProfileId)
                        if(!taxLink) {
                            String defaultTaxProfile = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.TAX, "default_tax_profile");
                            taxLink = defaultTaxProfile ? MyobLink.findByLinkComponentAndComponentId(LinkComponent.TYPES.TAX, defaultTaxProfile.toLong()) : null;
                        }
                        if(!taxLink) {
                            throw new MyOBException("surcharge.product.tax.code.not.linked.with.myob")
                        }
                        JSONObject line = new JSONObject();
                        line.Description = "Surcharge"
                        line.Total = order.totalSurcharge
                        line.TaxTotal = 0
                        line.Item = new JSONObject();
                        line.Item.UID = itemLink.uid
                        line.TaxCode = new JSONObject()
                        line.TaxCode.UID = taxLink.uid
                        line.ShipQuantity = 1
                        invoice.Lines.add(line);
                    }
                    if(isUpdate) {
                        invoice.UID = myobLink.uid
                        invoice.RowVersion = myobLink.myobVersion
                    }
                    MYOB operation = isUpdate ? MYOB.UPDATE : MYOB.CREATE
                    invoice.JournalMemo = order.id
                    String objectLocationUri = client.performOperation(operation, MyobService.tabs.order.endpoint, myobLink.uid, invoice);
                    JSONObject responseObject = client.performOperation(MYOB.READ, null, null, null, objectLocationUri)

                    oiLinkList.eachWithIndex { MyobLink lineLink, int i ->
                        lineLink.uid = responseObject.Lines[i].RowID;
                        lineLink.myobVersion = responseObject.Lines[i].RowVersion;
                        lineLink.save()
                        if (lineLink.hasErrors()) {
                            throw new MyOBException("myob.link.could.not.save");
                        }
                    }

                    myobLink.uid = responseObject.UID;
                    myobLink.myobVersion = responseObject.RowVersion;
                    myobLink.save()
                    if (myobLink.hasErrors()) {
                        throw new MyOBException("myob.link.could.not.save")
                    }
                    def paymentErrors = ""
                    def paymentErrorMap = [:]
                    try {
                        paymentErrorMap = exportPayment(responseObject, config, task, client, order)
                    }
                    catch (Exception ex) {
                        paymentErrors += ex.getMessage();
                    }
                    finally {
                        paymentErrorMap.each {
                            paymentErrors += "Payment Id: ${it.key}, Error: ${it.value}"
                        }
                        task.meta.orderProgress = taskService.countProgress(task.meta.totalOrderCount, ++task.meta.orderComplete as Integer)
                        task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                        task.taskLogger.success("Order ${': ' + order?.id}", "Order Export Success ${paymentErrors ? ' Payment Error ' + paymentErrors: ''}")
                        task.meta.orderSuccessCount++
                    }
                } catch (Exception ex) {
                    task.meta.orderProgress = taskService.countProgress(task.meta.totalOrderCount, ++task.meta.orderComplete as Integer)
                    task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                    task.taskLogger.error("Order ${': ' + order?.id}", ex.message)
                    task.meta.orderErrorCount++
                    status.setRollbackOnly();
                }
            }
        }
    }

    def exportPayment(def successInvoice, def config, MultiLoggerTask task, MyOBClient client, Order order) {
        List<Payment> wcPaymentList = Payment.findAll() {
            eq "order.id", order.id
            eq "status", "success"
        }

        Map paymentErrorMap = [:]

        wcPaymentList.each {Payment payment ->
            try {
                JSONObject customerPayment = new JSONObject()
                def paymentAccountConfig =  AppUtil.getConfig( DomainConstants.SITE_CONFIG_TYPES.MYOB, "payment_account_mapping");
                def defaultPaymentAccount =  AppUtil.getConfig( DomainConstants.SITE_CONFIG_TYPES.MYOB, "default_payment_account");
                def paymentAccountJosnObject = paymentAccountConfig ? JSON.parse(paymentAccountConfig) : null
                customerPayment."AmountReceived" = payment.amount
                customerPayment."Customer" = new JSONObject()
                customerPayment."Customer".UID = successInvoice.Customer.UID
                customerPayment.Account = new JSONObject()
                customerPayment.Account.UID = paymentAccountJosnObject."${payment.gatewayCode}" ?: defaultPaymentAccount
                customerPayment.Invoices = new JSONArray()
                JSONObject invoice = new JSONObject()
                invoice.UID = successInvoice.UID
                invoice.AmountApplied = payment.amount
                invoice.Type = "Invoice"
                customerPayment.Invoices.add(invoice)

                String objectLocationUri = client.performOperation(MYOB.CREATE, "Sale/CustomerPayment", null, customerPayment);
                JSONObject responseObject = client.performOperation(MYOB.READ, null, null, null, objectLocationUri)
                responseObject
            }
            catch (Exception ex) {
                paymentErrorMap.put(payment.id, ex.getMessage())
            }
        }
        return paymentErrorMap
    }

    private int getSummaryInvoiceId () {
        int lastId = 0
        def lastNumber = AppUtil.getConfig( DomainConstants.SITE_CONFIG_TYPES.MYOB, "last_sync_summary_id");
        lastId = lastNumber ? lastNumber?.replaceAll("WS0*", "")?.toInteger() : 0
        return ++lastId
    }
}
