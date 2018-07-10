<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.webcommerce.ShippingProfile; com.webcommander.webcommerce.TaxProfile" %>
<form action="${app.relativeBaseUrl()}categoryAdmin/saveProductSettings" method="post" class="create-edit-form">
    <input type="hidden" name="id" value="${category.id}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="category.profiles"/></h3>
        </div>
        <div class="form-section-container">
            <div class="form-row chosen-wrapper">
                <label><g:message code="tax.profile"/><span class="suggestion"> e.g. Australian Tax</span> </label>
                <ui:domainSelect name="taxProfile" class="medium" domain="${TaxProfile}" prepend="${['': g.message(code: "none")]}" value="${category.taxProfile?.id}"/>
            </div>
            <div class="form-row chosen-wrapper">
                <label><g:message code="shipping.profile"/><span class="suggestion"> e.g. Victoria Shipping</span> </label>
                <ui:domainSelect name="shippingProfile" class="medium" domain="${ShippingProfile}" prepend="${['': g.message(code: "none")]}" value="${category.shippingProfile?.id}"/>
            </div>
        </div>
    </div>
    <div class="section-separator"></div>
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="price.cart"/></h3>
        </div>

        <div class="form-section-container">
            <div class="form-row">
                <input type="checkbox" class="single" name="restrictPrice" toggle-target="restrict-price-dependent" ${category.restrictPriceFor != DomainConstants.PRODUCT_RESTRICT_PRICE_FOR.NONE ? "checked" : ""}>
                <span><g:message code="restrict.price"/></span>
                <sapn class="help">(<g:message code="who.can.view"/>)</sapn>
            </div>
            <div class="form-row restrict-price-dependent">
                <span class="radio-wrapper">
                    <input type="radio" name="restrictPriceFor" value="${DomainConstants.PRODUCT_RESTRICT_PRICE_FOR.EVERYONE}" ${category.restrictPriceFor == DomainConstants.PRODUCT_RESTRICT_PRICE_FOR.EVERYONE || category.restrictPriceFor == DomainConstants.PRODUCT_RESTRICT_PRICE_FOR.NONE ? "checked" : ""}>
                    <span class="value"><g:message code="nobody"/></span>
                </span>
                <span class="radio-wrapper">
                    <input type="radio" name="restrictPriceFor" value="${DomainConstants.PRODUCT_RESTRICT_PRICE_FOR.EXCEPT_CUSTOMER}" ${category.restrictPriceFor == DomainConstants.PRODUCT_RESTRICT_PRICE_FOR.EXCEPT_CUSTOMER ? "checked='checked'" : ""}>
                    <span class="value"><g:message code="only.customers"/></span>
                </span>
                <span class="radio-wrapper">
                    <input type="radio" toggle-target="restrict-price-except-select-customer" name="restrictPriceFor" value="${DomainConstants.PRODUCT_RESTRICT_PRICE_FOR.EXCEPT_SELECTED}" ${category.restrictPriceFor == DomainConstants.PRODUCT_RESTRICT_PRICE_FOR.EXCEPT_SELECTED ? "checked" : ""}>
                    <span class="value"><g:message code="selected.customers.only"/></span>
                    <span class="restrict-price-except-select-customer tool-icon choose choose-customer"></span>
                </span>
                <g:each in="${category.restrictPriceExceptCustomers}" var="customer">
                    <input type="hidden" name="restrictPriceExceptCustomer" value="${customer.id}">
                </g:each>
                <g:each in="${category.restrictPriceExceptCustomerGroups}" var="customerGroup">
                    <input type="hidden" name="restrictPriceExceptCustomerGroup" value="${customerGroup.id}">
                </g:each>
            </div>

            <div class="form-row">
                <input type="checkbox" class="single" name="restrictPurchase" toggle-target="restrict-purchase-dependent" ${category.restrictPurchaseFor != DomainConstants.PRODUCT_RESTRICT_PURCHASE_FOR.NONE ? "checked" : ""}>
                <span><g:message code="restrict.purchase"/></span>
                <sapn class="help">(<g:message code="who.can.purchase"/>)</sapn>
            </div>
            <div class="form-row restrict-purchase-dependent">
                <span class="radio-wrapper">
                    <input type="radio" name="restrictPurchaseFor" value="${DomainConstants.PRODUCT_RESTRICT_PURCHASE_FOR.EVERYONE}" ${category.restrictPurchaseFor == DomainConstants.PRODUCT_RESTRICT_PURCHASE_FOR.EVERYONE || category.restrictPurchaseFor == DomainConstants.PRODUCT_RESTRICT_PURCHASE_FOR.NONE ? "checked" : ""}>
                    <span class="value"><g:message code="nobody"/></span>
                </span>
                <span class="radio-wrapper">
                    <input type="radio" name="restrictPurchaseFor" value="${DomainConstants.PRODUCT_RESTRICT_PURCHASE_FOR.EXCEPT_CUSTOMER}" ${category.restrictPurchaseFor == DomainConstants.PRODUCT_RESTRICT_PURCHASE_FOR.EXCEPT_CUSTOMER ? "checked='checked'" : ""}>
                    <span class="value"><g:message code="only.customers"/></span>
                </span>
                <span class="radio-wrapper">
                    <input type="radio" toggle-target="restrict-purchase-except-select-customer" name="restrictPurchaseFor" value="${DomainConstants.PRODUCT_RESTRICT_PURCHASE_FOR.EXCEPT_SELECTED}" ${category.restrictPurchaseFor == DomainConstants.PRODUCT_RESTRICT_PURCHASE_FOR.EXCEPT_SELECTED ? "checked" : ""}>
                    <span class="value"><g:message code="selected.customers.only"/></span>
                    <span class="restrict-purchase-except-select-customer tool-icon choose choose-customer"></span>
                </span>
                <g:each in="${category.restrictPurchaseExceptCustomers}" var="customer">
                    <input type="hidden" name="restrictPurchaseExceptCustomer" value="${customer.id}">
                </g:each>
                <g:each in="${category.restrictPurchaseExceptCustomerGroups}" var="customerGroup">
                    <input type="hidden" name="restrictPurchaseExceptCustomerGroup" value="${customerGroup.id}">
                </g:each>
            </div>
            <div class="form-row">
                <label>&nbsp;</label>
                <button type="submit" class="submit-button"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>