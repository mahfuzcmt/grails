<%@ page import="com.webcommander.util.StringUtil" %>
<form action="${app.relativeBaseUrl()}productAdmin/changeOnSale" method="post" class="edit-popup-form" enctype="multipart/form-data">
    <div class="form-row">
        <label><g:message code="on.sale"/></label>
        <g:set var="isOnSaleId" value="${com.webcommander.util.StringUtil.uuid}"/>
        <input id="${isOnSaleId}" name="isOnSale" type="checkbox" class="single" toggle-target="display-sale-price" value="true" ${params.isOnSale == "true" ? "checked" : ""}>
    </div>
    <div class="form-row display-sale-price mandatory">
        <label><g:message code="sale.price"/></label>
        <input name="salePrice" type="text" class="medium" restrict="decimal" maxlength="9" validation="required@if{global:#${isOnSaleId}:checked} number maxlength[9]" value="${params.salePrice}">
    </div>
    <div class="button-line">
        <button type="submit" class="submit-button"><g:message code="change.all"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>