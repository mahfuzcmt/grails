package com.webcommander.webcommerce

import com.webcommander.ApplicationTagLib
import com.webcommander.admin.Country
import com.webcommander.admin.State
import com.webcommander.admin.Zone
import com.webcommander.admin.ZoneService
import com.webcommander.annotations.Initializable
import com.webcommander.common.CommonService
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.conversion.MassConversions
import com.webcommander.events.AppEventManager
import com.webcommander.manager.HookManager
import com.webcommander.shipping.ShippingRateData
import com.webcommander.shipping.ShippingRuleData
import com.webcommander.shipping.ZoneData
import com.webcommander.task.MultiLoggerTask
import com.webcommander.task.Task
import com.webcommander.task.TaskService
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.util.AppUtil
import com.webcommander.util.DomainUtil
import com.webcommander.util.StringUtil
import com.webcommander.util.TrashUtil
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.hibernate.sql.JoinType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.MessageSource

@Initializable
@Transactional
class ShippingService {

    ZoneService zoneService
    CommonService commonService
    MessageSource messageSource
    TaskService taskService
    CommonImportService commonImportService

    @Autowired
    @Qualifier("com.webcommander.ApplicationTagLib")
    ApplicationTagLib app

    static void initialize() {
        HookManager.register("shippingClass-delete-veto") { response, id ->
            Integer count = ShippingRule.createCriteria().count { eq("shippingClass.id", id) }
            if (count) {
                response."shipping.rule" = count
            }
            return response
        }
        HookManager.register("shippingClass-delete-veto-list") { response, id ->
            List<String> ruleNames = ShippingRule.createCriteria().list {
                projections {
                    property("name")
                }
                eq("shippingClass.id", id)
            }
            if(ruleNames.size()) {
                response."shipping.rule" = ruleNames
            }
            return response
        }
        HookManager.register("zone-delete-veto") { response, id ->
            Integer count = ShippingRule.createCriteria().count {
                zoneList {
                    eq("id", id)
                }
            }
            if (count) {
                response."shipping.rule" = count
            }
            return response
        }
        HookManager.register("zone-delete-veto-list") { response, id ->
            List<String> ruleNames = ShippingRule.createCriteria().list {
                projections {
                    property("name")
                }
                zone {
                    eq("id", id)
                }
            }
            if(ruleNames.size()) {
                response."shipping.rule" = ruleNames
            }
            return response
        }
        HookManager.register("shippingPolicy-delete-veto", { response, id ->
            Long count = ShippingRule.createCriteria().count { eq("shippingPolicy.id", id) }
            if (count) {
                response."shipping.rule" = count
            }
            return response
        })
        HookManager.register("shippingPolicy-delete-veto-list", { response, id ->
            List<String> ruleNames = ShippingPolicy.createCriteria().list {
                projections {
                    property("name")
                }
                eq("shippingPolicy.id", id)
            }
            if (ruleNames.size()) {
                response."shipping.rule" = ruleNames
            }
            return response
        })
    }

    private Closure getFilterClosureForShippingPolicy(GrailsParameterMap params) {
        return {
            if (params.searchText) {
                String searchText = "%${params.searchText.trim().encodeAsLikeText()}%";
                ilike("name", searchText)
            }
            if (params.policyType) {
                eq "policyType", params."policyType"
            }
            if (params.handlingCost == "WITH_HANDLING") {
                sqlRestriction("exists(select id from shipping_condition c where c.shipping_policy_id = this_.id and c.handling_cost is not null and c.handling_cost > 0)")
            }
            if (params.handlingCost == "WITHOUT_HANDLING") {
                sqlRestriction("exists(select id from shipping_condition c where c.shipping_policy_id = this_.id and (c.handling_cost is null or  c.handling_cost = 0))")
            }

        }
    }

    List<ShippingPolicy> getShippingPolicy(GrailsParameterMap params) {
        def listMap = [max: params.max, offset: params.offset];
        return ShippingPolicy.createCriteria().list(listMap) {
            and getFilterClosureForShippingPolicy(params)
            order(params.sort ?: "created", params.dir ?: "desc")
        }
    }

    private Closure getFilterClosureForShippingRule(GrailsParameterMap params) {
        return {
            if (params.searchText) {
                String searchText = "%${params.searchText.trim().encodeAsLikeText()}%";
                ilike("name", searchText)
            }
            if (params.shippingClass) {
                eq "shippingClass.id", params.long("shippingClass")
            }
            if (params.policyType) {
                createAlias('shippingPolicy', 'sp')
                eq "sp.policyType", params."policyType"
            }
            if (params.zone) {
                zoneList {
                    eq "id", params.long("zone")
                }
            }
            if (params.handlingCost) {
                if (params.handlingCost == "WITH_HANDLING") {
                    shippingPolicy {
                        conditions {
                            isNotNull("handlingCost")
                        }
                    }
                } else if(params.handlingCost == "WITHOUT_HANDLING"){
                    createAlias "shippingPolicy", "sp", JoinType.LEFT_OUTER_JOIN
                    createAlias "sp.conditions", "spc", JoinType.LEFT_OUTER_JOIN
                    isNull "spc.handlingCost"
                    /*shippingPolicy {
                        conditions {
                            isNull("handlingCost")
                        }
                    }*/
                }
            }
        }
    }

    Integer getShippingRuleCount(GrailsParameterMap params) {
        return ShippingRule.createCriteria().count(getFilterClosureForShippingRule(params))
    }

    Integer getShippingPolicyCount(GrailsParameterMap params) {
        return ShippingPolicy.createCriteria().count(getFilterClosureForShippingPolicy(params))
    }

    List<ShippingPolicy> getShippingRule(GrailsParameterMap params) {
        def listMap = [max: params.max, offset: params.offset];
        return ShippingRule.createCriteria().list(listMap) {
            and getFilterClosureForShippingRule(params)
            order(params.sort ?: "created", params.dir ?: "desc")
        }
    }

    private Closure getFilterClosureForShippingProfile(Map params) {
        return {
            if (params.searchText) {
                String searchText = "%${params.searchText.trim().encodeAsLikeText()}%";
                or {
                    ilike("name", searchText)
                }
            }
        }
    }

    private Closure getFilterClosureForShippingClass(GrailsParameterMap params) {
        return {
            if (params.searchText) {
                String searchText = "%${params.searchText.trim().encodeAsLikeText()}%";
                or {
                    ilike("name", searchText)
                }
            }
        }
    }

    List<ShippingProfile> getShippingProfile(Map params) {
        def listMap = [max: params.max, offset: params.offset];
        return ShippingProfile.createCriteria().list(listMap) {
            and getFilterClosureForShippingProfile(params)
            order(params.sort ?: "created", params.dir ?: "desc")
        }
    }

    List<ShippingClass> getShippingClass(GrailsParameterMap params) {
        def listMap = [max: params.max, offset: params.offset];
        return ShippingClass.createCriteria().list(listMap) {
            and getFilterClosureForShippingClass(params)
            order(params.sort ?: "created", params.dir ?: "desc")
        }
    }

    ShippingPolicy saveShippingPolicy(def params) {
        ShippingPolicy shippingPolicy;
        long id
        if(params.id) {
            id = params.id.toLong()
            shippingPolicy = ShippingPolicy.get(id);
            AppEventManager.fire("before-shippingPolicy-update", [shippingPolicy])
            shippingPolicy.conditions*.delete();
            shippingPolicy.conditions.clear();
        } else {
            shippingPolicy = new ShippingPolicy();
        }
        shippingPolicy.name = params.name;
        shippingPolicy.policyType = params.policyType
        shippingPolicy.isCumulative = params.isCumulative == "true"
        if(params.policyType == "sba") {
            shippingPolicy.isPriceEnterWithTax = params.isPriceEnterWithTax == "true"
        }
        Boolean isByWeight = shippingPolicy.policyType == DomainConstants.SHIPPING_POLICY_TYPE.SHIP_BY_WEIGHT
        String unitWeight = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "unit_weight")
        addConditionToPolicy(shippingPolicy, params)
        boolean additional = params."additional-condition".toBoolean(false)
        shippingPolicy.isAdditional = additional
        if(additional) {
            shippingPolicy.additionalAmount = isByWeight ? MassConversions.convertMassToSI(unitWeight, Double.parseDouble(params."additional-amount")).doubleValue() :
                    Double.parseDouble(params."additional-amount");
            shippingPolicy.additionalCost = Double.parseDouble(params."additional-cost")
        }
        HookManager.hook("before-shipping-policy-save", shippingPolicy)
        shippingPolicy.save();
        if(!shippingPolicy.hasErrors()) {
            if(id) {
                AppEventManager.fire("shipping-policy-update", [id])
            }
            if(params."shipping-rule-id") {
                ShippingRule shippingRule = ShippingRule.get(params."shipping-rule-id")
                if(shippingRule) {
                    shippingRule.setShippingPolicy(shippingPolicy)
                    AppEventManager.fire("shipping-rule-update", [shippingRule.id])
                }
            }
            return shippingPolicy
        }
        return null;
    }

    def addConditionToPolicy(ShippingPolicy shippingPolicy, params) {
        Boolean isByWeight = shippingPolicy.policyType == DomainConstants.SHIPPING_POLICY_TYPE.SHIP_BY_WEIGHT
        Boolean isByQty = shippingPolicy.policyType == DomainConstants.SHIPPING_POLICY_TYPE.SHIP_BY_QUANTITY
        String unitWeight = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "unit_weight")
        if(shippingPolicy.policyType == DomainConstants.SHIPPING_POLICY_TYPE.FREE_SHIPPING) {
            ShippingCondition shippingCondition = new ShippingCondition();
            Map singleHandlingCost = StringUtil.extractPercentNumber(params.singleHandlingCost)
            shippingCondition.handlingCost = singleHandlingCost.number?.toDouble()
            shippingCondition.handlingCostType = singleHandlingCost.sign == "%" ? DomainConstants.SHIPPING_AMOUNT_TYPE.PERCENT : DomainConstants.SHIPPING_AMOUNT_TYPE.FLAT
            shippingCondition.shippingCost = 0;
            shippingCondition.shippingPolicy = shippingPolicy;
            shippingPolicy.addToConditions(shippingCondition);
            shippingPolicy.isCumulative = false //free shipping always false
        } else if(shippingPolicy.policyType == DomainConstants.SHIPPING_POLICY_TYPE.FLAT_RATE) {
            Map singleHandlingCost = StringUtil.extractPercentNumber(params.singleHandlingCost)
            Map singleShippingCost = StringUtil.extractPercentNumber(params.singleShippingCost)
            ShippingCondition shippingCondition = new ShippingCondition();
            shippingCondition.handlingCost = singleHandlingCost.number?.toDouble()
            shippingCondition.handlingCostType = singleHandlingCost.sign == "%" ? DomainConstants.SHIPPING_AMOUNT_TYPE.PERCENT : DomainConstants.SHIPPING_AMOUNT_TYPE.FLAT
            shippingCondition.shippingCost = singleShippingCost.number?.toDouble()
            shippingCondition.shippingCostType = singleShippingCost.sign == "%" ? DomainConstants.SHIPPING_AMOUNT_TYPE.PERCENT : DomainConstants.SHIPPING_AMOUNT_TYPE.FLAT
            shippingCondition.shippingPolicy = shippingPolicy;
            shippingPolicy.addToConditions(shippingCondition);
        } else if(shippingPolicy.policyType == DomainConstants.SHIPPING_POLICY_TYPE.API) {
            ShippingCondition shippingCondition = new ShippingCondition();
            shippingCondition.handlingCost = params.'apiHandlingCost'?.toDouble();
            shippingCondition.apiType = params.api
            shippingCondition.apiServiceType = params.apiService
            shippingCondition.extraCover = params.double("extraCoverValue")
            shippingCondition.packingAlgorithm = params.packingAlgorithm
            shippingCondition.shippingPolicy = shippingPolicy;
            shippingPolicy.addToConditions(shippingCondition);
            shippingPolicy.isCumulative = true //api policy always cumulative
        } else {
            def fromList = params."from" instanceof String[] || params."from" instanceof Collection ? params."from" : [params."from"];
            def toList = params."to" instanceof String[] || params."to" instanceof Collection ? params."to" :  [params."to"]
            def shippingCostList = params."shippingCost" instanceof String[] || params."shippingCost" instanceof Collection ? params."shippingCost" : [params."shippingCost"]
            def handlingCostList = params."handlingCost" instanceof String[] || params."handlingCost" instanceof Collection ? params."handlingCost" : [params."handlingCost"]
            def packetWeightList = params."packetWeight" instanceof String[] || params."packetWeight" instanceof Collection ? params."packetWeight" : [params."packetWeight"]
            Integer conditionCount = fromList.size();
            for (int i = 0; i < conditionCount; i++) {
                ShippingCondition shippingCondition = new ShippingCondition(shippingPolicy: shippingPolicy);
                def fromAmount = Double.parseDouble(fromList[i])
                def toAmount = Double.parseDouble(toList[i])
                if(isByWeight) {
                    shippingCondition.fromAmount = MassConversions.convertMassToSI(unitWeight, Double.parseDouble(fromList[i])).doubleValue()
                    shippingCondition.toAmount = MassConversions.convertMassToSI(unitWeight, Double.parseDouble(toList[i])).doubleValue()
                } else if(isByQty) {
                    shippingCondition.fromAmount = Math.round(fromAmount)
                    shippingCondition.toAmount = Math.round(toAmount)
                } else {
                    shippingCondition.fromAmount = fromAmount
                    shippingCondition.toAmount = toAmount
                }
                Map shippingCost = StringUtil.extractPercentNumber(shippingCostList[i])
                Map handlingCost = StringUtil.extractPercentNumber(handlingCostList[i])
                shippingCondition.shippingCost = shippingCost.number?.toDouble();
                shippingCondition.shippingCostType = shippingCost.sign == "%" ? DomainConstants.SHIPPING_AMOUNT_TYPE.PERCENT : DomainConstants.SHIPPING_AMOUNT_TYPE.FLAT
                shippingCondition.handlingCost = handlingCost?.number?.toDouble()
                shippingCondition.handlingCostType = handlingCost.sign == "%" ? DomainConstants.SHIPPING_AMOUNT_TYPE.PERCENT : DomainConstants.SHIPPING_AMOUNT_TYPE.FLAT
                if(packetWeightList && packetWeightList[i]) {
                    shippingCondition.packetWeight = MassConversions.convertMassToSI(unitWeight, Double.parseDouble(packetWeightList[i])).doubleValue();
                } else {
                    shippingCondition.packetWeight = null;
                }
                shippingPolicy.addToConditions(shippingCondition);
            }
        }
    }

    Boolean deleteShippingPolicy(Long id, String at1, String at2) {
        TrashUtil.preProcessFinalDelete("shippingPolicy", id, at2 != null, at1 != null)
        AppEventManager.fire("before-shippingPolicy-delete", [id, at1])
        ShippingPolicy policy = ShippingPolicy.get(id);
        policy.delete();
        AppEventManager.fire("shippingPolicy-delete", [id])
        return true;
    }

    ShippingRule addRateToRule(def params) {
        ShippingRule rule = ShippingRule.get(params.long("ruleId"))
        ShippingPolicy policy = ShippingPolicy.get(params.long("rateId"))
        rule.shippingPolicy = policy
        rule.save()
        if(!rule.hasErrors())
            return rule
        else null
    }

    ShippingRule addZoneToRule(def params) {
        ShippingRule rule = ShippingRule.get(params.long("ruleId"))
        Zone zone = Zone.get(params.long("zoneId"))
        rule.addToZoneList(zone)
        rule.save()
        if(!rule.hasErrors())
            return rule
        else null
    }

    ShippingRule saveShippingRule(GrailsParameterMap params) {
        ShippingClass shippingClass = params.shippingClass ? ShippingClass.get(params.long("shippingClass")): null
        ShippingRule shippingRule = params.ruleId? ShippingRule.get(params.long("ruleId")) : new ShippingRule()
        shippingRule.name = params.name;
        shippingRule.description = params.description;
        shippingRule.shippingClass = shippingClass
        shippingRule.save();

        if(!shippingRule.hasErrors()) {
            if(params.ruleId) {
                AppEventManager.fire("shipping-rule-update", [params.id])
            } else {
                ShippingProfile shippingProfile = params.profileId? ShippingProfile.get(params.long("profileId")): null
                shippingProfile? shippingProfile.addToShippingRules(shippingRule).save(): null
            }
            return shippingRule
        }
        return null;
    }

    ShippingProfile assignRuleToProfile(def params) {
        ShippingProfile profile = ShippingProfile.get(params.long("profileId"))
        if(profile.shippingRules.id.contains(params.long("id"))) {
            throw new ApplicationRuntimeException("rule.already.exist.in.this.profile")
        }
        ShippingRule rule = ShippingRule.get(params.long("id"))
        profile.addToShippingRules(rule)
        profile.save()
        if (!profile.hasErrors()){
            return profile
        }
        return null
    }

    ShippingRule copyRule(def params) {
        ShippingRule rule = ShippingRule.get(params.long("ruleId"))
        ShippingRule newRule = DomainUtil.clone(rule, ["name", "description", "shippingClass"], [])
        newRule.name = params.name
        newRule.shippingClass = params.shippingClass ? ShippingClass.get(params.long("shippingClass")): null
        newRule.description = params.description
        if(newRule.shippingPolicy) {
            newRule.shippingPolicy.name = commonService.getCopyNameForDomain(newRule.shippingPolicy)
            newRule.shippingPolicy.save()
        }
        newRule.save()
        if(!newRule.hasErrors()) {
            ShippingProfile shippingProfile = params.profileId? ShippingProfile.get(params.long("profileId")): null
            shippingProfile ? shippingProfile.addToShippingRules(newRule) : null
            return newRule
        } else {
            return null
        }
    }

    def sortRules (def params) {
        ShippingProfile profile = ShippingProfile.get(params.long("id"))
        List<ShippingRule> ruleList = []
        params.ruleIdList.split(",").each {
            ruleList.add(ShippingRule.get(it))
        }
        profile.shippingRules = ruleList
        profile.save()
    }

    Boolean deleteShippingRule(List<Long> ids) {
        List<ShippingRule> rules = ShippingRule.where { id in ids }.list();
        ShippingProfile.where {
            shippingRules {
                id in ids
            }
        }.list().each { profile ->
            rules.each {
                if (profile.shippingRules.contains(it)) {
                    profile.removeFromShippingRules(it)
                }
            }
            profile.merge(flush: true)
        }
        rules*.delete();
        return true;
    }

    ShippingClass saveShippingClass(GrailsParameterMap params) {
        ShippingClass clazz;
        if(params.id) {
            clazz = ShippingClass.get(params.id);
        } else {
            clazz = new ShippingClass();
        }
        clazz.name = params.name;
        clazz.description = params.description;
        clazz.save();
        return clazz;
    }

    ShippingRule addToProfile(GrailsParameterMap params) {
        ShippingProfile profile = ShippingProfile.get(params.profile_id);
        def rule = ShippingRule.proxy(params.rule_id)
        profile.addToShippingRules(rule)
        profile.save();
        if(!profile.hasErrors()) {
            AppEventManager.fire("shipping-profile-update", [profile.id])
            return rule
        }
        return null
    }

    Boolean deleteShippingProfile(Long id, String at1, String at2) {
        TrashUtil.preProcessFinalDelete("shippingProfile", id, at2 != null, at1 != null)
        AppEventManager.fire("before-shippingProfile-delete", [id, at1])
        ShippingProfile shippingProfile = ShippingProfile.proxy(id)
        try {
            shippingProfile.delete()
        } catch (Exception ex) {
            return false
        }
        AppEventManager.fire("shippingProfile-delete", [id])
        return true
    }

    def deleteShippingClass(Long id, String at1, String at2) {
        TrashUtil.preProcessFinalDelete("shippingClass", id, at2 != null, at1 != null)
        AppEventManager.fire("before-shippingClass-delete", [id, at1])
        ShippingClass clazz = ShippingClass.get(id);
        clazz.delete();
        AppEventManager.fire("shippingClass-delete", [id])
        return true;
    }

    ShippingProfile saveProfile(GrailsParameterMap params) {
        ShippingProfile profile;
        if(params.id) {
            profile = ShippingProfile.get(params.id);
        } else {
            profile = new ShippingProfile();
        }
        profile.name = params.name;
        if(!params.id && !commonService.isUnique(ShippingProfile, [field: "name", value: params.name])) {
            throw new ApplicationRuntimeException("profile.name.already.exists")
        }
        profile.description = params.description;
        profile.rulePrecedence = params.rulePrecedence ?: DomainConstants.SHIPPING_PROFILE_RULE_PRECEDENCE.RULES_WITH_HIGHER_PRIORITY;
        List ids = params.list("shippingRules");
        profile.shippingRules = params.id? profile.shippingRules : ids.collect { ShippingRule.proxy(it) };
        profile.save();
        if(profile.hasErrors()) {
           return null
        }
        if(params.id) {
            AppEventManager.fire("shipping-profile-update", [params.id])
        } else {
            AppEventManager.fire("shipping-profile-create")
        }
        return profile
    }

    Long copyProfile(params) {
        ShippingProfile shippingProfile = ShippingProfile.get(params.long("profileId"))
        ShippingProfile newProfile
        if(params.copyType == DomainConstants.COPY_SHIPPING_PROFILE_OPTIONS.USE_RULE_AND_CONFIG) {
            newProfile = DomainUtil.clone(shippingProfile, ["rulePrecedence", "name", "description"], ["shippingRules"])
        } else {
            newProfile = DomainUtil.clone(shippingProfile, ["rulePrecedence", "name", "description"])
            for (ShippingRule rule : newProfile.shippingRules) {
                rule.name = commonService.getCopyNameForDomain(rule)
                if(rule.shippingPolicy) {
                    rule.shippingPolicy.name = commonService.getCopyNameForDomain(rule.shippingPolicy)
                    rule.shippingPolicy.save()
                }
                rule.save()
            }
        }
        newProfile.name = params.name
        newProfile.description = params.description
        newProfile.rulePrecedence = params.rulePrecedence
        newProfile.save()
        if(shippingProfile.hasErrors()) {
            return null
        }
        return newProfile.id;
    }

    Boolean detachShippingRule(Map params) {
        ShippingProfile profile = ShippingProfile.get(params.profileId)
        if(profile) {
            ShippingRule rule = ShippingRule.get(params.ruleId)
            if(!rule) {
                throw new ApplicationRuntimeException("rule.not.exist")
            }
            profile.removeFromShippingRules(rule)
            profile.save()
            if(!profile.hasErrors()) {
                return true
            }
        }
        return false
    }

    Boolean detachRate(Map params) {
        ShippingRule rule = ShippingRule.get(params.ruleId)
        if(rule) {
            rule.shippingPolicy = null
            rule.save()
            return true
        }
        return false
    }

    Boolean detachZone(Map params) {
        ShippingRule rule = ShippingRule.get(params.rule_id)
        if(rule) {
            rule.removeFromZoneList(Zone.get(params.long("zone_id")))
            rule.save()
            if(!rule.hasErrors()) {
                return true
            }
        }
        return false
    }

    String getCopyName(def object) {
        Class clazz = object.getClass()
        String name = "Copy of - " + object.name
        def total = clazz.findAllByNameLike(name + "%").size()
        return total > 0 ? name + "-${total+1}" : name
    }

    def removeShippingClass(Long id) {
        ShippingClass shippingClass = ShippingClass.proxy(id)
    }

    def initImport(Map params, Workbook ref) {
        def methodKeys = NamedConstants.SHIPPING_POLICY_TYPE_MESSAGE_KEYS.collectEntries {[(it.key) : app.message(code: it.value).toString()]}
        MultiLoggerTask task = new MultiLoggerTask("Rule Import")
        task.detail_url = app.relativeBaseUrl() + "taskCommon/progressView";
        task.detail_status_url = app.relativeBaseUrl() + "taskCommon/progressStatus";
        task.detail_viewer = "app.tabs.setting.import_status_viewer"
        task.meta = [
                totalZoneRecord : 0,
                zoneComplete : 0,
                zoneSuccessCount : 0,
                zoneWarningCount : 0,
                zoneErrorCount : 0,
                totalRateRecord : 0,
                rateComplete : 0,
                rateSuccessCount : 0,
                rateWarningCount : 0,
                rateErrorCount : 0,
                totalRuleRecord : 0,
                ruleComplete : 0,
                ruleSuccessCount : 0,
                ruleWarningCount : 0,
                ruleErrorCount : 0,
                successCount : 0,
                warningCount : 0,
                errorCount : 0,
        ]

        Sheet zoneSheet = ref.getSheet(params.zoneWorkSheet)
        Sheet rateSheet = ref.getSheet(params.rateWorkSheet)
        Sheet ruleSheet = ref.getSheet(params.ruleWorkSheet)

        List<ZoneData> zoneDataList = zoneSheet ? getZoneDataList(zoneSheet) : []
        List<ShippingRateData> rateDataList = rateSheet ? getRateDataList(rateSheet) : []
        List<ShippingRuleData> ruleDataList = ruleSheet ? getRuleDataList(ruleSheet) : []
        task.totalRecord =  zoneDataList.size() + rateDataList.size() + ruleDataList.size();
        task.meta.totalZoneRecord =  zoneDataList.size();
        task.meta.totalRuleRecord =  ruleDataList.size();
        task.meta.totalRateRecord =  rateDataList.size();
        task.onComplete {
            taskService.saveLogToSession(task);
            taskService.saveMultiLoggerTaskLog(task,  ["Status", "Name", "Remark"]);
            Thread.sleep(1000000);
        }

        task.onError {Throwable t ->
            taskService.saveLogToSession(task);
            taskService.saveMultiLoggerTaskLog(task,  ["Status", "Name", "Remark"]);
            Thread.sleep(1000000);
        }

        task.async {
            zone {
                saveZone(task, zoneDataList, params)
            }
            rate {
                List importList = []
                saveRate(task, rateDataList, params, importList, methodKeys)
            }
            rule {
                saveRule(task, ruleDataList, params)
            }
        }
        return task
    }

    List getRateDataList(def sheet) {
        Map fieldMapping = getHeaderMapping(sheet, NamedConstants.RATE_IMPORT_FIELDS);
        List<ShippingRateData> rates = [];
        Iterator<Row> rowIterator = sheet.rowIterator();
        Integer i = 0;
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            if(i == 0) {
                i++;
                continue;
            }
            ShippingRateData data = new ShippingRateData();
            NamedConstants.RATE_IMPORT_FIELDS.each {
                Integer index = fieldMapping[app.message(code: it.value).toString()]
                data."$it.key" = index != -1 ? commonImportService.getCellValue(row, index) : null
            }
            rates.add(data)
            i++;
        }
        return rates;
    }

    List getRuleDataList(def sheet) {
        Map fieldMapping = getHeaderMapping(sheet, NamedConstants.RULE_IMPORT_FIELDS);
        List<ShippingRuleData> rules = [];
        Iterator<Row> rowIterator = sheet.rowIterator();
        Integer i = 0;
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            if(i == 0) {
                i++;
                continue;
            }
            ShippingRuleData data = new ShippingRuleData();
            NamedConstants.RULE_IMPORT_FIELDS.each {
                Integer index = fieldMapping[app.message(code: it.value).toString()]
                data."$it.key" = index != -1 ? commonImportService.getCellValue(row, index) : null
            }
            rules.add(data)
            i++;
        }
        return rules;
    }

    List getZoneDataList(def sheet) {
        Map fieldMapping = getHeaderMapping(sheet, NamedConstants.ZONE_IMPORT_FIELDS);
        List<ZoneData> zones = [];
        Iterator<Row> rowIterator = sheet.rowIterator();
        Integer i = 0;
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            if(i == 0) {
                i++;
                continue;
            }
            ZoneData data = new ZoneData();
            NamedConstants.ZONE_IMPORT_FIELDS.each {
                Integer index = fieldMapping[app.message(code: it.value).toString()]
                data."$it.key" = index != -1 ? commonImportService.getCellValue(row, index) : null
            }
            zones.add(data)
            i++;
        }
        return zones;
    }

    Map getHeaderMapping(Sheet sheet, def fields) {
        Map mapping = [:];
        fields.values().each {
            mapping[app.message(code: it).toString()] = -1
        }
        Row header = sheet.getAt(0);
        Iterator<Cell> iterator = header.iterator();
        Integer i = 0;
        while (iterator.hasNext()) {
            String value = iterator.next().getStringCellValue().trim();
            if (mapping.containsKey(value)) {
                mapping[value] = i
            }
            i++;
        }
        return mapping;
    }


    def saveRate(Task task, List<ShippingRateData> dataList, def params, List importList, def methodKeys) {
        dataList.each { ShippingRateData rateData ->
            ShippingPolicy.withNewTransaction {status ->
                try {
                    if(!rateData.name) {
                        throw new Exception("name.not.found")
                    }
                    if (rateData.method?.trim()?.size() == 0) {
                        throw new Exception("valid.method.required")
                    }
                    ShippingPolicy rate = ShippingPolicy.findByName(rateData.name)
                    if(!rate || (rate && params.rateOverwrite == "1")) {
                        if(!rate) {
                            rate = new ShippingPolicy()
                        }
                        rate.name = rateData.name
                        if (!importList.contains(rate.name)) {
                            rate.conditions.each {
                                it.shippingPolicy = null
                                it.delete()
                            }
                            rate.conditions = null
                        }
                        rate.policyType = methodKeys.find {it.value == rateData.method?.trim()}?.key
                        if(!rate.policyType){
                            throw new Exception("valid.method.required")
                        }
                        def isByWeight =  rate.policyType == DomainConstants.SHIPPING_POLICY_TYPE.SHIP_BY_WEIGHT
                        String unitWeight = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "unit_weight")
                        if(rate.policyType in [DomainConstants.SHIPPING_POLICY_TYPE.FLAT_RATE, DomainConstants.SHIPPING_POLICY_TYPE.FREE_SHIPPING] ) {
                            rateData.singleHandlingCost = rateData.handlingCost
                            rateData.singleShippingCost = rateData.shippingCost
                            if(rate.policyType == DomainConstants.SHIPPING_POLICY_TYPE.FLAT_RATE && !rateData.shippingCost) {
                                throw new Exception("shipping.cost.required")
                            }
                        } else {
                            if(rate.policyType == DomainConstants.SHIPPING_POLICY_TYPE.SHIP_BY_WEIGHT && rateData.packetWeight?.toInteger() < 0) {
                                throw new Exception("packet.weight.required")
                            }
                            if(!rateData.from) {
                                throw new Exception("min.value.not.valid")
                            }
                            if(!rateData.to) {
                                throw new Exception("max.value.not.valid")
                            }
                            if(rateData.from > rateData.to) {
                                throw new Exception("range.not.valid")
                            }
                            if(!rateData.shippingCost) {
                                throw new Exception("shipping.cost.required")
                            }
                        }
                        addConditionToPolicy(rate, rateData)
                        boolean additional = rateData.isAdditional?.toBoolean()
                        rate.isAdditional = additional
                        rate.isCumulative = rateData.isCumulative?.toBoolean()
                        if(additional) {
                            if(!rateData.additionalAmount) {
                                throw new Exception("for.each.additional.amount.required")
                            }
                            if(!rateData.additionalCost) {
                                throw new Exception("additional.cost.required")
                            }
                            rate.additionalAmount = isByWeight ? MassConversions.convertMassToSI(unitWeight, Double.parseDouble(rateData.additionalAmount)).doubleValue() :
                                    Double.parseDouble(rateData.additionalAmount);
                            rate.additionalCost = Double.parseDouble(rateData.additionalCost)
                        }
                        HookManager.hook("before-shipping-policy-save", rate)
                        rate.save();
                        importList.add(rate.name)
                    }
                    task.meta.rateProgress = taskService.countProgress(task.meta.totalRateRecord, ++task.meta.rateComplete as Integer)
                    task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                    task.taskLogger.success(rateData.name, "shipping.rate.save.success")
                    task.meta.rateSuccessCount++
                    task.meta.successCount++
                } catch (Exception e) {
                    task.meta.rateProgress = taskService.countProgress(task.meta.totalRateRecord, ++task.meta.rateComplete as Integer)
                    task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                    task.taskLogger.error(rateData.name, e.getMessage())
                    task.meta.rateErrorCount++
                    task.meta.errorCount++;
                    status.setRollbackOnly();
                    e.printStackTrace()
                }
            }
        }
    }

    def saveRule(Task task, List<ShippingRuleData> dataList, def params) {
        dataList.each { ShippingRuleData ruleData ->
            ShippingRule.withNewTransaction {status ->
                try {
                    if(!ruleData.name) {
                        throw new Exception("name.not.found")
                    }
                    ShippingRule rule = ShippingRule.findByName(ruleData.name)
                    if(!rule || (rule && params.ruleOverwrite == "1")) {
                        if(!rule) {
                            rule = new ShippingRule()
                        }
                        rule.name = ruleData.name
                        rule.description = ruleData.description
                        ShippingPolicy rate = ShippingPolicy.findByName(ruleData.shippingPolicy)
                        if(!rate) {
                            //todo add warning
                        }
                        rule.shippingPolicy = rate
                        rule.zoneList = null
                        ruleData.zoneList?.split(",").each {
                            Zone zone = Zone.findByName(it)
                            zone ? rule.addToZoneList(zone) : null
                        }
                        Boolean classEnabled = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.SHIPPING, "enable_shipping_class")?.toBoolean();
                        if(classEnabled) {
                            ShippingClass shippingClass
                            if (ruleData.shippingClass?.trim()) {
                                shippingClass = ShippingClass.findByName(ruleData.shippingClass)
                                if(!shippingClass) {
                                    shippingClass = new ShippingClass(name: ruleData.shippingClass, description: ruleData.shippingClass).save()
                                }
                            } else {
                                def defaultClass = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.SHIPPING, "default_shipping_class")
                                shippingClass = defaultClass? ShippingClass.findById(defaultClass?.toLong()) : null
                            }

                            rule.shippingClass = shippingClass ?: null
                        }
                        rule.save()
                    }
                    task.meta.ruleProgress = taskService.countProgress(task.meta.totalRuleRecord, ++task.meta.ruleComplete as Integer)
                    task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                    task.taskLogger.success(ruleData.name, "shipping.rule.save.success")
                    task.meta.ruleSuccessCount++
                    task.meta.successCount++
                } catch (Exception e) {
                    task.meta.ruleProgress = taskService.countProgress(task.meta.totalRuleRecord, ++task.meta.ruleComplete as Integer)
                    task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                    task.taskLogger.error(ruleData.name, e.getMessage())
                    task.meta.ruleErrorCount++
                    task.meta.errorCount++;
                    status.setRollbackOnly();
                    e.printStackTrace()
                }
            }
        }
    }

    def saveZone(Task task, List<ZoneData> dataList, def params) {
        dataList.each { ZoneData zoneData ->
            Zone.withNewTransaction {status ->
                try {
                    if(!zoneData.name) {
                        throw new Exception("name.not.found")
                    }
                    Zone zone = Zone.findByName(zoneData.name)
                    if(!zone || (zone && params.zoneOverwrite == "1")) {
                        if(!zone) {
                            zone = new Zone()
                        } else {
                            zone.countries = null
                            zone.states = null
                            zone.postCodes = null
                        }
                        zone.name = zoneData.name
                        zoneData.countries?.split(",").each {
                            Country country = Country.findByName(it.trim())
                            country ? zone.addToCountries(country) : null
                        }
                        if(!zone.countries?.size()) {
                            throw new Exception("valid.country.required")
                        } else if(zone.countries.size() == 1) {
                            zoneData.states?.split(",").each {
                                def name = it.trim()
                                State state = State.findByNameAndCountry(name, zone.countries.first()) ?: State.findByCodeAndCountry(name, zone.countries.first())
                                state ? zone.addToStates(state) : null
                            }
                            if(!zone.states?.size() ) {
                                task.taskLogger.warning(zone.name, "no.valid.state.found")
                                task.meta.zoneWarningCount++
                                task.meta.warningCount++
                            }
                            if(!(zone.states?.size() > 1)) {
                                zoneData.postCodes?.split(",").each {
                                    zone.addToPostCodes(it)
                                }
                            }
                        }

                        zone.save()
                    }
                    task.meta.zoneProgress = taskService.countProgress(task.meta.totalZoneRecord, ++task.meta.zoneComplete as Integer)
                    task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                    task.taskLogger.success(zoneData.name, "zone.save.success")
                    task.meta.zoneSuccessCount++
                    task.meta.successCount++
                } catch (Exception e) {
                    task.meta.zoneProgress = taskService.countProgress(task.meta.totalZoneRecord, ++task.meta.zoneComplete as Integer)
                    task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                    task.taskLogger.error(zoneData.name, e.getMessage())
                    task.meta.zoneErrorCount++
                    task.meta.errorCount++;
                    status.setRollbackOnly();
                    e.printStackTrace()
                }
            }
        }
    }

    def export(OutputStream stream) {
        XSSFWorkbook ruleWorkBook = new XSSFWorkbook()
        Sheet zoneSheet = ruleWorkBook.createSheet("Zone")
        Sheet rateSheet = ruleWorkBook.createSheet("Rate")
        Sheet ruleSheet = ruleWorkBook.createSheet("Rule")
        addHeaderRow(zoneSheet, NamedConstants.ZONE_IMPORT_FIELDS)
        addHeaderRow(rateSheet, NamedConstants.RATE_IMPORT_FIELDS)
        addHeaderRow(ruleSheet, NamedConstants.RULE_IMPORT_FIELDS)
        List<Zone> zoneList = Zone.all?.sort{-it.id}
        List<ShippingPolicy> rateList = ShippingPolicy.all?.sort{-it.id}
        List<ShippingRule> ruleList = ShippingRule.all?.sort{-it.id}
        addRow(zoneSheet, NamedConstants.ZONE_IMPORT_FIELDS, zoneList)
        addRateRow(rateSheet, NamedConstants.RATE_IMPORT_FIELDS, rateList)
        addRow(ruleSheet, NamedConstants.RULE_IMPORT_FIELDS, ruleList)
        ruleWorkBook.write(stream)
        stream.close()

    }

    def addRow(Sheet sheet, Map headerMap, List dataList) {
        int rowIndex = 1;
        dataList.each { def domain ->
            Row row = sheet.createRow(rowIndex)
            headerMap.eachWithIndex { def entry, int idx ->
                Cell cell = row.createCell(idx)
                grails.core.GrailsApplication grailsApp = grails.util.Holders.grailsApplication;
                if(domain[entry.key] instanceof java.util.Collection) {
                    if(domain[entry.key]?.size() > 0) {
                        if(domain[entry.key]?.first() instanceof String) {
                            cell.setCellValue(domain[entry.key]?.collect{it}?.join(","))
                        } else {
                            cell.setCellValue(domain[entry.key]?.collect{it.name}?.join(","))
                        }
                    }
                } else if(domain[entry.key] && grailsApp.isDomainClass(domain[entry.key]?.class)) {
                    cell.setCellValue(domain[entry.key]?.name?.toString())
                } else {
                    cell.setCellValue(domain[entry.key]?.toString())
                }
            }
            rowIndex++
        }
    }

    def addRateRow(Sheet sheet, Map headerMap, List dataList) {
        int rowIndex = 1;
        dataList.each { ShippingPolicy rate ->
            rate.conditions.each { ShippingCondition condition ->
                Row row = sheet.createRow(rowIndex)
                headerMap.eachWithIndex { def entry, int idx ->
                    Cell cell = row.createCell(idx)
                    switch (entry.key) {
                        case "shippingCost" :
                        case "handlingCost" :
                        case "packetWeight" :
                            cell.setCellValue(condition[entry.key])
                            break;
                        case "from" :
                            cell.setCellValue(condition.fromAmount)
                            break
                        case "to" :
                            cell.setCellValue(condition.toAmount)
                            break
                        case "method" :
                            cell.setCellValue(app.message(code: (NamedConstants.SHIPPING_POLICY_TYPE_MESSAGE_KEYS[condition.shippingPolicy.policyType])).toString())
                            break
                        default:
                            cell.setCellValue(condition.shippingPolicy[entry.key])
                            break;
                    }
                }
                rowIndex++
            }

        }
    }

    def addHeaderRow(Sheet sheet, Map headerMap) {
        Row row = sheet.createRow(0)
        headerMap.eachWithIndex { def entry, int idx ->
            Cell cell = row.createCell(idx)
            cell.setCellValue(app.message(code: (entry.value)).toString())
        }
    }
}
