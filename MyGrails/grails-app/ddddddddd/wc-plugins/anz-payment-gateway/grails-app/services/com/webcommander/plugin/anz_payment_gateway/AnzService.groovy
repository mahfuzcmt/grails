package com.webcommander.plugin.anz_payment_gateway

import com.webcommander.ApplicationTagLib
import com.webcommander.AutoGeneratedPage
import com.webcommander.constants.DomainConstants
import com.webcommander.manager.CartManager
import com.webcommander.models.AddressData
import com.webcommander.models.CardInfo
import com.webcommander.models.Cart
import com.webcommander.models.PaymentInfo
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.util.AppUtil
import com.webcommander.util.HttpUtil
import com.webcommander.webcommerce.PaymentGatewayMeta
import grails.gorm.transactions.Transactional
import com.webcommander.webcommerce.Currency
import grails.util.Holders

@Transactional
class AnzService {
    static ApplicationTagLib _app
    static getApp() {
        if(!_app) {
            return _app = Holders.grailsApplication.mainContext.getBean(ApplicationTagLib)
        }
        return _app
    }

    PaymentInfo resolveInfo(Map params) {
        PaymentInfo info = new PaymentInfo()
        def session = AppUtil.session
        String status = (params.vpc_txnresponseCode == "0") ? DomainConstants.PAYMENT_STATUS.SUCCESS : DomainConstants.PAYMENT_STATUS.FAILED
        info.amount = params.vpc_Amount.toDouble() / 100
        info.paymentRef = params["vpc_MerchTxnRef"].toLong()
        info.payerInfo =  session.customer ? "Customer#" +  session.customer : "Guest Customer"
        info.gatewayResponse = params.vpc_Message
        info.success = status == DomainConstants.PAYMENT_STATUS.SUCCESS
        info.trackInfo = params.vpc_transactionNo
        return info
    }

    public def processNetPayment(Map configMap, AddressData billingAddress, Cart cart) {
        boolean isLive
        if(configMap["mode"] == "live") {
            isLive = true
        }
        Currency currency = AppUtil.baseCurrency
        if(currency.code != "AUD") {
            throw new ApplicationRuntimeException("could.not.contact.payment.provider.sorry.inconvenience")
        }
        AutoGeneratedPage page = AutoGeneratedPage.findByName(DomainConstants.AUTO_GENERATED_PAGES.PAYMENT_SUCCESS_PAGE)
        String successScheme = page.isHttps ? "https" : "http"
        def total = (cart.tagged.payable + cart.tagged.surcharge).toCurrency() * 100
        String reference = "" + cart.tagged.payment.id
        String secureHash = (configMap.merchantId + cart.orderId + Math.round(total)).encodeAsMD5()
        String return_url = app.baseUrl(scheme: successScheme) + "anz/paymentReturn"
        String cancel_url = app.baseUrl(scheme: successScheme) + "anz/paymentReturn"
        Map models = [
            vpc_Merchant: configMap.merchantId,
            vpc_AccessCode: configMap.access_code,
            vpc_Amount: "" + Math.round(total),
            vpc_Command: "pay",
            vpc_Locale: "en",
            vpc_MerchTxnRef: reference,
            vpc_OrderInfo: cart.orderId,
            vpc_ReturnURL: return_url,
            vpc_Version: "1",
            vpc_TicketNo: cart.orderId,
            AgainLink: cancel_url,
            vpc_SecureHash: (secureHash + cancel_url + configMap.access_code + Math.round(total) + "pay" + "en" + cart.orderId +
                    cart.orderId + configMap.merchantId + "1").encodeAsMD5().toUpperCase()
        ]
        return [models: models, requestUrl: "https://migs.mastercard.com.au/vpcpay"]
    }

    PaymentInfo processApiPayment(CardInfo cardInfo, Double amount, Long orderId, Long paymentId) {
        def configs = PaymentGatewayMeta.findAllByFieldFor(DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.ANZ)
        Map configMap = [:]
        configs.each {
            configMap[it.name] = it.value
        }
        boolean isLive
        if(configMap["mode"] == "live") {
            isLive = true
        }
        String reference = paymentId + ""
        String total = amount.toCurrency().toPrice()
        Map model = [
                vpc_CardNum: cardInfo.cardNumber,
                vpc_CardSecurityCode: cardInfo.cvv,
                vpc_AccessCode: configMap.access_code,
                vpc_Amount: total,
                vpc_Command: "pay",
                vpc_Locale: "en",
                vpc_MerchTxnRef: reference,
                vpc_Merchant: configMap.merchantId,
                vpc_OrderInfo: orderId,
                vpc_Version: "1",
                vpc_CardExp: cardInfo.expiryYear + cardInfo.expiryMonth
        ]
        String url = isLive ? "https://migs.mastercard.com.au/vpcdps" : "https://migs.mastercard.com.au/vpcdps"
        String truncatedCardNo = cardInfo.cardNumber.substring(0, 4) + "...." + cardInfo.cardNumber.substring (cardInfo.cardNumber.length() - 4)
        Map logConf = [
                loggerName: "ANZLogger",
                replaces: [(cardInfo.cardNumber): truncatedCardNo]
        ]
        String response = HttpUtil.doPostRequest(url, AppUtil.getQueryStringFromMap(model), [:], true, logConf)
        Map properties = AppUtil.getURLQueryMap(response)
        String status = (properties.vpc_AcqResponseCode == "00") ? DomainConstants.PAYMENT_STATUS.SUCCESS : DomainConstants.PAYMENT_STATUS.FAILED
        PaymentInfo info = new PaymentInfo()
        info.amount = properties["vpc_Amount"].toDouble()
        info.trackInfo = properties.vpc_TransactionNo
        info.gatewayResponse = properties.vpc_Message
        info.success = status == DomainConstants.PAYMENT_STATUS.SUCCESS
        info.payerInfo = AppUtil.loggedCustomer ? "Customer#" +  AppUtil.loggedCustomer :"Guest Customer" + " (" + truncatedCardNo + ")"
        info.paymentRef = properties.vpc_MerchTxnRef.toLong()
        return info
    }

    PaymentInfo processApiPayment(Map params) {
        def session = AppUtil.session
        Cart cart = CartManager.getCart(session.id, true)
        CardInfo cardInfo = new CardInfo(params.vpc_CardNum, params.vpc_CardSecurityCode, params.cardExpiryMonth, params.cardExpiryYear)
        return processApiPayment(cardInfo, cart.tagged.payable + cart.tagged.surcharge, cart.orderId, cart.tagged["payment"].id)
    }
}
