<%@ page import="com.webcommander.util.AppUtil; com.webcommander.constants.DomainConstants; com.webcommander.manager.HookManager; com.webcommander.webcommerce.PaymentGatewayMeta" %>
<g:set var="fields" bean="configService"/>
<div id="my-carts" class="bmui-tab">
    <div class="bmui-tab-header-container top-box">

        <g:each in="${fields.getSortedFields(viewConfig)}" var="field">
            <g:set var="active" value="${viewConfig[field + '_active'].toBoolean(null)}"/>
            <g:set var="order" value="${viewConfig[field + '_order']}"/>
            <g:set var="label" value="${viewConfig[field + '_label']}"/>

                <g:if test="${viewConfig.save_cart_active == "true"  && field == 'save_cart'}">
                    <div class="bmui-tab-header top" data-tabify-tab-id="${field}">
                        <span class="title"> ${label}</span>
                    </div>
                </g:if>
                <g:if test="${viewConfig.abandoned_cart_active == "true"  && field == 'abandoned_cart'}">
                    <div class="bmui-tab-header top" data-tabify-tab-id="${field}">
                        <span class="title"> ${label}</span>
                    </div>
                </g:if>

        </g:each>

    </div>

    <div class="bmui-tab-body-container">
            <g:each in="${fields.getSortedFields(viewConfig)}" var="field">

                <g:set var="active" value="${viewConfig[field + '_active'].toBoolean(null)}"/>
                <g:set var="order" value="${viewConfig[field + '_order']}"/>
                <g:set var="label" value="${viewConfig[field + '_label']}"/>
                <g:if test="${active != null}">
                    <g:if test="${viewConfig.save_cart_active == "true"  && field == 'save_cart'}">
                        <div id="bmui-tab-${field}">
                            <div class="save_cart">
                                <plugin:hookTag hookPoint="customerProfileSaveCarts" attrs="${[:]}"/>
                            </div>
                        </div>
                    </g:if>

                    <g:if test="${viewConfig.abandoned_cart_active == "true"  && field == 'abandoned_cart'}">
                        <div id="bmui-tab-${field}">
                            <div class="abandoned_cart">
                                <plugin:hookTag hookPoint="customerProfileAbandonedCarts" attrs="${[:]}"/>
                            </div>
                        </div>
                    </g:if>
                </g:if>
            </g:each>

    </div>
</div>
