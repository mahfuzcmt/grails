package com.webcommander.plugin.form_editor.controllers.site

import com.webcommander.captcha.CaptchaService
import com.webcommander.constants.DomainConstants
import com.webcommander.plugin.form_editor.Form
import com.webcommander.plugin.form_editor.admin.FormService
import com.webcommander.util.AppUtil
import grails.converters.JSON

class FormPageController {

    FormService formService
    CaptchaService captchaService

    def submitForm() {
        def captchaSettings = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "captcha_setting");
        def captchaType = captchaSettings == "enable" ? AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "captcha_type") : "none";
        Form form = Form.get(params.id)
        params.customerIP = request.ip
        params.addressString = g.message(code: "submitted.ip")
        if (captchaSettings == "enable" && form.useCaptcha == true && !captchaService.validateCaptcha(params, request)) {
            render([status: "error", captchaValidation: "failure", captchaType: captchaType, message: g.message(code: "invalid.captcha.entry"), hasCaptcha: form.useCaptcha] as JSON)
        } else {
            if (formService.processSubmissionData(form, params)) {
                render([status: "success", message: g.message(code: form.successMessage ?: "form.submit.success"), captchaType: captchaType, hasCaptcha: form.useCaptcha] as JSON)
            } else {
                render([status: "error", message: g.message(code: form.failureMessage ?: "form.submit.failure"), captchaType: captchaType, hasCaptcha: form.useCaptcha] as JSON)
            }
        }
    }
}
