package com.webcommander.plugin.ebay_listing.controllers.admin.webmarketing

import com.webcommander.admin.ConfigService
import com.webcommander.authentication.annotations.License
import com.webcommander.common.CommonService
import com.webcommander.constants.DomainConstants
import com.webcommander.plugin.ebay_listing.EbayListingService
import com.webcommander.plugin.ebay_listing.admin.mapping.EbayCategoryProfileMapping
import com.webcommander.plugin.ebay_listing.admin.mapping.EbayProfileMapping
import com.webcommander.plugin.ebay_listing.admin.webmarketing.EbayListingProfile
import com.webcommander.plugin.ebay_listing.admin.webmarketing.EbayUpdateSchedule
import com.webcommander.throwables.AttachmentExistanceException
import com.webcommander.util.AppUtil
import grails.converters.JSON

class EbayListingAdminController {

    CommonService commonService
    EbayListingService ebayListingService
    ConfigService configService

    @License(required = "allow_ebay_feature")
    def loadAppView() {
        params.max = params.max ?: "10";
        Integer count = ebayListingService.getProfileCount(params);
        List<EbayListingProfile> profiles = commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.max = max;
            params.offset = offset;
            ebayListingService.getProfiles(params);
        }
        render(view: "/plugins/ebay_listing/admin/webmarketing/appView", model: [profiles: profiles, count: count])
    }

    def loadEbayProfile() {
        EbayProfileMapping profileMapping = EbayProfileMapping.createCriteria().get {
            eq("product.id", params.long("productId"))
        }
        EbayListingProfile profile = profileMapping?.listingProfile ?: new EbayListingProfile();
        render(view: "/plugins/ebay_listing/admin/webmarketing/profileMapping", model: [profile: profile])
    }

    def mapProductProfile() {
        if(ebayListingService.mapProductProfile(params)) {
            render([status: "success", message: g.message(code: "product.save.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "product.could.not.save")] as JSON)}
    }

    def loadEbayProfileForCategory() {
        EbayCategoryProfileMapping profileMapping = EbayCategoryProfileMapping.createCriteria().get {
            eq("category.id", params.long("categoryId"))
        }
        EbayListingProfile profile = profileMapping?.listingProfile ?: new EbayListingProfile();
        render(view: "/plugins/ebay_listing/admin/webmarketing/categoryProfileMapping", model: [profile: profile])
    }

    def mapCategoryProfile() {
        if(ebayListingService.mapCategoryProfile(params)) {
            render([status: "success", message: g.message(code: "category.save.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "category.could.not.save")] as JSON)}
    }

    @License(required = "allow_ebay_feature")
    def listProductOnEbay() {
        Map response = ebayListingService.listProductOnEbay(params)
        if(response.status == "success") {
            render([status: "success", message: g.message(code: "product.list.on.ebay.success")] as JSON)
        } else {
            render([status: "error", message: response.message ?: g.message(code: "product.list.on.ebay.failed")] as JSON)
        }
    }

    @License(required = "allow_ebay_feature")
    def listCategoryOnEbay() {
        Map response = ebayListingService.listCategoryOnEbay(params)
        if(response.success == response.total) {
            render([status: "success", message: g.message(code: "category.list.on.ebay.success")] as JSON)
        } else if(response.success == 0) {
            render([status: "error", message: response.message ?: g.message(code: "product.list.on.ebay.failed")] as JSON)
        } else {
            render([status: "error", message: response.message ?: g.message(code: "selected.not.listed", args: [response.total, response.total - response.success, "product"])] as JSON)
        }
    }

    @License(required = "allow_ebay_feature")
    def create() {
        render(view: "/plugins/ebay_listing/admin/webmarketing/create", model: [:])
    }

    def loadProfileEditor() {
        EbayListingProfile profile = EbayListingProfile.get(params.profileId.toLong(0)) ?: new EbayListingProfile()
        render(view: "/plugins/ebay_listing/admin/webmarketing/profileEditor", model: [profileId: profile.id])
    }

    def loadProfileProperties() {
        EbayListingProfile profile = EbayListingProfile.get(params.profileId.toLong(0)) ?: new EbayListingProfile()
        switch(params.property) {
            default:
                render(view: "/plugins/ebay_listing/admin/webmarketing/profile/basic", model: [profile: profile])
                break
            case "pricing":
                render(view: "/plugins/ebay_listing/admin/webmarketing/profile/pricing", model: [profile: profile])
                break
            case "payment-method":
                render(view: "/plugins/ebay_listing/admin/webmarketing/profile/paymentMethod", model: [profile: profile])
                break
            case "postage":
                render(view: "/plugins/ebay_listing/admin/webmarketing/profile/postage", model: [profile: profile])
                break
            case "return-policy":
                render(view: "/plugins/ebay_listing/admin/webmarketing/profile/returnPolicy", model: [profile: profile])
                break
            case "schedule":
                render(view: "/plugins/ebay_listing/admin/webmarketing/profile/schedule", model: [profile: profile])
                break
            case "setting":
                render(view: "/plugins/ebay_listing/admin/webmarketing/profile/settings", model: [profile: profile])
                break
        }
    }

    @License(required = "allow_ebay_feature")
    def createProfile() {
        EbayListingProfile profile = ebayListingService.createProfile(params)
        if(!profile.hasErrors()) {
            render([status: "success", message: g.message(code: "ebay.listing.profile.save.success"), data: [id: profile.id, name: profile.name]] as JSON)
        } else {
            render([status: "success", message: g.message(code: "ebay.listing.profile.save.failed")] as JSON)
        }
    }

    def updateBasic() {
        if(ebayListingService.updateBasic(params)) {
            render([status: "success", message: g.message(code: "update.basic.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "update.basic.failed")] as JSON)
        }
    }

    def updatePricing() {
        if(ebayListingService.updatePricing(params)) {
            render([status: "success", message: g.message(code: "update.pricing.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "update.pricing.failed")] as JSON)
        }
    }

    def updatePaymentMethod() {
        if(ebayListingService.updatePaymentMethod(params)) {
            render([status: "success", message: g.message(code: "update.payment.method.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "update.payment.method.failed")] as JSON)
        }
    }

    def updatePostage() {
        if(ebayListingService.updatePostage(params)) {
            render([status: "success", message: g.message(code: "update.postage.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "update.postage.failed")] as JSON)
        }
    }

    def updateReturnPolicy() {
        if(ebayListingService.updateReturnPolicy(params)) {
            render([status: "success", message: g.message(code: "update.return.policy.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "update.return.policy.failed")] as JSON)
        }
    }

    def updateSchedule() {
        if(ebayListingService.updateSchedule(params)) {
            render([status: "success", message: g.message(code: "update.schedule.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "update.schedule.failed")] as JSON)
        }
    }

    @License(required = "allow_ebay_feature")
    def updateSettings() {
        if(ebayListingService.updateSettings(params)) {
            render([status: "success", message: g.message(code: "update.settings.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "update.settings.failed")] as JSON)
        }
    }

    def loadCategoryTree() {
        EbayListingProfile profile = EbayListingProfile.get(params.id.toLong(0)) ?: new EbayListingProfile()
        def children = ebayListingService.getEbayCategoryTree(profile)
        if(children) {
            render([status: "success", children: children] as JSON)
        } else {
            render([status: "error"] as JSON)
        }
    }

    @License(required = "allow_ebay_feature")
    def loadSettings() {
        String type = DomainConstants.SITE_CONFIG_TYPES.EBAY
        Map configs = AppUtil.getConfig(type)
        EbayUpdateSchedule schedule = EbayUpdateSchedule.first();
        render(view: "/plugins/ebay_listing/admin/settings/loadEbayListingSettings", model: [type: type, configs: configs, schedule: schedule])
    }

    def saveSettings() {
        def configs = [];
        params.list("type").each { type ->
            params."${type}".each {
                configs.add([type: type, configKey: it.key, value: it.value]);
            }
        }
        Boolean result = configService.update(configs)
        if(result && ebayListingService.updateSchedule(params)) {
            render([status: "success", message: g.message(code: "setting.updated.successfully")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "setting.not.updated")] as JSON)
        }
    }

    def view() {
        Long id = params.long("id")
        EbayListingProfile profile = EbayListingProfile.get(id)
        render(view: "/plugins/ebay_listing/admin/webmarketing/infoView", model: [profile: profile]);
    }

    @License(required = "allow_ebay_feature")
    def deleteProfile() {
        Long id = params.long("id");
        try {
            if (ebayListingService.deleteProfile(id, params.at1_reply, params.at2_reply)) {
                render([status: "success", message: g.message(code: "ebay.listing.profile.delete.success")] as JSON)
            } else {
                render([status: "error", message: g.message(code: "ebay.listing.profile.delete.failed")] as JSON)
            }
        } catch(AttachmentExistanceException att) {
            render([status: "error", error_code: "attachment.exists", attachments: att.attachmentInfo] as JSON)
        }
    }

    @License(required = "allow_ebay_feature")
    def deleteSelectedProfile() {
        List<Long> ids = []
        params.list("ids").each {
            ids.add(it.toLong(0))
        }
        try {
            Integer total = ebayListingService.deleteSelectedProfile(ids, params.at1_reply, params.at2_reply)
            if ( total > 0) {
                render([status: "success", message: g.message(code: "selected.ebay.listing.profile.delete.success", args: [total])] as JSON)
            } else {
                render([status: "error", message: g.message(code: "ebay.listing.profile.delete.failed")] as JSON)
            }
        } catch(AttachmentExistanceException att) {
            render([status: "error", error_code: "attachment.exists", attachments: att.attachmentInfo] as JSON)
        }

    }

    @License(required = "allow_ebay_feature")
    def copyProfile() {
        Long id = params.long("id");
        if(ebayListingService.copyProfile(id)) {
            render([status: "success", message: g.message(code: "ebay.listing.profile.copy.success")] as JSON)
        } else {
            render([status: "success", message: g.message(code: "ebay.listing.profile.copy.failed")] as JSON)
        }
    }

    def test() {
        ebayListingService.synchronizeInventory()
        render("tested")
    }

}
