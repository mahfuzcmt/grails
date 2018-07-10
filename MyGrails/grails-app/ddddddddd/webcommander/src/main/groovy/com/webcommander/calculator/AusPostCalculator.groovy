package com.webcommander.calculator

import com.webcommander.constants.DomainConstants
import groovy.json.JsonSlurper

import javax.net.ssl.HttpsURLConnection

class AusPostCalculator {
    String toCountry;
    String fromPostCode;
    String toPostCode;
    Double width;
    Double length;
    Double height;
    Double weight;
    String serviceCode;
    Integer extraCover;
    Boolean isDomestic = true
    private static final String authKey = "3261c24c-7efa-4713-bd24-c23e19d7b3bc"
    private static final String domesticUrl = "https://auspost.com.au/api/postage/parcel/domestic/calculate.json";
    private static final String internationalUrl = "https://auspost.com.au/api/postage/parcel/international/calculate.json";
    //domestic postage
    public AusPostCalculator(String fromPost, String toPost, Double weight, Double length, Double width, Double height, String serviceCode, Double extraCover = null) {
        this.fromPostCode= fromPost;
        this.toPostCode= toPost;
        this.width = width;
        this.height = height;
        this.length = length;
        this.weight = weight;
        this.serviceCode = serviceCode;
        this.extraCover = extraCover;
    }

    //international postage
    public AusPostCalculator(String toCountry, Double weight, String serviceCode, Double extraCover = null) {
        this.toCountry = toCountry;
        this.weight = weight;
        this.serviceCode = serviceCode;
        this.extraCover = extraCover;
        isDomestic = false;
    }
    private String getRequestUrl() {
        String url;
        if(isDomestic) {
            url = domesticUrl + "?from_postcode=${fromPostCode}&to_postcode=${toPostCode}&weight=${weight}&height=${height}&width=${width}&length=${length}" +
                    getAustralianParcelParameter(serviceCode)
        } else {
            url = internationalUrl + "?country_code=${toCountry}&weight=${weight}" + getInternationalParcelParameter(serviceCode)
        }
        return url;
    }

    public Double getCost() throws Exception {
        def slurper = new JsonSlurper();
        String requestUrl = getRequestUrl()
        URL url = new URL(requestUrl);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setInstanceFollowRedirects(true);
        connection.setRequestMethod("GET")
        connection.setRequestProperty("Auth-Key", authKey);
        Integer responseCode = connection.getResponseCode();
        BufferedReader br
        String line, responseText = "";
        def resultMap;
        if (responseCode != 200) {
            br = new BufferedReader(new InputStreamReader(connection.getErrorStream()))
            while ((line = br.readLine()) != null) {
                responseText += line + "\n";
            }
            resultMap = slurper.parseText(responseText)
            throw new Exception(resultMap['error']?.errorMessage ?: "Connection Error");
        }
        br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        while ((line = br.readLine()) != null) {
            responseText += line + "\n";
        }
        br.close();
        connection.disconnect();
        resultMap = slurper.parseText(responseText)
        return resultMap["postage_result"]["total_cost"].toDouble();
    }

    private static String getAustralianParcelParameter(String type) {
        String strServiceParam = ""
        switch (type) {
            case DomainConstants.SHIPPING_API_SERVICE_TYPE.REGULAR_PARCEL:
                strServiceParam = "&service_code=AUS_PARCEL_REGULAR";
                break;
            case DomainConstants.SHIPPING_API_SERVICE_TYPE.REGULAR_PARCEL_STANDARD:
                strServiceParam = "&service_code=AUS_PARCEL_REGULAR" +
                    "&option_code=AUS_SERVICE_OPTION_STANDARD";
                break;
            case DomainConstants.SHIPPING_API_SERVICE_TYPE.REGULAR_PARCEL_REGISTER_POST:
                strServiceParam = "&service_code=AUS_PARCEL_REGULAR" +
                    "&option_code=AUS_SERVICE_OPTION_REGISTERED_POST";
                break;
            case DomainConstants.SHIPPING_API_SERVICE_TYPE.REGULAR_PARCEL_REGISTER_POST_WITH_DELIVERY_CONFAFIRMATION:
                strServiceParam = "&service_code=AUS_PARCEL_REGULAR" +
                    "&option_code=AUS_SERVICE_OPTION_REGISTERED_POST" +
                    "&suboption_code=AUS_SERVICE_OPTION_DELIVERY_CONFIRMATION";
                break;
            case DomainConstants.SHIPPING_API_SERVICE_TYPE.REGULAR_PARCEL_REGISTER_POST_WITH_EXTRA_COVER:
                strServiceParam = "&service_code=AUS_PARCEL_REGULAR" +
                    "&option_code=AUS_SERVICE_OPTION_REGISTERED_POST" +
                    "&suboption_code=AUS_SERVICE_OPTION_EXTRA_COVER";
                break;
            case DomainConstants.SHIPPING_API_SERVICE_TYPE.REGULAR_PARCEL_REGISTER_POST_WITH_DELIVERY_CONFAFIRMATION_AND_EXTRA_COVER:
                strServiceParam = "&service_code=AUS_PARCEL_REGULAR" +
                    "&option_code=AUS_SERVICE_OPTION_REGISTERED_POST" +
                    "&suboption_code=AUS_SERVICE_OPTION_DELIVERY_CONFIRMATION" +
                    "&suboption_code=AUS_SERVICE_OPTION_EXTRA_COVER";
                break;
            case DomainConstants.SHIPPING_API_SERVICE_TYPE.EXPRESS_PARCEL_SERVICE:
                strServiceParam = "&service_code=AUS_PARCEL_EXPRESS";
                break;
            case DomainConstants.SHIPPING_API_SERVICE_TYPE.PLATINUM_PARCEL_SERVICE:
                strServiceParam = "&service_code=AUS_PARCEL_PLATINUM";
                break;
            case DomainConstants.SHIPPING_API_SERVICE_TYPE.PLATINUM_PARCEL_SERVICE_WITH_EXTRA_COVER:
                strServiceParam = "&service_code=AUS_PARCEL_PLATINUM" +
                    "&option_code=AUS_SERVICE_OPTION_PLATINUM_EXTRA_COVER_SERVICE" +
                    "&suboption_code=AUS_SERVICE_OPTION_EXTRA_COVER";
                break;
        }
        return strServiceParam;
    }

    private static String getInternationalParcelParameter(String type) {
        String strServiceParam = "";
        switch (type) {
            case "EXPRESS_COURIER_INTERNATIONAL_MERCHANDISE":
                strServiceParam = "&service_code=INTL_SERVICE_ECI_M";
                break;
            case "EXPRESS_COURIER_INTERNATIONAL_MERCHANDISE_PICKUP":
                strServiceParam = "&service_code=INTL_SERVICE_ECI_M" +
                    "&option_code=INTL_SERVICE_OPTION_PICKUP_METRO";
                break;
            case "EXPRESS_COURIER_INTERNATIONAL_MERCHANDISE_EXTRA_COVER":
                strServiceParam = "&service_code=INTL_SERVICE_ECI_M" +
                    "&option_code=INTL_SERVICE_OPTION_EXTRA_COVER";
                break;
            case "EXPRESS_COURIER_INTERNATIONAL_MERCHANDISE_PICKUP_EXTRA_COVER":
                strServiceParam = "&service_code=INTL_SERVICE_ECI_M" +
                    "&option_code=INTL_SERVICE_OPTION_PICKUP_METRO" +
                    "&option_code=INTL_SERVICE_OPTION_EXTRA_COVER";
                break;
            case "EXPRESS_COURIER_INTERNATIONAL_DOCUMENTS":
                strServiceParam = "&service_code=INTL_SERVICE_ECI_D";
                break;
            case "EXPRESS_COURIER_INTERNATIONAL_DOCUMENTS_PICKUP":
                strServiceParam = "&service_code=INTL_SERVICE_ECI_D" +
                    "&option_code=INTL_SERVICE_OPTION_PICKUP_METRO";
                break;
            case "EXPRESS_COURIER_INTERNATIONAL_DOCUMENTS_EXTRA_COVER":
                strServiceParam =  "&service_code=INTL_SERVICE_ECI_D" +
                    "&option_code=INTL_SERVICE_OPTION_EXTRA_COVER";
                break;
            case "EXPRESS_COURIER_INTERNATIONAL_DOCUMENTS_PICKUP_EXTRA_COVER":
                strServiceParam = "&service_code=INTL_SERVICE_ECI_D" +
                    "&option_code=INTL_SERVICE_OPTION_PICKUP_METRO" +
                    "&option_code=INTL_SERVICE_OPTION_EXTRA_COVER";
                break;
            case "EXPRESS_POST_INTERNATIONAL":
                strServiceParam ="&service_code=INTL_SERVICE_EPI" +
                    "&option_code=INTL_SERVICE_OPTION_EXTRA_COVER";
                break;
            case "EXPRESS_POST_INTERNATIONAL_EXTRA_COVER":
                strServiceParam ="&service_code=AUS_PARCEL_PLATINUM" +
                    "&option_code=AUS_SERVICE_OPTION_PLATINUM_EXTRA_COVER_SERVICE" +
                    "&suboption_code=AUS_SERVICE_OPTION_EXTRA_COVER";
                break;
            case "REGISTERED_POST_INTERNATIONAL":
                strServiceParam ="&service_code=INTL_SERVICE_RPI";
                break;
            case "REGISTERED_POST_INTERNATIONAL_DELIVERY_CONFIRMATION":
                strServiceParam ="&service_code=INTL_SERVICE_RPI" +
                    "&option_code=INTL_SERVICE_OPTION_CONFIRM_DELIVERY";
                break;
            case "AIR_MAIL":
                strServiceParam ="&service_code=INTL_SERVICE_AIR_MAIL";
                break;
            case "AIR_MAIL_DELIVERY_CONFIRMATION":
                strServiceParam ="&service_code=INTL_SERVICE_AIR_MAIL" +
                    "&option_code=INTL_SERVICE_OPTION_CONFIRM_DELIVERY";
                break;
            case "SEA_MAIL":
                strServiceParam ="&service_code=INTL_SERVICE_SEA_MAIL";
                break;
            case "SEA_MAIL_DELIVERY_CONFIRMATION":
                strServiceParam ="&service_code=INTL_SERVICE_SEA_MAIL" +
                    "&option_code=INTL_SERVICE_OPTION_CONFIRM_DELIVERY";
                break;
        }
        return strServiceParam;
    }
}

