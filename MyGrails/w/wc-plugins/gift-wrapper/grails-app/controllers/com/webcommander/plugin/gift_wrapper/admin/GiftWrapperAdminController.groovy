package com.webcommander.plugin.gift_wrapper.admin

import com.webcommander.authentication.annotations.License
import com.webcommander.calculator.TaxCalculator
import com.webcommander.common.CommonService
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.events.AppEventManager
import com.webcommander.manager.CartManager
import com.webcommander.models.Cart
import com.webcommander.models.CartItem
import com.webcommander.models.ProductData
import com.webcommander.plugin.gift_wrapper.GiftWrapper
import com.webcommander.plugin.gift_wrapper.GiftWrapperService
import com.webcommander.throwables.AttachmentExistanceException
import com.webcommander.throwables.CartManagerException
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.Product
import com.webcommander.webcommerce.ProductService
import com.webcommander.webcommerce.TaxProfile
import grails.converters.JSON


class GiftWrapperAdminController {

    CommonService commonService
    GiftWrapperService giftWrapperService
    ProductService productService

    @License(required = "allow_gift_wrapper_feature")
    def loadAppView() {
        params.max = params.max ?: "10"
        Integer count = giftWrapperService.getGiftWrappersCount(params)
        List<GiftWrapper> giftWrappers = commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.max = max
            params.offset = offset
            giftWrapperService.getGiftWrappers(params)
        }
        render(view: "/plugins/gift_wrapper/admin/appView.gsp", model: [giftWrappers: giftWrappers, count: count])
    }



    def isGiftWrapperUnique() {
        if (commonService.isUnique(GiftWrapper, params)) {
            render([status: "success", message: g.message(code: "provided.field.available", args: [params.field, params.value])] as JSON)
        } else {
            render([status: "error", message: g.message(code: "provided.field.value.exists", args: [params.field, params.value])] as JSON)
        }
    }

    @License(required = "allow_gift_wrapper_feature")
    def edit() {
        GiftWrapper giftWrapper = params.id ?  giftWrapperService.getGiftWrapper(params.long("id")) : new GiftWrapper()
        render(view: "/plugins/gift_wrapper/admin/infoEdit.gsp", model: [giftWrapper: giftWrapper])
    }

    @License(required = "allow_gift_wrapper_feature")
    def save() {
        def uploadedFile = request.getFile("manufacturerLogo")
        if (giftWrapperService.saveGiftWrapper(params, uploadedFile)) {
            render([status: "success", message: g.message(code: "gift.wrapper.save.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "gift.wrapper.save.failure")] as JSON)
        }
    }

    @License(required = "allow_gift_wrapper_feature")
    def delete() {
        try {
            Long id = params.long("id")
            if (giftWrapperService.deleteGiftWrapper(id)) {
                render([status: "success", message: g.message(code: "gift.wrapper.delete.success")] as JSON)
            } else {
                render([status: "error", message: g.message(code: "gift.wrapper.delete.failure")] as JSON)
            }
        } catch(AttachmentExistanceException att) {
            render([status: "error", error_code: "attachment.exists", attachments: att.attachmentInfo] as JSON)
        }
    }

    @License(required = "allow_gift_wrapper_feature")
    def deleteSelected() {
        List<Long> ids = []
        params.list("ids").each {
            ids.add(it.toLong(0))
        }
        if (giftWrapperService.deleteSelectedGiftWrappers(ids)) {
            render([status: "success", message: g.message(code: "selected.manufacturers.delete.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "selected.manufacturers.could.not.delete")] as JSON)
        }
    }


}
