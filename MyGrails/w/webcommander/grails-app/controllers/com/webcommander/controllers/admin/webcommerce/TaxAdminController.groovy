package com.webcommander.controllers.admin.webcommerce

import com.webcommander.admin.AdministrationService
import com.webcommander.admin.Zone
import com.webcommander.admin.ZoneService
import com.webcommander.authentication.annotations.Restriction
import com.webcommander.common.CommonService
import com.webcommander.constants.DomainConstants
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.throwables.AttachmentExistanceException
import com.webcommander.util.AppUtil
import com.webcommander.validator.TaxZoneSelectValidator
import com.webcommander.webcommerce.*
import grails.converters.JSON
import org.springframework.beans.factory.annotation.Autowired

class TaxAdminController {
    CommonService commonService
    AdministrationService administrationService
    TaxService taxService
    ZoneService zoneService

    @Autowired
    TaxZoneSelectValidator zoneSelectValidator

    @Restriction(permission = "tax.view.list")
    def loadAppView() {
        render(view: "/admin/tax/appView");
    }

    def loadLeftPanel() {
        params.isDefault = false
        List<TaxProfile> profiles = taxService.getTaxProfiles(params)
        Long selected = params.long("profileId");
        TaxProfile profile = selected ? TaxProfile.get(selected) : profiles ? profiles.first() : null
        AppUtil.session.selectedTaxProfileId = profile ? profile.id : null
        render(view: "/admin/tax/leftPanel", model: [
                profiles: profiles, profile: profile,
                selectedRuleId: params.long("selected")
        ]);
    }

    def explorerView() {
        TaxRule rule = TaxRule.get(params.id)
        render(view: "/admin/tax/explorerView", model: [rule: rule]);
    }

    def addRulePopup() {
        render view: "/admin/shipping/rule/createPopup"
    }

    def detachRule() {
        Boolean remove = taxService.detachRule(params);
        if(remove) {
            render([status: "success", message: g.message(code: "tax.rule.remove.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "tax.rule.could.not.remove")] as JSON)
        }
    }

    def selectCode() {
        if (!params.codeId || !params.ruleId)
            throw new ApplicationRuntimeException("no.rule.selected")
        TaxRule rule = taxService.addCodeToRule(params)
        if (rule) {
            render([status: "success", message: g.message(code: "tax.code.save.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "tax.code.could.not.save")] as JSON)
        }
    }

    def addRuleToProfile() {
        if (!params.profileId || !params.ruleId)
            throw new ApplicationRuntimeException("no.rule.selected")
        TaxProfile profile = taxService.addRuleToProfile(params)
        if (profile) {
            render([status: "success", message: g.message(code: "tax.rule.save.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "tax.rule.could.not.save")] as JSON)
        }
    }

    def detachCode() {
        if (taxService.detachCode(params)) {
            render([status: "success", message: g.message(code: "shipping.rate.delete.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "shipping.rate.delete.failure")] as JSON)
        }
    }


    def profileInfoEdit() {
        TaxProfile taxProfile = params.id ? TaxProfile.get(params.id) : new TaxProfile()
        render view: "/admin/tax/new/profile/infoEdit", model: [profile: taxProfile]
    }

    def deleteProfile() throws Throwable {
        Long id = params.long("id");
        try {
            if (taxService.deleteTaxProfile(id, params.at1_reply, params.at2_reply)) {
                render([status: "success", message: g.message(code: "tax.profile.delete.success")] as JSON)
            } else {
                render([status: "error", message: g.message(code: "tax.profile.could.not.delete")] as JSON)
            }
        } catch(AttachmentExistanceException att) {
            render([status: "error", error_code: "attachment.exists", attachments: att.attachmentInfo] as JSON)
        }
    }

    def ruleAppView () {
        params.max = params.max ?: "10";
        params.offset = params.offset ?: "0";
        Integer count = taxService.getTaxRulesCount(params)
        List<TaxRule> ruleList = taxService.getTaxRules(params)
        render(view: "/admin/tax/new/rule/appView", model: [ruleList: ruleList, count: count])
    }

    def codeAppView () {
        params.max = params.max ?: "10";
        params.offset = params.offset ?: "0";
        params.isDefault = (AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.TAX, "configuration_type") == DomainConstants.TAX_CONFIGURATION_TYPE.MANUAL) ? false : true
        Integer count = taxService.getTaxCodeCount(params)
        List<TaxCode> codes = taxService.getTaxCodes(params)
        render(view: "/admin/tax/new/rate/appView", model: [codes: codes, count: count])
    }

    def addCode() {
        params.isDefault = false
        List<TaxCode> codes = taxService.getTaxCodes(params)
        render(view: "/admin/tax/new/rate/addCode", model: [codes: codes])
    }

    def useExistingRulePopup() {
        params.max = params.max ?: "5";
        Integer count = taxService.getTaxRulesCount(params)
        List<TaxRule> ruleList = commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.offset = offset
            return taxService.getTaxRules(params)
        }
        render view: "/admin/tax/new/rule/useExistingRule", model: [ruleList: ruleList, count: count]
    }

    def selectedRule() {
        def selectedRule
        if(params."ruleId") {
            selectedRule = TaxRule.get(params.long("ruleId"))
        }
        else {
            throw new ApplicationRuntimeException("no.rule.selected")
        }
        render(view: "/admin/tax/new/rule/selectedRule", model: [ruleList: [selectedRule]])
    }

    def ruleInfoEdit() {
        TaxRule taxRule = params.ruleId? TaxRule.get(params.long("ruleId")) : null
        render view: "/admin/tax/new/rule/infoEdit", model: [profileId: params.profileId, taxRule: taxRule]
    }

    def deleteRule() {
        if (taxService.deleteRule(params.long("id"))) {
            render([status: "success", message: g.message(code: "tax.rule.delete.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "tax.rule.could.not.delete")] as JSON)
        }
    }

    def addZonePop() {
        TaxRule rule = TaxRule.get(params.ruleId)
        List zoneIds = rule?.zones?.id
        List<Zone> zones = zoneService.getZones([
            searchText: params.searchText,
            excludeIds: zoneIds,
            isDefault: false,
            max: 5
        ])

        // if no zones found then suggest admin to add "REST_OF_THE_WORLD" zone
        if (!zones) {
            zones.add( Zone.findByName("REST_OF_THE_WORLD") );
        }

        render(view: "/admin/zone/addZonePop", model: [zones: zones])
    }

    def selectZone() {
        if (!params.zoneId || !params.ruleId)
            throw new ApplicationRuntimeException("no.rule.selected")

        params.selectedTaxProfileId = AppUtil.session.selectedTaxProfileId
        if (!zoneSelectValidator.validate(params)) {
            render([status: "success", message: g.message(code: "tax.profile.zone.already.added")] as JSON)
            return
        }

        TaxRule rule = taxService.addZoneToRule(params)

        if (rule) {
            render([status: "success", message: g.message(code: "tax.rule.save.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "tax.rule.save.failure")] as JSON)
        }
    }

    def detachZone() {
        Boolean deleted = taxService.detachZone(params)
        if (deleted) {
            render([status: "success", message: g.message(code: "shipping.zone.delete.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "shipping.zone.delete.failure")] as JSON)
        }
    }

    def editTaxCode() {
        TaxCode code = params.id ? TaxCode.get(params.id) : new TaxCode();
        render(view: "/admin/tax/new/rate/edit", model: [code: code]);
    }


    def saveTaxCode() {
        TaxCode code = taxService.saveTaxCode(params);
        if (code) {
            render([status: "success", message: g.message(code: "tax.code.save.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "tax.code.could.not.save")] as JSON)
        }
    }

    def isProfileUnique() {
        if (commonService.isUnique(TaxProfile, params)) {
            render([status: "success", message: g.message(code: "provided.field.available", args: [params.field, params.value])] as JSON)
        } else {
            render([status: "error", message: g.message(code: "provided.field.value.exists", args: [params.field, params.value])] as JSON)
        }
    }

    def isCodeUnique() {
        if (commonService.isUnique(TaxCode, params)) {
            render([status: "success", message: g.message(code: "provided.field.available", args: [params.field, params.value])] as JSON)
        } else {
            render([status: "error", message: g.message(code: "provided.field.value.exists", args: [params.field, params.value])] as JSON)
        }
    }

    def isRuleUnique() {
        if (commonService.isUnique(TaxRule, params)) {
            render([status: "success", message: g.message(code: "provided.field.available", args: [params.field, params.value])] as JSON)
        } else {
            render([status: "error", message: g.message(code: "provided.field.value.exists", args: [params.field, params.value])] as JSON)
        }
    }
    /* Old codes*/
    @Restriction(permission = "tax.view.list")
    def loadTaxProfiles() {
        List<TaxProfile> profiles = TaxProfile.all?.sort {-it.id};
        render(view: "/admin/tax/profile/appView", model: [profiles: profiles, selected: profiles? profiles.first().id : 0]);
    }

    def loadTaxProfilesLeftPanel() {
        List<TaxProfile> profileList = TaxProfile.all?.sort {-it.id};
        Long selected = params.long("selected");
        selected = selected ?: (profileList ? profileList.first().id : 0)
        render(view: "/admin/tax/profile/leftPanel", model: [profiles: profileList, selected: selected]);
    }

    def loadTaxProfilesRightPanel() {
        TaxProfile profile = TaxProfile.get(params.long("selected"))
        def profiles = TaxProfile.all;
        profile = profile ?: (profiles ? profiles.sort {-it.id}.first() : []);
        render(view: "/admin/tax/profile/rightPanel", model: [profile: profile]);
    }

    def loadCodeView() {
        def codes = TaxCode.all?.sort {-it.id};
        render(view: "/admin/tax/code/appView", model: [codes: codes ?: []]);
    }

    def editTaxRule() {
        TaxRule rule = params.id ? TaxRule.get(params.id) : new TaxRule();
        def states = administrationService.getStatesForCountry(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, 'default_country').toLong());
        render(view: "/admin/tax/rule/edit", model: [rule: rule, states: states])
    }

    def viewTaxRule(){
        TaxRule rule = TaxRule.get(params.id);
        render(view: "/admin/tax/rule/view", model: [rule: rule]);
    }

    def createTaxProfile() {
        Long savedId = taxService.saveTaxProfile(params);
        if(savedId) {
            render([status: "success", message: g.message(code: "tax.profile.save.success"), id: savedId] as JSON);
        } else {
            render([status: "error", message: g.message(code: "tax.profile.could.not.save")] as JSON);
        }
    }

    def saveTaxRule() {
        if (taxService.saveTaxRule(params)) {
            render([status: "success", message: g.message(code: "tax.rule.save.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "tax.rule.could.not.save")] as JSON)
        }
    }

    def deleteTaxCode() {
        def id = params.long("id");
        try {
            if (taxService.deleteTaxCode(id, params.at1_reply, params.at2_reply)) {
                render([status: "success", message: g.message(code: "tax.code.remove.success")] as JSON)
            } else {
                render([status: "error", message: g.message(code: "tax.code.could.not.delete")] as JSON)
            }
        } catch(AttachmentExistanceException att) {
            render([status: "error", error_code: "attachment.exists", attachments: att.attachmentInfo] as JSON)
        }
    }

    def viewProducts(){
        TaxProfile profile = TaxProfile.proxy(params.long("id"));
        List<Product> products = Product.where {
            taxProfile == profile
        }.list()
        render(view: "/admin/common/productList", model: [products: products])
    }

    def loadTaxRulesForSelection(){
        params.max = params.max ?: "10"
        Integer count = taxService.getTaxRulesCount(params)
        List<TaxRule> rules = commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.offset = offset;
            taxService.getTaxRules(params);
        }
        render(view: "/admin/tax/rule/selectionPanel", model: [count: count, rules: rules]);
    }

    def loadRule() {
        List<Long> ids = params.list("ids").collect{ it.toLong()};
        List<TaxRule> rules = TaxRule.createCriteria().list {
            if (ids) {
                not {'in'("id", ids)}
            }
            if(params.searchText) {
                ilike("name", "%${params.searchText.trim().encodeAsLikeText()}%")
            }
        }
        render(view: "/admin/tax/rule/addRulePopup", model: [rules: rules])
    }

    def editRuleRow() {
        TaxRule rule = taxService.updateTaxRule(params);
        if(params.codeId) {
            forward(controller: "taxAdmin", action: "addRule", params: [id: params.profileId, ruleId: rule.id])
        } else {
            render(view: "/admin/tax/rule/ruleRow", model: [rule: rule]);
        }
    }

    def addRule() {
        if(params.profileId) {
            params.id = params.profileId;
        }
        TaxRule rule = taxService.saveRule(params);
        render(view: "/admin/tax/rule/ruleRow", model: [rule: rule]);
    }

    def loadRuleView() {
        List<TaxRule> rules = TaxRule.all?.sort {-it.id};
        render(view: "/admin/tax/rule/appView", model: [rules: rules ?: []])
    }
}
