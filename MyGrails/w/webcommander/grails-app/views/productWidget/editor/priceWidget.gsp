<%@ page import="com.webcommander.util.AppUtil" %>
<g:applyLayout name="_productwidget">
    <span class="previous-price price">
        <span class="currency-symbol">${AppUtil.baseCurrency.symbol}</span><span class="price-amount">110.00</span>
    </span>
    <span class="current-price price">
        <span class="currency-symbol">${AppUtil.baseCurrency.symbol}</span><span class="price-amount">100.00</span>
            <span class="tax-message">Lorem ipsum dolor sit amet, consectetur adipiscing elit.</span>
    </span>
</g:applyLayout>