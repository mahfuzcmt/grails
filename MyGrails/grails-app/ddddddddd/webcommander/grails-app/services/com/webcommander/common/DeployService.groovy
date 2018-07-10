package com.webcommander.common

import com.webcommander.constants.DomainConstants
import com.webcommander.util.AppUtil
import com.webcommander.util.HttpUtil
import com.webcommander.util.security.InformationEncrypter

class DeployService {

    URLConnection getConnection(String url, Map params = [:]) {
        String domainName = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "template_domain");
        InformationEncrypter encrypter = new InformationEncrypter();
        encrypter.hideInfo(domainName)
        return HttpUtil.getPostConnection(domainName + url, HttpUtil.serializeMap(params), ['auth-info': encrypter.toString()]);
    }

    String getDataFromTemplate(String dataUrl, Map params = [:]) {
        URLConnection connection = getConnection(dataUrl, params)
        return HttpUtil.getResponseText(connection)
    }
}
