package com.webcommander.webcommerce

import com.webcommander.admin.Zone
import com.webcommander.annotations.Initializable
import com.webcommander.common.CommonService
import com.webcommander.events.AppEventManager
import com.webcommander.manager.HookManager
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.util.TrashUtil
import grails.gorm.transactions.Transactional
import grails.web.databinding.DataBindingUtils
import grails.web.servlet.mvc.GrailsParameterMap
import org.hibernate.SessionFactory

@Initializable
class TaxService {
    CommonService commonService
    SessionFactory sessionFactory

    static void initialize() {
        HookManager.register("taxRule-delete-veto", { response, id ->
            Integer count = TaxProfile.createCriteria().count {
                rules{
                    id == id
                }
            }
            if(count) {
                response."tax.profile" = count
            }
            return response
        })

        HookManager.register("zone-delete-veto") {response, id ->
            Integer count = TaxRule.where {
                zones {
                    eq("id", id)
                }
            }.count();
            if (count) {
                response."tax.rule" = count;
            }
            return response;
        }
        HookManager.register("zone-delete-veto-list") { response, id ->
            List rules = TaxRule.createCriteria().list {
                zones {
                    eq("id", id)
                }
            }
            if(rules.size()) {
                response."tax.rule" = rules.collect { it.name }
            }
            return response;
        }
        HookManager.register("taxCode-delete-veto", { response, id ->
            Integer count = TaxRule.where {
                code.id == id
            }.count();
            if(count) {
                response."tax.rule" = count
            }
            return response;
        })
        HookManager.register("taxCode-delete-veto-list", { response, id ->
            List rules = TaxRule.createCriteria().list {
                eq("code.id", id)
            }
            if(rules.size()) {
                response."tax.code" = rules.collect { it.name }
            }
        })
    }

    private taxCodeClosure(Map params) {
        return {
            if (params.searchText) {
                or {
                    ilike("name", "%${params.searchText.trim().encodeAsLikeText()}%")
                    ilike("label", "%${params.searchText.trim().encodeAsLikeText()}%")
                }
            }

            if (params.isDefault != null) {
                eq("isDefault", params.isDefault)
            }
        }
    }

    private taxRuleClosure(Map params) {
        return {
            if (params.searchText) {
                ilike("name", "%${params.searchText.trim().encodeAsLikeText()}%")
            }
        }
    }

    private taxProfileClosure(Map params) {
        return {
            if (params.searchText) {
                ilike("name", "%${params.searchText.trim().encodeAsLikeText()}%")
            }
            if (params.isDefault != null) {
                eq("isDefault", params.isDefault)
            }
        }
    }

    Integer getTaxCodeCount(Map params) {
        return TaxCode.createCriteria().count {
            and taxCodeClosure(params)
        }
    }

    List<TaxCode> getTaxCodes(Map params) {
        def listMap = [max: params.max, offset: params.offset]
        return TaxCode.createCriteria().list(listMap) {
            and taxCodeClosure(params)
            order(params.sort ?: "name", params.dir ?: "asc");
        }
    }
    Integer getTaxRulesCount(Map params) {
        TaxRule.createCriteria().count(taxRuleClosure(params))
    }

    List<TaxRule> getTaxRules(Map params) {
        TaxRule.createCriteria().list(max: params.max, offset: params.offset) {
            and taxRuleClosure(params)
            order(params.sort ?: "name", params.dir ?: "asc")
        }
    }

    List<TaxProfile> getTaxProfiles(Map params) {
        TaxProfile.createCriteria().list(offset: params.offset, max: params.max) {
            and taxProfileClosure(params)
            order(params.sort ?: "id", params.dir ?: "desc");
        }
    }

    @Transactional
    Long saveTaxProfile(Map params) {
        TaxProfile profile = params.id ? TaxProfile.get(params.id) : new TaxProfile();
        if (!profile) {
            return false
        }
        DataBindingUtils.bindObjectToInstance(profile, params.subMap(["name", "description"]))
        if(!commonService.isUnique(profile, "name")){
            throw new ApplicationRuntimeException("tax.profile.name.in.use", "alert")
        }
        if(params.containsKey("taxRule")) {
            List ruleIds = params.list("taxRule");
            profile.rules = [];
            ruleIds?.each{
                profile.addToRules(TaxRule.proxy(it.toLong()));
            }
        }
        if(params["tax-rule"]) {
            List ids = params.list("tax-rule");
            profile.rules = []
            ids.each {
                profile.addToRules(TaxRule.proxy(it.toLong()));
            }
        }
        if (profile.id) {
            profile.merge()
        } else {
            profile.save()
        }
        if(!profile.hasErrors()) {
            if(params.id) {
                AppEventManager.fire("tax-profile-update", [params.id])
            }
            return profile.id
        }
        return 0
    }

    @Transactional
    boolean saveTaxRule(Map params){
        TaxRule rule = params."ruleId" ? TaxRule.get(params."ruleId") : new TaxRule();
        if(!rule){
            return false
        }
        DataBindingUtils.bindObjectToInstance(rule, params.subMap(["name", "description"]));
        if(!commonService.isUnique(rule, "name")){
            throw new ApplicationRuntimeException("tax.rule.name.in.use", "alert")
        }
        rule.validate()
        if(rule.id){
            rule.merge()
        } else {
            rule.save()
        }
        if(!rule.hasErrors()) {
            if(params."ruleId") {
                AppEventManager.fire("tax-rule-update", [params.id])
            } else if(params.profileId) {
                TaxProfile profile = TaxProfile.get(params.profileId)
                profile ? profile.addToRules(rule) : null
            }
            return true
        }
        return false
    }

    @Transactional
    TaxRule addCodeToRule(def params) {
        TaxRule rule = TaxRule.get(params.long("ruleId"))
        TaxCode code = TaxCode.get(params.long("codeId"))
        rule.code = code
        rule.save()
        if(!rule.hasErrors())
            return rule
        else null
    }

    @Transactional
    TaxProfile addRuleToProfile(def params) {
        TaxRule rule = TaxRule.proxy(params.long("ruleId"))
        TaxProfile profile = TaxProfile.get(params.long("profileId"))
        profile.addToRules(rule)
        profile.save()
        if(!profile.hasErrors())
            return profile
        else null
    }

    @Transactional
    Boolean detachCode(Map params) {
        TaxRule rule = TaxRule.get(params.ruleId)
        if(rule) {
            rule.code = null
            rule.save()
            return true
        }
        return false
    }

    @Transactional
    TaxRule addZoneToRule(def params) {
        TaxRule rule = TaxRule.get(params.long("ruleId"))
        Zone zone = Zone.get(params.long("zoneId"))
        rule.addToZones(zone)
        rule.save()
        if(!rule.hasErrors())
            return rule
        else null
    }

    @Transactional
    Boolean detachZone(Map params) {
        TaxRule rule = TaxRule.get(params.rule_id)
        if(rule) {
            rule.removeFromZones(Zone.get(params.long("zone_id")))
            rule.save()
            if(!rule.hasErrors()) {
                return true
            }
        }
        return false
    }

    @Transactional
    TaxCode saveTaxCode(Map params) {
        TaxCode code = params.id ? TaxCode.get(params.id) : new TaxCode()
        if (!code) {
            return null
        }
        DataBindingUtils.bindObjectToInstance(code, params.subMap(["name", "description", "rate", "label"]));
        if(!commonService.isUnique(code, "name")){
            throw new ApplicationRuntimeException("tax.code.name.in.use", "alert")
        }
        code.save()
        if(!code.hasErrors()) {
            if(params.id) {
                AppEventManager.fire("tax-code-update", [params.id])
            }
            if(params.ruleId) {
                TaxRule taxRule = TaxRule.get(params.ruleId)
                taxRule?.code = code
            }
            return code
        }
        return null
    }

    @Transactional
    boolean deleteTaxProfile(Long id, String at1, String at2){
        TrashUtil.preProcessFinalDelete("taxProfile", id, at2 != null, at1 != null)
        AppEventManager.fire("before-taxProfile-delete", [id]);
        TaxProfile profile = TaxProfile.proxy(id);
        profile.delete()
        AppEventManager.fire("taxProfile-delete", [id]);
        return true;
    }

    @Transactional
    boolean deleteTaxCode(Long id, String at1, String at2) {
        TrashUtil.preProcessFinalDelete("taxCode", id, at2 != null, at1 != null)
        AppEventManager.fire("before-taxCode-delete", [id, at1])
        TaxCode taxCode = TaxCode.get(id)
        taxCode.delete();
        AppEventManager.fire("taxCode-delete", [id])
        return true;
    }

    @Transactional
    boolean deleteRule(Long id){
        TaxRule rule = TaxRule.proxy(id)
        List<TaxProfile> profiles = TaxProfile.where {
            rules {
                id == id
            }
        }.list()
        profiles*.removeFromRules(rule);
        profiles*.merge(flush: true);
        rule.delete()
        return true
    }

    @Transactional
    TaxRule updateTaxRule(GrailsParameterMap params) {
        TaxRule rule = params.codeId ? new TaxRule(name: "Tax Rule") : TaxRule.proxy(params.long("id"));
        if (params.codeId) {
            String name = commonService.getNameForDomain(rule);
            rule.name = name;
            rule.code = TaxCode.proxy(params.long("codeId"));
        }
        if (params.zoneId) {
            rule.zone = Zone.proxy(params.long("zoneId"));
        }
        rule.save();
        if(!rule.hasErrors()) {
            return rule
        }
        return rule;
    }

    @Transactional
    TaxRule saveRule(GrailsParameterMap params) {
        TaxProfile profile = (params.codeId == null) ? TaxProfile.proxy(params.long("id")) : null;
        TaxRule rule = TaxRule.proxy(params.long("ruleId"));
        if(profile && params.ruleId) {
            profile.addToRules(rule);
            profile.save();
        }
        return rule;
    }

    @Transactional
    Boolean detachRule(GrailsParameterMap params) {
        TaxProfile profile = TaxProfile.proxy(params.long("id"));
        TaxRule rule = TaxRule.proxy(params.long("ruleId"));
        if(profile.rules?.remove(rule)) {
            profile.save()
            return true;
        }
        return false;
    }

    @Transactional
    void cleanTaxConfig() {
        Iterator<TaxProfile> iterator = TaxProfile.list().iterator()
        while (iterator.hasNext()) {
            TaxProfile profile = iterator.next()
            deleteTaxProfile(profile.id, "include", "true")
        }
        sessionFactory.currentSession.flush()
        TaxRule.executeUpdate("DELETE from TaxRule")
        TaxCode.executeUpdate("DELETE from TaxCode")
    }
}
