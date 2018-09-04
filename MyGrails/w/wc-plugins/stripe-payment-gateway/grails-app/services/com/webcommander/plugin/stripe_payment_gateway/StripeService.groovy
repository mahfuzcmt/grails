package com.webcommander.plugin.stripe_payment_gateway


import com.webcommander.constants.DomainConstants
import com.webcommander.log.WcLogManager
import com.webcommander.manager.CartManager
import com.webcommander.models.CardInfo
import com.webcommander.models.Cart
import com.webcommander.models.PaymentInfo
import com.webcommander.throwables.PaymentGatewayException
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.PaymentGatewayMeta
import com.stripe.Stripe
import com.stripe.exception.*
import com.stripe.model.Charge
import grails.gorm.transactions.Transactional

@Transactional
class StripeService {

    PaymentInfo processPayment(Double total, Long orderId, Long paymentId, Object source) {
        Map creditCardConfig = [:]
        PaymentGatewayMeta.findAllByFieldFor(DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.STRIPE).each {
            creditCardConfig[it.name] = it.value;
        }
        Stripe.apiKey = creditCardConfig["secret_key"]
        String errorMessage
        Integer totalAmount = total * 100
        PaymentInfo paymentInfo = new PaymentInfo()
        paymentInfo.paymentRef = paymentId
        String log = "\nRequest Data: \nSource: ${source instanceof Map ? AppUtil.getQueryStringFromMap(source) : source}, " +
                "Amount: ${totalAmount}\nCurrency: ${AppUtil.baseCurrency.code.toLowerCase()}\nDescription: Charge for WC Order ${orderId}\n"
        try {
            Map<String, Object> chargeParams = new HashMap<String, Object>();
            chargeParams.put("amount", totalAmount);
            chargeParams.put("currency", AppUtil.baseCurrency.code.toLowerCase());
            chargeParams.put("source", source);
            chargeParams.put("description", "Charge for WC Order ${orderId}");
            Charge charge = Charge.create(chargeParams);
            if (charge.paid) {
                paymentInfo.success = DomainConstants.PAYMENT_STATUS.SUCCESS
                paymentInfo.amount = charge.amount / 100
                paymentInfo.trackInfo = "Charged Id: " + charge.id
                paymentInfo.gatewayResponse = charge.status
                paymentInfo.payerInfo = "Card ID: " + charge.source.id
                log = log + "Response:\n${paymentInfo.trackInfo}\n${paymentInfo.payerInfo}"
                WcLogManager.log(log, "STRIPE")
                return paymentInfo
            } else {
                paymentInfo.success = false
                paymentInfo.amount = total
                paymentInfo.gatewayResponse = charge.status
                errorMessage = "Error code: " + charge.failureCode + " Message: " + charge.failureMessage
                paymentInfo.trackInfo = errorMessage
                log = log + "Response:\n${errorMessage}"
                WcLogManager.log(log, "STRIPE")
                throw new PaymentGatewayException(charge.failureMessage, [], paymentInfo)
            }
        } catch (CardException e) {
            paymentInfo.success = false
            paymentInfo.amount = total
            errorMessage = "Status: " + e.getCode() + " Message: " + e.getMessage()
            paymentInfo.trackInfo = errorMessage
            log = log + "Response:\n${errorMessage}"
            WcLogManager.log(log, "STRIPE")
            throw new PaymentGatewayException(e.getMessage(), [], paymentInfo)
        }  catch (StripeException e) {
            paymentInfo.success = false
            paymentInfo.amount = total
            errorMessage = "Status: " + e.getStatusCode() + " Message: " + e.getMessage()
            paymentInfo.trackInfo = errorMessage
            log = log + "Response:\n${errorMessage}"
            WcLogManager.log(log, "STRIPE")
            throw new PaymentGatewayException("could.not.contact.payment.provider.sorry.inconvenience", [], paymentInfo)
        } catch (Exception e) {
            paymentInfo.success = false
            paymentInfo.amount = total
            log = log + "Response:\n${e.getMessage()}"
            WcLogManager.log(log, "STRIPE")
            throw new PaymentGatewayException("could.not.contact.payment.provider.sorry.inconvenience", [], paymentInfo)
        }
    }

    PaymentInfo processApiPayment(Map params) {
        def session = AppUtil.session
        Cart cart = CartManager.getCart(session.id, true)
        Double total = cart.tagged.payable + cart.tagged.surcharge
        return processPayment(total, cart.orderId, cart.tagged["payment"].id, params["stripeToken"])
    }

    PaymentInfo processApiPayment(CardInfo cardInfo, Double amount, Long orderId, Long paymentId) {
        Map source = [
            exp_month: cardInfo.expiryMonth,
            exp_year: cardInfo.expiryYear,
            number: cardInfo.cardNumber,
            cvc: cardInfo.cvv
        ]
        return processPayment(amount, orderId, paymentId, source)
    }
}
