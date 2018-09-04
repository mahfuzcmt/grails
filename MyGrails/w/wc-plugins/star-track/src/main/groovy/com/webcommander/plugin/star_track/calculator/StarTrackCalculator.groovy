package com.webcommander.plugin.star_track.calculator

import com.webcommander.config.StoreDetail
import com.webcommander.constants.DomainConstants
import com.webcommander.models.AddressData
import com.webcommander.plugin.star_track.ShippingPolicyExtension
import com.webcommander.plugin.star_track.communicator.StarTrackCommunicator
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.ShippingCondition
import grails.converters.XML;

/**
 * Created by sajedur on 5/28/2015.
 */
public class StarTrackCalculator {
    private static String wsStagingHost = "services.startrackexpress.com.au:7560";
    private static String wsLiveHost = "services.startrackexpress.com.au:443";
    private static String wsStagingURL = "https://services.startrackexpress.com.au:7560/DMZExternalService/InterfaceServices/ExternalOps.serviceagent/OperationsEndpoint1";
    private static String wsLiveURL = "https://services.startrackexpress.com.au:443/DMZExternalService/InterfaceServices/ExternalOps.serviceagent/OperationsEndpoint1";


    private static String getSOAPHeader(Map config) {
       return  "<soapenv:Header>\n<wsse:Security soapenv:mustUnderstand=\"1\" xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\">\n" +
                "   <wsse:UsernameToken wsu:Id=\"UsernameToken-1\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">\n" +
                "       <wsse:Username>${config.user_name}</wsse:Username>\n" +
                "       <wsse:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText\">${config.password}</wsse:Password>\n" +
                "       <wsse:Nonce>EGlSVmA=</wsse:Nonce>\n" +
                "       <wsu:Created xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">${new Date().encodeAsJavaScript()}</wsu:Created>\n" +
                "   </wsse:UsernameToken>\n" +
                "</wsse:Security>\n</soapenv:Header>";
    }

    private static String getSOAPMessage(String SOAP_BODY, Map config) {
        return  "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:v1=\"http://startrackexpress/Common/actions/externals/FreightCalculation/v1\" xmlns:v11=\"http://startrackexpress/Common/Primitives/v1\">" +
                "\n" + getSOAPHeader(config) + "\n" + SOAP_BODY + "\n</soapenv:Envelope>";
    }

    public static Double calculateCost(Map config, AddressData senderLocation, AddressData receiverLocation, ShippingCondition condition, Integer noOfItem, Double weight, Double volume) {
        ShippingPolicyExtension extension = ShippingPolicyExtension.findByShippingCondition(condition)
        String SOAPAction = "/DMZExternalService/InterfaceServices/ExternalOps.serviceagent/OperationsEndpoint1/calculateCost";
        String SOAP_BODY = "<soapenv:Body>\n" +
                "    <v1:calculateCostRequest>\n" +
                "        <v1:header>\n" +
                "            <v11:source>${config.source}</v11:source>\n" +
                "            <v11:accountNo>${config.account_no}</v11:accountNo>\n" +
                "            <v11:userAccessKey>${config.user_access_key}</v11:userAccessKey>\n" +
                "        </v1:header>\n" +
                "        <v1:senderLocation>\n" +
                "            <v11:suburb>${senderLocation.city}</v11:suburb>\n" +
                "            <v11:postCode>${senderLocation.postCode}</v11:postCode>\n" +
                "            <v11:state>${senderLocation.stateCode}</v11:state>\n" +
                "        </v1:senderLocation>\n" +
                "        <v1:receiverLocation>\n" +
                "            <v11:suburb>${receiverLocation.city}</v11:suburb>\n" +
                "            <v11:postCode>${receiverLocation.postCode}</v11:postCode>\n" +
                "            <v11:state>${receiverLocation.stateCode}</v11:state>\n" +
                "        </v1:receiverLocation>\n" +
                "        <v1:serviceCode>${condition.apiServiceType}</v1:serviceCode>\n" +
                "        <v1:noOfItems>${noOfItem}</v1:noOfItems>\n" +
                "        <v1:weight>${weight.toFixed(2, false)}</v1:weight>\n" +
                "        <v1:volume>${volume.toFixed(3, false)}</v1:volume>\n" +
                "        <v1:includeTransitWarranty>${extension.includeTransitWarranty}</v1:includeTransitWarranty>\n" +
                "        <v1:transitWarrantyValue>${extension.transitWarrantyValue ?: 0}</v1:transitWarrantyValue>\n" +
                "        <v1:includeFuelSurcharge>${extension.includeFuelSurcharge}</v1:includeFuelSurcharge>\n" +
                "        <v1:includeSecuritySurcharge>${extension.includeSecuritySurcharge}</v1:includeSecuritySurcharge>\n" +
                "    </v1:calculateCostRequest>\n" +
                "</soapenv:Body>";
        String SOAP_MESSAGE = getSOAPMessage(SOAP_BODY, config)
        Map headers = new HashMap();
        headers.put("SOAPAction", SOAPAction);
        headers.put("Accept-Encoding", "gzip,deflate");
        headers.put("Host", config.mode == "live" ? wsLiveHost : wsStagingHost);
        String wsURL = config.mode == "live" ? wsLiveURL : wsStagingURL;
        Map result = StarTrackCommunicator.callUrl(wsURL, "POST", SOAP_MESSAGE, StarTrackCommunicator.CONTENT_TYPE_XML, headers);
        if(result.code == 200) {
            def xml = XML.parse(result.response)
            Double total = 0
            total += Double.parseDouble(xml.Body.calculateCostResponse.cost.text())
            if(extension.includeSecuritySurcharge) {
                total += Double.parseDouble(xml.Body.calculateCostResponse.securitySurcharge.text())
            }
            if(extension.includeFuelSurcharge) {
                total += Double.parseDouble(xml.Body.calculateCostResponse.fuelSurcharge.text())
            }
            if(extension.includeTransitWarranty) {
                total += Double.parseDouble(xml.Body.calculateCostResponse.transitWarrantyCharge.text())
            }
            return total
        }
        return null;
    }


    public static Double calculateCost(AddressData receiverLocation, ShippingCondition condition, Integer noOfItem, Double width, Double height, length, Double weight){
        Map config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.STAR_TRACK);
        AddressData senderLocation = new AddressData(StoreDetail.first().address);
        Double volume = width * height * length;
        return  calculateCost(config, senderLocation, receiverLocation, condition, noOfItem, weight, volume);
    }


}
