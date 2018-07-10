<g:if test="${params.id}">
    <h1><g:message code="edit.wish.list"/></h1>
</g:if>
<g:else>
    <h1><g:message code="create.new.wish.list"/></h1>
</g:else>
<form class="wish-list-edit-form" action="${app.relativeBaseUrl()}wishlist/saveWishList">
    <input type="hidden" name="id" value="${wishList.id}">
    <div class="form-row mandatory">
        <label><g:message code="name"/>:</label>
        <input type="text" name="name" validation="required rangelength[2, 100]" value="${wishList.name}">
    </div>
    <g:if test="${wishList.id}">
        <table>
            <colgroup>
                <col class="name-column" style="width: 55%">
                <col class="action-column" style="width: 45%">
            </colgroup>
            <tr>
                <th><g:message code="product.name"/></th>
                <th><g:message code="actions"/></th>
            </tr>
            <g:each in="${productDataList}" var="data">
                <g:set var="item" value="${wishList.wishListItems.find {it.product.id == data.id}}"/>

                <tr>
                    <input type="hidden" name="wlItemId" value="${item.id}">
                    <td>${item.product.name.encodeAsBMHTML()}</td>
                    <td>
                        <span class="action-icon remove delete" item-id="${item.id}" title="<g:message code="remove"/>"></span>
                    </td>
                </tr>
            </g:each>
            <g:if test="${wishList.wishListItems.size() == 0}">
                <tr>
                    <td colspan="3">
                        <g:message code="no.product.added.to.wish.list"/>
                    </td>
                </tr>
            </g:if>
        </table>
    </g:if>

    <div class="form-row btn-row">
        <label></label>
        <button type="submit" class="submit-button"><g:message code="${wishList.id ? "update" : "save"}"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>