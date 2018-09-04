package com.webcommander.plugin.sendle.communicator

import com.webcommander.config.StoreDetail
import com.webcommander.constants.DomainConstants
import com.webcommander.models.AddressData
import com.webcommander.plugin.sendle.constants.Constants
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.Product

import groovy.json.JsonSlurper
import grails.converters.JSON
import java.nio.charset.StandardCharsets

import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.HttpMethod
import org.apache.commons.httpclient.HttpStatus
import org.apache.commons.httpclient.methods.GetMethod
import org.apache.commons.httpclient.methods.PostMethod
import org.apache.commons.httpclient.methods.StringRequestEntity
import org.apache.commons.codec.binary.Base64
import org.apache.commons.httpclient.URI

class SendleCommunicator {

    private static enum HttpMethodType{
        GET,
        POST
    }

    private static final String sendleTestUrl = "https://sandbox.sendle.com/api/"
    private static final String sendleLiveUrl = "https://api.sendle.com/api/"

    private static final String ping = "ping"
    private static final String quote = "quote"
    private static final String orders = "orders"

    private static final String APPLICATION_JSON = "application/json"
    private static final String UTF_8_CHARSET = "UTF-8"
    private static final String AUSTRALIA = "AU"

    private static String getApiUrl(){
        Map config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.SENDLE)
        if(config.mode == Constants.MODES['test']){
            return "${sendleTestUrl}"
        }
        return "${sendleLiveUrl}"
    }

    private static String getAuthorization(){
        Map config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.SENDLE)
        String auth = config.sendleId + ":" + config.apiKey
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.ISO_8859_1))
        String authHeader = "Basic " + new String(encodedAuth)
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

    private static def getResponse(HttpMethod method){
        JsonSlurper slurper = new JsonSlurper()
        return slurper.parseText(method.getResponseBodyAsString())
    }

    public static def ping(){
        String url = "${getApiUrl()}${ping}"

        HttpClient httpClient = new HttpClient()
        HttpMethod httpMethod = getHttpMethod(url, HttpMethodType.GET)
        httpClient.executeMethod(httpMethod)

        return getResponse(httpMethod)
    }

    public static def calculateShippingCost(AddressData pickupAddress, AddressData deliveryAddress, Double weight, Double volume, Integer noOfItem){

        String url = "${getApiUrl()}${quote}?"

        Map queryParams = [
                pickup_suburb: pickupAddress.city,
                pickup_postcode: pickupAddress.postCode,
                delivery_suburb: deliveryAddress.city,
                delivery_postcode: deliveryAddress.postCode,
                kilogram_weight: weight,
                cubic_metre_volume: volume
        ]

        if(deliveryAddress.countryCode != AUSTRALIA){
            queryParams.put("delivery_country", deliveryAddress.countryCode)
        }

        queryParams.each { name, value ->
            url += "${name}=${value}&"
        }

        HttpMethod httpMethod = getHttpMethod(url, HttpMethodType.GET)
        HttpClient httpClient = new HttpClient()
        httpClient.executeMethod(httpMethod)

        Integer statusCode = httpMethod.statusCode

        if (statusCode == HttpStatus.SC_OK) {
            JsonSlurper slurper  = new JsonSlurper()
            def responseFromSendle = slurper.parseText(httpMethod.getResponseBodyAsString())
            return responseFromSendle.quote.gross.amount[0] * noOfItem
        } else {
            JsonSlurper slurper  = new JsonSlurper()
            def responseFromSendle = slurper.parseText(httpMethod.getResponseBodyAsString())
            println("Sendle " + statusCode + " Error: " + responseFromSendle.toString())
        }
        return null
    }

    public static def placeOrder(AddressData receiver, Product product, String pickupDate, String description, String receiverInstruction){
        Map additionalInfo = [:]
        additionalInfo.put("pickupDate", pickupDate)
        additionalInfo.put("description", description)
        additionalInfo.put("weight", product.weight)
        if(!isSatchel(product)) {
            additionalInfo.put("receiverInstruction", receiverInstruction)
        }

        if(product.length && product.width && product.height){
            Double volume = (product.length * product.width * product.height) / 1000000
            additionalInfo.put("volume", volume)
        }
        if(receiver.countryCode != AUSTRALIA){
            additionalInfo.put("price", product.basePrice)
            additionalInfo.put("countryOfOrigin", StoreDetail.first().address.country.name)
        }

        AddressData sender = new AddressData(StoreDetail.first().address)

        return createOrder(sender, receiver, additionalInfo, product)
    }

    private static boolean isSatchel(Product product){
        Double maxWeightOfSatchel = 0.5
        Double maxVolumeOfSatchel = 1540
        if(product.weight > maxWeightOfSatchel){
            return false
        }
        if(product.length > 35.5 || product.width > 35.5 || product.height > 35.5){
            return false
        }
        if((product.length * product.width * product.height) > maxVolumeOfSatchel){
            return false
        }
        return true
    }

    public static def createOrder(AddressData sender, AddressData receiver, Map additionalInfo, Product product){

        Map postParams  = getOrderData(sender, receiver, additionalInfo, product)
        String url = "${getApiUrl()}${orders}"
        Map responseMap =  [:]

        HttpMethod httpMethod = getHttpMethod(url, HttpMethodType.POST, postParams)
        HttpClient httpClient = new HttpClient()
        httpClient.executeMethod(httpMethod)

        Integer responseStatus = httpMethod.statusCode

        if (responseStatus == HttpStatus.SC_CREATED) {
            JsonSlurper slurper  = new JsonSlurper()
            def responseJson = slurper.parseText(httpMethod.getResponseBodyAsString())
            responseMap.put("orderId", responseJson.order_id)
            responseMap.put("orderUrl", responseJson.order_url)
            responseMap.put("trackingUrl", responseJson.tracking_url)
            responseMap.put("trackingInfo", responseJson.sendle_reference)
            return responseMap
        }else if(responseStatus == HttpStatus.SC_UNPROCESSABLE_ENTITY){
            JsonSlurper slurper  = new JsonSlurper()
            def responseFromSendle = slurper.parseText(httpMethod.getResponseBodyAsString())
            if(responseFromSendle.messages?.pickup_date){
                throw new ApplicationRuntimeException("Pickup Date: ${responseFromSendle.messages.pickup_date[0]}")
            }
            println("Sendle 422 Error: " + responseFromSendle.toString())
            throw new ApplicationRuntimeException("sendle.unprocessable.entity")
        }else if(responseStatus == HttpStatus.SC_UNAUTHORIZED){
            throw new ApplicationRuntimeException("sendle.unauthorized")
        }else if(responseStatus == HttpStatus.SC_PAYMENT_REQUIRED){
            throw new ApplicationRuntimeException("sendle.payment.required")
        }else if(responseStatus == HttpStatus.SC_INTERNAL_SERVER_ERROR){
            throw new ApplicationRuntimeException("sendle.internal.server.error")
        }else if(responseStatus == HttpStatus.SC_SERVICE_UNAVAILABLE){
            throw new ApplicationRuntimeException("sendle.service.unavailable")
        }else if(responseStatus == HttpStatus.SC_BAD_REQUEST){
            throw new ApplicationRuntimeException("sendle.bad.request")
        }

        return null
    }

    private static def getOrderData(AddressData sender, AddressData receiver, Map additionalInfo, Product product){

        if(!additionalInfo.description){
            throw new ApplicationRuntimeException("sendle.provide.product.description")
        }

        if(!additionalInfo.receiverInstruction && !isSatchel(product)){
            throw new ApplicationRuntimeException("sendle.provide.receiver.instruction")
        }

        if(!additionalInfo.pickupDate){
            throw new ApplicationRuntimeException("sendle.provide.pickup.date")
        }

        Map parcelDetails = [:]
        parcelDetails.put("pickup_date", additionalInfo.pickupDate)
        parcelDetails.put("description", additionalInfo.description)
        parcelDetails.put("kilogram_weight", additionalInfo.weight)
        if(additionalInfo.volume) {
            parcelDetails.put("cubic_metre_volume", additionalInfo.volume)
        }

        if(receiver.countryCode != AUSTRALIA){
            Map contents = [:]
            contents.put("description", additionalInfo.description)
            contents.put("value", additionalInfo.price)
            contents.put("country_of_origin", additionalInfo.countryOfOrigin)
            parcelDetails.put("contents", contents)
        }

        Map senderContact = getContact(sender)
        Map senderAddress = getAddress(sender)

        Map senderDetails = [:]
        senderDetails.put("contact", senderContact)
        senderDetails.put("address", senderAddress)
        parcelDetails.put("sender", senderDetails)

        Map receiverContact = getContact(receiver)
        Map receiverAddress = getAddress(receiver)

        Map receiverDetails = [:]
        receiverDetails.put("contact", receiverContact)
        receiverDetails.put("address", receiverAddress)
        receiverDetails.put("instructions", additionalInfo.receiverInstruction)
        parcelDetails.put("receiver", receiverDetails)

        return parcelDetails
    }

    private static Map getContact(AddressData addressData){
        Map contact = [:]
        contact.put("name", addressData.getFullName())
        contact.put("phone", addressData.phone)
        contact.put("email", addressData.email)
        return contact
    }

    private static Map getAddress(AddressData addressData){
        Map address = [:]
        address.put("address_line1", addressData.addressLine1)
        address.put("address_line2", addressData.addressLine2)
        address.put("suburb", addressData.city)
        address.put("state_name", addressData.stateCode)
        address.put("postcode", addressData.postCode)
        address.put("country", addressData.countryName)
        return address
    }

}
