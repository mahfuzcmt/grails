package com.webcommander.captcha

import com.webcommander.constants.DomainConstants
import com.webcommander.util.AppUtil
import com.webcommander.util.captcha.recaptacha.ReCaptcha

import javax.servlet.http.HttpServletRequest

class CaptchaService {
    SimpleCaptchaService simpleCaptchaService;

    boolean validateCaptcha(Map params, HttpServletRequest request) {
        def captchaType = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "captcha_type");
        if (captchaType == "simple_captcha") {
            return simpleCaptchaService.validateCaptcha(params.captcha)
        } else  {
            def response = new ReCaptcha().checkAnswer(request.ip, params['g-recaptcha-response']?.trim())
            return response.valid
        }
    }
}
