<%@ page import="com.webcommander.util.StringUtil; com.webcommander.webcommerce.PaymentGateway;com.webcommander.constants.DomainConstants; com.webcommander.constants.NamedConstants;" %>
<%@ page import="com.webcommander.admin.Zone" %>
<form action="${app.relativeBaseUrl()}paymentGateway/update" method="post" class="edit-popup-form" enctype="multipart/form-data">
    <input type="hidden" name="id" value="${paymentGateway.id}"/>
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="payment.gateway.config"/></h3>
            <div class="info-content"><g:message code="section.text.payment.gateway.config"/></div>
        </div>
        <div class="form-section-container">
            <div class="bmui-tab">
                <div class="bmui-tab-header-container top-side-header">
                    <div class="bmui-tab-header" data-tabify-tab-id="general">
                        <span class="title"><g:message code="general"/></span>
                    </div>
                    <div class="bmui-tab-header" data-tabify-tab-id="messages">
                        <span class="title"><g:message code="after.payment.messages"/></span>
                    </div>
                </div>
                <div class="bmui-tab-body-container">
                    <g:set var="uuid" value="${StringUtil.uuid}"/>
                    <div id="bmui-tab-general">
                        <div class="double-input-row">
                            <div class="form-row">
                                <input type="checkbox" class="single" id="payment-gateway-config-gateway-enabled" uncheck-value="false" value="true" ${paymentGateway.isEnabled ? 'checked="checked"' : ''}
                                       name="isEnabled" toggle-target="default-gateway">
                                <span><g:message code="enable" /></span>
                                <g:if test="${!paymentGateway.isPromotional}">
                                    </div><div class="form-row">
                                    <input type="checkbox" class="single" id="is-default" uncheck-value="false" value="true" ${paymentGateway.isDefault ? 'checked="checked"' : ''} name="isDefault">
                                    <span><g:message code="default.gateway" /></span>
                                </g:if>
                            </div>
                        </div>
                        <g:include controller="paymentGateway" action="paymentProcessorFields" params="${[gateway: paymentGateway.code]}"/>
                        <div class="form-row chosen-wrapper">
                            <label><g:message code="zone"/></label>
                            <ui:domainSelect name="zone.id" class="large" domain="${Zone}" value="${paymentGateway.zone?.id}" prepend="${["": g.message(code: "any.zone")]}"
                                             append="${['create-zone':g.message(code: "new.zone")]}" filter="${{or {eq("isDefault", false);eq("name", "REST_OF_THE_WORLD")}}}"/>
                        </div>
                        <div class="form-row tinymce-container tinymce-oneline-btn">
                            <label><g:message code="information"/><span class="suggestion"> <g:message code="suggestion.payment.gateway.information"/></span> </label>
                            <textarea class="no-auto-size xx-large" toolbar-type="basic" name="information" maxlength="2000" validation="maxlength[2000]">${paymentGateway.information}</textarea>
                        </div>
                        <g:if test="${paymentGateway.isSurChargeApplicable}">
                            <div class="double-input-row">
                                <div class="form-row chosen-wrapper">
                                    <label><g:message code="surcharge.type"/></label>
                                    <ui:namedSelect id="surchargeType" class="large" name="surchargeType" key="${NamedConstants.SURCHARGE_TYPE}" value="${paymentGateway?.surchargeType}"/>
                                </div><div id="surcharge" class="form-row surcharge mandatory">
                                    <label for="surcharge"><g:message code="surcharge.amount" /><span class="suggestion"> e.g. 10</span></label>
                                <g:textField name="flatSurcharge" maxlength="16" class="large" restrict="decimal" validation="skip@if{self::hidden} required max[99999999] gt[0] maxlength[16]" value="${paymentGateway?.flatSurcharge}"/>
                                </div>
                            </div>

                            <div class="form-row block-error">
                                <div id="surcharge-range-selector" validate-on="call-only" validation="skip@if{self::hidden} skip@if{this::input[name='surcharge-amount']} fail" message_template="<g:message code="least.one.condition.required.to.configure.payment.gateway"/>">
                                    <h4 class="group-label"><g:message code="surcharge.range" /></h4>
                                    <g:include controller="paymentGateway" action="loadSurchargeDetails" params="[paymentGatewayId: paymentGateway.id]" />
                                </div>
                            </div>
                        </g:if>
                    </div>
                    <div id="bmui-tab-messages">
                        <g:if test="${paymentGateway.code != DomainConstants.PAYMENT_GATEWAY_CODE.STORE_CREDIT}">
                            <div class="form-row tinymce-container tinymce-oneline-btn">
                                <label><g:message code="pending.message"/></label>
                                <textarea class="wceditor no-auto-size xx-large" toolbar-type="basic" name="pendingMessage" maxlength="2000" validation="maxlength[2000]">${paymentGateway.pendingMessage}</textarea>
                            </div>
                        </g:if>
                        <g:if test="${!([DomainConstants.PAYMENT_GATEWAY_CODE.PAY_IN_STORE, DomainConstants.PAYMENT_GATEWAY_CODE.CHEQUE, DomainConstants.PAYMENT_GATEWAY_CODE.ACCOUNTS_PAYABLE, DomainConstants.PAYMENT_GATEWAY_CODE.BANK_DEPOSIT, DomainConstants.PAYMENT_GATEWAY_CODE.MONEY_ORDER].contains(paymentGateway.code))}">
                            <div class="form-row tinymce-container tinymce-oneline-btn">
                                <label><g:message code="completed.message"/></label>
                                <textarea class="wceditor no-auto-size xx-large" toolbar-type="basic" name="successMessage" maxlength="2000" validation="maxlength[2000]">${paymentGateway.successMessage}</textarea>
                            </div>
                        </g:if>
                    </div>
                </div>
            </div>
            <div class="form-row">
                <button type="submit" class="edit-popup-form-submit submit-button"><g:message code="update" /></button>
                <button type="button" class="cancel-button"><g:message code="cancel" /></button>
            </div>
        </div>
    </div>
</form>