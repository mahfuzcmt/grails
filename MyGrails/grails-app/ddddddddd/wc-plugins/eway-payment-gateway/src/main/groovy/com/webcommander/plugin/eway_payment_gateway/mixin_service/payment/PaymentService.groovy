package com.webcommander.plugin.eway_payment_gateway.mixin_service.payment

import com.webcommander.ApplicationTagLib
import com.webcommander.AutoGeneratedPage
import com.webcommander.admin.Customer
import com.webcommander.constants.DomainConstants
import com.webcommander.log.WcLogManager
import com.webcommander.models.AddressData
import com.webcommander.models.CardInfo
import com.webcommander.models.Cart
import com.webcommander.models.PaymentInfo
import com.webcommander.plugin.eway_payment_gateway.EwayPaymentService
import com.webcommander.plugin.eway_payment_gateway.TransactionRequestResult
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.util.AppUtil
import com.webcommander.util.HttpUtil
import com.webcommander.webcommerce.Payment
import com.webcommander.webcommerce.PaymentGatewayMeta
import grails.util.Holders

import javax.servlet.http.HttpSession

/**
 * Created by zobair on 08/02/14.*/
class PaymentService {
    static ApplicationTagLib _app
    static getApp() {
        if(!_app) {
            return _app = Holders.grailsApplication.mainContext.getBean(ApplicationTagLib)
        }
        return _app
    }
    private static EwayPaymentService _ewayPaymentService
    private static EwayPaymentService getEwayPaymentService() {
        return _ewayPaymentService ?: (_ewayPaymentService = Holders.grailsApplication.mainContext.getBean(EwayPaymentService))
    }
    void renderEWAYCRDPaymentPage(Cart cart, Closure renderer) {
        def configs = PaymentGatewayMeta.findAllByFieldFor(DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.EWAY)
        def type = configs.find {it.name == "type"}.value
        "render${type}EWAYCRDPaymentPage"(cart, configs, renderer)
    }

    private TransactionRequestResult parseResults(String xmlText) throws Exception {
        XmlParser xml = new XmlParser();
        Node node = xml.parse(new StringReader(xmlText));
        TransactionRequestResult result = new TransactionRequestResult();
        String value = node.Result.text();
        result.result = value.toBoolean();

        value = node.URI.text();
        result.url = value;

        value = node.Error.text();
        result.error = value;
        return result;
    }

    private void renderSHAREDEWAYCRDPaymentPage(Cart cart, List<PaymentGatewayMeta> configs, Closure renderer) {
        String customerId;
        String userName;
        boolean isLive
        configs.each {
            if(it.name == "customerId") {
                customerId = it.value
            } else if(it.name == "userName") {
                userName = it.value
            } else if(it.name == "mode") {
                isLive = it.value == "live"
            }
        }
        if(!isLive) {
            customerId = "87654321"
        }
        HttpSession session = AppUtil.session
        AddressData address = session.effective_billing_address
        if(!address && AppUtil.session.customer) {
            Customer customer = Customer.get(AppUtil.session.customer)
            address = new AddressData(customer.address)
        }

        AutoGeneratedPage page = AutoGeneratedPage.findByName(DomainConstants.AUTO_GENERATED_PAGES.CHECKOUT);
        String cancelScheme = "http";
        String successScheme = "http";
        if(page.isHttps) {
            cancelScheme = "https";
        }
        page = AutoGeneratedPage.findByName(DomainConstants.AUTO_GENERATED_PAGES.PAYMENT_SUCCESS_PAGE);
        if(page.isHttps) {
            successScheme = "https";
        }

        Map requestMap = [
            CustomerID: customerId,
            UserName: userName,
            Amount: (cart.tagged.payable + cart.tagged.surcharge).toCurrency().toPrice(),
            Currency: "AUD",
            CustomerFirstName: address.firstName,
            CustomerLastName: address.lastName,
            CustomerAddress: address.addressLine1,
            CustomerCity: address.city ?: "",
            CustomerState: address.stateName ?: "",
            CustomerPostCode: address.postCode ?: "",
            CustomerCountry: address.countryName ?: "",
            CustomerEmail: address.email,
            CustomerPhone: address.phone ?: "",
            CancelURL: app.baseUrl(scheme: cancelScheme) + "eway/cancelPayment",
            ReturnUrl: app.baseUrl(scheme: successScheme) + "eway/returnPayment",
            MerchantReference: "" + cart.tagged.payment.id,
            MerchantInvoice: "" + cart.orderId
        ]

        String log = "Action: Redirecting to Eway\nRequestData:\n${AppUtil.getQueryStringFromMap(requestMap)}\n"
        WcLogManager.log(log, "EwayLogger")
        String url = "https://au.ewaygateway.com/Request?" + HttpUtil.serializeMap(requestMap);
        String resultXML = HttpUtil.doGetRequest(url);
        try {
            TransactionRequestResult result = parseResults(resultXML);
            if (result.result) {
                AppUtil.response.sendRedirect(result.url);
                return;
            } else {
                log.error("Unexpected result # Order # " + cart.orderId + " : " + resultXML)
            }
        } catch (Throwable e) {
            log.error("EWay Processing Error # Order # " + cart.orderId, e)
        }
        throw new ApplicationRuntimeException("could.not.contact.payment.provider.sorry.inconvenience")
    }

    private void renderDIRECTEWAYCRDPaymentPage(Cart cart, List<PaymentGatewayMeta> configs, Closure renderer) {
        Map creditCardConfig = [:]
        PaymentGatewayMeta.findAllByFieldFor(DomainConstants.PAYMENT_GATEWAY_CODE.CREDIT_CARD).each {
            creditCardConfig[it.name] = it.value;
        }
        renderer(view: "/site/siteAutoPage", model: [name: DomainConstants.AUTO_GENERATED_PAGES.PAYMENT_PAGE, view: "/plugins/eway_payment_gateway/direct.gsp", creditCardConfig: creditCardConfig])
    }

    PaymentInfo processEWAYCRDAPIPayment(CardInfo cardInfo, Payment payment) {
        return ewayPaymentService.processApiPayment(cardInfo, new AddressData(payment.order.billing), payment.amount, payment.order.id, payment.id)
    }

    void processEWAYWalletPayment(Map params, Closure redirect) {
        redirect(controller: "eway", action: "processWalletPayment", params: params)
    }
}