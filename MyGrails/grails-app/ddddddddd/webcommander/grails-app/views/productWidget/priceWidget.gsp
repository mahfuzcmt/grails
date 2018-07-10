<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.util.AppUtil" %>
<g:set var="productSetting" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.PRODUCT)}"/>
<g:if test="${!productData.isCallForPriceEnabled && !productData.isPriceRestricted(AppUtil.loggedCustomer, AppUtil.loggedCustomerGroupIds)}">
    <g:applyLayout name="_productwidget">
        <plugin:hookTag hookPoint="productPriceWidget" attrs="[productData: productData]">
            <g:if test="${productData.isOnSale}">
                <span class="previous-price price${productSetting.strike_through_previous_price == "true" ? " strike-through" : ""}">
                    <span class="currency-symbol">${AppUtil.siteCurrency.symbol}</span><span class="price-amount">${productData.previousPriceToDisplay.toCurrency().toPrice()}</span>
                </span>
            </g:if>
            <g:if test="${productData.isExpectToPay}">
                <g:set var="productConfig" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.PRODUCT)}"/>
                <g:set var="expectToPayPrice" value="${productConfig.expect_to_pay_price_with_tax == 'true' ? productData.expectToPayPriceWithTax : productData.expectToPayPrice}"/>
               <div class="expect-to-pay-container">
                   <label class="label-for-expect-to-pay"><site:message code="${productSetting.label_for_expect_to_pay}"/></label>
                   <span class="previous-price price${productSetting.strike_through_previous_price == "true" ? " strike-through" : ""}">
                       <span class="currency-symbol">${AppUtil.siteCurrency.symbol}</span><span class="price-amount">${expectToPayPrice.toCurrency().toPrice()}</span>
                   </span>
               </div>
            </g:if>
            <div class="current-price-container" is-on-sale="${productData.isOnSale}" is-expect-to-pay="${productData.isExpectToPay}">
                <label class="label-for-base-price"><site:message code="${productSetting.label_for_base_price}"/></label>
                <span class="current-price price">
                    <span class="currency-symbol">${AppUtil.siteCurrency.symbol}</span><span class="price-amount">${productData.priceToDisplay.toCurrency().toPrice()}</span>
                    <g:if test="${productData.taxMessage}">
                        <span class="tax-message"><g:message code="${productData.taxMessage.encodeAsBMHTML()}"/></span>
                    </g:if>
                </span>
            </div>
        </plugin:hookTag>
    </g:applyLayout>
</g:if>
