package com.webcommander.util

import com.webcommander.admin.Customer
import com.webcommander.admin.Zone
import com.webcommander.config.SiteConfig
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.events.AppEventManager
import com.webcommander.manager.CacheManager
import com.webcommander.manager.PathManager
import com.webcommander.models.AddressData
import com.webcommander.webcommerce.Currency
import grails.converters.JSON
import grails.gsp.PageRenderer
import grails.util.Holders
import grails.web.mvc.FlashScope
import grails.web.servlet.mvc.GrailsHttpSession
import grails.web.servlet.mvc.GrailsParameterMap
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.web.context.request.RequestContextHolder
import org.w3c.dom.Node

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class AppUtil {
    static Long api_monthly_hit_count = 0
    private static final ThreadLocal<GrailsWebRequest> requestCache = new ThreadLocal<GrailsWebRequest>()
    private static final ThreadLocal<List> loggedCustomerGroupsCache = new ThreadLocal<List<Long>>()
    private static String appVersion

    static Currency getSiteCurrency() {
        return session.currency ?: baseCurrency
    }

    static Currency getBaseCurrency() {
        Currency currency = CacheManager.get(NamedConstants.CACHE.SCOPE_APP, "base_currency")
        return currency ?: setBaseCurrency()
    }

    static setBaseCurrency() {
        Currency currency = Currency.findByBaseAndActive(true, true)
        CacheManager.cache(NamedConstants.CACHE.SCOPE_APP, currency, "base_currency")
        return currency
    }

    static def getConfig(String type, String key = null) {
        return getConfig(type, key, false)
    }


    static def getProvisioningApiCredential(){
        def provisionCredential = CacheManager.get(NamedConstants.CACHE.SCOPE_APP, "provisioning_api_credential")
        if (!provisionCredential) {
            def config = SiteConfig.findByTypeAndConfigKey(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "provision")
            provisionCredential = JSON.parse(config.value)
            CacheManager.cache(NamedConstants.CACHE.SCOPE_APP, provisionCredential, "provisioning_api_credential")
        }
        return provisionCredential
    }

    static def getConfig(String type, String key = null, Boolean isTypeCast) {
        Map site_config = CacheManager.get(NamedConstants.CACHE.SCOPE_APP, "config", type)
        if (!site_config) {
            List<SiteConfig> configs = SiteConfig.findAllByType(type)
            site_config = new LinkedHashMap()
            configs.each {
                String value = it.value
                if (value) {
                    site_config.put(it.configKey, isTypeCast ? StringUtil.autoCast(value) : value)
                }
            }
            AppEventManager.fire(type + "-refresh-site-config", [site_config])
            CacheManager.cache(NamedConstants.CACHE.SCOPE_APP, site_config, "config", type)
        }
        if (key && site_config != null) {
            return site_config[key]
        }
        return site_config
    }

    static clearConfig(String type = null) {
        CacheManager.removeCache(NamedConstants.CACHE.SCOPE_APP, "config", type ?: "*")
    }
    
    private static GrailsWebRequest getCurrentRequest() {
        GrailsWebRequest request = requestCache.get()
        if(!request) {
            request = RequestContextHolder.currentRequestAttributes()
            if(!request) {
                requestCache.set(request)
            }
        }
        return request
    }

    static GrailsParameterMap getParams() {
        try {
            currentRequest.params
        } catch (Throwable t) {
            return null
        }
    }

    static GrailsHttpSession getSession() {
        try {
            currentRequest.session
        } catch (Throwable t) {
            return null
        }
    }

    static HttpServletRequest getRequest() {
        try {
            currentRequest.request
        } catch (Throwable t) {
            return null
        }
    }

    static HttpServletResponse getResponse() {
        try {
            currentRequest.response
        } catch (Throwable t) {
            return null
        }
    }

    static FlashScope getFlash() {
        try {
            currentRequest.flashScope
        } catch (Throwable t) {
            return null
        }
    }

    static Locale getLocale() {
        try {
            currentRequest.locale
        } catch (Throwable t) {
            return Locale.default
        }
    }

    static String convertToByteNotation(Long size) {
        if (size < 1024) {
            return size.toString() + " B"
        }
        size = size / 1024
        if (size < 1024) {
            return size.toString() + " KB"
        }
        size = size / 1024
        if (size < 1024) {
            return size.toString() + " MB"
        }
        size = size / 1024
        return size.toString() + " GB"
    }

    static String pluginPackageCase(string) {
        def parts = string.split("-")
        if(parts.size() == 1) {
            return string
        }
        parts.eachWithIndex { v, i ->
            if(i == 0) {
                string = v
                return
            }
            string += v.capitalize()
        }
        return string
    }

    static waitFor(Object obj, String property, Object compareValue, Long timeout = 30000) {
        if(obj."$property" != compareValue && timeout > 0) {
            Thread.sleep(1000)
            waitFor(obj, property, compareValue, timeout - 1000)
        }
    }

    static GrailsWebRequest initialDummyRequest() {
        HttpServletRequest _request = PageRenderer.PageRenderRequestCreator.createInstance("/page/dummy")
        _request.IS_DUMMY = true
        GrailsWebRequest webRequest = new GrailsWebRequest(_request, PageRenderer.PageRenderResponseCreator.createInstance(new PrintWriter(new StringWriter())), Holders.servletContext)
        RequestContextHolder.setRequestAttributes(webRequest)
        return webRequest
    }

    static def initializeDefaultImages(List<String> types) {
        types.each { type ->
            def imagePath = PathManager.getResourceRoot("$type/default")
            File imageLok = new File(imagePath)
            if(!imageLok.list()?.length) {
                def repositoryPath = PathManager.getRestrictedResourceRoot("default-images/$type")
                File repositoryLok = new File(repositoryPath)
                repositoryLok.eachFile { image ->
                    File copyImage = new File(imageLok.absolutePath + "/" + image.name)
                    copyImage.parentFile.mkdirs()
                    copyImage.createNewFile()
                    image.withInputStream { stream ->
                        copyImage << stream
                    }
                }
            }
        }
    }

    static String getQueryStringFromMap(Map params) {
        if(params.size() == 0) {
            return ""
        }
        StringBuffer buffer = new StringBuffer()
        for (Map.Entry param: params.entrySet()) {
            buffer.append("&").append(param.key).append("=").append(param.value.encodeAsURL())
        }
        return buffer.substring(1)
    }

    static Map<String, String> getURLQueryMap(String query) {
        String[] params = query.split("&")
        Map<String, String> map = new HashMap<String, String>()
        for (String param: params) {
            String name = param.split("=")[0]
            String value = param.split("=")[1]
            map.put(name, value.decodeURL())
        }
        return map
    }

    static Boolean matchAddressWithZone(AddressData address, Zone zone) {
        if (zone.countries && !zone.countries.id.contains(address.countryId)) {
            return false
        }
        if (zone.states && !zone.states.id.contains(address.stateId)) {
            return false
        }
        if(zone.postCodes) {
            String addressPostcode = address.postCode.trim()
            return zone.postCodes.find { postCode ->
                if(postCode.contains("-")) {
                    List<Long> parts = postCode.split("-").collect {it.toLong(0)}
                    Long longPostCode = addressPostcode.toLong(null)
                    return (parts[0] <= longPostCode && parts[1] >= longPostCode) || (parts[1] <= longPostCode && parts[0] >= longPostCode)
                }
                return postCode.trim() == addressPostcode
            }
        }
        return true
    }

    static Boolean matchAddressWithZones(AddressData address, List<Zone> zones) {
        for (Zone zone : zones) {
            if(matchAddressWithZone(address, zone)) {
                return true
            }
        }
        return false
    }

    static XMLNodeToString(Node nd) {
        Transformer trans = TransformerFactory.newInstance().newTransformer()
        ByteArrayOutputStream serialized = new ByteArrayOutputStream()
        trans.transform(new DOMSource(nd), new StreamResult(serialized))
        return serialized.toString()
    }

    static String docBaseUrl() {
        return Holders.config.kb_server.base_url
    }

    static Map getResponseMapFromUrl(String url, Map model = null) {
        URLConnection con = new URL(url).openConnection()
        con.doOutput = true
        con.doInput = true
        con.outputStream << AppUtil.getQueryStringFromMap(model ?: [:])
        InputStream stream = con.getInputStream()
        ByteArrayOutputStream output = new ByteArrayOutputStream()
        output << stream
        String response = output.toString()
        Properties properties = new Properties()
        properties.load(new StringReader(response))
        return properties
    }

    static Long getLoggedOperator() {
        return isApiRequest() ? request.getAttribute(DomainConstants.REQUEST_ATTR_KEYS.ADMIN) : session.admin
    }

    static Boolean isApiRequest() {
        return request.getAttribute(DomainConstants.REQUEST_ATTR_KEYS.IS_API_REQUEST) ?: false
    }

    static Long getLoggedCustomer() {
        return isApiRequest() ? request.getAttribute(DomainConstants.REQUEST_ATTR_KEYS.CUSTOMER) : session.customer
    }

    static AddressData getEffectiveShippingAddress() {
        AddressData data = session.effective_shipping_address
        if(data) { return data}
        Long customerId = loggedCustomer
        if(customerId) {
            data = new AddressData(Customer.get(customerId).activeShippingAddress)
        }
        return data
    }

    static List<Long> getLoggedCustomerGroupIds() {
        List<Long> ids = loggedCustomerGroupsCache.get()
        if(ids == null) {
            Long customerId = loggedCustomer
            if(customerId) {
                ids = Customer.get(customerId).groups.findAll { it.status == 'A' }
            } else {
                ids = []
            }
            loggedCustomerGroupsCache.set(ids)
        }
        return ids
    }

    static Long getAPIClientId() {
        return isApiRequest() ? request.getAttribute(DomainConstants.REQUEST_ATTR_KEYS.API_CLIENT) : null
    }

    static Integer getIntervalInMinute(Map config) {
        if(config.interval_type == "day") {
            return config.interval.toInteger() * 24 * 60
        } else if(config.interval_type == "hr") {
            return config.interval.toInteger() * 60
        } else {
            return config.interval.toInteger()
        }
    }

    static def getBean(String beanIdentifier) {
        try {
            return Holders.grailsApplication.mainContext.getBean(beanIdentifier)
        } catch (Exception e) {
            return null
        }
    }

    static <T> T getBean(Class<T> requiredType) {
        try {
            return Holders.grailsApplication.mainContext.getBean(requiredType)
        } catch (Exception e) {
            return null
        }
    }

    static getAppConfig(String key, def defaultVal = null) {
        def result = Holders.config.get(key)
        return (result != "" || result != null ? result : defaultVal)
    }

    static getAppVersion() {
        appVersion ?: (appVersion = Holders.config.webcommander.version.number)
    }

    static String getCurrencySymbol() {
        return session.currency?.symbol ?: baseCurrency.symbol
    }
}
