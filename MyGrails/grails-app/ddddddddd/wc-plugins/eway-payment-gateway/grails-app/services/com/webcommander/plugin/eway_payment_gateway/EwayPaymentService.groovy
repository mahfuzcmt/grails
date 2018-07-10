package com.webcommander.plugin.eway_payment_gateway

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
import com.webcommander.util.Base64Coder
import com.webcommander.util.HttpUtil
import com.webcommander.webcommerce.CreditCard
import com.webcommander.webcommerce.Order
import com.webcommander.webcommerce.Payment
import com.webcommander.webcommerce.PaymentGatewayMeta
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import groovy.xml.MarkupBuilder

import javax.servlet.http.HttpSession

@Transactional
class EwayPaymentService {

    static {
        HookManager.register("before-EWAY-wallet-save", { CreditCard card, Map params ->
            def ewayPaymentService = AppUtil.getBean(EwayPaymentService)
            String response = ewayPaymentService.sendTokenRequest(params)
            return ewayPaymentService.saveCreditCard(card, response)
        })
    }

    private String sendTokenRequest(Map cardInfo) {
        Customer customer = Customer.load(AppUtil.loggedCustomer)
        Map paymentInfo = [:]
        PaymentGatewayMeta.findAllByFieldFor(DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.EWAY).each {
            if(it.name == "walletMode") {
                paymentInfo.walletMode = it.value
            } else if(it.name == "walletMerchantApiKey") {
                paymentInfo.walletMerchantApiKey = it.value
            } else if(it.name == "walletMerchantPassword") {
                paymentInfo.walletMerchantPassword = it.value
            }
        }
        String endPoint = "https://api.sandbox.ewaypayments.com/Transaction"

        String authString = Base64Coder.encode(paymentInfo.walletMerchantApiKey + ":" + paymentInfo.walletMerchantPassword)
        Map requestMap = [
            Customer: [
                FirstName: customer.firstName,
                LastName: customer.lastName,
                country: customer.address.country.code.toLowerCase(),
                CardDetails: [
                    Name       : cardInfo.cardHolderName,
                    Number     : cardInfo.cardNumber,
                    ExpiryMonth: cardInfo.expireMonth,
                    ExpiryYear : cardInfo.expireYear,
                    CVN        : cardInfo.cvn
                ]
            ],
            Payment : [
                TotalAmount: 0
            ],
            Method  : "CreateTokenCustomer",
            TransactionType: "Purchase"
        ]

        String jsonBody = requestMap as JSON
        String content = HttpUtil.doPostRequest(endPoint, jsonBody, [
                Authorization : "Basic " + authString,
                "Content-Type": "application/json"
        ])

        return content
    }

    private CreditCard saveCreditCard(CreditCard card, String response) {
        Map resp = JSON.parse(response)
        if(resp.Errors) {
            String message = GatewayResponse.TOKEN_PAYMENT[resp.Errors.split(",")[0]]
            throw new ApplicationRuntimeException(message)
        }
        Map cardCustomer = resp.Customer
        Map cardDetails = cardCustomer.CardDetails

        card.cardHolderName = cardDetails.Name
        card.cardMonth = cardDetails.ExpiryMonth
        card.cardYear = cardDetails.ExpiryYear
        card.gatewayName = DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.EWAY
        card.gatewayToken = cardCustomer.TokenCustomerID

        return card
    }

    public PaymentInfo resolveInfo(String accessPaymentCode) {
        TransactionResponse result = checkAccessCode(accessPaymentCode);
        PaymentInfo info = new PaymentInfo();
        info.amount = result.returnAmount
        info.trackInfo = "" + (result.trxnNumber ?: '')
        info.gatewayResponse = result.trxnResponseMessage
        info.success = result.trxnStatus
        info.paymentRef = result.merchantReference.toLong()
        return info;
    }

    public TransactionResponse checkAccessCode(String accessPaymentCode) {
        String customerId;
        String userName;
        boolean isLive = true
        PaymentGatewayMeta.findAllByFieldFor(DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.EWAY).each {
            if(it.name == "customerId") {
                customerId = it.value
            } else if(it.name == "userName") {
                userName = it.value
            } else if(it.name == "mode") {
                isLive = it.value == "live"
            }
        }
        Map requestMap = [
            CustomerID: isLive ? customerId : "87654321",
            AccessPaymentCode: accessPaymentCode,
            UserName: userName
        ]
        String url = "https://au.ewaygateway.com/Result?" + HttpUtil.serializeMap(requestMap);
        try {
            String resultXML = HttpUtil.doGetRequest(url);
            return parseSharedXMLResult(resultXML);
        } catch (Exception e) {
            return null;
        }
    }

    private TransactionResponse parseSharedXMLResult(String resultXML) {
        TransactionResponse result = new TransactionResponse();
        XmlParser parser = new XmlParser();
        Node node = parser.parse(new StringReader(resultXML))
        result.authCode = node.AuthCode.text()
        result.responseCode = node.ResponseCode.text()
        result.returnAmount = node.ReturnAmount.text().toDouble()
        result.trxnNumber = node.TrxnNumber.text().toInteger(0)
        result.trxnStatus = node.TrxnStatus.text().toBoolean(false)
        result.trxnResponseMessage = node.TrxnResponseMessage.text()
        result.merchantOption1 = node.MerchantOption1.text()
        result.merchantOption2 = node.MerchantOption2.text()
        result.merchantOption3 = node.MerchantOption3.text()
        result.merchantReference = node.MerchantReference.text()
        result.merchantInvoice = node.MerchantInvoice.text()
        result.errorMessage = node.ErrorMessage.text()
        return result;
    }

    private TransactionResponse parseDirectXMLResult(String resultXML) {
        TransactionResponse result = new TransactionResponse();
        XmlParser parser = new XmlParser();
        Node node = parser.parse(new StringReader(resultXML))
        result.authCode = node.ewayAuthCode.text()
        result.returnAmount = node.ewayReturnAmount.text().toDouble()
        result.trxnNumber = node.ewayTrxnNumber.text().toInteger(0)
        result.trxnStatus = node.ewayTrxnStatus.text().toBoolean()
        result.merchantOption1 = node.ewayTrxnOption1.text()
        result.merchantOption2 = node.ewayTrxnOption2.text()
        result.merchantOption3 = node.ewayTrxnOption3.text()
        result.merchantReference = node.ewayTrxnReference.text()
        result.errorMessage = node.ewayTrxnError.text()
        return result;
    }

    public PaymentInfo processApiPayment(CardInfo cardInfo, AddressData address, Double amount, Long orderId, Long paymentId) {
        List<PaymentGatewayMeta> configs = PaymentGatewayMeta.findAllByFieldFor(DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.EWAY)
        String customerId
        Boolean isLive
        configs.each {
            if(it.name == "customerId") {
                customerId = it.value
            } else if(it.name == "mode") {
                isLive = it.value == "live"
            }
        }
        if(!isLive) {
            customerId = "87654321"
        }
        StringWriter writer = new StringWriter();
        MarkupBuilder xml = new MarkupBuilder(writer);
        xml.ewaygateway {
            ewayCustomerID(customerId)
            ewayTotalAmount(Math.round(amount * 100).toString())
            ewayCardHoldersName(cardInfo.holderName)
            ewayCardNumber(cardInfo.cardNumber)
            ewayCardExpiryMonth(cardInfo.expiryMonth)
            ewayCardExpiryYear(cardInfo.expiryYear)
            ewayCustomerInvoiceDescription("")
            ewayTrxnNumber("" + paymentId)
            ewayCustomerInvoiceRef("" + orderId)
            ewayCVN(cardInfo.cvv)
            ewayCustomerFirstName(address.firstName)
            ewayCustomerLastName(address.lastName)
            ewayCustomerEmail(address.email)
            ewayCustomerAddress(address.addressLine1)
            ewayCustomerPostcode(address.postCode)
            ewayOption1("")
            ewayOption2("")
            ewayOption3("")
        }
        String truncatedCardNo = cardInfo.cardNumber.substring(0, 4) + "...." + cardInfo.cardNumber.substring (cardInfo.cardNumber.length() - 4)
        String url = isLive ? "https://www.eway.com.au/gateway_cvn/xmlpayment.asp" : "https://api.sandbox.ewaypayments.com/gateway/Xml/CvnXmlPaymentRequestHandler.ashx"
        Map loggerConf = [
            loggerName: "EwayLogger",
            replaces: [(cardInfo.cardNumber): truncatedCardNo]
        ]
        String response = HttpUtil.doPostRequest(url, writer.toString(), [:], true, loggerConf)
        TransactionResponse result = parseDirectXMLResult(response);
        PaymentInfo info = new PaymentInfo();
        info.amount = result.returnAmount / 100
        info.trackInfo = "" + result.trxnNumber
        info.gatewayResponse = result.trxnResponseMessage
        info.success = result.trxnStatus
        info.payerInfo = truncatedCardNo
        info.paymentRef = result.merchantReference.toLong()
        return info;
    }

    public PaymentInfo processApiPayment(Map params) {
        HttpSession session = AppUtil.session
        Cart cart = CartManager.getCart(session.id, true)
        CardInfo cardInfo = new CardInfo(params.holder, params.number, params.cvn, params.expiryMonth, params.expiryYear)
        AddressData address = session.effective_billing_address
        if(!address && AppUtil.session.customer) {
            Customer customer = Customer.get(AppUtil.session.customer)
            address = new AddressData(customer.address)
        }
        return processApiPayment(cardInfo, address, cart.tagged.payable + cart.tagged.surcharge, cart.orderId, cart.tagged.payment.id)
    }

    PaymentInfo processWalletPayment(Map params) {
        HttpSession session = AppUtil.session
        Cart cart = CartManager.getCart(session.id, true)
        Double amount = cart.tagged.payable + cart.tagged.surcharge;
        Order order = Order.load(cart.orderId)
        Payment payment = Payment.load(cart.tagged.payment.id)
        CreditCard card = CreditCard.load(params.walletPayment)

        def configs = PaymentGatewayMeta.findAllByFieldFor(DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.EWAY)
        Map configMap = [:]
        configs.each {
            configMap[it.name] = it.value;
        }
        String endPoint = "https://api.sandbox.ewaypayments.com/Transaction"
        String authString = Base64Coder.encode(configMap.walletMerchantApiKey + ":" + configMap.walletMerchantPassword)

        String payMap = [
            Customer       : [
                TokenCustomerID: card.token
            ],
            Payment        : [
                TotalAmount: amount.round(2)
            ],
            Method         : "ProcessPayment",
            TransactionType: "Recurring"
        ] as JSON

        String resp = HttpUtil.doPostRequest(endPoint, payMap, [
                Authorization : "Basic " + authString,
                "Content-Type": "application/json"
        ])
        Map respMap = JSON.parse(resp)

        PaymentInfo info = new PaymentInfo()
        String status = respMap.TransactionStatus ? DomainConstants.PAYMENT_STATUS.SUCCESS : DomainConstants.PAYMENT_STATUS.FAILED;
        info.paymentRef = payment.id
        info.success = status == DomainConstants.PAYMENT_STATUS.SUCCESS;
        info.amount = respMap.Payment.TotalAmount
        info.payerInfo = AppUtil.loggedCustomer ? "Customer#" +  AppUtil.loggedCustomer : "Guest Customer" + "(" + card.cardNumber + ")";
        info.gatewayResponse = respMap.responseCode
        info.trackInfo = respMap.transactionId
        if(status == DomainConstants.PAYMENT_STATUS.FAILED) {
            String errorMessage = "could.not.process.payment.contact.with.vendor"
            throw new PaymentGatewayException(errorMessage, [], info)
        }
        card.plusPaymentCount()
        card.plusPaymentTotal(amount)
        card.save()

        return info
    }
}
