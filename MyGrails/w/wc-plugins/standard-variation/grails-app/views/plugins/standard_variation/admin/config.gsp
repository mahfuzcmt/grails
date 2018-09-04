<%@ page import="com.webcommander.plugin.standard_variation.constant.NameConstants; com.webcommander.util.AppUtil" %>
<form class="standard-variation-panel" method="post" action="${app.relativeBaseUrl()}standardVariation/updateCombination">
    <input type="hidden" name="id" value="${variation.id}">
    <input type="hidden" class="image-id" name="image" value="${sDetails.imageId}">
    <table>
        <colgroup>
            <col style="width: 30%;">
            <col style="width: 30%;">
            <col style="width: 40%;">
        </colgroup>
        <tr>
            <th><g:message code="price.adjustment.type"/></th>
            <th><g:message code="amount"/></th>
            <th><g:message code="image"/></th>
        </tr>
        <tr>
            <g:set var="product" value="${variation.product}"/>
            <td>
                <g:set var="config" value="${NameConstants.PRICE_ADJUST_TYPES}"/>
                <g:select id="price-adjustment" name="adjustmentType" from="${config.values().collect {g.message(code: it)}}" keys="${config.keySet()}" value="${sDetails.priceAdjustableType}"/>
            </td>
            <td class="form-row amount-row">
                <span class="base-currency">${AppUtil.baseCurrency.symbol}</span>
                <input id="amount" type="text" base-price="${product.basePrice?.toAdminPrice()}" name="amount"${sDetails.priceAdjustableType == "b" ? " disabled" : ''}
                       value="${sDetails.price && sDetails.priceAdjustableType != "b" ? sDetails.price?.toAdminPrice() : product.basePrice?.toAdminPrice()}" restrict="decimal"
                       validation="required number maxlength[16] price">
            </td>
            <td>
                <span class="image-reference">
                    <g:if test="${sDetails.imageId  && product.images.find {it.id == sDetails.imageId.toLong()} }">
                        <img src="${appResource.getProductImageURL(image: product.images.find {it.id == sDetails.imageId.toLong(0)}, size: 150)}">
                    </g:if>
                    <g:else>
                        <span class="tool-icon add"></span><span class="add-image"><g:message code="add.image"/></span>
                    </g:else>
                </span>
            </td>
        </tr>
    </table>
    <div class="button-line">
        <button type="submit" class="submit-button"><g:message code="update"/></button>
    </div>
</form>