package com.webcommander.manager

import com.webcommander.config.SiteConfig
import com.webcommander.constants.NamedConstants
import com.webcommander.tenant.TenantContext
import org.grails.plugins.web.taglib.ApplicationTagLib
import com.webcommander.admin.ConfigService
import com.webcommander.common.CommanderMailService
import com.webcommander.config.StoreDetail
import com.webcommander.constants.DomainConstants
import com.webcommander.license.OperatorPendingNotification
import com.webcommander.license.validator.ApiValidator
import com.webcommander.license.validator.NewsletterValidator
import com.webcommander.license.validator.OperatorValidator
import com.webcommander.license.validator.PageValidator
import com.webcommander.license.validator.ProductValidator
import com.webcommander.models.License
import com.webcommander.sso.ProvisionAPIService
import com.webcommander.util.AppUtil
import grails.converters.JSON
import grails.util.Holders
import groovy.util.logging.Log
import org.springframework.context.MessageSource

import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Level

import static com.webcommander.constants.DomainConstants.SITE_CONFIG_TYPES

@Log
class LicenseManager {
    private static Map<String, License> getLicenses() {
        Map licenses = CacheManager.get(NamedConstants.CACHE.TENANT_STATIC, "licenses")
        if(licenses != null) {
            return licenses
        }
        return [:]
    }

    private static void setLicenses(Map<String, License> licenses) {
        CacheManager.cache(NamedConstants.CACHE.TENANT_STATIC, licenses, -1, "licenses")
    }

    static List<Class> validators = [
        ApiValidator,
        NewsletterValidator,
        OperatorValidator,
        PageValidator,
        ProductValidator
    ]

    private static Long expiryDate
    static Long nextBillingDate

    static Map DYNAMIC_IS_ADMIN_DISABLED = [:]
    static Map DYNAMIC_DISABLED_MESSAGE = [:]

    static Map getDISABLED_MESSAGE(){
        return DYNAMIC_DISABLED_MESSAGE[TenantContext.currentTenant] ?: null
    }

    static Map setDISABLED_MESSAGE(Map message){
        return DYNAMIC_DISABLED_MESSAGE[TenantContext.currentTenant] = message
    }

    static boolean getIS_ADMIN_DISABLED() {
        return DYNAMIC_IS_ADMIN_DISABLED[TenantContext.currentTenant]
    }

    static boolean setIS_ADMIN_DISABLED(Boolean isDisable){
        return DYNAMIC_IS_ADMIN_DISABLED[TenantContext.currentTenant] = isDisable
    }

    static boolean isProvisionActive() {
        if(Holders.config.webcommander.provision.enabled) {
            return true
        }
        return false
    }

    static String ssoURL(){
        return Holders.config.webcommander.sso.host
    }

    static def methodMissing(String property) {
        return licenses[property.toLowerCase()]
    }

    static License license(String name) {
        return licenses ? licenses[name] : null
    }

    static fetchLicense() {
        Map backupLicense = licenses
        Closure processLicense = { licenseMap ->
            // currently nextBillingDate is not being passed. Later have to remove it
            licenseMap.nextBillingDate = "2045-11-16"
            if(!licenseMap.features) {
                sendValidationFailedNotice()
                return
            }
            expiryDate = licenseMap.expiryDate.toDate("yyyy-MM-dd").getTime()
            nextBillingDate = licenseMap.nextBillingDate.toDate("yyyy-MM-dd").toZone(TimeZone.getDefault()).getTime()
            licenses = new ConcurrentHashMap<>()
            if(expiryDate < new Date().getTime()) {
                sendExpiredNotice()
                return
            }
            licenseMap.features.each { licenseObj ->
                if(licenseObj.isEnable) {
                    License license = new License()
                    license.id = licenseObj.name
                    license.limit = licenseObj.limit
                    license.isLimitExtensible = licenseObj.extensible ?: false
                    licenses.put(license.id, license)
                }
            }
            if(licenses.size() == 0) {
                sendInvalidLicenseNotice()
            }
            if(licenses.api_limit) {
                if(licenses.api) {
                    License limit = licenses.api_limit
                    licenses.api.limit = limit.limit
                    licenses.api.isLimitExtensible = limit.isLimitExtensible
                }
                licenses.remove("api_limit")
            }
        }
        try {
            setIS_ADMIN_DISABLED(false)
            setDISABLED_MESSAGE(null)
            ProvisionAPIService wizardService = Holders.applicationContext.getBean(ProvisionAPIService)
            try {
                wizardService.updateMyInstalledPlugin()
            } catch (Throwable ignored) {}
            Map licenseMap = wizardService.fetchLicense()
            processLicense(licenseMap)
            List licenseConfig = [
                [
                    type: DomainConstants.SITE_CONFIG_TYPES.LICENSE,
                    configKey: "licenseCache",
                    value: (licenseMap as JSON)
                ],
                [
                    type: DomainConstants.SITE_CONFIG_TYPES.LICENSE,
                    configKey: "package_name",
                    value: licenseMap.packageName
                ],
                [
                    type: DomainConstants.SITE_CONFIG_TYPES.LICENSE,
                    configKey: "package_weight",
                    value: licenseMap.packageWeight
                ],
                [
                    type: DomainConstants.SITE_CONFIG_TYPES.LICENSE,
                    configKey: "is_trial_package",
                    value: licenseMap.isTrial
                ]
            ]
            ConfigService service = Holders.applicationContext.getBean(ConfigService)
            if(SiteConfig.findByTypeAndConfigKey(SITE_CONFIG_TYPES.ADMINISTRATION, "instance_id")) {
              licenseConfig.add([
                  type: SITE_CONFIG_TYPES.ADMINISTRATION,
                  configKey: "instance_id",
                  value: licenseMap.instanceId
              ])
            } else {
                new SiteConfig(type: SITE_CONFIG_TYPES.ADMINISTRATION, configKey: "instance_id", value: licenseMap.instanceId).save()
            }
            service.update(licenseConfig)
        } catch(Throwable k) {
            log.log(Level.SEVERE, "WebCommander: Could not fetch license", k)
            licenses = backupLicense
            if(licenses == null) {
                String response = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.LICENSE, "licenseCache")
                if(response) {
                    processLicense(response)
                }
            }
            sendConnectFailedNotice()
        }
    }

    private static sendValidationFailedNotice() {
        sendNotice("couldnt.validate.license.contact.with.vendor")
    }

    private static sendExpiredNotice() {
        sendNotice("license.expired.update.license")
    }

    private static sendInvalidLicenseNotice() {
        sendNotice("license.invalid.contact.with.vendor")
    }

    private static sendConnectFailedNotice() {
        sendNotice("couldn't.connect.license.server.contact.with.vendor")
    }

    static sendNotice(String msgKey, String type = "critical") {
        sendNotice(0, msgKey, null, null, type)
    }

    static generateMessage(OperatorPendingNotification notification) {
        ApplicationTagLib g = Holders.applicationContext.getBean(ApplicationTagLib)
        String msgKey = notification.message
        List savedArgs = notification.jsonArgs ? JSON.parse(notification.jsonArgs) : []
        List displayArgs
        String displayIdentifier
        switch(notification.subjectId.substring(8).toInteger()) {
            case 0:
                break
            case 1:
                displayIdentifier = g.message(code: "license." + savedArgs[1].replace("_", "."))
                displayArgs = [savedArgs[0], displayIdentifier]
                break
            case 2:
                displayIdentifier = g.message(code: "license." + savedArgs[0].replace("_", "."))
                displayArgs = [displayIdentifier, savedArgs[1]]
                break
            case 3:
                displayIdentifier = g.message(code: "license." + savedArgs[1].replace("_", "."))
                String name = savedArgs[1].substring(0, savedArgs[1].length() - 6).capitalize()
                displayArgs = [savedArgs[0], displayIdentifier, name, Class.forName("com.webcommander.license.validator.${name}Validator").currentCount - savedArgs[0].toInteger(), 3 - (new Date().gmt() - notification.created).days]
                break
        }
        return g.message(code: msgKey, args: displayArgs)
    }

    static sendNotice(int msgNumber, String identifier, Integer limit, Integer currentCount, String type = "error") {
        String msgKey
        String saveArgs
        List displayArgs
        Locale locale = Locale.getDefault()
        MessageSource source = Holders.applicationContext.getBean(MessageSource)
        String displayIdentifier = source.getMessage(identifier, [] as Object[], identifier, locale)
        String name = identifier.substring(0, identifier.length() - 6).capitalize()
        switch (msgNumber) {
            case 0:
                msgKey = identifier
                break
            case 1:
                msgKey = "limit.feature.exceeded.limit"
                saveArgs = ([limit, identifier] as JSON).toString()
                displayArgs = [limit, displayIdentifier]
                break
            case 2:
                msgKey = "limit.feature.approaching.limit"
                saveArgs = ([identifier, limit] as JSON).toString()
                displayArgs = [displayIdentifier, limit]
                break
            case 3:
                msgKey = "limit.feature.exceeded.extras.disabled.n.days"
                saveArgs = ([limit, identifier] as JSON).toString()
                displayArgs = [limit, displayIdentifier, name, currentCount - limit, 3]
                break
        }
        CommanderMailService mailService = Holders.applicationContext.getBean(CommanderMailService)
        String message = source.getMessage(msgKey, displayArgs as Object[], msgKey, locale)

        OperatorPendingNotification notification = OperatorPendingNotification.createCriteria().get {
            eq("message", msgKey)
            eq("jsonArgs", saveArgs)
            eq("isObsolete", false)
            maxResults(1)
        }
        if(!notification) {
            notification = new OperatorPendingNotification(message: msgKey, subjectId: "license-" + msgNumber, type: "license", msgType: type, jsonArgs: saveArgs, licenseIdentifier: identifier).save()
        }
        if(msgNumber == 3) {
            int dayDiff = (new Date().gmt() - notification.created).days
            if(dayDiff > 2) {
                notification.isObsolete = true
                Class.forName("com.webcommander.license.validator.${name}Validator").deActivateExtras()
                notification.save()
                return
            }
            hideObsoleteNotification(identifier, "license-3", notification.id)
        }
        if(type == "critical" && msgNumber == 0) {
            setIS_ADMIN_DISABLED(true)
            setDISABLED_MESSAGE([message: msgKey, subjectId: "license-" + msgNumber, type: "license", msgType: type, jsonArgs: saveArgs, created: notification.created])
        }
        try {
            Map macrosAndTemplate = mailService.getMacrosAndTemplateByIdentifier("license-validation-failed")
            if(!macrosAndTemplate.emailTemplate.active) {
                return
            }
            Map refinedMacros = macrosAndTemplate.commonMacros
            macrosAndTemplate.macros.each {
                switch (it.key.toString()) {
                    case "message" :
                        refinedMacros[it.key] = message
                        break
                    case "operator_full_name" :
                        refinedMacros[it.key] = "Admin"
                        break
                }
            }
            mailService.sendMail(macrosAndTemplate.emailTemplate, macrosAndTemplate.activeHtml, macrosAndTemplate.activeText, refinedMacros, StoreDetail.first().address.email)
        } catch(Throwable t) {
            //just skip for send mail failed
        }
    }

    static sendNotice(int msgNumber, String identifier, Integer limit, String type = "error") {
        sendNotice(msgNumber, identifier, limit, null, type)
    }

    static validateLicense() {
        validators.each { validator ->
            validator.validateLicense()
        }
    }

    static List<OperatorPendingNotification> readNotifications() {
        List<OperatorPendingNotification> notifications = OperatorPendingNotification.createCriteria().list {
            eq "isObsolete", false
        }
        notifications.each {
            if(it.subjectId != "license-3") {
                it.isObsolete = true
                it.save()
            }
        }
        return notifications
    }

    static void hideObsoleteNotification(String identifier, String subjectId, Long exclude = null) {
        List<OperatorPendingNotification> notifications = OperatorPendingNotification.createCriteria().list {
            eq "isObsolete", false
            eq("licenseIdentifier", identifier)
            eq("subjectId", subjectId)
            if(exclude) {
                ne("id", exclude)
            }
        }
        notifications*.isObsolete = true
        notifications*.save()
    }

    static Boolean isAllowed(String name) {
        if(isProvisionActive() && name && !license(name)) {
            return false
        }
        return true
    }
}