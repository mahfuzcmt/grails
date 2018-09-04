<%@ page import="com.webcommander.config.StoreProductAssoc; com.webcommander.constants.DomainConstants; com.webcommander.util.StringUtil" %>
<form action="${app.relativeBaseUrl()}productAdmin/saveMultiStoreProperties" method="post" class="create-edit-form product-properties-and-store">
    <div class="multiStore table-view">
        <input type="hidden" name="id" value="${product.id}">
        <div class="body">
            <table class="content">
                <colgroup>
                    <col class="select-column">
                    <col class="name-column">
                    %{--<col class="price-column">
                    <col class="quantity-column">--}%
                </colgroup>
                <tr>
                    <th class="select-column"></th>
                    <th><g:message code="name"/></th>
                    %{--
                    todo: later implementation for now hide
                    <th><g:message code="price"/></th>
                    <th><g:message code="quantity"/></th>
                    --}%
                </tr>
                <g:each in="${stores}" var="store" status="i">
                    <g:set var="storeProductAssoc" value="${StoreProductAssoc.findByStoreAndProduct(store, product)}"/>
                    <tr class="data-row" data-storeId="${store.id}">
                        <td class="select-column">
                            <input entity-id="${store.id}" type="checkbox" name="selected" value="${store.id}" ${storeProductAssoc?"checked":""} class="multiple select-store">
                        </td>
                        <td class="name">
                            <span class="value">${store.location.encodeAsBMHTML()}</span>
                        </td>
                        %{--
                        todo: later implementation for now hide
                        <td class="editable store-price">
                            <span class="inline-editable" data-editable-validation="required number">${storeProductAssoc ? storeProductAssoc.price : product.getDisplayPrice()}</span>
                            <input class="column-name price" type="hidden" name="storeAssoc.${store.id}.price" value="${storeProductAssoc ? storeProductAssoc.price : product.getDisplayPrice()}">
                        </td>
                        <td class="editable store-quantity">
                            <span class="inline-editable" data-editable-validation="required number">${storeProductAssoc ? storeProductAssoc.availableStock : product.availableStock}</span>
                            <input class="column-name quantity" type="hidden" name="storeAssoc.${store.id}.quantity" value="${storeProductAssoc ? storeProductAssoc.availableStock : product.availableStock}">
                        </td>--}%
                    </tr>
                </g:each>
            </table>
        </div>
        <div class="button-line">
            <label>&nbsp</label>
            <button type="submit" class="edit-popup-form-submit submit-button"><g:message code="update"/></button>
        </div>
    </div>
</form>