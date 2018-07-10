package com.webcommander.plugin.afterpay_payment_gateway

import com.webcommander.ApplicationTagLib
import com.webcommander.admin.Customer
import com.webcommander.models.AddressData
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.util.AppUtil
import com.webcommander.util.Base64Coder
import com.webcommander.util.HttpUtil
import com.webcommander.webcommerce.PaymentGatewayMeta
import grails.util.Holders
import grails.converters.JSON
import com.webcommander.models.Cart
import com.webcommander.models.PaymentInfo
import grails.gorm.transactions.Transactional

import javax.servlet.http.HttpSession

@Transactional
class AfterpayService {

    static ApplicationTagLib _app

    static AFTERPAY_SERVICE_URL = [
        TEST: "https://api-sandbox.secure-afterpay.com.au/v1/",
        LIVE: "https://api.secure-afterpay.com.au/v1/"
    ]

    static getApp() {
        return _app ?: (_app = Holders.grailsApplication.mainContext.getBean(ApplicationTagLib))
    }

    public PaymentInfo resolveCancelInfo(Map response, Map params) {
        PaymentInfo info = new PaymentInfo();
        Boolean status = false
        info.amount = Double.parseDouble(response.totalAmount.amount)
        info.paymentRef = Long.parseLong(response.merchantReference)
        info.success = status
        info.gatewayResponse = params.status
        info.payerInfo = response.consumer.givenNames
        info.trackInfo = "Empty"
        return info
    }

    public PaymentInfo resolveConfirmInfo(Map response, Map params) {
        PaymentInfo info = new PaymentInfo();
        Boolean status = false;
        if ( response.status.equals("APPROVED") ) {
            status = true
        }
        info.amount = Double.parseDouble(response.totalAmount.amount)
        info.paymentRef = Long.parseLong(response.merchantReference)
        info.success = status
        info.gatewayResponse = response.status
        info.payerInfo = response.orderDetails.consumer.givenNames
        info.trackInfo = response.id
        return info
    }

    public Map getToken(Cart cart) {
        String cancelScheme = "http";
        String successScheme = "http";

        AddressData customerAddress = getCustomerAddress()
        Map config = getAfterpayConfig()
        Map logConfig = new HashMap()
        String request = config.serviceUrl + "orders"

        if ( customerAddress.lastName == null || customerAddress.lastName.equals("") ) {
            customerAddress.lastName = customerAddress.firstName;
        }

        Map orderInfo = [
            totalAmount: [
                amount: (cart.tagged.payable + cart.tagged.surcharge).toPrice(),
                currency: "AUD"
            ],
            consumer: [
                givenNames: customerAddress.firstName,
                surname: customerAddress.lastName,
                email: customerAddress.email
            ],
            merchant: [
                redirectConfirmUrl: app.baseUrl(scheme: cancelScheme) + "afterpay/confirmPayment",
                redirectCancelUrl: app.baseUrl(scheme: successScheme) + "afterpay/cancelPayment"
            ],
            merchantReference: cart.tagged.payment.id
        ]
        String data = orderInfo as JSON
        String postData = data.toString();

        Map requestProperty = [
            "Accept-Language": "en-US,en;q=0.5",
            "Accept": "application/json",
            "Content-Type": "application/json",
            "Authorization": config.authCode
        ]

        try {
            String apiResponse = HttpUtil.doPostRequest(request, postData, requestProperty, true, logConfig )
            Map response = JSON.parse(apiResponse);
            return response
        } catch (Throwable e) {
            throw new ApplicationRuntimeException("could.not.contact.afterpay.payment.gateway.sorry.inconvenience")
        }

    }

    public Map getOrder(params) {
        Map config = getAfterpayConfig()
        String request = config.serviceUrl + "orders/" + params.orderToken
        Map requestProperty = [
            Authorization: config.authCode
        ]

        try {
            String apiResponse = doGetRequest(request, requestProperty) ;
            Map response = JSON.parse(apiResponse);
            return response
        } catch (Throwable e) {
            throw new ApplicationRuntimeException("could.not.contact.afterpay.payment.gateway.sorry.inconvenience")
        }
    }

    public Map capturePayment(response, params) {
        if (!params.status.equals("CANCELLED")) {
            Map logConfig = new HashMap();
            Map config = getAfterpayConfig();
            String request = config.serviceUrl + "payments/capture";
            Map requestProperty = [
                    "Accept-Language": "en-US,en;q=0.5",
                    "Accept": "application/json",
                    "Content-Type": "application/json",
                    "Authorization": config.authCode
            ]
            Map requestParameters = [
                    token: response.token,
                    merchantReference: response.merchantReference
            ]

            def postData = (requestParameters as JSON).toString();

            try {
                String apiResponse = HttpUtil.doPostRequest(request, postData, requestProperty, true, logConfig )
                return JSON.parse(apiResponse);
            } catch (Throwable e) {
                throw new ApplicationRuntimeException("could.not.contact.afterpay.payment.gateway.sorry.inconvenience")
            }
        }
    }



    public AddressData getCustomerAddress(){
        HttpSession session = AppUtil.session
        AddressData address = session.effective_billing_address
        if(!address && AppUtil.session.customer) {
            Customer customer = Customer.get(AppUtil.session.customer)
            address = new AddressData(customer.address)
        }
        return address
    }

    public Map getAfterpayConfig(){
        Map afterpayConfig = new HashMap()
        def configs = PaymentGatewayMeta.findAllByFieldFor("APY")

        configs.each {
            if(it.name == "mode") {
                afterpayConfig.serviceUrl = it.value == 'live' ? AFTERPAY_SERVICE_URL.LIVE : AFTERPAY_SERVICE_URL.TEST;
            }
            afterpayConfig[it.name] = it.value;
        }

        String authString = afterpayConfig.merchantId + ":" + afterpayConfig.merchantSecretKey;
        afterpayConfig.authCode = "Basic " + Base64Coder.encode(authString)
        return afterpayConfig
    }

    public static String doGetRequest(String server, Map requestProperty) throws IOException {
        URL url = new URL( server );
        URLConnection conn = url.openConnection();
        conn.setDoOutput( true );
        conn.setInstanceFollowRedirects( false );
        conn.setRequestMethod( "GET" );
        requestProperty.each {
            conn.setRequestProperty(it.key, it.value);
        }
        conn.setUseCaches( false );
        return HttpUtil.getResponseText(conn)
    }

    public static PaymentGatewayMeta getPaymentGatewayMeta (String name) {
        return PaymentGatewayMeta.findByFieldForAndName("APY", name);
    }

    public static boolean isInstallmentShowActive() {
        return (getPaymentGatewayMeta("showInstallmentAmount").value).toBoolean();
    }

    public static void updateMetaValue(String name, String value) {
        PaymentGatewayMeta paymentGatewayMeta = getPaymentGatewayMeta(name);
        paymentGatewayMeta.value = value;
        paymentGatewayMeta.save();
    }

    public static String getValue(String name) {
        return getPaymentGatewayMeta(name).value;
    }
}
