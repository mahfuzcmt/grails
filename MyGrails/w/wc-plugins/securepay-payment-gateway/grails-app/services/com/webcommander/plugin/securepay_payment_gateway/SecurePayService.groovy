package com.webcommander.plugin.securepay_payment_gateway

import com.webcommander.ApplicationTagLib
import com.webcommander.AutoGeneratedPage
import com.webcommander.admin.Customer
import com.webcommander.constants.DomainConstants
import com.webcommander.manager.CartManager
import com.webcommander.manager.HookManager
import com.webcommander.models.AddressData
import com.webcommander.models.CardInfo
import com.webcommander.models.Cart
import com.webcommander.models.PaymentInfo
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.throwables.PaymentGatewayException
import com.webcommander.util.AppUtil
import com.webcommander.util.HttpUtil
import com.webcommander.webcommerce.*
import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.xml.MarkupBuilder
import org.apache.commons.lang.RandomStringUtils

import javax.servlet.http.HttpSession
import java.security.MessageDigest
import java.text.SimpleDateFormat

import static com.webcommander.constants.DomainConstants.SITE_CONFIG_TYPES

@Transactional
class SecurePayService {
    private String randomString
    static ApplicationTagLib _app
    static getApp() {
        return _app ?: (_app = Holders.grailsApplication.mainContext.getBean(ApplicationTagLib))
    }

    static {
        HookManager.register("before-SECUREPAY-wallet-save", { CreditCard card, Map params ->
            def securePayService = AppUtil.getBean(SecurePayService)
            String response = securePayService.sendTokenRequest(params)
            return securePayService.saveCreditCard(card, response, params)
        })
    }

    private String sendTokenRequest(Map cardInfo, String token = "") {
        Customer customer = Customer.load(AppUtil.loggedCustomer)
        Map paymentInfo = [:]
        PaymentGatewayMeta.findAllByFieldFor(DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.SECUREPAY).each {
            if(it.name == "walletMode") {
                paymentInfo.walletMode = it.value
            } else if(it.name == "walletMerchantId") {
                paymentInfo.walletMerchantId = it.value
            } else if(it.name == "walletMerchantPassword") {
                paymentInfo.walletMerchantPassword = it.value
            }
        }
        String url = paymentInfo.walletMode == "live" ? "https://api.securepay.com.au/xmlapi/periodic" : "https://test.api.securepay.com.au/xmlapi/periodic"

        long timestamp = System.currentTimeMillis()
        String messageId = timestamp.toString().encodeAsMD5()
        String messageTimeStamp = new SimpleDateFormat("yyyyMMddHHmmssZ").format(new Date())
        String caroNo = cardInfo.cardNumber
        String crn = customer.id + "-" + caroNo.substring(0, 2) + "." + caroNo.substring(caroNo.length() - 4, caroNo.length()) + "-" + messageId;

        StringWriter writer = new StringWriter()
        MarkupBuilder xml = new MarkupBuilder(writer)

        xml.SecurePayMessage {
            MessageInfo {
                messageID(messageId)
                messageTimestamp(messageTimeStamp)
                timeoutValue(60)
                apiVersion("spxml-4.2")
            }
            MerchantInfo {
                merchantID(paymentInfo.walletMerchantId)
                password(paymentInfo.walletMerchantPassword)
            }
            RequestType("Periodic")
            Periodic {
                PeriodicList(count: "1") {
                    PeriodicItem(ID: "1") {
                        actionType("add")
                        clientID(crn.substring(0, 20).toUpperCase())
                        CreditCardInfo{
                            cardNumber(caroNo)
                            cvv(cardInfo.cvn)
                            expiryDate(cardInfo.expireMonth + "/" + cardInfo.expireYear)
                        }
                        amount(1)
                        periodicType(4)
                    }
                }
            }
        }

        String response = HttpUtil.doPostRequest(url, writer.toString())
        return response
    }

    private CreditCard saveCreditCard(CreditCard card, String response, Map params) {
        def root = new XmlSlurper().parseText(response)
        if(root.Status && root.Status.statusCode.text() == "0") {
            def item = root.Periodic.PeriodicList.PeriodicItem
            if(item.responseCode.text() != "00") {
                card.discard()
                throw new ApplicationRuntimeException(item.responseText.text())
            }
            card.gatewayToken = item.clientID.text()
            def cardInfo = item.CreditCardInfo
            def expire = cardInfo.expiryDate.text().split("/")
            card.cardMonth = expire[0]
            card.cardYear = expire[1]

            card.cardHolderName = params.cardHolderName
            card.gatewayName = DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.SECUREPAY
        } else {
            card.discard()
        }
        return card
    }

    private String getMessageId(Long orderId, Long paymentId) {
        String instance = AppUtil.getConfig(SITE_CONFIG_TYPES.ADMINISTRATION, "instance_id")
        instance = instance ?: (randomString ?: (randomString = RandomStringUtils.random(8, true, true)))
        return "WC" + instance + "O" +  orderId + "P" + paymentId
    }

    private String getMessageTimeStamp() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyddMMHHmmssSSS000")
        int tzOffsetMin = (cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET))/(1000*60);
        return dateFormat.format(cal.getTime()) + (tzOffsetMin >= 0 ? "+" : "") + tzOffsetMin.toString()
    }

    PaymentInfo resolveInfo(Map params) {
        def configs = PaymentGatewayMeta.findAllByFieldFor(DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.SECUREPAY)
        Map configMap = [:]
        configs.each {
            configMap[it.name] = it.value
        }
        String shaString = configMap.merchantId + "|" + configMap.transaction_password + "|" + params.refid + "|" + params.amount  + "|" + params.timestamp + "|" + params.summarycode
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
        shaString = messageDigest.digest(shaString.getBytes()).encodeHex().toString();
        if(shaString != params.fingerprint) {
            return null
        }
        PaymentInfo info = new PaymentInfo()
        def session = AppUtil.session
        String status = (params.rescode == "00" || params.rescode == "08" || params.rescode == "11") ? DomainConstants.PAYMENT_STATUS.SUCCESS : DomainConstants.PAYMENT_STATUS.FAILED
        info.amount = params.amount.toDouble() / 100
        info.paymentRef = params["refid"].toLong()
        info.payerInfo =  session.customer ? "Customer#" +  session.customer : "Guest Customer" + " (" + params.pan + ")"
        info.gatewayResponse = params.restext
        info.success = status == DomainConstants.PAYMENT_STATUS.SUCCESS
        info.trackInfo = params.txnid

        return info
    }

    public def securepaySecureFrame(Map configMap, Cart cart) {
        HttpSession session = AppUtil.session
        AddressData billingAddress = session.effective_billing_address;
        AddressData shippingAddress = session.effective_shipping_address;
        boolean isLive
        if(configMap["mode"] == "live") {
            isLive = true
        }
        Currency currency = AppUtil.baseCurrency
        if(currency.code != "AUD") {
            throw new ApplicationRuntimeException("could.not.contact.payment.provider.sorry.inconvenience")
        }
        AutoGeneratedPage page = AutoGeneratedPage.findByName(DomainConstants.AUTO_GENERATED_PAGES.PAYMENT_SUCCESS_PAGE);
        String successScheme = page.isHttps ? "https" : "http"
        String transactionType = "0"
        Long total = Math.round((cart.tagged.payable + cart.tagged.surcharge) * 100)
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddkkmmss")
        String timeStamp = dateFormat.format(new Date().gmt())
        String reference = "" + cart.tagged.payment.id
        String shaString = configMap.merchantId + "|" + configMap.transaction_password + "|" + transactionType + "|" + reference + "|" + total + "|" + timeStamp
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-1")
        def fingerPrint = messageDigest.digest(shaString.getBytes()).encodeHex()
        Map models = [
            bill_name: "transact",
            merchant_id: configMap["merchantId"],
            txn_type: transactionType,
            primary_ref: reference,
            amount: "" + total,
            currency: currency.code,
            fp_timestamp: timeStamp,
            fingerprint: fingerPrint.toString(),
            display_receipt: "no",
            display_cardholder_name: "yes",
            billing_country: billingAddress.countryCode ?: "",
            delevery_country: shippingAddress.countryCode ?: "",
            emai_address: billingAddress.email,
            callback_url: app.baseUrl(scheme: "https")  + "securePay/paymentNotify",
            return_url: app.baseUrl(scheme: successScheme) + "securePay/paymentReturn",
            return_url_text: "Continue...",
            return_url_target: "top",// self, new, parent or top.
            cancel_url_text: "Cancel",
            cancel_url_target: "top"
        ]
        String requestUrl = isLive ? "https://payment.securepay.com.au/live/v2/invoice" : "https://payment.securepay.com.au/test/v2/invoice"
        return [models: models, requestUrl: requestUrl]
    }

    private TransactionResponse parseDirectXMLResult(String resultXML) {
        TransactionResponse result = new TransactionResponse()
        XmlParser parser = new XmlParser()
        Node node = parser.parse(new StringReader(resultXML))
        result.messageID = node.MessageInfo.messageID.text()
        result.merchantID = node.MerchantInfo.merchantID.text()
        result.statusCode = node.Status.statusCode.text()
        result.statusDescription = node.Status.statusDescription.text()
        def txn = node.Payment.TxnList.Txn
        if(txn) {
            result.txnType = txn.txnType.text()
            result.amount = txn.amount.text().toDouble()
            result.purchaseOrderNo = txn.purchaseOrderNo.text().toInteger()
            result.approved = txn.approved.text() == "Yes"
            result.responseCode = txn.responseCode.text()
            result.responseText = txn.responseText.text()
            result.txnID = txn.txnID.text()
        }
        return result
    }

    PaymentInfo processApiPayment(CardInfo cardInfo, Double amounts, Long orderId, Long paymentId) {
        def configs = PaymentGatewayMeta.findAllByFieldFor(DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.SECUREPAY)
        Map configMap = [:]
        configs.each {
            configMap[it.name] = it.value
        }
        boolean isLive
        if(configMap["mode"] == "live") {
            isLive = true
        }

        StringWriter writer = new StringWriter()
        MarkupBuilder xml = new MarkupBuilder(writer)
        xml.SecurePayMessage {
            MessageInfo {
                messageID(getMessageId(orderId, paymentId))
                messageTimestamp(messageTimeStamp)
                timeoutValue(80)
                apiVersion("xml-4.2")
            }
            MerchantInfo {
                merchantID(configMap["merchantId"])
                password(configMap["transaction_password"])
            }
            RequestType("Payment")
            Payment {
                TxnList(count: "1") {
                    Txn(ID: "1") {
                        txnType(0)
                        txnSource(23)
                        amount(Math.round(amounts * 100))
                        currency("AUD")
                        purchaseOrderNo(orderId)
                        CreditCardInfo {
                            cardNumber(cardInfo.cardNumber)
                            cvv(cardInfo.cvv)
                            expiryDate( cardInfo.expiryMonth + "/" + cardInfo.expiryYear)
                        }
                    }
                }
            }
        }
        String url = isLive ? "https://api.securepay.com.au/xmlapi/payment" : "https://test.securepay.com.au/xmlapi/payment"
        String truncatedCardNo = cardInfo.cardNumber.substring(0, 4) + "...." + cardInfo.cardNumber.substring (cardInfo.cardNumber.length() - 4)
        Map logConf = [
                loggerName: "SecurePayLogger",
                replaces: [
                        (cardInfo.cardNumber): truncatedCardNo
                ]
        ]
        String response = HttpUtil.doPostRequest(url, writer.toString(), [:], true, logConf)
        TransactionResponse result = parseDirectXMLResult(response)
        PaymentInfo info = new PaymentInfo()
        info.amount = result.amount / 100;
        info.trackInfo = "" + result.txnID
        info.gatewayResponse = result.responseText
        info.success = result.approved
        info.payerInfo = AppUtil.loggedCustomer ? "Customer#" +  AppUtil.loggedCustomer :"Guest Customer" + " (" +  truncatedCardNo + ")"
        info.paymentRef = paymentId
        if(!info.success) {
            String errorMessage = result.responseText ?: "could.not.process.payment.contact.with.vendor"
            throw new PaymentGatewayException(errorMessage, [], info)
        }
        return info
    }

    PaymentInfo processApiPayment(Map params) {
        def session = AppUtil.session
        Cart cart = CartManager.getCart(session.id, true)
        def total = (cart.tagged.payable + cart.tagged.surcharge)
        CardInfo cardInfo = new CardInfo(params["EPS_CARDNUMBER"], params["EPS_CCV"], params["EPS_EXPIRYMONTH"], params["EPS_EXPIRYYEAR"])
        return processApiPayment(cardInfo, total, cart.orderId, cart.tagged["payment"].id)
    }

    PaymentInfo processWalletPayment(Map params) {
        def session = AppUtil.session
        Cart cart = CartManager.getCart(session.id, true)
        Double total = (cart.tagged.payable + cart.tagged.surcharge)
        Order order = Order.load(cart.orderId)
        Payment payment = Payment.load(cart.tagged.payment.id)
        CreditCard card = CreditCard.load(params.walletPayment)

        def configs = PaymentGatewayMeta.findAllByFieldFor(DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.SECUREPAY)
        Map configMap = [:]
        configs.each {
            configMap[it.name] = it.value;
        }

        String url = configMap.walletMode == "live" ? "https://api.securepay.com.au/xmlapi/periodic" : "https://test.securepay.com.au/xmlapi/periodic"
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssZ")
        String messageTimeStamp = dateFormat.format(new Date())
        String messageId = messageTimeStamp.encodeAsMD5()

        StringWriter writer = new StringWriter()
        MarkupBuilder xml = new MarkupBuilder(writer)
        xml.SecurePayMessage {
            MessageInfo {
                messageID(messageId)
                messageTimestamp(messageTimeStamp)
                timeoutValue(60)
                apiVersion("spxml-4.2")
            }
            MerchantInfo {
                merchantID(configMap.walletMerchantId)
                password(configMap.walletMerchantPassword)
            }
            RequestType("Periodic")
            Periodic {
                PeriodicList(count: 1) {
                    PeriodicItem(ID: 1) {
                        actionType("trigger")
                        periodicType(8)
                        clientID(card.token)
                        transactionReference(order.id)
                        amount(Math.round(total * 100))
                        currency("AUD")
                    }
                }
            }
        }

        String response = HttpUtil.doPostRequest(url, writer.toString())
        def root = new XmlSlurper().parseText(response)
        PaymentInfo info = new PaymentInfo()

        info.success = root.Status.statusCode.text() == "0"
        def periodicItem = root.Periodic.PeriodicList.PeriodicItem

        info.amount = periodicItem.amount.text().toLong(0) / 100
        info.trackInfo = periodicItem.txnID.text()
        info.gatewayResponse = periodicItem.responseText.text()
        info.payerInfo = AppUtil.loggedCustomer ? "Customer#" +  AppUtil.loggedCustomer :"Guest Customer" + " (" + card.cardNumber + ")"
        info.paymentRef = payment.id
        if(!info.success) {
            String errorMessage = periodicItem.responseText.text() ?: "could.not.process.payment.contact.with.vendor"
            throw new PaymentGatewayException(errorMessage, [], info)
        }

        card.plusPaymentCount()
        card.plusPaymentTotal(total)
        card.save()

        return info
    }
}
