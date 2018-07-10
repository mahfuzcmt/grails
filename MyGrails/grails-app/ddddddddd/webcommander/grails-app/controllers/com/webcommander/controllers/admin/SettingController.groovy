package com.webcommander.controllers.admin

import com.webcommander.AppResourceTagLib
import com.webcommander.ApplicationTagLib
import com.webcommander.admin.AdministrationService
import com.webcommander.admin.ConfigService
import com.webcommander.admin.DisposableUtilService
import com.webcommander.common.CommanderMailService
import com.webcommander.common.FileService
import com.webcommander.common.ImageService
import com.webcommander.config.RedirectMapping
import com.webcommander.config.SiteConfig
import com.webcommander.config.StoreDetail
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.content.NavigationService
import com.webcommander.content.PageService
import com.webcommander.design.Resolution
import com.webcommander.design.TemplateService
import com.webcommander.events.AppEventManager
import com.webcommander.listener.SessionManager
import com.webcommander.manager.CloudStorageManager
import com.webcommander.task.Task
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.util.*
import com.webcommander.webcommerce.*
import grails.converters.JSON
import grails.util.Holders
import grails.validation.ValidationException
import groovy.json.JsonSlurper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.web.multipart.MultipartFile

import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.util.concurrent.TimeUnit

class SettingController {
    PageService pageService
    ImageService imageService
    ConfigService configService
    AdministrationService administrationService
    TemplateService templateService
    CommanderMailService commanderMailService
    NavigationService navigationService
    DisposableUtilService disposableUtilService
    TaxService taxService
    ShippingService shippingService
    FileService fileService
    CurrencyService currencyService

    @Autowired
    @Qualifier("com.webcommander.ApplicationTagLib")
    ApplicationTagLib app

    def loadAppView() {
        render view: "/admin/setting/appView", model: [d: true]
    }

    def loadGeneralAppView() {
        Map pageSelectMap = pageService.getPagesAndUrls()
        def pageNames = [g.message(code: "system.defined")]
        def pageUrls = [""]
        pageNames.addAll(pageSelectMap.pageNames)
        pageUrls.addAll(pageSelectMap.pageUrls)
        def generalSettings = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL)
        Long defaultCountryId = generalSettings.default_country.toLong(0)
        def states = administrationService.getStatesForCountry(defaultCountryId)
        render(view: "/admin/setting/generalAppView", model: [pageNames: pageNames, pageUrls: pageUrls, generalSettings: generalSettings, states: states])
    }

    def loadUpdatedPageSelects() {
        Map pageSelectMap = pageService.getPagesAndUrls()
        def pageNames = [g.message(code: "system.defined")]
        def pageUrls = [""]
        pageNames.addAll(pageSelectMap.pageNames)
        pageUrls.addAll(pageSelectMap.pageUrls)
        def generalSettings = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL)
        render([select: g.select(name: 'page', from: pageNames, keys: pageUrls), page404: generalSettings.page404, page403: generalSettings.page403] as JSON)
    }

    def loadEmailAppView() {
        def emailSettings = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.EMAIL)
        render view: "/admin/setting/emailAppView", model: [email: emailSettings]
    }

    def loadEmailForm() {
        def emailSettings = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.EMAIL)
        render view: "/admin/setting/emailSettings", model: [email: emailSettings]
    }

    def loadECommerceSetting() {
        Map pageSelectMap = pageService.getPagesAndUrls()
        def pageNames = []
        def pageUrls = []
        pageNames.addAll(pageSelectMap.pageNames)
        pageUrls.addAll(pageSelectMap.pageUrls)
        def eCommerceConfigs = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.E_COMMERCE)
        render(view: "/admin/setting/eCommerceSettings", model: [pageNames: pageNames, pageUrls: pageUrls, config: eCommerceConfigs])
    }

    def loadSEOSetting() {
        def webmasterConfigs = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.WEBTOOL)
        String seoRootLocation = "${appResource.getSeoUplodRoot()}"
        List seoCloudFiles = CloudStorageManager.getFileList(AppUtil.getBean(AppResourceTagLib).SEO_UPLOAD, NamedConstants.CLOUD_CONFIG.SYSTEM_DEFAULT)
        File seoRootFile = new File(seoRootLocation)
        List seoRootFiles = []
        if (seoRootFile.isDirectory()) {
            seoRootFiles = seoRootFile.list()
        }

        render(view: "/admin/setting/seoSettings", model: [config: webmasterConfigs, seoRootFiles: seoCloudFiles ?: seoRootFiles])
    }

    def load301Redirect() {
        List<RedirectMapping> mappingList = RedirectMapping.list()
        render(view: "/admin/setting/load301Redirect", model: [mappings: mappingList])
    }

    def loadFrontendPages() {
        String type = DomainConstants.SITE_CONFIG_TYPES.FRONTEND_PAGES
        Map config = AppUtil.getConfig(type)
        render(view: "/admin/setting/frontendPages", model: [type: type, config: config])
    }

    def seoUpload() {
        MultipartFile uploadedFile = request.getFile('seoUpload')
        String filePath = appResource.getSeoUplodRoot()
        String originalName = uploadedFile.originalFilename
        Boolean isExist = new File(filePath + File.separator + originalName).exists()
        try {
            configService.seoUpload(uploadedFile, filePath)
            render([status: "success", message: g.message(code: "file.save.success"), fileName: isExist ? "" : uploadedFile.originalFilename] as JSON)
        } catch (Exception e) {
            render([status: "error", message: g.message(code: e.message)] as JSON)
        }
    }

    def removeSeoFile() {
        String fileName = params.fileName
        try {
            configService.removeSeoFile(fileName)
            render([status: "success", message: g.message(code: "file.delete.success")] as JSON)
        } catch (Exception e) {
            render([status: "error", message: g.message(code: "file.delete.failure")] as JSON)
        }
    }

    def loadLocaleAppView() {
        def emailSettings = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.EMAIL)
        def localeSettings = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.LOCALE)
        Map timeZone = [:]
        List zoneName = []
        TimeZone.getAvailableIDs().collect {
            TimeZone timeZn = TimeZone.getTimeZone(it)
            String displayName = timeZn.displayName
            if(it.contains("/") && !zoneName.contains(displayName)) {
                long offset = (long)timeZn.rawOffset
                long hours = TimeUnit.MILLISECONDS.toHours(offset)
                long minutes = TimeUnit.MILLISECONDS.toMinutes(offset) - TimeUnit.HOURS.toMinutes(hours)
                String hm = String.format("%02d:%02d", hours, Math.abs(minutes))
                if(!hm.contains("-") && (hours != 0 || minutes != 0)) {
                    hm = "+" + hm
                }
                zoneName.add(displayName)
                timeZone.putAt(it, "(GMT " + hm + ") " + displayName)
            }
        }
        render view: "/admin/setting/localeAppView", model: [email: emailSettings, locale: localeSettings, timeZone: timeZone]
    }

    def loadTaxAndCurrencySettings() {
        Map taxSettings = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.TAX)

        params.isDefault = false
        List<TaxProfile> profiles = taxService.getTaxProfiles(params)

        params.isDefault = (AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.TAX, "configuration_type") == DomainConstants.TAX_CONFIGURATION_TYPE.MANUAL) ? false : true
        List<TaxCode> codes = taxService.getTaxCodes(params)

        render(view: "/admin/setting/taxAndCurrency", model: [taxSettings: taxSettings, profiles: profiles, codes: codes])
    }

    def saveTaxAndCurrency() {

        boolean isValidated = true
        String errorMessage = null

        if(params["tax.configuration_type"] == DomainConstants.TAX_CONFIGURATION_TYPE.DEFAULT) {
            isValidated = false
            errorMessage = g.message(code: "admin.setting.tax.default.country.mapped.tax.profile.notfound")
            String defaultCountry = params["tax.default_country"]
            String countryMappedTaxProfile = NamedConstants.DEFAULT_COUNTRY_WITH_DEFAULT_TAX_PROFILE_MAPPING[defaultCountry]
            if (countryMappedTaxProfile) {
                TaxProfile profile = TaxProfile.findByName(countryMappedTaxProfile)
                if (profile) {
                    params["tax.default_tax_profile"] = profile.id
                    isValidated = true
                    errorMessage = null
                }
            }
        }

        if (isValidated) {
            saveConfigurations()
        } else {
            render([status: "error", message: errorMessage] as JSON)
        }
    }

    def loadStoreDetails() {
        StoreDetail storeDetail = StoreDetail.first()
        def states = storeDetail?.address ? administrationService.getStatesForCountry(storeDetail.address.country.id) : []
        render(view: "/admin/setting/store", model: [storeDetail: storeDetail, states: states])
    }

    def loadSearchSettings() {
        def searchConfig = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.SEARCH_PAGE)
        render(view: "/admin/setting/searchSettings", model: [searchConfig: searchConfig])
    }
    
    def loadAdministrationSetting() {
        Map config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION)
        Map systemInformation = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.MANAGEMENT_HUB)?:[:]
        Map license = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.LICENSE)?:[:]
        systemInformation.build = Holders.config.webcommander.version.build
        systemInformation.version = Holders.config.webcommander.version.number
        systemInformation.licenseCode = license?.licenseCode
        render(view: "/admin/setting/administrationSettings", model: [d: true, config: config, systemInformation:systemInformation])
    }

    def loadDefaultImageSettings() {
        render(view: "/admin/setting/defaultImageAppView", model: [:])
    }

    def loadDefaultImage() {
        String type = params.type
        String imageName = ImageService.RESIZABLE_IMAGE_SIZES[DomainConstants.DEFAULT_IMAGES[type]]?.keySet()?.first() + "-default.png"
        File image = new File(servletContext.getRealPath("/WEB-INF/system-resources/default-images/" + type + "/" + imageName))
        OutputStream stream = response.outputStream
        image.withInputStream { input ->
            stream << input
        }
        stream.flush()
    }

    def saveDefaultImageSettings() {
        Map defaultImages = params.defaultImages
        Map resetImages = params.resetImages
        Set cachesToClear = new HashSet()
        defaultImages.each {
            if(it.value != "") {
                cachesToClear.add(appResource.getDefaultImageURL(entity: it.key))
            }
        }
        resetImages.each {
            if(it.value == "1") {
                cachesToClear.add(appResource.getDefaultImageURL(entity: it.key))
            }
        }
        if(configService.updateDefaultImages(defaultImages, resetImages)) {
            render([status: "success", message: g.message(code: "default.images.change.success"), cachesToClear: cachesToClear] as JSON)
        } else {
            render([status: "error", message: g.message(code: "default.images.change.error")] as JSON)
        }
    }

    def loadLoginFormSettings() {
        def loginConfig = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.CUSTOMER_LOGIN_SETTINGS)
        render(view: "/admin/setting/customerLoginFormSettings", model: [loginConfig: loginConfig])
    }

    def saveCustomerLoginFormSettings() {
        String type = DomainConstants.SITE_CONFIG_TYPES.CUSTOMER_LOGIN_SETTINGS
        def config = []
        Long failCount = params[type].fail_count.toLong()
        if(failCount <1 || failCount >10){
            render([status: "error", message: g.message(code: "setting.not.updated")] as JSON)
        }else{
            params[type].each{
                config.add([type: type, configKey: it.key, value: it.value])
            }
            params.list("field").each { field->
                if(params[field]){
                    config.add([type: type, configKey: field , value: "activated"])
                }else{
                    config.add([type: type, configKey: field , value: "deactivated"])
                }
            }
            commitConfigsInDB(config)

        }
    }

    def loadCustomerRegistrationSettings() {
        Map configs = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.CUSTOMER_REGISTRATION_SETTINGS)
        Map registrationFieldConfigs = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.CUSTOMER_REGISTRATION_FIELD)
        List sortedFields = configService.getSortedFields(registrationFieldConfigs)
        Map fieldsConfigs = configService.getCustomerRegistrationFieldsConfigs(sortedFields, registrationFieldConfigs)
        fieldsConfigs['registration_terms_active'] = registrationFieldConfigs['registration_terms_active'].toBoolean()
        render(view: "/admin/setting/customerRegistrationSettings", model: [fields: sortedFields, configs: configs, fieldsConfigs: fieldsConfigs, registrationFieldConfigs: registrationFieldConfigs])
    }

    def restoreCustomerRegistrationSettings() {
        String type = DomainConstants.SITE_CONFIG_TYPES.CUSTOMER_REGISTRATION_SETTINGS
        def configs = SiteConfig.INITIAL_DATA[type]
        List newConfigs = []
        configs.each { k, v->
            newConfigs.add([type: type, configKey: k.toString(), value: v.toString()])
        }

        type = DomainConstants.SITE_CONFIG_TYPES.CUSTOMER_REGISTRATION_FIELD
        configs = SiteConfig.INITIAL_DATA[type]
        configs.each { k, v->
            newConfigs.add([type: type, configKey: k.toString(), value: v.toString()])
        }
        commitConfigsInDB(newConfigs)
    }

    def loadBillingAddressSettings() {
        def configs = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.BILLING_ADDRESS_FIELD)
        List sortedFields = configService.getSortedFields(configs)
        render(view: "/admin/setting/addressSettings", model: [fields: sortedFields, configs: configs, type: "billing"])
    }

    def restoreBillingAddressSettings() {
        String type = DomainConstants.SITE_CONFIG_TYPES.BILLING_ADDRESS_FIELD
        def configs = SiteConfig.INITIAL_DATA[type]
        List newConfigs = []
        configs.each { k, v->
            newConfigs.add([type: type, configKey: k.toString(), value: v.toString()])
        }
        commitConfigsInDB(newConfigs)
    }

    def loadShippingAddressSettings() {
        def configs = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.SHIPPING_ADDRESS_FIELD)
        List sortedFields = configService.getSortedFields(configs)
        render(view: "/admin/setting/addressSettings", model: [fields: sortedFields, configs: configs, type: "shipping"])
    }

    def restoreShippingAddressSettings() {
        String type = DomainConstants.SITE_CONFIG_TYPES.SHIPPING_ADDRESS_FIELD
        def configs = SiteConfig.INITIAL_DATA[type]
        List newConfigs = []
        configs.each { k, v->
            newConfigs.add([type: type, configKey: k.toString(), value: v.toString()])
        }
        commitConfigsInDB(newConfigs)
    }

    private def commitConfigsInDB(configs) {
        if (configService.update(configs)) {
            configs.type.each {
                AppEventManager.fire(it + "-after-settings-updated", [configs])
            }
            render([status: "success", message: g.message(code: "setting.updated.successfully")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "setting.not.updated")] as JSON)
        }
    }

    def saveConfigurations() {
        def configs = []
        params.list("type").each { type ->
            params."${type}".each {
                configs.add([type: type, configKey: it.key, value: it.value])
            }
        }
        commitConfigsInDB(configs)
        currencyService.saveBasicBulkProperties(params)
    }


    def saveGeneralSettings() {
        MultipartFile imgFile = request.getFile("image")
        String cloudType = NamedConstants.CLOUD_CONFIG.SYSTEM_DEFAULT
        def configs = []
        if (!params.general.captcha_setting) {
            params.general.captcha_setting = "disable"
        }
        params.list("type").each { type ->
            params."${type}".each {
                configs.add([type: type, configKey: it.key, value: it.value])
            }
        }
        if (imgFile) {
            if (!imgFile.originalFilename.endsWith(".ico")) {
                BufferedImage bufferedImage = ImageIO.read(imgFile.inputStream)
                if(!bufferedImage) {
                    throw new ApplicationRuntimeException("cannot.read", [imgFile.getOriginalFilename()])
                }
                if (bufferedImage.getHeight() > 256) {
                    throw new ApplicationRuntimeException("invalid.image.height", [256])
                }
                if (bufferedImage.getWidth() > 256) {
                    throw new ApplicationRuntimeException("invalid.image.width", [256])
                }
            }
            InputStream inputStream = imgFile.inputStream
            fileService.putResource(inputStream, AppResourceTagLib.getFaviconRelativePath(), cloudType)
            def tMap = [
                    type     : "general",
                    configKey: "favicon_enabled",
                    value    : "true"
            ]
            configs << tMap
        } else if (params["remove-image"]) {
            fileService.removeResource(AppResourceTagLib.getFaviconRelativePath(), cloudType)
            configService.saveFavicon(null)
            def tMap = [
                    type     : "general",
                    configKey: "favicon_enabled",
                    value    : "false"
            ]
            configs << tMap
        }
        commitConfigsInDB(configs)
    }

    def saveStoreDetails() {
        MultipartFile uploadedFile = params.image ?: null
        if(configService.saveStoreDetails(params, uploadedFile)){
            render([status: "success", message: g.message(code: "store.detail.save.success"), imageName: uploadedFile?.originalFilename] as JSON)
        } else {
            render([status: "error", message: g.message(code: "store.detail.could.not.save")] as JSON)
        }
    }

    def loadProductAppView() {
        def productSettings = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.PRODUCT)
        def relatedProductSettings = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.RELATED_PRODUCT) ?: [:]
        def productPropertiesSettings = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.PRODUCT_PROPERTIES) ?: [:]
        render(view: "/admin/setting/productAppView", model: [productSettings: productSettings, relatedProductSettings: relatedProductSettings, productPropertiesSettings: productPropertiesSettings])
    }

    def loadProductImageAppView() {
        def productImageSettings = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.PRODUCT_IMAGE)
        render(view: "/admin/setting/productImageAppView", model: [productImageSettings: productImageSettings])
    }

    def loadCategoryImageAppView() {
        def categoryImageSetting = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.CATEGORY_IMAGE)
        render(view: "/admin/setting/categoryImageAppView", model: [sizes: categoryImageSetting])
    }

    def loadCategoryDetailsAppView() {
        def config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.CATEGORY_PAGE)
        render(view: "/admin/setting/categoryDetailsAppView", model: [config: config])
    }

    def loadShoppingCartDetails() {
        def shoppingCartSettings = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.CART_PAGE)
        render(view: "/admin/setting/shoppingCart", model: [shoppingCartSettings: shoppingCartSettings])
    }

    def loadCheckoutPageConfigs() {
        def checkoutSettings = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.CHECKOUT_PAGE)
        render(view: "/admin/setting/checkoutPage", model: [configs: checkoutSettings])
    }

    def loadOrderPrintAndEmailSetting() {
        def config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ORDER_PRINT_AND_EMAIL)
        render(view: "/admin/setting/orderPrintAndEmail", model: [configs: config])
    }

    def loadShipping() {
        def shippingSettings = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.SHIPPING)
        List<ShippingClass> classList = ShippingClass.all

        params.isDefault = false
        List<TaxProfile> taxProfiles = taxService.getTaxProfiles(params)

        render(view: "/admin/setting/shipping", model: [shippingSettings: shippingSettings, clazzs: classList, taxProfiles: taxProfiles] )
    }

    def viewTemplate() {
        Map data = templateService.getEmailTemplateData(params.id)
        render (view: "/admin/setting/viewTemplate", model: [text: data.text, html: data.html, template: data.template])
    }

    def editTemplate() {
        Map data = templateService.getEmailTemplateData(params.id)
        render (view: "/admin/setting/editTemplate", model: [text: data.text, html: data.html, macros: data.macros, template: data.template])
    }

    def saveTemplate() {
        if(templateService.saveEmailTemplate(params)) {
            render([status: "success", message: g.message(code: "template.save.successful")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "template.save.error")] as JSON)
        }
    }

    def testMailView() {
        render(view: "/admin/setting/testMailView", model: [params: params])
    }

    def sendMail() {
        Map data = templateService.getEmailTemplateData(params.templateId)
        String recipient = params.recipient
        try {
            commanderMailService.sendTestMail(data, recipient)
        } catch (Exception e) {
            e.printStackTrace()
            render([status: "error", message: g.message(code: "test.mail.could.not.be.sent")] as JSON)
            return
        }
        render([status: "success", message: g.message(code: "test.mail.sent.successfully")] as JSON)
    }

    def loadReferenceSelectorBasedOnType(){
        String type = params.type
        def items
        if (type && type != DomainConstants.TERMS_AND_CONDITION_TYPE.EXTERNAL_LINK && type != DomainConstants.NAVIGATION_ITEM_TYPE.URL && type != DomainConstants.TERMS_AND_CONDITION_TYPE.SPECIFIC_TEXT) {
            def consumer = Holders.applicationContext.getBean(navigationService.domains[type])
            String domain = StringUtil.getCapitalizedAndPluralName(type)
            items = consumer.getClass().getDeclaredMethod("get" + domain, Map as Class[]).invoke(consumer, [[:]] as Object[])
        }else {
            items = []
        }

        render (
                view: "/admin/setting/loadRef",
                model: [items: items, type: type, ref: params.ref]
        )
    }

    def loadCustomerProfileSetting() {
        def configs = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.CUSTOMER_PROFILE_PAGE)
        List sortedFields = configService.getSortedFields(configs)
        render(view: "/admin/setting/customerProfileSetting", model: [fields: sortedFields, configs: configs])
    }

    def loadResponsiveSetting() {
        def config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.RESPONSIVE)
        List<Resolution> resolutions = Resolution.list()
        render(view: "/admin/setting/responsiveSetting", model: [config: config, resolutions: resolutions])
    }

    def saveResolution() {
        Long result
        String errorMessage = "resolution.save.failed"
        try {
            result = configService.saveResolution(params)
        } catch (ValidationException ex) {
            errorMessage = "resolution.already.exists"
        }
        if(result) {
            render([status: "success", message:  g.message(code: "resolution.save.success"), id: result] as JSON)
        } else {
            render([status: "error", message:  g.message(code: errorMessage)] as JSON)
        }
    }

    def removeResolution() {
        configService.removeResolution(params)
        render([status: "success", message:  g.message(code: "resolution.delete.success")] as JSON)
    }

    def loadBackupStatus() {
        String backupRestoreUrl = Holders.config.backup_server.base_url + "api/webCommander/"
        String username = Holders.config.backup_server.username
        String password = Holders.config.backup_server.password
        String authString = username + ":" + password
        byte[] authEncBytes = Base64Coder.encode(authString)
        String authStringEnc = new String(authEncBytes)
        String data = AppUtil.getQueryStringFromMap([siteName: ConfigurationReader.getProperty("domain", "db98796b.webcommander.biz")])
        def slurper = new JsonSlurper()
        try {
            def resp = slurper.parseText(HttpUtil.doPostRequest(backupRestoreUrl + "getBackupList", data, [Authorization: "Basic " + authStringEnc]))
            if(resp.sites) {
                render(view: "/admin/setting/backupRestore", model: [backupList: resp["sites"].collect {it.split("\\.")[0]}])
            } else {
                render(view: "/admin/setting/backupRestore", model: [:])
            }
        } catch(Exception e) {
            render(view: "/admin/setting/backupRestore", model: [:])
        }
    }

    def backupRestore() {
        String backupRestoreUrl = Holders.config.backup_server.base_url + "api/webCommander/"
        String username = Holders.config.backup_server.username
        String password = Holders.config.backup_server.password
        String authString = username + ":" + password
        byte[] authEncBytes = Base64Coder.encode(authString)
        String authStringEnc = new String(authEncBytes)
        def slurper = new JsonSlurper()
        String status = "", code = "", data = ""
        def config = [:], resp
        if(params.backup) {
            String date = new Date().gmt().toString()
            String backupName = date.replaceAll(" ", "_").replaceAll(":", "-")
            data = AppUtil.getQueryStringFromMap([siteName: ConfigurationReader.getProperty("domain", "vulture.webcommander.biz"), occurred: backupName])
            resp = slurper.parseText(HttpUtil.doPostRequest(backupRestoreUrl + "backup", data, [Authorization: "Basic " + authStringEnc])) ?: [:]
            code = resp.statusCode
            if(code) {
                while(true) {
                    sleep(1000)
                    status = HttpUtil.doPostRequest(backupRestoreUrl + "getBackupStatus", AppUtil.getQueryStringFromMap(statusCode: code), [Authorization: "Basic " + authStringEnc])
                    config = status ? slurper.parseText(status) : [:]
                    if(config.code == "133" || config.success == "false") {
                        break
                    }
                }
                if(config.success == "true" && config.code == "133") {
                    Map backup = [value: backupName, text: Date.parse("E MMM dd H:m:s z yyyy", date).toAdminFormat(true, false, session.timezone)]
                    render([status: "success", backup: backup] as JSON)
                } else {
                    render([status: "error", code: config.code] as JSON)
                }
            } else {
                render([status: "error", code: config.code] as JSON)
            }
        } else if(params.restorePoint) {
            data = AppUtil.getQueryStringFromMap([siteName: ConfigurationReader.getProperty("domain", "vulture.webcommander.biz"), occurred: params.restorePoint])
            resp = slurper.parseText(HttpUtil.doPostRequest(backupRestoreUrl + "restore", data, [Authorization: "Basic " + authStringEnc])) ?: [:]
            if(resp.statusCode) {
                render([status: "success"] as JSON)
            } else {
                render([status: "error"] as JSON)
            }
        }
    }

    def loadStoreCredit(){
        Map config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.STORE_CREDIT)
        render view: "/admin/setting/storeCreditAppView", model: [config: config]
    }

    def initClearDisposable() {
        Task task = disposableUtilService.initImport()
        task.meta.logger_dump_folder = "disposable-clear"
        task.meta.logger_dump_file = new Date().toFormattedString("dd.MM.yyyy", true, "HH.mm.ss", false, null)
        task.meta.task_cache_location = SessionManager.protectedTempFolder.absolutePath + "/" + task.token + "/disposable_clear.job"
        task.meta.cache_file = "disposable_clear.job"
        render([token: task.token, name: task.name] as JSON)
    }

    def loadMyAccountView() {
        Map config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.MY_ACCOUNT_PAGE)
        render(view: "/admin/setting/myAccountPage", model: [config: config])
    }

    def loadCloudStorageSetting() {
        def configs = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.CLOUD_STORAGE)
        render(view: "/admin/setting/cloudStorageSetting", model: [configs : configs])
    }
}