package com.webcommander.plugin.square_payment_gateway.communicator

import com.webcommander.config.StoreDetail
import com.webcommander.constants.DomainConstants
import com.webcommander.log.WcLogManager
import com.webcommander.models.AddressData
import com.webcommander.throwables.PaymentGatewayException
import com.webcommander.util.AppUtil
import com.webcommander.util.StringUtil
import com.webcommander.webcommerce.Order
import com.webcommander.webcommerce.PaymentGatewayMeta
import grails.converters.JSON
import groovy.json.JsonSlurper

import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.HttpMethod
import org.apache.commons.httpclient.HttpStatus
import org.apache.commons.httpclient.methods.GetMethod
import org.apache.commons.httpclient.methods.PostMethod
import org.apache.commons.httpclient.methods.StringRequestEntity

class SquareCommunicator {

    private static enum HttpMethodType{
        GET,
        POST
    }

    private static final String SQUARE_CONNECT_URL = "https://connect.squareup.com"

    private static final String LOCATIONS_ENDPOINT = "/v2/locations"
    private static String OAUTH2_ENDPOINT = "/oauth2"

    private static final String TRANSACTION_POSTFIX = "/transactions"
    private static final String CHECKOUT_POSTFIX = "/checkouts"
    private static String ACCESS_TOKEN_RENEW_POSTFIX = "/access-token/renew"

    private static final String TRANSACTIONS = "transactions"
    private static String AUTHORIZE = "authorize"
    private static String TOKEN = "token"
    private static String CLIENT = "clients"

    private static String MERCHANT_PROFILE_READ = "MERCHANT_PROFILE_READ"
    private static String PAYMENTS_READ = "PAYMENTS_READ"
    private static String PAYMENTS_WRITE = "PAYMENTS_WRITE"

    public static String WEB_COMMANDER = "webCommander"
    private static final String CREDIT_CARD_PROCESSING = "CREDIT_CARD_PROCESSING"

    private static final String APPLICATION_JSON = "application/json"
    private static final String UTF_8_CHARSET = "UTF-8"

    private static String getId(String idName){
        def configs = PaymentGatewayMeta.findAllByFieldFor(DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.SQUARE)
        Map configMap = [:]
        configs.each {
            configMap[it.name] = it.value
        }
        return configMap[idName]
    }

    private static String getAuthorization(){
        String authHeader = "Bearer " + getId("accessToken")
        return authHeader
    }

    private static HttpMethod getHttpMethod(String url, HttpMethodType httpMethodType, Map postParams = null){
        URI httpURI = new URI(url)
        HttpMethod method
        if(httpMethodType == HttpMethodType.GET) {
            method = new GetMethod(httpURI.toString())
        } else{
            method = new PostMethod(httpURI.toString())
            if(postParams){
                String jsonString = postParams as JSON
                method.setRequestEntity(new StringRequestEntity(jsonString, APPLICATION_JSON, UTF_8_CHARSET))
            }
        }
        method.setRequestHeader("Content-Type", APPLICATION_JSON)
        method.setRequestHeader("Accept", APPLICATION_JSON)
        method.setRequestHeader("Authorization", getAuthorization())
        return method
    }

    private static def getResponse(HttpMethod httpMethod){
        Map response = [:]
        JsonSlurper slurper = new JsonSlurper()
        def responseBody = slurper.parseText(httpMethod.getResponseBodyAsString())
        response.put('body', responseBody)
        response.put('statusCode', httpMethod.statusCode)
        response.put('statusText', httpMethod.statusText)
        if(responseBody?.errors){
            response.put('errorMessage', responseBody.errors.detail?.first())
        }
        return response
    }

    private static def getLocationId(){
        String url = "${SQUARE_CONNECT_URL}${LOCATIONS_ENDPOINT}"
        HttpClient httpClient = new HttpClient()
        HttpMethod httpMethod = getHttpMethod(url, HttpMethodType.GET)
        httpClient.executeMethod(httpMethod)
        def response =  getResponse(httpMethod)
        List<String> locationIds = []
        if(response.statusCode == HttpStatus.SC_OK && response.body.locations){
            def locations = response.body.locations
            locations?.each { location ->
                if(location.capabilities?.contains(CREDIT_CARD_PROCESSING) && location.id == getId("locationId")){
                    locationIds.add(location.id)
                }
            }
            if(locationIds.size() > 0) {
                return locationIds.first()
            }
        }
        throwHttpResponseError(response)
        return null
    }

    public static def requestTransaction(def nonce, AddressData billingAddress, AddressData shippingAddress, Integer amount, String ref){
        Map postParams = createTransactionRequestData(nonce, billingAddress, shippingAddress, amount, ref)

        String url = "${SQUARE_CONNECT_URL}${LOCATIONS_ENDPOINT}/${getLocationId()}${TRANSACTION_POSTFIX}"

        HttpMethod httpMethod = getHttpMethod(url, HttpMethodType.POST, postParams)
        HttpClient httpClient = new HttpClient()
        httpClient.executeMethod(httpMethod)

        def response =  getResponse(httpMethod)
        if(response.statusCode == HttpStatus.SC_OK && response.body.transaction){
            Map transactionMap = createTransactionMap(response)
            return transactionMap
        }
        doLogRequest(postParams)
        throwHttpResponseError(response)
        return null
    }

    private static def createTransactionRequestData(def nonce, AddressData billingAddress, AddressData shippingAddress, Integer amount, String ref){
        Map buyerInfo = [:]
        buyerInfo.put('buyer_email_address', billingAddress.email)

        Map shippingAddressMap = createAddressDataMap(shippingAddress)
        buyerInfo.put('shipping_address', shippingAddressMap)

        Map billingAddressMap = createAddressDataMap(billingAddress)
        buyerInfo.put('billing_address', billingAddressMap)

        buyerInfo.put('reference_id', ref)

        Map paymentInfo = [:]
        paymentInfo.put('idempotency_key', StringUtil.uuid)

        Map amountMoneyMap = [:]
        amountMoneyMap.put('amount', amount)
        def currency = AppUtil.session.currency ?: AppUtil.baseCurrency
        amountMoneyMap.put('currency', currency.code)

        paymentInfo.put('amount_money', amountMoneyMap)
        paymentInfo.put('card_nonce', nonce)

        buyerInfo << paymentInfo

        return buyerInfo
    }

    private static def createAddressDataMap(AddressData addressData){
        Map addressMap = [:]
        addressMap.put('address_line_1', addressData.addressLine1)
        addressMap.put('locality', addressData.city)
        addressMap.put('administrative_district_level_1', addressData.stateCode)
        addressMap.put('postal_code', addressData.postCode)
        addressMap.put('country', addressData.countryCode)
        return addressMap
    }

    public static def getCheckoutUrl(String redirectUrl, Order order, AddressData shipping, Integer totalAmount, String paymentId){
        Map postParams = createDataForCheckout(redirectUrl, order, shipping, totalAmount, paymentId)

        def locationId = getLocationId()
        String url = "${SQUARE_CONNECT_URL}${LOCATIONS_ENDPOINT}/${locationId}${CHECKOUT_POSTFIX}"

        HttpMethod httpMethod = getHttpMethod(url, HttpMethodType.POST, postParams)
        HttpClient httpClient = new HttpClient()
        httpClient.executeMethod(httpMethod)

        def response =  getResponse(httpMethod)
        if(response.statusCode == HttpStatus.SC_OK && response.body.checkout){
            def checkoutUrl = response.body.checkout.checkout_page_url
            return checkoutUrl
        }
        doLogRequest(postParams)
        throwHttpResponseError(response)
        return null
    }

    private static def createDataForCheckout(String redirectUrl, Order order, AddressData shipping, Integer totalAmount, String paymentId){
        Map data = [:]
        data.put("redirect_url", redirectUrl)
        data.put("idempotency_key", StringUtil.uuid)
        data.put("ask_for_shipping_address", false)
        data.put("merchant_support_email", StoreDetail.first().address.email)

        Map orderMap = [:]
        orderMap.put("reference_id", paymentId)
        List<Map> lineItems = []
        lineItems.add(createProductItemMap(totalAmount))
        orderMap.put("line_items", lineItems)

        data.put("order", orderMap)
        data.put("pre_populate_buyer_email", shipping.email)

        return data
    }

    private static def createProductItemMap(Integer totalAmount){
        Map itemMap = [:]
        itemMap.put("name", "Order Items")
        itemMap.put("quantity", "1")

        Map priceMap = [:]
        priceMap.put("amount", totalAmount)

        def currency = AppUtil.session.currency ?: AppUtil.baseCurrency
        priceMap.put("currency", currency.code)

        itemMap.put("base_price_money", priceMap)

        return itemMap
    }

    public static def getTransactionInfo(def transactionId){
        String url = "${SQUARE_CONNECT_URL}${LOCATIONS_ENDPOINT}/${getLocationId()}/${TRANSACTIONS}/${transactionId}"
        HttpClient httpClient = new HttpClient()
        HttpMethod httpMethod = getHttpMethod(url, HttpMethodType.GET)
        httpClient.executeMethod(httpMethod)
        def response =  getResponse(httpMethod)
        if(response.statusCode == HttpStatus.SC_OK && response.body.transaction){
            Map transactionMap = createTransactionMap(response)
            return transactionMap
        }
        throwHttpResponseError(response)
        return null
    }

    private static def createTransactionMap(def response){
        Map transactionMap = [:]
        transactionMap.put("statusCode", response.statusCode)
        transactionMap.put("amount", response.body.transaction.tenders.first().amount_money.amount)
        transactionMap.put("paymentRef", response.body.transaction.reference_id)
        transactionMap.put("gatewayResponse", response.body.transaction.tenders.first().card_details.status)
        transactionMap.put("trackInfo", response.body.transaction.id)
        String cardBrand = response.body.transaction.tenders.first().card_details.card.card_brand
        String last4 = response.body.transaction.tenders.first().card_details.card.last_4
        transactionMap.put("cardInfo", cardBrand + last4)
        return transactionMap
    }

    private static throwHttpResponseError(def response){
        doLogError(response)
        throw new PaymentGatewayException(response.body.errors)
    }

    private static void doLogError(def response) {
        String log = "Action: IPN from Square\nError Data:\n${response.toString()}"
        WcLogManager.log(log, "SquareLogger")
    }

    private static void doLogRequest(def postParams) {
        String log = "Action: IPN from Square\nRequest Data:\n${postParams.toString()}"
        WcLogManager.log(log, "SquareLogger")
    }

    //OPEN AUTH 2

    public static String getRequestPermissionUrl(){
        String appId = getId("applicationId")

        String url = "${SQUARE_CONNECT_URL}${OAUTH2_ENDPOINT}/${AUTHORIZE}?"
        url += "client_id=${appId}"
        url += "&scope="
        url += "${MERCHANT_PROFILE_READ}%20"
        url += "${PAYMENTS_READ}%20"
        url += "${PAYMENTS_WRITE}"
        url += "&state=${WEB_COMMANDER}"

        return url
    }

    public static def requestAccessToken(String authCode, String redirectUrl){
        String url = "${SQUARE_CONNECT_URL}${OAUTH2_ENDPOINT}/${TOKEN}"
        Map postParams = [:]
        postParams.put("client_id", getId("applicationId"))
        postParams.put("client_secret", getId("applicationSecret"))
        postParams.put("code", authCode)
        postParams.put("redirect_uri", redirectUrl)

        HttpMethod httpMethod = getHttpMethod(url, HttpMethodType.POST, postParams)
        HttpClient httpClient = new HttpClient()
        httpClient.executeMethod(httpMethod)

        def response =  getResponse(httpMethod)
        if(response.statusCode == HttpStatus.SC_OK){
            Map accessTokenMap = getAccessTokenMap(response)
            return accessTokenMap.accessToken
        }
        throwHttpResponseError(response)
        return null
    }

    public static def renewAccessToken(String expiredAccessToken){
        String appId = getId("applicationId")
        String url = "${SQUARE_CONNECT_URL}${OAUTH2_ENDPOINT}/${CLIENT}/${appId}${ACCESS_TOKEN_RENEW_POSTFIX}"

        Map postParams = [:]
        postParams.put("access_token", expiredAccessToken)

        HttpMethod httpMethod = getHttpMethod(url, HttpMethodType.POST, postParams)
        HttpClient httpClient = new HttpClient()
        httpClient.executeMethod(httpMethod)

        def response =  getResponse(httpMethod)
        if(response.statusCode == HttpStatus.SC_OK){
            Map accessTokenMap = getAccessTokenMap(response)
            return accessTokenMap.accessToken
        }
        throwHttpResponseError(response)
        return null
    }


    private static def getAccessTokenMap(def response){
        Map accessTokenMap = [:]
        accessTokenMap.put("accessToken", response.body.access_token)
        accessTokenMap.put("tokenType", response.body.token_type)
        accessTokenMap.put("expiresAt", response.body.expires_at)
        accessTokenMap.put("merchantId", response.body.merchant_id)
        return accessTokenMap
    }

}
