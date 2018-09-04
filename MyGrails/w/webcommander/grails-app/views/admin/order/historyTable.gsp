<%@ page import="com.webcommander.webcommerce.Product" %>
<div class="history-table">
    <table>
        <tr>
            <th><g:message code="product.details"/></th>
            <th><g:message code="quantity"/></th>
            <th><g:message code="shipping.method"/></th>
            <th><g:message code="track.info"/></th>
            <th><g:message code="shipping.date"/></th>
        </tr>
        <g:each in="${historyList}" var='history' status="i">
            <g:set var="product" value="${Product.get(history.orderItem.productId)}"/>
            <tr>
                <td>
                    <span class="sku">${product?.sku}</span>
                    <span class="name">${history.orderItem.productName}</span>
                </td>
                <td>
                    <span class="previous-quantity">${history.previousQuantity}</span>
                    <span class="changed-quantity">${history.changedQuantity}</span>
                </td>
                <g:if test="${i == 0}">
                    <td rowspan="${historyList.size()}">
                        <span class="previous-method">${history.previousMethod}</span>
                        <span class="changed-method">${history.changedMethod}</span>
                    </td>
                    <td rowspan="${historyList.size()}">
                        <span class="previous-track">${history.previousTrack}</span>
                        <span class="changed-track">${history.changedTrack}</span>
                    </td>
                    <td rowspan="${historyList.size()}">
                        <span class="previous-date">${history.previousDate.toAdminFormat(false, false, session.timezone)}</span>
                        <span class="changed-current">${history.changedDate.toAdminFormat(false, false, session.timezone)}</span>
                    </td>
                </g:if>
            </tr>
        </g:each>
    </table>
</div>