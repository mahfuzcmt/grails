package com.webcommander.plugin.ask_question.controllers.site

import com.webcommander.captcha.CaptchaService
import com.webcommander.constants.DomainConstants
import com.webcommander.plugin.ask_question.AskQuestionService
import com.webcommander.util.AppUtil
import grails.converters.JSON
import grails.util.Holders

class AskQuestionController {
    AskQuestionService askQuestionService
    CaptchaService captchaService;

    def questionTab() {
        render(view: '/plugins/ask_question/site/loadQuestionForm')
    }

    def saveQuestion() {
        String captchaConfig = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "captcha_setting")
        def captchaType = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "captcha_type");
        try {
            if (captchaConfig == "enable" && !captchaService.validateCaptcha(params, request)) {
                render([status: "error", captchaValidation: "failure", message: g.message(code: "invalid.captcha.entry"), captchaType: captchaType] as JSON)
            } else if (askQuestionService.saveQuestion(params)) {
                render([status: "success", message: g.message(code: "question.send.success"), captchaType: captchaConfig == "enable" ? captchaType : "disabled"] as JSON)
            } else {
                render([status: "error", message: g.message(code:  "question.send.failure"), captchaType: captchaConfig == "enable" ? captchaType : "disabled"] as JSON)
            }
        } catch (Exception ex) {
            render([status: "error", message: g.message(code:  "question.send.failure"), captchaType: captchaConfig == "enable" ? captchaType : "disabled"] as JSON)
        }
    }
}
