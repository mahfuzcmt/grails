package com.webcommander.plugin.braintree_payment_gateway

import com.webcommander.constants.DomainConstants
import com.webcommander.log.WcLogManager
import com.webcommander.manager.CartManager
import com.webcommander.models.CardInfo
import com.webcommander.models.Cart
import com.webcommander.models.PaymentInfo
import com.webcommander.throwables.PaymentGatewayException
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.PaymentGatewayMeta
import com.braintreegateway.*
import grails.gorm.transactions.Transactional

@Transactional
class BrainTreeService {

    private Transaction.Status[] TRANSACTION_SUCCESS_STATUSES = [
        Transaction.Status.AUTHORIZED,
        Transaction.Status.AUTHORIZING,
        Transaction.Status.SETTLED,
        Transaction.Status.SETTLEMENT_CONFIRMED,
        Transaction.Status.SETTLEMENT_PENDING,
        Transaction.Status.SETTLING,
        Transaction.Status.SUBMITTED_FOR_SETTLEMENT
    ];

    BraintreeGateway getGateway() {
        Map<String, String> braintreeConfig = [:]
        PaymentGatewayMeta.findAllByFieldFor(DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.BRAINTREE).each {
            braintreeConfig[it.name] = it.value
        }
        return  new BraintreeGateway(braintreeConfig["mode"], braintreeConfig["merchantId"], braintreeConfig["public_key"], braintreeConfig["private_key"])
    }

    PaymentInfo processPayment(Double total, Long paymentId, Object card) {
        BigDecimal decimalAmount  = new BigDecimal(total.toString());
        String log = "\nRequest Data: Amount: ${decimalAmount}\n"
        TransactionRequest request = new TransactionRequest()
                .amount(decimalAmount);
        if(card instanceof CardInfo) {
            request.creditCard().cardholderName(card.holderName).number(card.cardNumber).cvv(card.cvv).expirationMonth(card.expiryMonth).expirationYear(card.expiryYear)
            log += "Card: ${card.cardNumber.substring(0, 4) + "...." + card.cardNumber.substring (card.cardNumber.length() - 4)}\nCVV: ${card.cvv}\nExpiry Date:${card.expiryMonth}/${card.expiryYear}"
        } else {
            request = request.paymentMethodNonce((String) card)
            log = log + "Nonce: ${card}"
        }
        request = request.options().submitForSettlement(true).done();
        PaymentInfo paymentInfo = new PaymentInfo()
        paymentInfo.paymentRef = paymentId
        Result<Transaction> result = gateway.transaction().sale(request);
        if (result.isSuccess()) {
            Transaction transaction = result.getTarget()
            paymentInfo.success = Arrays.asList(TRANSACTION_SUCCESS_STATUSES).contains(transaction.getStatus())
            paymentInfo.amount = (Double) result.target.amount
            paymentInfo.trackInfo = "Transaction Id: " + transaction.getId() + ", Status: " + transaction.status
            paymentInfo.gatewayResponse = result.target.processorResponseText
            CreditCard creditCard = transaction.getCreditCard();
            paymentInfo.payerInfo = "Card: " + creditCard.bin + "..." + creditCard.last4 + ", Cart Type: " + creditCard.cardType
            log = log + "Response:\n${paymentInfo.trackInfo}\n${paymentInfo.payerInfo}"
            WcLogManager.log(log, "BRAINTREE")
            return paymentInfo
        } else {
            paymentInfo.success = false
            paymentInfo.amount = total
            String errorString = "";
            for (ValidationError error : result.getErrors().getAllDeepValidationErrors()) {
                errorString += "Error: " + error.getCode() + ": " + error.getMessage() + "\n";
            }
            paymentInfo.gatewayResponse = errorString.substring(0, 250)
            log = log + "Response:\n${errorString}"
            WcLogManager.log(log, "BRAINTREE")
            throw new PaymentGatewayException(result.message, [], paymentInfo)
        }
    }

    PaymentInfo processApiPayment(Map params) {
        Cart cart = CartManager.getCart(AppUtil.session.id, true)
        String nonce = params["payment-method-nonce"]
        return processPayment(cart.tagged.payable + cart.tagged.surcharge, cart.tagged["payment"].id, nonce)
    }

    PaymentInfo processApiPayment(CardInfo cardInfo, Double amount, Long paymentId) {
        return processPayment(amount, paymentId, cardInfo)
    }

}
