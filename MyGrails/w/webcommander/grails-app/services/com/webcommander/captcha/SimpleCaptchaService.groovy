package com.webcommander.captcha

import com.webcommander.manager.CacheManager
import com.webcommander.util.AppUtil

class SimpleCaptchaService {

    static final CAPTCHA_SOLUTION_ATTR = 'captcha'
    static final CAPTCHA_IMAGE_ATTR = 'captchaImage'

    boolean validateCaptcha(String captchaSolution) {

        def session = AppUtil.session
        String solution = session[CAPTCHA_SOLUTION_ATTR]

        session.removeAttribute(CAPTCHA_SOLUTION_ATTR)
        session.removeAttribute(CAPTCHA_IMAGE_ATTR)
        CacheManager.removeCache("session", session.id, SimpleCaptchaService.CAPTCHA_IMAGE_ATTR)

        captchaSolution ? solution?.compareToIgnoreCase(captchaSolution) == 0 : false
    }

}
