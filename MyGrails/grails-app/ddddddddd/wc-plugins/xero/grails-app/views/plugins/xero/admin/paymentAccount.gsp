<%@ page import="com.webcommander.webcommerce.Product; com.webcommander.admin.Customer" %>
<div class="toolbar-share">
    <div class="toolbar toolbar-right ">
        <div class="tool-group">
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>
<form action="${app.relativeBaseUrl()}xero/saveConfigurations" method="post" class="create-edit-form">
    <input type="hidden" name="type" value="xero">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="payment.account.integration.settings"/></h3>
            <div class="info-content"><g:message code="section.text.xero.payment.account"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row">
                <label><g:message code="order.export.type"/></label>
                <div class="radio-group horizontal">
                    <div class="radio">
                        <input type="radio" class="single" name="xero.order_sync_type" value="detail" toggle-target="detail-panel" ${config.order_sync_type == "detail" ? "checked='checked'" : ""}>
                        <label><g:message code="detail"/></label>
                    </div>
                    <div class="radio">
                        <input type="radio" class="single" name="xero.order_sync_type" value="summary" ${config.order_sync_type == "summary" ? "checked='checked'" : ""}>
                        <label><g:message code="summary"/></label>
                    </div>
                </div>
            </div>
            <div class="form-row chosen-wrapper">
                <label><g:message code="default.payment.account"/></label>
                <g:select from="${accounts}" optionKey="code" optionValue="name" name="xero.default_payment_account" value="${config.default_payment_account ?: null}"/>
            </div>

            <g:each in="${paymentGatewayList}" var="gateWay">
                <div class="form-row chosen-wrapper detail-panel">
                    <label>${gateWay.name}</label>
                    <g:select from="${accounts}" noSelection="${['':"${g.message(code: 'select.account')}"]}" optionKey="code" optionValue="name" name="payment_account.${gateWay.code}" value="${paymentMapping?."${gateWay.code}"}"/>
                </div>
            </g:each>
            <div class="form-row">
                <button type="submit" class="submit-button"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>