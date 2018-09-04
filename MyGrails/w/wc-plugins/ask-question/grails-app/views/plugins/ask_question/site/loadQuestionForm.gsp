<%@ page import="com.webcommander.util.AppUtil; com.webcommander.constants.DomainConstants" %>
<div class="ask-question-panel">
    <div class="message-container">

    </div>
    <form class="question-form" action="${app.relativeBaseUrl()}askQuestion/saveQuestion">
        <input type="hidden" name="productId" value="${params.productId}">
        <div class="form-row mandatory">
            <label><g:message code="name"/>:</label>
            <input name="name" type="text" validation="required maxlength[200]" maxlength="200">
        </div>
        <div class="form-row mandatory">
            <label><g:message code="email"/>:</label>
            <input name="email" type="text" validation="required email maxlength[200]" maxlength="200">
        </div>
        <div class="form-row mandatory">
            <label><g:message code="question"/>:</label>
            <textarea name="question" validation="required maxlength[250]" maxlength="250"></textarea>
        </div>
        <g:set var="captchaConfig" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL)}"/>
        <g:if test="${captchaConfig.captcha_setting == 'enable'}">
            <ui:captcha/>
        </g:if>
        <div class="form-row">
            <label>&nbsp;</label>
            <button type="submit"><g:message code="ask"/></button>
        </div>
    </form>
</div>
