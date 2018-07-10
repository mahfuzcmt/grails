package com.webcommander.plugin.gift_card

import com.amazonaws.services.opsworks.model.App
import com.webcommander.AppResourceTagLib
import com.webcommander.admin.Country
import com.webcommander.admin.Customer
import com.webcommander.admin.Operator
import com.webcommander.admin.State
import com.webcommander.common.CommanderMailService
import com.webcommander.common.CommonService
import com.webcommander.constants.DomainConstants as BASE_DC
import com.webcommander.events.AppEventManager
import com.webcommander.manager.HookManager
import com.webcommander.models.Cart
import com.webcommander.models.CartItem
import com.webcommander.models.ProductData
import com.webcommander.models.ProductInCart
import com.webcommander.payment.DefaultPaymentService
import com.webcommander.plugin.gift_card.constants.DomainConstants
import com.webcommander.plugin.gift_card.constants.NamedConstants
import com.webcommander.plugin.gift_card.model.CacheOfGiftCardInCart
import com.webcommander.plugin.gift_card.webcommerce.GiftCard
import com.webcommander.plugin.gift_card.webcommerce.GiftCardAmountAdjustment
import com.webcommander.plugin.gift_card.webcommerce.GiftCardUsage
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.util.AppUtil
import com.webcommander.util.StringUtil
import com.webcommander.webcommerce.Order
import com.webcommander.webcommerce.OrderItem
import com.webcommander.webcommerce.Product
import com.webcommander.webcommerce.ProductService
import grails.gorm.transactions.Transactional
import grails.util.Holders
import grails.util.TypeConvertingMap
import grails.web.servlet.mvc.GrailsParameterMap
import groovy.time.TimeCategory
import org.grails.gsp.GroovyPageBinding
import org.grails.plugins.web.taglib.ApplicationTagLib
import org.grails.taglib.GroovyPageAttributes
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import com.webcommander.tenant.Thread

import javax.servlet.http.HttpServletRequest

@Transactional
class GiftCardService {
    ProductService productService
    CommonService commonService
    CommanderMailService commanderMailService
    @Autowired
    @Qualifier("org.grails.plugins.web.taglib.ApplicationTagLib")
    ApplicationTagLib g
    @Autowired
    @Qualifier("com.webcommander.ApplicationTagLib")
    com.webcommander.ApplicationTagLib app;

    static {
        AppEventManager.on("before-order-update", { Long orderId ->
            GiftCard.createCriteria().list {
                eq("orderRef", orderId)
            }*.delete()
        });
        AppEventManager.on("order-item-create", { OrderItem orderItem, CartItem cartItem ->
            if(cartItem.object.product.productType == BASE_DC.PRODUCT_TYPE.GIFT_CARD) {
                GiftCardService giftCardService = Holders.applicationContext.getBean("giftCardService")
                Order order = orderItem.order
                giftCardService.generateGiftCard(order, cartItem, orderItem)
            }
        });
        AppEventManager.on("paid-for-cart", { Collection<Cart> carts ->
            GiftCardService giftCardService = Holders.applicationContext.getBean("giftCardService")
            carts.each { Cart cart ->
                Order order = Order.get(cart.orderId)
                giftCardService.activateGiftCard(order)
            }
        })
        AppEventManager.on("paid-for-order", { Order order ->
            GiftCardService giftCardService = Holders.applicationContext.getBean("giftCardService")
            giftCardService.activateGiftCard(order)
        })
        HookManager.register("variationsForCartAdd", {List variations, ProductData productData, TypeConvertingMap params ->
            if(productData.productType == BASE_DC.PRODUCT_TYPE.GIFT_CARD) {
                GiftCardService cardService = Holders.applicationContext.getBean("giftCardService")
                cardService.giftCardExtraFields(variations, productData, params)
            }
        })
        AppEventManager.on("cart-cleared cart-removed cart-item-add cart-item-quantity-update cart-modified", { cart ->
            def defaultPaymentService = AppUtil.getBean(DefaultPaymentService)
            defaultPaymentService.removeGiftCardCachedAmount(cart.sessionId)
        });
        AppEventManager.on("import-product", { Product product, conf, task ->
            if(product.productType == BASE_DC.PRODUCT_TYPE.GIFT_CARD) {
                product.shippingProfile = null
                product.productCondition = BASE_DC.PRODUCT_CONDITION.NEW
                product.model = null
                product.height = 0
                product.width = 0
                product.length = 0
                product.weight = 0
                product.isFeatured = false
                product.isNew = false
                product.isCallForPriceEnabled = false
                product.globalTradeItemNumber = null
            }
        })
    }

    private Closure getCriteriaClosure(TypeConvertingMap params) {
        return {
            if (params.searchText) {
                like("code", "%${params.searchText.trim().encodeAsLikeText()}%");
            }
            if (params.productId) {
                eq("productId", params.long("productId"))
            }
            eq("isPaid", true)
        }
    }

    Integer getGiftCardCount(Map params) {
        return GiftCard.createCriteria().count {
            and getCriteriaClosure(params)
        }
    }

    List<GiftCard> getGiftCards(Map params) {
        def listMap = [max: params.max, offset: params.offset];
        return GiftCard.createCriteria().list(listMap) {
            and getCriteriaClosure(params)
            order(params.sort ?: "created", params.dir ?: "desc")
        }
    }

    GiftCardAmountAdjustment adjustAmount(TypeConvertingMap params) {
        GiftCard giftCard = GiftCard.get(params.id)
        Double changeAmount = params.double("amount")
        if(params.type == "deduct") {
            changeAmount = changeAmount * -1
        }
        GiftCardAmountAdjustment adjustment = new GiftCardAmountAdjustment(
            changeAmount: changeAmount,
            giftCard: giftCard,
            note: params.note,
            createdBy: Operator.get(AppUtil.loggedOperator)
        )
        adjustment.save()
        giftCard.amount += changeAmount
        giftCard.save()
        return adjustment
    }

    Boolean changeStatus(Long id) {
        GiftCard giftCard = GiftCard.get(id)
        giftCard.isActive = !giftCard.isActive
        giftCard.save()
        return true
    }

    void generateGiftCard(Order order, CartItem item, OrderItem orderItem) {
        ProductInCart productInCart = item.object
        ProductData productData = productInCart.product
        Map cardData = productInCart.requestedParams.gift_card
        Customer customer = AppUtil.session.customer ? Customer.get(AppUtil.session.customer) : null
        String customerEmail = customer ? customer.userName : order.billing.email
        item.quantity.times {
            new GiftCard(
                orderRef: order.id,
                orderItemId: orderItem.id,
                code: GiftCard.codePrefix + generateGiftCardCode(),
                sendingType: cardData.sendingType ?: "email",
                amount: productData.basePrice,
                productName: productData.name,
                productId: productData.id,
                email: cardData.email,
                firstName: cardData.firstName,
                lastName: cardData.lastName,
                senderEmail: customerEmail,
                senderName: cardData.senderName,
                phone: cardData.phone,
                mobile: cardData.mobile,
                city: cardData.city,
                postCode: cardData.postCode,
                country: cardData.countryId ? Country.get(cardData.countryId) : null,
                state: cardData.stateId ? State.get(cardData.stateId) : null,
                address: cardData.address,
                message: cardData.message,
                isActive: false,
                isPaid: false,
                createdBy: customerEmail
            ).save()
        }
    }

    private void sendNotification(GiftCard giftCard) {
        sendNotificationToRecipient(giftCard)
    }

    public void activateGiftCard(Order order, Boolean sendNotificationEmail = true) {
        List<GiftCard> cards = GiftCard.findAllByOrderRef(order.id)
        cards.each { GiftCard giftCard ->
            giftCard.isActive = true
            giftCard.isPaid = true
            giftCard.activated = new Date().gmt()
            Map giftCardConfig = AppUtil.getConfig(BASE_DC.SITE_CONFIG_TYPES.GIFT_CARD)
            if (giftCardConfig.is_expiry_threshold_enabled == "1") {
                use(TimeCategory) {
                    giftCard.availableTo = giftCard.activated + (giftCardConfig.expiry_threshold.toInteger(0)).(giftCardConfig.expiry_threshold_unit)
                }
            }
            giftCard.save()
            if (!giftCard.hasErrors() && sendNotificationEmail) {
                sendNotification(giftCard);
            }
        }
    }

    public void sendNotificationToRecipient(GiftCard giftCard) {
        Map macrosAndTemplate = commanderMailService.getMacrosAndTemplateByIdentifier(DomainConstants.GIFT_CARD_EMAIL_TEMPLATES.RECIPIENT_NOTIFICATION)
        if (!macrosAndTemplate.emailTemplate.active) {
            return;
        }
        Product product = Product.get(giftCard.productId)
        ProductData productData = product ? productService.getProductData(product) : null
        Map refinedMacros = macrosAndTemplate.commonMacros
        macrosAndTemplate.macros.each {
            switch (it.key.toString()) {
                case "recipient_name":
                    refinedMacros[it.key] = (giftCard.firstName + " " + (giftCard.lastName ?: "")).encodeAsBMHTML()
                    break
                case "gift_code":
                    refinedMacros[it.key] = giftCard.code.encodeAsBMHTML()
                    break
                case "gift_amount":
                    refinedMacros[it.key] = giftCard.amount.toPrice()
                    break
                case "gift_available_to":
                    refinedMacros[it.key] = AppUtil.getConfig(BASE_DC.SITE_CONFIG_TYPES.GIFT_CARD, "is_expiry_threshold_enabled") == "1" ? giftCard.availableTo.toEmailFormat() : "Anytime"
                    break
                case "gift_sender_name":
                    refinedMacros[it.key] = giftCard.senderName ? giftCard.senderName.encodeAsBMHTML() : ""
                    break
                case "gift_sender_email":
                    refinedMacros[it.key] = giftCard.senderEmail
                    break
                case "gift_sender_message":
                    refinedMacros[it.key] = giftCard.message.encodeAsBMHTML() ?: ""
                    break
                case "product_image_url":
                    refinedMacros[it.key] = productData ? AppUtil.getBean(AppResourceTagLib).getProductImageFullUrl(product: productData) : ""
                    break
                case "product_name":
                    refinedMacros[it.key] = productData ? productData.name : ""
                    break
                case "product_url":
                    refinedMacros[it.key] = productData ? app.siteBaseUrl() + productData.link : ""
                    break

            }
        }
        Thread.start {
            AppUtil.initialDummyRequest()
            GiftCard.withNewSession {
                commanderMailService.sendMail(macrosAndTemplate.emailTemplate, macrosAndTemplate.activeHtml, macrosAndTemplate.activeText, refinedMacros, giftCard.email, null)
            }
        }
    }

    public String generateGiftCardCode() {
        String cardCode = StringUtil.uuid.replace("-", "").substring(0, 10)
        if (!commonService.isUnique(GiftCard, [field: "code", value: cardCode])) {
            generateGiftCardCode()
        }
        return cardCode
    }

    public Double getRedeemAmount(Order order, GiftCard giftCard = null) {
        return GiftCardUsage.createCriteria().get {
            projections {
                sum("amount")
            }
            eq("order", order)
            if (giftCard) {
                eq("giftCard", giftCard)
            }
        }
    }

    public Boolean isGiftCardRedeem(GroovyPageAttributes attr, HttpServletRequest request, GroovyPageBinding pageScope) {
        if (attr.page == "checkoutConfirmStep") {
            if (CacheOfGiftCardInCart.getAllGiftCard()) {
                return true
            }
        } else if (attr.page == "paymentSuccess") {
            if (GiftCardUsage.findByOrder(pageScope.order)) {
                return true
            }
        }
        return false
    }

    public Map updateCheckoutConfirmStepModel(Map model) {
        GrailsParameterMap params = model.params ?: AppUtil.params
        if (!params.giftCardCode) {
            return model
        }
        Cart cart = model.cart
        Map giftCardModel = [:]
        try {
            GiftCard card = getValidGiftCard(params.giftCardCode, cart)
            if (!AppUtil.isApiRequest()) {
                CacheOfGiftCardInCart.add([cart: cart, giftCard: card, availableAmount: card.availableBalance])
            }
            giftCardModel.giftCodeRedeemStatusMsg = g.message(code: NamedConstants.APPLIED_GIFT_CARD_STATUS.SUCCESSFULLY_APPLIED)
            giftCardModel.giftCodeRedeemStatusMsgType = "success"
        } catch (ApplicationRuntimeException ex) {
            giftCardModel.giftCodeRedeemStatusMsg = ex.message
            giftCardModel.giftCodeRedeemStatusMsgType = "error"
        }
        if (AppUtil.isApiRequest()) {
            model.giftCardModel = giftCardModel
        } else {
            model << giftCardModel
        }
        return model
    }

    GiftCard getValidGiftCard(String giftCardCode, Cart cart) {
        if (giftCardCode && !giftCardCode.toString().startsWith(GiftCard.codePrefix)) {
            throw new ApplicationRuntimeException(NamedConstants.APPLIED_GIFT_CARD_STATUS.INVALID_CODE)
        }
        GiftCard giftCard = GiftCard.findByCode(giftCardCode)
        validateGiftCard(giftCard, cart)
        return giftCard
    }

    public void validateGiftCard(GiftCard card, Cart cart) {
        if (!card) {
            throw new ApplicationRuntimeException(NamedConstants.APPLIED_GIFT_CARD_STATUS.INVALID_CARD)
        }
        if (!card.isActive) {
            throw new ApplicationRuntimeException(NamedConstants.APPLIED_GIFT_CARD_STATUS.INACTIVE_CARD)
        }
        if (CacheOfGiftCardInCart.find(cart, card)) {
            throw new ApplicationRuntimeException(NamedConstants.APPLIED_GIFT_CARD_STATUS.ALREADY_REDEEMED)
        }
        if (card.availableTo && card.availableTo < new Date().gmt()) {
            throw new ApplicationRuntimeException(NamedConstants.APPLIED_GIFT_CARD_STATUS.NO_LONGER_AVAILABLE)
        }
        Double availableBalance = card.availableBalance
        if (!availableBalance) {
            throw new ApplicationRuntimeException(NamedConstants.APPLIED_GIFT_CARD_STATUS.NO_BALANCE)
        }
    }

    public Double resolveRedeemAmount(Double availableBalance, Double payable) {
        return availableBalance < payable ? availableBalance : payable
    }

    public Double resolvePayableAfterRedeem(Double availableBalance, Double payable) {
        return availableBalance < payable ? payable - availableBalance : 0.0
    }

    def giftCardExtraFields(List variations, ProductData productData, TypeConvertingMap params) {
        def giftCardParamData = params?.gift_card
        if(giftCardParamData) {
            String giftCardDataHash = Long.toHexString(Math.abs(params.gift_card.collect{it.value}.join(",").hashCode())).toUpperCase().padLeft(8, "0")
            variations.add( "Card Token: "  + giftCardDataHash)
        }
        productData.isVirtual = true
        return variations
    }
}
