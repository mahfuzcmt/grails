<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.webcommerce.Product" %>
<g:applyLayout name="_productwidget">
    <g:if test="${product?.productType == DomainConstants.PRODUCT_TYPE.GIFT_CARD}">
        <g:include view="plugins/gift_card/giftCardExtraFields.gsp"/>
    </g:if>
</g:applyLayout>