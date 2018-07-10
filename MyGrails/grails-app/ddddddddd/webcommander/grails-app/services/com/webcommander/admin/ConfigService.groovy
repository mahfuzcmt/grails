package com.webcommander.admin

import com.webcommander.AppResourceTagLib
import com.webcommander.common.FileService
import com.webcommander.common.ImageService
import com.webcommander.config.SiteConfig
import com.webcommander.config.StoreDetail
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.content.RemoteRepositoryService
import com.webcommander.content.WcStaticResource
import com.webcommander.design.Resolution
import com.webcommander.events.AppEventManager
import com.webcommander.manager.CacheManager
import com.webcommander.manager.CloudStorageManager
import com.webcommander.manager.PathManager
import com.webcommander.models.MockStaticResource
import com.webcommander.tenant.TenantContext
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.Address
import grails.gorm.transactions.Transactional
import grails.util.TypeConvertingMap
import grails.web.databinding.DataBindingUtils
import groovy.io.FileType
import org.apache.commons.collections.map.HashedMap
import org.apache.commons.io.FilenameUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.web.multipart.MultipartFile

class ConfigService {
    ImageService imageService
    FileService fileService
    @Autowired
    @Qualifier("com.webcommander.AppResourceTagLib")
    AppResourceTagLib appResource
    RemoteRepositoryService remoteRepositoryService

    private static Map tabs = [
        general: [
            url: "setting/loadGeneralAppView",
            message_key: "general"
        ],
        locale: [
            url: "setting/loadLocaleAppView",
            message_key: "locale"
        ],
        email: [
            url: "setting/loadEmailAppView",
            message_key: "email"
        ],
        store: [
            url: "setting/loadStoreDetails",
            message_key: "store.details"
        ],
        search: [
            url: "setting/loadSearchSettings",
            message_key: "search"
        ],
        defaultImages: [
            url: "setting/loadDefaultImageSettings",
            message_key: "default.images"
        ],
        product: [
            url: "setting/loadProductAppView",
            message_key: "product.page",
            ecommerce  : true
        ],
        productImage: [
            url: "setting/loadProductImageAppView",
            message_key: "product.image",
            ecommerce  : true
        ],
        categoryImage: [
            url: "setting/loadCategoryImageAppView",
            message_key: "category.image",
            ecommerce  : true
        ],
        categoryDetailsPage: [
            url: "setting/loadCategoryDetailsAppView",
            message_key: "category.page",
            ecommerce  : true
        ],
        eCommerce: [
            url: "setting/loadECommerceSetting",
            message_key: "e.commerce",
            ecommerce  : true
        ],
        taxAndCurrency: [
            url: "setting/loadTaxAndCurrencySettings",
            message_key: "tax.and.currency",
            ecommerce  : true
        ],
        shipping: [
            url: "setting/loadShipping",
            message_key: "shipping",
            ecommerce  : true
        ],
        shoppingCart: [
            url: "setting/loadShoppingCartDetails",
            message_key: "shopping.cart.page",
            ecommerce  : true
        ],
        checkoutPage: [
            url: "setting/loadCheckoutPageConfigs",
            message_key: "checkout.page",
            ecommerce  : true
        ],
        loginPage: [
            url: "setting/loadLoginFormSettings",
            message_key: "login.page.settings"
        ],
        customerRegistration: [
            url: "setting/loadCustomerRegistrationSettings",
            message_key: "registration"
        ],
        billingAddress: [
            url: "setting/loadBillingAddressSettings",
            message_key: "billing.address.fields",
            ecommerce  : true
        ],
        shippingAddress: [
            url: "setting/loadShippingAddressSettings",
            message_key: "shipping.address.fields",
            ecommerce  : true
        ],
        webtool: [
            url: "setting/loadSEOSetting",
            message_key: "webtool"
        ],
        frontendPages: [
            url: "setting/loadFrontendPages",
            message_key: "front.end.pages"
        ],
        orderPrint:   [
            url: "setting/loadOrderPrintAndEmailSetting",
            message_key: "order.print",
            ecommerce  : true
        ],
        customerProfilePage: [
            url: "setting/loadCustomerProfileSetting",
            message_key: "customer.profile.page"
        ],
        responsive: [
            url: "setting/loadResponsiveSetting",
            message_key: "responsive"
        ],
        backupRestore: [
            url: "setting/loadBackupStatus",
            message_key: "backup.or.restore"
        ],
        administration: [
            url: "setting/loadAdministrationSetting",
            message_key: "administration"
        ],
        application: [
            url: "application/list",
            message_key: "applications"
        ],
        store_credit: [
           url: "setting/loadStoreCredit",
           message_key: "store.credit",
           ecommerce  : true
        ],
        my_account_page: [
            url: "setting/loadMyAccountView",
            message_key: "my.account.page"
        ],
        cloudStorage: [
            url: "setting/loadCloudStorageSetting",
            message_key: "cloud.storage"
        ]
    ]

    private static Map _TAB_HOLDER = [:]

    private static Map getTAB_HOLDER() {
        Map dynamic = _TAB_HOLDER[TenantContext.currentTenant]
        if (!dynamic) {
            dynamic = _TAB_HOLDER[TenantContext.currentTenant] = [:].withDefault { [:] }
        }
        return dynamic
    }

    static addTab(String tabKey, Map value) {
        TAB_HOLDER.put(tabKey, value)
    }

    static removeTab(String tabKey) {
        TAB_HOLDER.remove(tabKey)
    }

    static Map getTabs() {
        Map tabs = CacheManager.get(NamedConstants.CACHE.LOCAL_STATIC, "config_ui_tabs")
        if(!tabs) {
            tabs = new HashedMap(ConfigService.@tabs)
            CacheManager.cache(NamedConstants.CACHE.LOCAL_STATIC, tabs, "config_ui_tabs")
        }
        return tabs + TAB_HOLDER
    }

    @Transactional
    boolean saveStoreDetails(Map params, MultipartFile uploadedFile) {
        Map addressMap = params.address
        addressMap.country = Country.get(addressMap.country)
        addressMap.firstName = params.name
        if(addressMap.state) {
            addressMap.state = State.get(addressMap.state)
        }
        StoreDetail storeDetail = StoreDetail.first() ?: new StoreDetail(address: new Address())
        DataBindingUtils.bindObjectToInstance(storeDetail.address, addressMap)
        storeDetail.address.save()
        storeDetail.name = params.name
        storeDetail.abn = params.abn
        storeDetail.additionalInfo = params.additionalInfo
        if (params["remove-image"]) {
            storeDetail.removeResource()
            storeDetail.image = null
        }
        storeDetail.save()
        if(!storeDetail.hasErrors() && uploadedFile) {
            saveStoreLogo(uploadedFile, storeDetail)
        }
        if(!storeDetail.hasErrors()) {
            AppEventManager.fire("store-detail-update")
            return true
        }
        return false
    }

    @Transactional
    boolean saveStoreLogo(MultipartFile uploadedFile, StoreDetail storeDetail){
        String cloudType = NamedConstants.CLOUD_CONFIG.DEFAULT
        fileService.removeResource(AppResourceTagLib.STORE, cloudType)
        storeDetail.image = "${appResource.STORE_LOGO}.${FilenameUtils.getExtension(uploadedFile.originalFilename)}"
        imageService.uploadImage(uploadedFile, NamedConstants.IMAGE_RESIZE_TYPE.STORE_LOGO, storeDetail, 51200)
        storeDetail.merge()
        if(!storeDetail.hasErrors()) {
            return true
        }
        return false
    }

    @Transactional
    boolean update(def configs) {
        def isSaved = true
        HashSet types = new HashSet()
        configs.each { config ->
            types.add(config.type)
            SiteConfig _config = SiteConfig.where {
                configKey == config.configKey
                type == config.type
            }.get()
            if(!_config) {
                _config = new SiteConfig(type: config.type, configKey: config.configKey)
            }
            _config.value = config.value
            if(!_config.save()) {
                isSaved = false
                return false
            }
        }
        if(isSaved) {
            types.each { type ->
                AppUtil.clearConfig type
                AppEventManager.fire(type + "-configuration-update")
            }
        }
        return isSaved
    }

    boolean saveFavicon(imgFile) {
        String resourceId = "favicon"
        WcStaticResource resource = WcStaticResource.findByResourceId(resourceId) ?: new WcStaticResource(resourceId: resourceId, relativeUrl: "resources/favicon/", resourceName: "site.ico")
        resource.removeResource()
        if (imgFile) {
            imageService.uploadImage(imgFile, NamedConstants.IMAGE_RESIZE_TYPE.FAVICON_IMAGE, resource, 10 * 1024 )
            resource.save()
        } else if (resource.id){
           resource.delete()
        }
    }

    List getSortedFields(def configs, String order_key = "_order") {
        Map fields = [:]
        configs.each { k, v->
            String key = k.toString()
            String val = v.toString()
            if(key.endsWith(order_key)) {
                fields += [(val): key.replace(order_key, "")]
            }
        }
        List sortedFields = []
        fields.eachWithIndex{ k, v, i ->
            sortedFields.add(fields[i + 1 + ""])
        }
        return sortedFields
    }

    void reorderFields(def type,def configkey, String order_key = "_order") {
        SiteConfig siteConfig = SiteConfig.createCriteria().get{ eq("configKey", configkey) }
        List<SiteConfig> configs  = SiteConfig.findAllByType(type, [ sort: "value", order: "asc"])
        Map site_config = new LinkedHashMap()
        Map fields = [:]
        configs.each {
            String value = it.value
            if (value) {
                site_config.put(it.configKey, value)
            }
        }
        site_config.each { k, v->
            String key = k.toString()
            String val = v.toString()
            if(key.endsWith(order_key)) {
                fields += [(val): key]
            }
        }
        fields.eachWithIndex { k, v, i ->
            siteConfig = SiteConfig.createCriteria().get{ eq("configKey", v) }
            siteConfig.value = i+1+""
            siteConfig.save()
        }
    }

    void setOrder(def type, def configkey) {
        def totalSortableItem = getSortedFields(AppUtil.getConfig(type)).size()
        if(SiteConfig.findAllByTypeAndConfigKey(type, configkey)){
            SiteConfig siteConfig = SiteConfig.createCriteria().get{ eq("configKey", configkey) }
            siteConfig.value = totalSortableItem.toString()
            siteConfig.save()
        }
        else {
            totalSortableItem++
            new SiteConfig(type:  type, configKey: configkey, value: totalSortableItem.toString()).save(flush:true)
        }
    }

    def macroReplace(def welcomeHtml, def customer){
        def loginedCustomer = Customer.get(customer)
        def replacedContent= welcomeHtml.replaceAll("%FIRST_NAME%",loginedCustomer.firstName)
        replacedContent = replacedContent.replaceAll("%LAST_NAME%", loginedCustomer.lastName)
        replacedContent = replacedContent.replaceAll("%first_name%",loginedCustomer.firstName)
        replacedContent = replacedContent.replaceAll("%last_name%", loginedCustomer.lastName)
        return replacedContent
    }


    List getActiveFields(List fields, Map fieldsConfigs) {
        List activeFields = []
        fields.each {
            String field = it
            if(fieldsConfigs[field + "_active"] == null || fieldsConfigs[field + "_active"].toBoolean()) {
                activeFields.add(field)
            }
        }
        return activeFields
    }

    Map getCustomerRegistrationFieldsConfigs(List sortedFields, Map registrationFieldConfigs) {
        Map billingFieldsConfigs = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.BILLING_ADDRESS_FIELD)
        Map shippingFieldsConfigs = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.SHIPPING_ADDRESS_FIELD)

        Map fieldsConfigs = [:]
        sortedFields.each {
            String field = it.toString()
            String active = field + '_active'
            String required = field + '_required'
            String order = field + '_order'
            String label = field + '_label'
            fieldsConfigs[order] = registrationFieldConfigs[order]
            fieldsConfigs[label] = registrationFieldConfigs[label]
            if(registrationFieldConfigs[active] != null) {
                fieldsConfigs[active] = registrationFieldConfigs[active].toBoolean(null)
                fieldsConfigs[required] = registrationFieldConfigs[required].toBoolean(true)
            } else {
                fieldsConfigs[active] = billingFieldsConfigs[active].toBoolean(true) || shippingFieldsConfigs[active].toBoolean(true)
                fieldsConfigs[required] = billingFieldsConfigs[required].toBoolean(true) || shippingFieldsConfigs[required].toBoolean(true)
            }
        }
        return fieldsConfigs
    }

    void seoUpload(MultipartFile uploadedFile, String filePath) {
        String originalName = uploadedFile.originalFilename
        if (originalName.lastIndexOf('.') < 0 ) {
            throw new Exception("file.should.contain.extension")
        }
        MockStaticResource resource = new MockStaticResource(relativeUrl: filePath, resourceName: originalName)
        fileService.uploadFile(uploadedFile, NamedConstants.CLOUD_CONFIG.SYSTEM_DEFAULT, originalName, resource, filePath)
    }

    public void removeSeoFile(fileName) {
        String filePath = appResource.getSeoUplodRoot() + File.separator + fileName
        String cloudFilePath = fileService.getRelativeURLPattern(filePath, NamedConstants.RESOURCE_TYPE.RESOURCE)
        File file = new File(filePath)
        CloudStorageManager.deleteData(cloudFilePath, NamedConstants.CLOUD_CONFIG.SYSTEM_DEFAULT)
        if (!file.delete()) {
            throw new ApplicationRuntimeException("file.not.delete")
        }
    }

    Boolean updateDefaultImages(Map uploadedImages, Map resetImages) {
        Boolean success = true
        List resetImageTypes = []
        try {
            DomainConstants.DEFAULT_IMAGES.each {
                if(uploadedImages[it.key]) {
                    MockStaticResource resource = new MockStaticResource(relativeUrl: "$it.key/${AppResourceTagLib.DEFAULT}/", resourceName: AppResourceTagLib.DEFAULT_PNG, uploadToCloud: true)
                    imageService.uploadImage((MultipartFile)uploadedImages[it.key], it.value, resource)
                }
                if(resetImages[it.key] == "1") {
                    new File(PathManager.getResourceRoot("$it.key/${AppResourceTagLib.DEFAULT}")).deleteDir()
                    resetImageTypes.add(it.key)
                }
            }
            AppUtil.initializeDefaultImages(resetImageTypes)
        } catch(Exception e) {
            log.error "upload default image", e
            success = false
        }
        return success
    }

    @Transactional
    Long saveResolution(TypeConvertingMap params) {
        Resolution resolution = params.id ? Resolution.get(params.id) : new Resolution()
        resolution.min = params.int("minWidth") ?: null
        resolution.max = params.int("maxWidth") ?: null
        resolution.save()
        return resolution.id
    }

    @Transactional
    Boolean removeResolution(Map params) {
        Resolution resolution = Resolution.get(params.id)
        resolution.delete()
        return true
    }
}