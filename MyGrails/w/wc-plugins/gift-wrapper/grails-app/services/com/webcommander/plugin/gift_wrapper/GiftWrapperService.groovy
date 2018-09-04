package com.webcommander.plugin.gift_wrapper


import com.webcommander.calculator.TaxCalculator
import com.webcommander.common.CommonService
import com.webcommander.common.ImageService
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.webcommerce.TaxCode
import com.webcommander.webcommerce.TaxProfile
import grails.gorm.transactions.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

@Transactional
class GiftWrapperService {

    ImageService imageService
    CommonService commonService

    @Autowired
    @Qualifier("com.webcommander.plugin.gift_wrapper.GiftWrapperTagLib")
    GiftWrapperTagLib giftWrapperTL

    private Closure getCriteriaClosure(Map params) {
        return {
            if (params.searchText) {
                ilike("name", "%${params.searchText.trim().encodeAsLikeText()}%")
            }
            if (params.ids) {
                inList("id", params.list("ids").collect { it.toLong() })
            }
        }
    }

    Integer getGiftWrappersCount(Map params) {
        return GiftWrapper.createCriteria().count {
            and getCriteriaClosure(params)
        }
    }

    GiftWrapper getGiftWrapper(Long id) {
        return GiftWrapper.get(id)
    }

    def deleteGiftWrapper(Long id) {
        GiftWrapper giftWrapper = GiftWrapper.get(id)
        giftWrapper.delete()
        if (giftWrapper.hasErrors()) {
            return false
        }
        return true
    }

    def deleteSelectedGiftWrappers(List<String> ids) {
        ids.each {
            GiftWrapper giftWrapper = GiftWrapper.get(it.toLong())
            giftWrapper.delete()
            if (giftWrapper.hasErrors()) {
                return false
            }
        }
        return true
    }

    List<GiftWrapper> getGiftWrappers(Map params) {
        def listMap = [max: params.max, offset: params.offset];
        return GiftWrapper.createCriteria().list(listMap) {
            and getCriteriaClosure(params)
            order(params.sort ?: "name", params.dir ?: "asc")
        }
    }

    List<GiftWrapper> getGiftWrappersForCustomer(Map params) {
        def customerGiftWrappers = GiftWrapper.createCriteria().list {
            eq("isVisibleToCustomer", true)
        }
        return customerGiftWrappers
    }

    void saveGiftWrapperData(def item, def cartItem, String type) {
        GiftWrapperAssoc giftWrapperAssoc = new GiftWrapperAssoc()
        if (type == "order") {
            if (cartItem.getAdjustWithUnitPriceLogObject(GiftWrapperResourceTagLib.GIFT_WRAPPER)) {
                GiftWrapper giftWrapper = cartItem.getAdjustWithUnitPriceLogObject(GiftWrapperResourceTagLib.GIFT_WRAPPER).giftWrapper
                giftWrapperAssoc.assocItemId = item.id
                giftWrapperAssoc.productId = item.productId
                giftWrapperAssoc.giftWrapperId = giftWrapper.id
                giftWrapperAssoc.giftWrapperName = giftWrapper.name
                giftWrapperAssoc.giftWrapperPrice = giftWrapper.actualPrice
                String price = giftWrapperTL.getGiftWrapperPrice(price: giftWrapper.actualPrice, cartItem: cartItem)
                giftWrapperAssoc.price = Double.parseDouble(price)
                giftWrapperAssoc.message = cartItem.getAdjustWithUnitPriceLogObject(GiftWrapperResourceTagLib.GIFT_WRAPPER).message
                giftWrapperAssoc.assocType = type
                giftWrapperAssoc.assocTypeId = item.orderId
                giftWrapperAssoc.save()
            }
        } else if (type == "quote") {
            item.quoteItems.eachWithIndex { def it, int i ->
                if (cartItem[i].getAdjustWithUnitPriceLogObject(GiftWrapperResourceTagLib.GIFT_WRAPPER)) {
                    GiftWrapper giftWrapper = cartItem[i].getAdjustWithUnitPriceLogObject(GiftWrapperResourceTagLib.GIFT_WRAPPER).giftWrapper
                    giftWrapperAssoc.assocItemId = it.id
                    giftWrapperAssoc.productId = it.itemId
                    giftWrapperAssoc.giftWrapperId = giftWrapper.id
                    giftWrapperAssoc.giftWrapperName = giftWrapper.name
                    giftWrapperAssoc.giftWrapperPrice = giftWrapper.actualPrice
                    giftWrapperAssoc.price = giftWrapper.actualPrice
                    giftWrapperAssoc.message = cartItem[i].getAdjustWithUnitPriceLogObject(GiftWrapperResourceTagLib.GIFT_WRAPPER).message
                    giftWrapperAssoc.assocType = type
                    giftWrapperAssoc.assocTypeId = it.quoteId
                    giftWrapperAssoc.save()
                }
            }
        } else {
            item.cartItems.eachWithIndex { def it, int i ->
                if (cartItem[i].getAdjustWithUnitPriceLogObject(GiftWrapperResourceTagLib.GIFT_WRAPPER)) {
                    GiftWrapper giftWrapper = cartItem[i].getAdjustWithUnitPriceLogObject(GiftWrapperResourceTagLib.GIFT_WRAPPER).giftWrapper
                    giftWrapperAssoc.assocItemId = it.id
                    giftWrapperAssoc.productId = it.itemId
                    giftWrapperAssoc.giftWrapperId = giftWrapper.id
                    giftWrapperAssoc.giftWrapperName = giftWrapper.name
                    giftWrapperAssoc.giftWrapperPrice = giftWrapper.actualPrice
                    String price = giftWrapperTL.getGiftWrapperPrice(price: giftWrapper.actualPrice, cartItem: cartItem[i])
                    giftWrapperAssoc.price = Double.parseDouble(price)
                    giftWrapperAssoc.message = cartItem[i].getAdjustWithUnitPriceLogObject(GiftWrapperResourceTagLib.GIFT_WRAPPER).message
                    giftWrapperAssoc.assocType = type
                    giftWrapperAssoc.assocTypeId = it.savedCartId
                    giftWrapperAssoc.save()
                }
            }
        }
    }

    Double getGiftWrapperPriceWithTax(Double price, def cartItem) {
        TaxCode taxCode = cartItem.object.product.exitTaxCode
        price += TaxCalculator.getTax((TaxCode) taxCode, price)
        return price
    }

    @Transactional
    boolean saveGiftWrapper(Map params, def uploadedFile) {
        Long id = params.id ? params.id.toLong(0) : null
        Map properties = [
                name               : params.name, price: params.price,
                description        : params.description,
                isAllowGiftMessage : params.isAllowGiftMessage,
                isVisibleToCustomer: params.isVisibleToCustomer
        ]
        GiftWrapper giftWrapper = params.id ? GiftWrapper.proxy(id) : new GiftWrapper()
        if (params["remove-image"]) {
            giftWrapper.removeResource()
            properties.image = null
        }
        giftWrapper.setProperties(properties)
        if (!commonService.isUnique(giftWrapper, "name")) {
            throw new ApplicationRuntimeException("gift.wrapper.name.exists")
        }
        giftWrapper.save()
        if (!giftWrapper.hasErrors()) {
            if (uploadedFile?.originalFilename) {
                giftWrapper.removeResource()
                giftWrapper.image = uploadedFile.originalFilename
                imageService.uploadImage(uploadedFile, "manufacturer-logo", giftWrapper, 2 * 1024 * 1024)
                giftWrapper.merge()
            }
            return true
        } else {
            return false
        }
    }

}
