<%@ page import="com.webcommander.webcommerce.Product" %>
<g:if test="${shipmentList}">
    <div class="shipment-log">
        <g:each in="${shipmentList}" var="shipment" status="i">
            <span class="log-title"><g:message code="shipment.log"/></span>
            <g:set var="relatedItems" value="${shipmentItems.findAll{it.shipment.id == shipment.id}}"/>
            <div class="shipment-details">
                <div class="details-head">
                    <div class="left-head">
                        <div class="date">
                            <span><g:message code="date"/></span>
                            <span>${shipment.shippingDate}</span>
                        </div>
                        <div class="method">
                            <span><g:message code="shipping.method"/></span>
                            <span>${shipment.method}</span>
                        </div>
                        <div class="track">
                            <span><g:message code="track.info"/></span>
                            <span>${shipment.trackingInfo}</span>
                        </div>
                    </div>
                    <div class="right-head">
                        <button type="button" data-shipping-id="${shipment.id}" class="edit-ship"><g:message code="edit"/> </button>
                        <button type="button" data-shipping-id="${shipment.id}" class="show-edit-history"><g:message code="history"/> </button>
                    </div>
                </div>
                <div class="details-table">
                    <div class="table-view no-paginator-table-view">
                        <div class="body">
                            <table class="content">
                                <g:if test="${i == 0}">
                                    <colgroup>
                                        <col class="sku-column">
                                        <col class="name-column">
                                        <col class="quantity-column">
                                    </colgroup>
                                    <tr>
                                        <th><g:message code="sku"/></th>
                                        <th><g:message code="name"/></th>
                                        <th><g:message code="shipped.quantity"/></th>
                                    </tr>
                                </g:if>
                                <g:each in="${relatedItems}" var="item">
                                    <g:set var="product" value="${Product.get(item.orderItem.productId)}"/>
                                    <tr>
                                        <td>${product?.sku}</td>
                                        <td>${item.orderItem.productName}</td>
                                        <td>${item.quantity}</td>
                                    </tr>
                                </g:each>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </g:each>
    </div>
</g:if>
