<%@ page import="com.webcommander.webcommerce.ShippingClass" %>
<form class="select-shipping-class-form">
    <div class="form-row">
        <label><g:message code="shipping.class"/></label>
        <ui:domainSelect class="medium" name="shippingClass" domain="${ShippingClass}"/>
    </div>
    <div class="button-line">
        <button type="submit" class="submit-button"><g:message code="select"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>