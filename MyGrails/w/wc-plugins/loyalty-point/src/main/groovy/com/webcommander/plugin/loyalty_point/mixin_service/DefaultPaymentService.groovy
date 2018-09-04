package com.webcommander.plugin.loyalty_point.mixin_service

import com.webcommander.ApplicationTagLib
import com.webcommander.admin.Customer
import com.webcommander.constants.DomainConstants
import com.webcommander.models.Cart
import com.webcommander.models.DefaultPaymentMetaData
import com.webcommander.models.PaymentInfo
import com.webcommander.plugin.loyalty_point.LoyaltyPointService
import com.webcommander.constants.DomainConstants as DC
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.Order
import com.webcommander.webcommerce.Payment
import com.webcommander.webcommerce.PaymentGatewayMeta
import grails.util.Holders
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.web.context.request.RequestContextHolder

import javax.servlet.http.HttpSession
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by sanjoy on 12/08/2014.
 */
class DefaultPaymentService {
    private Map<String, Double> cachedLoyaltyPointRequestedAmount = new ConcurrentHashMap<String, Double>()

    private static LoyaltyPointService _loyaltyPointService;
    private static LoyaltyPointService getLoyaltyPointService() {
        return _loyaltyPointService ?: (_loyaltyPointService = Holders.grailsApplication.mainContext.getBean(LoyaltyPointService))
    }

    def processLoyaltyPointPaymentBeforeOrderConfirm(Map model) {
        Boolean isActive = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.LOYALTY_POINT, "apply_by_default").toBoolean();
        HttpSession session = AppUtil.session
        Customer customer = session.customer ? Customer.get(session.customer) : null
        Double payable = model.payable;
        Double availableLoyaltyPointCurrency;
        if(isActive && customer && payable > 0.0 && (availableLoyaltyPointCurrency = loyaltyPointService.getCustomerLoyaltyPointToCurrency(customer)) > 0.0) {
            GrailsParameterMap params = AppUtil.params;
            Cart cart = model.cart
            DefaultPaymentMetaData loyaltyPointPayment = new DefaultPaymentMetaData(name: "payment.from.loyalty.point", identifier: "loyaltyPoint");
            Double requestedLoyaltyPointCurrency = params.containsKey("loyaltyPointPayment") ? params.double("loyaltyPointPayment") : (cachedLoyaltyPointRequestedAmount[cart.sessionId] ?: null);
            Double surCharge = paymentGatewayService.calculateSurchargeAmount(DomainConstants.PAYMENT_GATEWAY_CODE.LOYALTY_POINT, payable);
            Double applicableAmount;
            if (params.containsKey("loyaltyPointPayment") || cachedLoyaltyPointRequestedAmount.containsKey(cart.sessionId)) {
                requestedLoyaltyPointCurrency = requestedLoyaltyPointCurrency ?: 0.0;
                if(requestedLoyaltyPointCurrency > availableLoyaltyPointCurrency) {
                    removeLoyaltyPointCachedAmount(cart.sessionId)
                    throw new ApplicationRuntimeException("insufficient.loyalty.points")
                }
                applicableAmount = requestedLoyaltyPointCurrency
                if(payable + surCharge < applicableAmount) {
                    removeLoyaltyPointCachedAmount(cart.sessionId)
                    throw new ApplicationRuntimeException("payment.should.not.greater.then.total")
                } else {
                    cachedLoyaltyPointRequestedAmount[cart.sessionId] = requestedLoyaltyPointCurrency;
                }
                loyaltyPointPayment.amount = requestedLoyaltyPointCurrency
                payable = payable + surCharge
            } else if(payable + surCharge > availableLoyaltyPointCurrency) {
                applicableAmount = availableLoyaltyPointCurrency;
                surCharge = paymentGatewayService.calculateSurchargeAmount(DomainConstants.PAYMENT_GATEWAY_CODE.LOYALTY_POINT, applicableAmount);
                loyaltyPointPayment.amount = applicableAmount
                payable = payable + surCharge
            } else {
                applicableAmount = payable
                loyaltyPointPayment.amount = applicableAmount + surCharge
            }
            model.surcharge = model.surcharge + surCharge;
            model.grandTotal = model.grandTotal + surCharge
            model.payable = payable - applicableAmount
            loyaltyPointPayment.max = availableLoyaltyPointCurrency
            model.defaultPayments.add(loyaltyPointPayment)
        }
    }

    def processLoyaltyPointPayment(Map paymentAsset) {
        Boolean isActive = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.LOYALTY_POINT, "apply_by_default").toBoolean();
        HttpSession session = AppUtil.session
        Customer customer = session.customer ? Customer.get(session.customer) : null
        Double payable = paymentAsset.payable;
        Double availableLoyaltyPointCurrency;
        PaymentInfo info = new PaymentInfo();
        Cart cart = paymentAsset.cart;
        if(isActive && customer && payable > 0.0 && (availableLoyaltyPointCurrency = loyaltyPointService.getCustomerLoyaltyPointToCurrency(customer)) > 0.0) {
            Order order = Order.get(cart.orderId);
            Double applicableAmount
            Double surCharge = paymentGatewayService.calculateSurchargeAmount(DomainConstants.PAYMENT_GATEWAY_CODE.LOYALTY_POINT, payable);
            if (cachedLoyaltyPointRequestedAmount.containsKey(cart.sessionId)) {
                applicableAmount = cachedLoyaltyPointRequestedAmount[cart.sessionId];
                surCharge = paymentGatewayService.calculateSurchargeAmount(DomainConstants.PAYMENT_GATEWAY_CODE.LOYALTY_POINT, applicableAmount);
                info.amount = applicableAmount
            } else if (payable + surCharge > availableLoyaltyPointCurrency) {
                applicableAmount = availableLoyaltyPointCurrency
                surCharge = paymentGatewayService.calculateSurchargeAmount(DomainConstants.PAYMENT_GATEWAY_CODE.LOYALTY_POINT, applicableAmount);
                info.amount = applicableAmount
            } else {
                applicableAmount = payable;
                info.amount = applicableAmount + surCharge
            }
            Long availableLoyaltyPoints = loyaltyPointService.getCustomerLoyaltyPoint(customer);
            Double convertRate = Double.parseDouble(PaymentGatewayMeta.findByFieldFor(DC.PAYMENT_GATEWAY_CODE.LOYALTY_POINT).value);
            Long targetLoyaltyPoint = (Long) Math.ceil((100 / convertRate) * applicableAmount)
            if (availableLoyaltyPoints >= targetLoyaltyPoint) {
                loyaltyPointService.cutLoyaltyPointsFormCustomerAccount(customer, targetLoyaltyPoint);
                info.success = true
            } else {
                info.success = false
                throw new ApplicationRuntimeException("insufficient.loyalty.points")
            }
            if(info.amount > 0) {
                Payment payment = new Payment();
                payment.amount = info.amount
                payment.surcharge = surCharge
                payment.gatewayCode = DomainConstants.PAYMENT_GATEWAY_CODE.LOYALTY_POINT;
                payment.status = DomainConstants.PAYMENT_STATUS.SUCCESS
                payment.payerInfo = "Customer# " + customer.id;
                payment.trackInfo = "Paid by loyalty point"
                payment.payingDate = new Date().gmt()
                payment.order = order
                payment.save();
                order.addToPayments(payment)
                order.totalSurcharge = order.totalSurcharge + surCharge;
                order.merge();
                if (!customer.hasErrors() && !payment.hasErrors()) {
                    info.trackInfo = payment.trackInfo
                    info.gatewayResponse = ""
                    info.success = true;
                    info.paymentRef = payment.id
                    paymentAsset.payable = payable - info.amount + surCharge;
                    paymentAsset.payments.add(info)
                }
            }

        }
        removeLoyaltyPointCachedAmount(cart.sessionId)
    }

    void removeLoyaltyPointCachedAmount(String key) {
        cachedLoyaltyPointRequestedAmount.remove(key)
    }
}
