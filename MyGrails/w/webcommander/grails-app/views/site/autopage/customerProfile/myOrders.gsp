<%@ page import="com.webcommander.util.AppUtil; com.webcommander.constants.DomainConstants; com.webcommander.manager.HookManager; com.webcommander.webcommerce.PaymentGatewayMeta" %>
<g:set var="fields" bean="configService"/>
<div class="bmui-tab">
    <div class="bmui-tab-header-container top-box">
        <g:each in="${fields.getSortedFields(viewConfig,"_orderkey")}" var="field">
            <g:set var="active" value="${viewConfig[field + '_active'].toBoolean(null)}"/>
            <g:set var="order" value="${viewConfig[field + '_order']}"/>
            <g:set var="label" value="${viewConfig[field + '_label']}"/>
            <g:if test="${active != null}">
                <license:allowed id="product_limit">
                    <g:if test="${viewConfig.pending_order_active == "true"  && field == 'pending_order'}">
                        <div class="bmui-tab-header top" data-tabify-tab-id="${field}">
                            <span class="title"> ${label}</span>
                        </div>
                    </g:if>
                    <g:if test="${viewConfig.completed_order_active == "true"  && field == 'completed_order'}">
                        <div class="bmui-tab-header top" data-tabify-tab-id="${field}">
                            <span class="title"> ${label}</span>
                        </div>
                    </g:if>
                </license:allowed>
            </g:if>
        </g:each>
    </div>
    <div class="bmui-tab-body-container">
        <license:allowed id="product_limit">
            <g:each in="${fields.getSortedFields(viewConfig,"_orderkey")}" var="field">
                <g:set var="active" value="${viewConfig[field + '_active'].toBoolean(null)}"/>
                <g:set var="order" value="${viewConfig[field + '_order']}"/>
                <g:set var="label" value="${viewConfig[field + '_label']}"/>
                <g:if test="${active != null}">
                   <g:if test="${viewConfig.pending_order_active == "true"  && field == 'pending_order'}">
                        <div id="bmui-tab-${field}">
                            <div class="pending_order">
                                <g:include view="/site/autopage/customerProfile/pendingOrder/orderListView.gsp" model="[ orders: pendingOrders, orderTotals: pendingOrderTotals]"/>
                            </div>
                        </div>
                    </g:if>
                    <g:if test="${viewConfig.completed_order_active == "true"  && field == 'completed_order'}">
                        <div id="bmui-tab-${field}">
                            <div class="completed_order">
                                <g:include view="/site/autopage/customerProfile/completedOrder/orderListView.gsp" model="[ orders:  completedOrders, orderTotals:  completedOrderTotals]"/>
                            </div>
                        </div>
                    </g:if>
                </g:if>
            </g:each>
        </license:allowed>
    </div>
</div>
