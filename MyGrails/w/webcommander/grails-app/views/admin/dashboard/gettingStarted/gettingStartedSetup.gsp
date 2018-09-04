<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.util.AppUtil" %>
<g:set var="steps" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GET_STARTED_WIZARD)}"/>
<input type="hidden" class="get_started_wizard_passed">
<div class="dashlet getting-started-wizard" id="wizard-dashlet">
    <div class="bmui-tab left-side-header alone">
        <div class="bmui-tab-header-container wizard-tab-header">
            <span class="title group-label"><g:message code="get.started"/></span>
            <div class="bmui-tab-header${steps.store_done == "true" ? " step-done" : ""}" data-tabify-tab-id="storeDetails" data-tabify-url="${app.relativeBaseUrl()}dashboard/loadStartedProperties?property=storeDetails">
                <span class="tool-icon store-details"></span>
                <span class="title-block"><span class="step-title"><g:message code="step1"/>:</span> <span class="title-name"><g:message code="store.detail"/></span></span>
                <g:if test="${steps.store_done == "true"}"><span class="tool-icon done"></span></g:if>
            </div>
            <div class="bmui-tab-header${steps.email_done == "true" ? " step-done" : ""}" data-tabify-tab-id="email" data-tabify-url="${app.relativeBaseUrl()}dashboard/loadStartedProperties?property=email">
                <span class="tool-icon email-setting"></span>
                <span class="title-block"><span class="step-title"><g:message code="step2"/>:</span> <span class="title-name"><g:message code="email.settings"/></span></span>
                <g:if test="${steps.email_done == "true"}"><span class="tool-icon done"></span></g:if>
            </div>
            <div class="bmui-tab-header${steps.design_done == "true" ? " step-done" : ""}" data-tabify-tab-id="design" data-tabify-url="${app.relativeBaseUrl()}dashboard/loadStartedProperties?property=design">
                <span class="tool-icon select-design"></span>
                <span class="title-block"><span class="step-title"><g:message code="step3"/>:</span> <span class="title-name"><g:message code="select.your.design"/></span></span>
                <g:if test="${steps.design_done == "true"}"><span class="tool-icon done"></span></g:if>
            </div>
            <div class="bmui-tab-header${steps.product_done == "true" ? " step-done" : ""}" data-tabify-tab-id="product" data-tabify-url="${app.relativeBaseUrl()}dashboard/loadStartedProperties?property=product">
                <span class="tool-icon add-first-product"></span>
                <span class="title-block"><span class="step-title"><g:message code="step4"/>:</span> <span class="title-name"><g:message code="add.your.first.product"/></span></span>
                <g:if test="${steps.product_done == "true"}"><span class="tool-icon done"></span></g:if>
            </div>
            <div class="bmui-tab-header${steps.page_done == "true" ? " step-done" : ""}" data-tabify-tab-id="page" data-tabify-url="${app.relativeBaseUrl()}dashboard/loadStartedProperties?property=page">
                <span class="tool-icon add-first-page"></span>
                <span class="title-block"><span class="step-title"><g:message code="step5"/>:</span> <span class="title-name"><g:message code="add.your.first.page"/></span></span>
                <g:if test="${steps.page_done == "true"}"><span class="tool-icon done"></span></g:if>
            </div>
            <div class="bmui-tab-header${steps.tax_done == "true" ? " step-done" : ""}" data-tabify-tab-id="tax" data-tabify-url="${app.relativeBaseUrl()}dashboard/loadStartedProperties?property=tax">
                <span class="tool-icon review-tax"></span>
                <span class="title-block"><span class="step-title"><g:message code="step6"/>:</span> <span class="title-name"><g:message code="review.tax"/></span></span>
                <g:if test="${steps.tax_done == "true"}"><span class="tool-icon done"></span></g:if>
            </div>
            <div class="bmui-tab-header${steps.shipping_done == "true" ? " step-done" : ""}" data-tabify-tab-id="shipping" data-tabify-url="${app.relativeBaseUrl()}dashboard/loadStartedProperties?property=shipping">
                <span class="tool-icon review-shipping"></span>
                <span class="title-block"><span class="step-title"><g:message code="step7"/>:</span> <span class="title-name"><g:message code="review.shipping"/></span></span>
                <g:if test="${steps.shipping_done == "true"}"><span class="tool-icon done"></span></g:if>
            </div>
            <div class="bmui-tab-header${steps.payment_done == "true" ? " step-done" : ""}" data-tabify-tab-id="paymentGateway" data-tabify-url="${app.relativeBaseUrl()}dashboard/loadStartedProperties?property=paymentGateway">
                <span class="tool-icon configure-payment-gateway"></span>
                <span class="title-block"><span class="step-title"><g:message code="step8"/>:</span> <span class="title-name"><g:message code="configure.payment.gateway"/></span></span>
                <g:if test="${steps.payment_done == "true"}"><span class="tool-icon done"></span></g:if>
            </div>
            <div class="bmui-tab-header${steps.launch_done == "true" ? " step-done" : ""}" data-tabify-tab-id="launchStore" data-tabify-url="${app.relativeBaseUrl()}dashboard/loadStartedProperties?property=launchStore">
                <span class="tool-icon launch-store"></span>
                <span class="title-block"><span class="step-title"><g:message code="step9"/>:</span> <span class="title-name"><g:message code="launch.store"/></span></span>
                <g:if test="${steps.launch_done == "true"}"><span class="tool-icon done"></span></g:if>
            </div>
        </div><div class="bmui-tab-body-container wizard-tab-body">
            <span class="title group-label"><span class="step-title"><g:message code="step1"/>:</span> <span class="title-name"><g:message code="store.detail"/></span></span>
            <div id="bmui-tab-storeDetails">
            </div>
            <div id="bmui-tab-email">
            </div>
            <div id="bmui-tab-design">
            </div>
            <div id="bmui-tab-product">
            </div>
            <div id="bmui-tab-page">
            </div>
            <div id="bmui-tab-tax">
            </div>
            <div id="bmui-tab-shipping">
            </div>
            <div id="bmui-tab-paymentGateway">
            </div>
            <div id="bmui-tab-launchStore">
            </div>
        </div>
    </div>
</div>