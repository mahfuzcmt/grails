<%@ page import="com.webcommander.util.StringUtil" %>
<g:set var="id" value="${StringUtil.uuid}"/>
<div class="form-row">
    <label>&nbsp;</label>
    <span class='recaptcha-container' id=captcha-'${id}' config-sitekey="${publicKey}" config-theme="light"></span>
</div>
<g:if test="${request.xhr}">
    <script type="text/javascript">
        app.captchaUtil.bindReCaptcha($("#captcha-${id}"), function () {
            bm.addScript("https://www.google.com/recaptcha/api.js?render=explicit", true)
        })
    </script>
</g:if>
