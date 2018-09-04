<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.constants.NamedConstants;" %>
<div class="wish-list-list">
    <g:if test="${wishLists.size()}">
        <table>
            <colgroup>
                <col class="name-col">
                <col class="product-col">
                <col class="action-col">
            </colgroup>
            <thead>
            <tr>
                <th><g:message code="name"/></th>
                <th><g:message code="products"/></th>
                <th><g:message code="action"/></th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${wishLists}" var="wishList">
                <tr>
                    <td><div class="wrapper" data-label="<g:message code="name"/>:">${wishList.name.encodeAsBMHTML()}</div></td>
                    <td><div class="wrapper" data-label="<g:message code="product"/>:">${wishList.wishListItems.size()}</div></td>
                    <td>
                        <div class="wrapper" data-label="<g:message code="actions"/>:">
                            <a href="${app.relativeBaseUrl()}wishlist/products/${wishList.id}"><span class="action-icon details" wishList-id="${wishList.id}" title="<g:message code="details"/>"></span></a>
                            <span class="action-icon edit" wishList-id="${wishList.id}" title="<g:message code="edit"/>"></span>
                            <span class="action-icon share" wishList-id="${wishList.id}" title="<g:message code="share" args="${[""]}"/>"></span>
                            <span class="action-icon remove delete" wishList-id="${wishList.id}" title="<g:message code="remove"/>"></span>
                        </div>
                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>
    </g:if>
    <g:else>
        <div class="no-data"><g:message code="no.wish.list.found"/> </div>
    </g:else>

    <span class="button create-wish-list"><g:message code="create.new.wish.list"/></span>
</div>