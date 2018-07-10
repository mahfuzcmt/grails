<%@ page import="com.webcommander.webcommerce.ShippingClass" %>
<form action="${app.relativeBaseUrl()}shipmentCalculator/calculate" class="shipment-calculator-form" method="post">
    <div class='shipment-calculator-popup'>
        <div class="header">
            <span class="close-popup close-icon"></span>
            <span class="status-message"><g:message code="shipment.calculator"/></span>
        </div>
        <div class="body">
            <div class="multi-column">
                <div class="first-column">
                    <div class="column-content">
                        <input name="page" type="hidden" value="${params.page}">
                        <g:if test="${params.page == 'product'}">
                            <input name="productId" type="hidden" value="${params.productId}">
                        </g:if>
                        <div class="form-row country-selector-row">
                            <label><g:message code="country"/>:</label>
                            <ui:countryList id="countryId" name="country.id" value="${defaultCountryId.toLong()}"/>
                        </div>
                        <g:include view="/admin/customer/stateFormFieldView.gsp" model="[states: states]"/>
                        <div class="form-row post-code-row">
                            <label><g:message code="post.code"/>:</label>
                            <input type="text" class="" name="postCode" validation="maxlength[5]" />
                        </div>
                        <div class="form-row city-selector-row  ${mandatory}">
                            <label><site:message code="${label ?: "s:suburb/city"}"/>:</label>
                            <g:include controller="app" action="loadCities"/>
                        </div>
                    </div>
                </div><div class="last-column">
                <div class="column-content">
                    <h4><g:message code="shipping.cost" /></h4>
                </div>
            </div>
            </div>
        </div>
        <div class="popup-bottom footer">
            <button class="submit-button send-email" type="submit"><g:message code="calculate"/></button>
            <button class="form-reset close-popup" onclick="return false"><g:message code="cancel"/></button>
        </div>
    </div>
</form>
