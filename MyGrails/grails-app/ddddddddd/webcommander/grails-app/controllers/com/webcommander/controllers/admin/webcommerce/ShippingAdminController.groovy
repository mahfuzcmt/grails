package com.webcommander.controllers.admin.webcommerce

import com.webcommander.admin.Zone
import com.webcommander.admin.ZoneService
import com.webcommander.authentication.annotations.Restriction
import com.webcommander.common.CommonService
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.throwables.AttachmentExistanceException
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.*
import grails.converters.JSON

class ShippingAdminController {
    CommonService commonService
    ShippingService shippingService
    ZoneService zoneService

    @Restriction(permission = "shipping.view.list")
    def loadAppView() {
        render(view: "/admin/shipping/profile/appView");
    }

    def loadLeftPanel() {
        List<ShippingProfile> profiles = shippingService.getShippingProfile([:])
        def classList = []
        ShippingProfile shippingProfile
        if(params.profileId) {
            shippingProfile = ShippingProfile.get(params.profileId)
        } else if (profiles){
            shippingProfile = ShippingProfile.get(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.SHIPPING, "default_shipping_profile").toLong(0)) ?: profiles.first()
        }
        Boolean classEnabled = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.SHIPPING, "enable_shipping_class") == "true";
        render(view: "/admin/shipping/profile/leftPanel", model: [
            profiles: profiles, profile: shippingProfile,
            classList: classList, classEnabled: classEnabled,
            selectedClassId: params.long("shippingClass"),
            selectedRuleId: params.long("selected")
        ]);
    }

    def explorerView() {
        ShippingRule shippingRule = ShippingRule.get(params.id)
        Boolean classEnabled = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.SHIPPING, "enable_shipping_class") == "true";
        render(view: "/admin/shipping/profile/explorerView", model: [rule: shippingRule, classEnabled: classEnabled]);
    }

    def loadRateRow() {
        render(view: "/admin/shipping/rate/rateDivRow", model: [rate: ShippingPolicy.get(params.rate_id)])
    }

    def loadNewRate() {
        Boolean classEnabled = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.SHIPPING, "enable_shipping_class")?.toBoolean();
        def rate = params.id ? ShippingPolicy.get(params.id) : new ShippingPolicy();
        render (view: "/admin/shipping/rate/addNewRate", model: [rate: rate, "shipping_rule_id": params."shipping-rule-id", classEnabled: classEnabled]);
    }

    def createProfilePopup() {
        render view: "/admin/shipping/profile/createPopup"
    }

    def createProfileForm() {
        ShippingProfile profile = params.id ? ShippingProfile.get(params.id) : new ShippingProfile()
        render view: "/admin/shipping/profile/createProfile", model: [profile: profile]
    }

    def saveProfile() {
        def result = shippingService.saveProfile(params)
        if (result) {
            render([status: "success", message: g.message(code: "shipping.profile.save.success"), id: result.id] as JSON)
        } else {
            render([status: "error", message: g.message(code: "shipping.profile.save.failure")] as JSON)
        }
    }

    def copyProfile() {
        Long savedId = shippingService.copyProfile(params)
        if(savedId) {
            render([status: "success", message: g.message(code: "shipping.profile.copy.success"), id: savedId] as JSON);
        } else {
            render([status: "error", message: g.message(code: "shipping.profile.copy.failure")] as JSON);
        }
    }

    def createShippingRuleForm() {
        def classList = ShippingClass.all?.sort {-it.id};
        ShippingRule shippingRule = params.ruleId? ShippingRule.get(params.long("ruleId")) : null
        Boolean classEnabled = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.SHIPPING, "enable_shipping_class")?.toBoolean();
        render view: "/admin/shipping/rule/createRule", model: [profileId: params.profileId, classList: classList, classEnabled: classEnabled, shippingRule: shippingRule]
    }

    def useExistingProfilePopup() {
        List<ShippingProfile> profileList = ShippingProfile.all?.sort {-it.id};
        render view: "/admin/shipping/profile/useExistingProfile", model: [profileList: profileList]
    }

    def useExistingRulePopup() {
        params.max = params.max ?: "5";
        def methodKeys = ["": "${g.message(code: 'all')}"]
        methodKeys.putAll(NamedConstants.SHIPPING_POLICY_TYPE_MESSAGE_KEYS)
        def classList = ShippingClass.all?.sort {-it.id};
        Integer count = shippingService.getShippingRuleCount(params)
        List<ShippingRule> ruleList = commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.offset = offset
            return shippingService.getShippingRule(params)
        }
        Boolean classEnabled = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.SHIPPING, "enable_shipping_class")?.toBoolean();
        render view: "/admin/shipping/rule/useExistingRule", model: [ruleList: ruleList, count: count, methodKeys: methodKeys, classList: classList, classEnabled: classEnabled]
    }


    def copyRule() {
        ShippingRule shippingRule = shippingService.copyRule(params)
        if(shippingRule) {
            render([status: "success", message: g.message(code: "shipping.rule.copy.success"), id: shippingRule.id] as JSON);
        } else {
            render([status: "error", message: g.message(code: "shipping.rule.copy.failure")] as JSON);
        }
    }

    def sortRules() {
        shippingService.sortRules(params)
        render([status: "success"] as JSON);
    }

    def assignRuleToProfile() {
        if(!params.id)
            throw new ApplicationRuntimeException("no.rule.selected")
        if(!params.profileId)
            throw new ApplicationRuntimeException("no.profile.selected")

        ShippingProfile shippingProfile = shippingService.assignRuleToProfile(params)
        if(shippingProfile) {
            render([status: "success", message: g.message(code: "shipping.rule.assign.success"), id: params.id] as JSON);
        } else {
            render([status: "error", message: g.message(code: "shipping.rule.assign.failure")] as JSON);
        }
    }

    def renameProfile() {
        if(params.id == null) {
            throw new ApplicationRuntimeException("no.profile.selected")
        }
        render view: "/admin/shipping/profile/renameProfile", model: [id: params.id]
    }

    def renameRule() {
        if(!params.id)
            throw new ApplicationRuntimeException("no.rule.selected")

        def classList = ShippingClass.all?.sort {-it.id};
        Boolean classEnabled = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.SHIPPING, "enable_shipping_class")?.toBoolean();
        render view: "/admin/shipping/rule/renameRule", model: [id: params.id, profileId: params.profileId, classEnabled: classEnabled, classList: classList]
    }

    def saveShippingPolicy() {
        ShippingPolicy rate = shippingService.saveShippingPolicy(params);
        if(rate) {
            render([status: "success", message: g.message(code: "shipping.rate.save.success", args: [params.field, params.value])] as JSON)
        } else {
            new ApplicationRuntimeException("shipping.rate.save.failure")
        }
    }

    def isShippingPolicyUnique() {
        if (commonService.isUnique(ShippingPolicy, params)) {
            render([status: "success", message: g.message(code: "provided.field.available", args: [params.field, params.value])] as JSON)
        } else {
            render([status: "error", message: g.message(code: "provided.field.value.exists", args: [params.field, params.value])] as JSON)
        }
    }

    def selectedRule() {
        def selectedRule
        if(params."id") {
            selectedRule = ShippingRule.get(params.long("id"))
        }
        else {
            throw new ApplicationRuntimeException("no.rule.selected")
        }
        Boolean classEnabled = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.SHIPPING, "enable_shipping_class")?.toBoolean();
        render(view: "/admin/shipping/rule/selectedRule", model: [selectedRule: selectedRule, classEnabled: classEnabled])
    }

    def deleteShippingPolicy() {
        def id = params.long("id");
        try {
            if (shippingService.deleteShippingPolicy(id, params.at1_reply, params.at2_reply)) {
                render([status: "success", message: g.message(code: "shipping.rate.delete.successful")] as JSON);
            } else {
                render([status: "error", message: g.message(code: "shipping.rate.delete.failure")] as JSON);
            }
        } catch(AttachmentExistanceException att) {
            render([status: "error", error_code: "attachment.exists", attachments: att.attachmentInfo] as JSON)
        }
    }

    def isShippingRuleUnique() {
        if (commonService.isUnique(ShippingRule, params)) {
            render([status: "success", message: g.message(code: "provided.field.available", args: [params.field, params.value])] as JSON)
        } else {
            render([status: "error", message: g.message(code: "provided.field.value.exists", args: [params.field, params.value])] as JSON)
        }
    }

    def ruleCopyType() {
        render view: "/admin/shipping/rule/copyType"
    }

    def deleteShippingRule() {
        def result = shippingService.deleteShippingRule(params.list("id").collect{it.toLong()})
        if(result) {
            render([status: "success", message: g.message(code: "shipping.rule.delete.success")] as JSON);
        } else {
            render([status: "error", message: g.message(code: "shipping.rule.delete.failure")] as JSON);
        }
    }

    def isShippingProfileUnique() {
        if (commonService.isUnique(ShippingProfile, params)) {
            render([status: "success", message: g.message(code: "provided.field.available", args: [params.field, params.value])] as JSON)
        } else {
            render([status: "error", message: g.message(code: "provided.field.value.exists", args: [params.field, params.value])] as JSON)
        }
    }



    def saveShippingClass() {
        def result = shippingService.saveShippingClass(params)
        if (result) {
            render([status: "success", message: g.message(code: "shipping.class.save.success"), id: result.id] as JSON)
        } else {
            render([status: "error", message: g.message(code: "shipping.class.save.failure")] as JSON)
        }
    }

    def isShippingClassUnique() {
        if (commonService.isUnique(ShippingClass, params)) {
            render([status: "success", message: g.message(code: "provided.field.available", args: [params.field, params.value])] as JSON)
        } else {
            render([status: "error", message: g.message(code: "provided.field.value.exists", args: [params.field, params.value])] as JSON)
        }
    }

    def deleteShippingProfile() {
        Long id = params.long("id")
        try {
            if (shippingService.deleteShippingProfile(id, params.at1_reply, params.at2_reply)) {
                render([status: "success", message: g.message(code: "shipping.profile.delete.success")] as JSON)
            } else {
                render([status: "error", message: g.message(code: "shipping.profile.delete.failure")] as JSON)
            }
        } catch (AttachmentExistanceException att) {
            render([status: "error", error_code: "attachment.exists", attachments: att.attachmentInfo] as JSON)
        }
    }

    def deleteShippingClass() {
        def id = params.long("id");
        try {
            if (shippingService.deleteShippingClass(id, params.at1_reply, params.at2_reply)) {
                render([status: "success", message: g.message(code: "shipping.class.delete.successful")] as JSON)
            } else {
                render([status: "error", message: g.message(code: "shipping.class.delete.failure")] as JSON)
            }
        } catch(AttachmentExistanceException att) {
            render([status: "error", error_code: "attachment.exists", attachments: att.attachmentInfo] as JSON)
        }
    }

    def detachShippingRule() {
        Boolean deleted = shippingService.detachShippingRule(params)
        if (deleted) {
            render([status: "success", message: g.message(code: "shipping.rule.delete.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "shipping.rule.delete.failure")] as JSON)
        }
    }

    def detachRate() {
        Boolean deleted = shippingService.detachRate(params)
        if (deleted) {
            render([status: "success", message: g.message(code: "shipping.rate.delete.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "shipping.rate.delete.failure")] as JSON)
        }
    }

    def detachZone() {
        Boolean deleted = shippingService.detachZone(params)
        if (deleted) {
            render([status: "success", message: g.message(code: "shipping.zone.delete.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "shipping.zone.delete.failure")] as JSON)
        }
    }

    def addRuleView() {
        List<Long> ids = params.list("ids").collect{ it.toLong()};
        List<ShippingRule> rules = ShippingRule.createCriteria().list {
            if(ids) {
                not {'in'("id", ids)}
            }
            if(params.searchText) {
                ilike("name", "%${params.searchText.trim().encodeAsLikeText()}%")
            }
        }
        render(view: "/admin/shipping/rule/addRule", model: [rules: rules])
    }

    def addRule() {
        ShippingRule rule = shippingService.addToProfile(params)
        render(view: "/admin/shipping/rule/ruleRow", model: [rule: rule, clazz: "scrollable-rule"])
    }

    def addRate() {
        params.max = params.max ?: 5
        List<ShippingPolicy> rates = shippingService.getShippingPolicy(params)
        render(view: "/admin/shipping/rate/addRate", model: [rates: rates])
    }

    def addRulePopup() {
        render view: "/admin/shipping/rule/createPopup"
    }

    def saveRule() {
        ShippingRule rule = shippingService.saveShippingRule(params)
        if (rule) {
            render([status: "success", message: g.message(code: "shipping.rule.save.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "shipping.rule.save.failure")] as JSON)
        }
    }

    def addZone() {
        ShippingRule rule = ShippingRule.get(params.ruleId)
        List zoneIds = rule?.zoneList?.id
        List<Zone> zones = zoneService.getZones([
            max: params.max ?: 5,
            searchText: params.searchText,
            excludeIds: zoneIds,
            isDefault: false
        ])
        render(view: "/admin/zone/addZonePop", model: [zones: zones])
    }

    def selectRate() {
        if (!params.rateId || !params.ruleId)
            throw new ApplicationRuntimeException("no.rule.selected")
        ShippingRule rule = shippingService.addRateToRule(params)
        if (rule) {
            render([status: "success", message: g.message(code: "shipping.rule.save.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "shipping.rule.save.failure")] as JSON)
        }
    }

    def selectZone() {
        if (!params.zoneId || !params.ruleId)
            throw new ApplicationRuntimeException("no.rule.selected")
        ShippingRule rule = shippingService.addZoneToRule(params)
        if (rule) {
            render([status: "success", message: g.message(code: "shipping.rule.save.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "shipping.rule.save.failure")] as JSON)
        }
    }

    def allRuleList () {
        params.max = params.max ?: "10";
        Boolean classEnabled = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.SHIPPING, "enable_shipping_class")?.toBoolean();
        Integer count = shippingService.getShippingRuleCount(params)
        List<ShippingRule> ruleList = commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.offset = offset
            return shippingService.getShippingRule(params)
        }
        render(view: "/admin/shipping/rule/allRuleList", model: [ruleList: ruleList, count: count, classEnabled: classEnabled])
    }

    def allRateList () {
        params.max = params.max ?: "10";
        Integer count = shippingService.getShippingPolicyCount(params)
        List<ShippingRule> rateList = commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.offset = offset
            return shippingService.getShippingPolicy(params)
        }
        render(view: "/admin/shipping/rate/allRateList", model: [rateList: rateList, count: count])
    }

    def advanceFilterPanel() {
        def methodKeys = ["": "${g.message(code: 'all')}"]
        methodKeys.putAll(NamedConstants.SHIPPING_POLICY_TYPE_MESSAGE_KEYS)
        def handlingCostFilterKeys = ["": "${g.message(code: 'all')}"]
        handlingCostFilterKeys.putAll(NamedConstants.HANDLING_COST_FILTER_KEYS)
        def classList
        def zoneList
        Boolean classEnabled = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.SHIPPING, "enable_shipping_class")?.toBoolean();
        if(params.ratePanel?.toBoolean() == false) {
            zoneList = Zone.all?.sort {-it.id};
            classList = ShippingClass.all?.sort {-it.id};
        }
        render(view: "/admin/shipping/rule/advanceFilter", model: [methodKeys: methodKeys, classList: classList,
                                                                   zoneList: zoneList, handlingCostFilterKeys: handlingCostFilterKeys, classEnabled: classEnabled])
    }

    def rateBulkEditForm() {
        if(!params."idList")
            throw new ApplicationRuntimeException("no.rate.selected")
        List<ShippingPolicy> rateList = ShippingPolicy.findAll() {inList "id", params.list("idList").collect {it.toLong()}}?.sort {-it.id}
        render(view: "/admin/shipping/rate/bulkedit/bulkEdit", model: [rateList: rateList])
    }

    def conditionRowTemplate() {
        params.newRow = true
        params.fromAmount = params.from && params.from.size() > 0 ? params.from?.toDouble() : null
        params.toAmount = params.to && params.to.size() > 0 ? params.to?.toDouble() : null
        params.packetWeight = params.packetWeigh && params.packetWeigh.size() > 0 ?  params.packetWeigh.toDouble() : null
        params.shippingCost = params.shippingCost && params.shippingCost.size() > 0 ?  params.shippingCost : null
        params.handlingCost =  params.handlingCost && params.handlingCost.size() > 0 ?  params.handlingCost : null

        def model = [condition: params, isWeight: params."isWeight"?.toBoolean()]
        if(params.bulkEdit?.toBoolean()){
            render(view: "/admin/shipping/rate/bulkedit/bulkEditConditionRowTemplate", model: model + [rateId: params.rateId.toLong()])
        } else {
            render(view: "/admin/shipping/rate/conditionRowTemplate", model: model)
        }
    }

    def bulkRateSave() {
        def rates = params.list("rates")
        rates.each {
            def rateParams = JSON.parse(it)
            shippingService.saveShippingPolicy(rateParams);
        }
        render([status: "success", message: g.message(code: "shipping.bulk.rate.save.success")] as JSON)
    }
}
