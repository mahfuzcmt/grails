package com.webcommander.plugin.xero

import com.webcommander.constants.DomainConstants
import com.webcommander.plugin.xero.exception.XeroException
import com.webcommander.plugin.xero.manager.XmlManager
import com.webcommander.plugin.xero.models.Item
import com.webcommander.plugin.xero.models.Items
import com.webcommander.plugin.xero.models.XeroPayments
import com.webcommander.plugin.xero.models.invoice.XeroInvoices
import com.webcommander.util.AppUtil
import grails.converters.JSON
import net.oauth.*
import net.oauth.client.OAuthClient
import net.oauth.client.OAuthResponseMessage
import net.oauth.client.httpclient4.HttpClient4
import net.oauth.http.HttpResponseMessage
import net.oauth.signature.RSA_SHA1

import javax.xml.bind.JAXBException

/**
 * Created by sajed on 6/12/2014.
 */
class XeroClient {
    private String ENDPOINT_URL = "https://api.xero.com/api.xro/2.0/";
    private String CONSUMER_KEY;
    private String CONSUMER_SECRET;
    private String PRIVATE_KEY;

    public XeroClient(String consumerKey, String consumerSecret, String privateKey) {
        this.CONSUMER_KEY = consumerKey;
        this.CONSUMER_SECRET = consumerSecret;
        this.PRIVATE_KEY = privateKey;
    }

    public OAuthAccessor getAccesor() {
        OAuthServiceProvider serviceProvider = new OAuthServiceProvider(null,null,null);
        OAuthConsumer consumer = new OAuthConsumer(null, CONSUMER_KEY, null, serviceProvider);
        consumer.setProperty(RSA_SHA1.PRIVATE_KEY, PRIVATE_KEY);
        consumer.setProperty(OAuth.OAUTH_SIGNATURE_METHOD, OAuth.RSA_SHA1);
        OAuthAccessor accessor = new OAuthAccessor(consumer);
        accessor.accessToken = CONSUMER_KEY;
        accessor.tokenSecret = CONSUMER_SECRET;
        return accessor;
    }

    public OAuthClient getClient() {
        return new OAuthClient(new HttpClient4());
    }

    public List<Item> getItems() {
        Items items = null;
        try {
            OAuthMessage response = getClient().invoke(getAccesor(), ENDPOINT_URL + "Items", null);
            String xml = response.readBodyAsString();
            if (xml.contains("<Items>")) {
                xml = xml.substring(xml.indexOf("<Items>"), xml.indexOf("</Items>") + 8);
                items = XmlManager.getObject(xml, Items.class);
            }
        }
        catch (XeroException e) {
        }
        return items?.itemList;
    }

    public Item addItem(Item item) throws OAuthException, IOException, URISyntaxException, JAXBException {
        OAuthAccessor accessor = getAccesor();
        String xml = XmlManager.getXml(item);
        OAuthMessage request = accessor.newRequestMessage(OAuthMessage.POST, ENDPOINT_URL + "Items"  , OAuth.newList("xml", xml));
        OAuthResponseMessage response =  getClient().access(request, ParameterStyle.BODY);
        HttpResponseMessage httpResponse = response.getHttpResponse();
        if (httpResponse.getStatusCode() != 200 ) {
            throw new XeroException("product.could.not.export")
        }
        String responseString = response.readBodyAsString();
        responseString = responseString.substring(responseString.indexOf("<Item>"), responseString.indexOf("</Item>") + 7);
        Item newITem = XmlManager.getObject(responseString, Item.class);
        return newITem;
    }

    public String addItemList(String itemList) throws OAuthException, IOException, URISyntaxException, JAXBException {
        OAuthAccessor accessor = getAccesor()
        OAuthMessage request = accessor.newRequestMessage(OAuthMessage.POST, ENDPOINT_URL + "Items?summarizeErrors=false"  , OAuth.newList("xml", itemList))
        request.getHeaders().add(new OAuth.Parameter("Accept", "application/json"))
        OAuthResponseMessage response =  getClient().access(request, ParameterStyle.BODY)
        HttpResponseMessage httpResponseMessage = response.getHttpResponse()
        if(httpResponseMessage.getStatusCode() != 200) {
            return null
        }
        return response.readBodyAsString()
    }

    public def getTaxRates() {
        OAuthAccessor accessor = getAccesor()
        OAuthMessage request = accessor.newRequestMessage(OAuthMessage.GET, ENDPOINT_URL + "TaxRates", null)
        request.getHeaders().add(new OAuth.Parameter("Accept", "application/json"))
        OAuthResponseMessage response =  getClient().access(request, ParameterStyle.BODY)
        HttpResponseMessage httpResponseMessage = response.getHttpResponse()
        if(httpResponseMessage.getStatusCode() != 200) {
            return null
        }
        return JSON.parse(response.readBodyAsString())."TaxRates".findAll{it."TaxType" in ["OUTPUT", "EXEMPTOUTPUT"]}
    }

    public def addContacts(String xml) throws OAuthException, IOException, URISyntaxException, JAXBException {
        OAuthAccessor accessor = getAccesor()
        OAuthMessage request = accessor.newRequestMessage(OAuthMessage.POST, ENDPOINT_URL + "Contacts?summarizeErrors=false"  , OAuth.newList("xml", xml))
        request.getHeaders().add(new OAuth.Parameter("Accept", "application/json"))
        OAuthResponseMessage response =  getClient().access(request, ParameterStyle.BODY)
        HttpResponseMessage httpResponseMessage = response.getHttpResponse()
        if(httpResponseMessage.getStatusCode() != 200) {
            return null
        }
        return  JSON.parse(response.readBodyAsString())."Contacts"
    }

    def addInvoices(XeroInvoices invoices) throws OAuthException, IOException, URISyntaxException, JAXBException {
        OAuthAccessor accessor = getAccesor()
        String xml = XmlManager.getXml(invoices)
        OAuthMessage request = accessor.newRequestMessage(OAuthMessage.POST, ENDPOINT_URL + "Invoices?summarizeErrors=false"  , OAuth.newList("xml", xml))
        request.getHeaders().add(new OAuth.Parameter("Accept", "application/json"))
        OAuthResponseMessage response =  getClient().access(request, ParameterStyle.BODY)
        HttpResponseMessage httpResponseMessage = response.getHttpResponse()
        if(httpResponseMessage.getStatusCode() != 200) {
            def error = response.readBodyAsString()
            identifyError(error)
            return error
        }
        return JSON.parse(response.readBodyAsString())."Invoices"
    }


    public def getContacts(String additionalParams = null) {
        OAuthAccessor accessor = getAccesor();
        OAuthMessage request = accessor.newRequestMessage(OAuthMessage.GET, ENDPOINT_URL + "Contacts/${additionalParams?: ""}", null);
        request.getHeaders().add(new OAuth.Parameter("Accept", "application/json"));
        OAuthResponseMessage response =  getClient().access(request, ParameterStyle.BODY);
        HttpResponseMessage httpResponseMessage = response.getHttpResponse();
        if(httpResponseMessage.getStatusCode() != 200) {
            return null
        }
        return JSON.parse(response.readBodyAsString())."Contacts"
    }

    public def getAccounts() {
        OAuthAccessor accessor = getAccesor();
        OAuthMessage request = accessor.newRequestMessage(OAuthMessage.GET, ENDPOINT_URL + "Accounts"  , null);
        request.getHeaders().add(new OAuth.Parameter("Accept", "application/json"));
        OAuthResponseMessage response =  getClient().access(request, ParameterStyle.BODY);
        HttpResponseMessage httpResponseMessage = response.getHttpResponse();
        if(httpResponseMessage.getStatusCode() != 200) {
            throw new XeroException(response.readBodyAsString())
        }
        return response.readBodyAsString()
    }

    public def getOrganisation() {
        OAuthAccessor accessor = getAccesor()
        OAuthMessage request = accessor.newRequestMessage(OAuthMessage.GET, ENDPOINT_URL + "Organisation", null)
        request.getHeaders().add(new OAuth.Parameter("Accept", "application/json"))
        OAuthResponseMessage response =  getClient().access(request, ParameterStyle.BODY)
        HttpResponseMessage httpResponseMessage = response.getHttpResponse()
        if(httpResponseMessage.getStatusCode() != 200) {
            return null
        }
        return response.readBodyAsString()
    }

    public static String getCurrentOrganisation() {
        return AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.XERO, "organisation_id");
    }


    public def addPayments(XeroPayments payments) throws OAuthException, IOException, URISyntaxException, JAXBException {
        OAuthAccessor accessor = getAccesor()
        String xml = XmlManager.getXml(payments)
        OAuthMessage request = accessor.newRequestMessage(OAuthMessage.POST, ENDPOINT_URL + "Payments?summarizeErrors=false"  , OAuth.newList("xml", xml))
        request.getHeaders().add(new OAuth.Parameter("Accept", "application/json"))
        OAuthResponseMessage response =  getClient().access(request, ParameterStyle.BODY)
        HttpResponseMessage httpResponseMessage = response.getHttpResponse()
        if(httpResponseMessage.getStatusCode() != 200) {
            identifyError(response.readBodyAsString())
            return null
        }
        return JSON.parse(response.readBodyAsString())."Payments"
    }

    static void identifyError(String body) {
        try {
            String[] parts = body.toString().split("&").collect { it = it.decodeURL() }
            if (parts[0].startsWith("oauth_problem=")) {
                String[] semi = parts[0].split("=")
                if (semi[1].equalsIgnoreCase("token_rejected")) {
                    throw new XeroException("failed.to.connect.to.item", ["xero"])
                }
                else if (semi[1].equalsIgnoreCase("consumer_key_unknown")) {
                    throw new XeroException("wrong.credentials")
                }
            }
        }
        catch (XeroException ex) {
            throw ex
        }
        catch (Exception ex) {
            ex.printStackTrace()
        }
    }
}
