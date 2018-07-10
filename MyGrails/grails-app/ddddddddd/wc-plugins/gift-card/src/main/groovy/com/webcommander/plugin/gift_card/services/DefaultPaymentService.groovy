package com.webcommander.plugin.gift_card.services

import com.webcommander.constants.DomainConstants
import com.webcommander.models.Cart
import com.webcommander.models.DefaultPaymentMetaData
import com.webcommander.models.PaymentInfo
import com.webcommander.plugin.gift_card.GiftCardService
import com.webcommander.plugin.gift_card.model.CacheOfGiftCardInCart
import com.webcommander.plugin.gift_card.webcommerce.GiftCard
import com.webcommander.plugin.gift_card.webcommerce.GiftCardUsage
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.Order
import com.webcommander.webcommerce.Payment
import grails.util.Holders
import grails.web.servlet.mvc.GrailsParameterMap

import java.util.concurrent.ConcurrentHashMap

/**
 * Created by sajedur on 5/7/2015.
 */
class DefaultPaymentService {
    private Map<String, Double> cachedGiftCardRequestedAmount = new ConcurrentHashMap<String, Double>()

    static GiftCardService _giftCardService
    private static GiftCardService getGiftCardService() {
        return _giftCardService ?: (_giftCardService = Holders.grailsApplication.mainContext.getBean(GiftCardService))
    }

    def processGiftCardPaymentBeforeOrderConfirm(Map model) {
        giftCardService.updateCheckoutConfirmStepModel(model)
        Cart cart = model.cart
        GrailsParameterMap params = model.params
        Double availableBalance = CacheOfGiftCardInCart.getAvailableAmount(cart)
        if (model.payable && availableBalance) {
            Double applicableAmount;
            Double payable = model.payable
            DefaultPaymentMetaData giftCardPayment = new DefaultPaymentMetaData(name: "payment.from.gift.card", identifier: "giftCard");
            Double requestedRedeemAmount = params.containsKey("giftCardPayment") ? params.double("giftCardPayment") : (cachedGiftCardRequestedAmount[cart.sessionId] ?: null);
            if (params.containsKey("giftCardPayment") || cachedGiftCardRequestedAmount.containsKey(cart.sessionId)) {
                requestedRedeemAmount = requestedRedeemAmount ?: 0.0;
                applicableAmount = requestedRedeemAmount;
                if(requestedRedeemAmount > availableBalance) {
                    removeGiftCardCachedAmount(cart.sessionId)
                    throw new ApplicationRuntimeException("insufficient.redeem.amount")
                }
                if(payable < applicableAmount) {
                    removeGiftCardCachedAmount(cart.sessionId)
                    throw new ApplicationRuntimeException("payment.should.not.greater.then.total")
                } else {
                    cachedGiftCardRequestedAmount[cart.sessionId] = requestedRedeemAmount;
                }
            } else if(availableBalance < payable) {
                applicableAmount = availableBalance;
            } else {
                applicableAmount = payable
            }
            model.payable = payable - applicableAmount
            giftCardPayment.amount = applicableAmount
            giftCardPayment.max = availableBalance
            model.defaultPayments.add(giftCardPayment)
        }
    }

    def processGiftCardPaymentForCalculationAPI(Map model) {
        giftCardService.updateCheckoutConfirmStepModel(model)
        Double availableBalance
        if(model.giftCardModel && model.giftCardModel.giftCodeRedeemStatusMsgType == "success" && model.payable && (availableBalance = GiftCard.findByCode(AppUtil.params.giftCardCode).availableBalance)) {
            Double applicableAmount = model.payable
            if(availableBalance < model.payable ) {
                applicableAmount = availableBalance;
            }
            model.payable -= applicableAmount
            model.giftCardModel.applicableAmount = applicableAmount
        }
        return model
    }

    synchronized List<PaymentInfo> payment(List<GiftCard> giftCards, Double applicableAmount, Cart cart) {
        Order order = Order.get(cart.orderId);
        List<PaymentInfo> payments = new ArrayList<PaymentInfo>()
        giftCards.each { giftCard ->
            Double redeemAmount = giftCardService.resolveRedeemAmount(giftCard.availableBalance, applicableAmount)
            if(redeemAmount) {
                GiftCardUsage giftCardUsage = new GiftCardUsage()
                giftCardUsage.amount = redeemAmount
                giftCardUsage.giftCard = giftCard
                giftCardUsage.order = order
                giftCardUsage.save()
                if (!giftCardUsage.hasErrors()) {
                    CacheOfGiftCardInCart.removeAll(cart, giftCard)
                    applicableAmount = giftCardService.resolvePayableAfterRedeem(giftCardUsage.amount, applicableAmount)
                    Payment payment = new Payment()
                    payment.amount = giftCardUsage.amount
                    payment.surcharge = 0.0
                    payment.gatewayCode = DomainConstants.PAYMENT_GATEWAY_CODE.GIFT_CARD
                    payment.gatewayResponse = "Paid by gift card"
                    payment.trackInfo = giftCard.code
                    payment.payerInfo = ""
                    payment.status = DomainConstants.PAYMENT_STATUS.SUCCESS
                    payment.payingDate = new Date().gmt()
                    payment.order = order
                    payment.save()
                    if (!payment.hasErrors()) {
                        PaymentInfo info = new PaymentInfo();
                        info.amount = payment.amount
                        info.trackInfo = payment.trackInfo
                        info.payerInfo = payment.payerInfo
                        info.gatewayResponse = payment.gatewayResponse
                        info.paymentRef = payment.id
                        info.success = true
                        payments.add(info)
                    }
                }
            }
        }
        return payments
    }

    def processGiftCardPayment(Map paymentAsset) {
        Cart cart = paymentAsset.cart
        Double availableBalance = CacheOfGiftCardInCart.getAvailableAmount(cart)
        Double payable = paymentAsset.payable;
        if (payable > 0.0 && availableBalance) {
            Double applicableAmount
            if(cachedGiftCardRequestedAmount.containsKey(cart.sessionId)) {
                applicableAmount = cachedGiftCardRequestedAmount[cart.sessionId];
            } else if(payable > availableBalance) {
                applicableAmount = availableBalance
            } else {
                applicableAmount = payable
            }
            List<GiftCard> giftCards = CacheOfGiftCardInCart.getAllGiftCard(cart)
            List<PaymentInfo> payments = payment(giftCards, applicableAmount, cart)
            paymentAsset.payments.addAll(payments)
            paymentAsset.payable = paymentAsset.payable - (payments.size() ? payments.sum { it.amount } : 0.0);
        }
        removeGiftCardCachedAmount(cart.sessionId)
    }

    def processGiftCardPaymentForAPI(Map paymentAsset) {
        try {
            Map params = AppUtil.params
            Cart cart = paymentAsset.cart
            Double payable = paymentAsset.payable;
            GiftCard card = giftCardService.getValidGiftCard(params.giftCardCode, cart)
            Double applicableAmount
            if(payable > card.availableBalance) {
                applicableAmount = card.availableBalance
            } else {
                applicableAmount = payable
            }
            List<PaymentInfo> payments = payment([card], applicableAmount, cart)
            paymentAsset.payments.addAll(payments)
            paymentAsset.payable = paymentAsset.payable - (payments.size() ? payments.sum { it.amount } : 0.0);
        } catch (ApplicationRuntimeException ignored) {}
    }

    void removeGiftCardCachedAmount(String key) {
        cachedGiftCardRequestedAmount.remove(key)
    }



}
