package com.webcommander.util.captcha.recaptacha

import com.webcommander.constants.DomainConstants
import com.webcommander.util.AppUtil
import groovy.json.JsonSlurper

public class ReCaptcha {
    public static final String HTTPS_SERVER = "https://www.google.com/recaptcha/api"
    public static final String VERIFY_URL = "/siteverify"

    public Map checkAnswer(String remoteAddr, String response) {
        String privateKey = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "captcha_private_key")
        def recaptchaServer = HTTPS_SERVER
        def post = new Post(url: recaptchaServer + VERIFY_URL)
        post.queryString.add("secret", privateKey)
        post.queryString.add("remoteip", remoteAddr)
        post.queryString.add("response", response)

        def responseMessage = post.text

        if (!responseMessage) {
            return [valid: false, errorMessage: "Null read from server."]
        }

        def responseObject = new JsonSlurper().parseText(responseMessage) as Map
        if (responseObject.isEmpty()) {
            return [valid: false, errorMessage: "No answer returned from recaptcha: $responseMessage"]
        }
        def isValid = responseObject.success
        def errorMessage = null;
        if (!isValid) {
            errorMessage = responseObject['error-codes'] != null ? responseObject['error-codes'] : "Unknown error"
        }
        return [valid: isValid, errorMessage: errorMessage]
    }
}