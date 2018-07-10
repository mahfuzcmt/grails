<%@ page import="com.webcommander.util.AppUtil; com.webcommander.constants.DomainConstants; com.webcommander.manager.HookManager; com.webcommander.webcommerce.PaymentGatewayMeta" %>
<g:set var="fields" bean="configService"/>
<div  id="my-lists" class="bmui-tab">
    <div class="bmui-tab-header-container top-box">
        <g:each in="${fields.getSortedFields(viewConfig)}" var="field">
            <g:set var="active" value="${viewConfig[field + '_active'].toBoolean(null)}"/>
            <g:set var="order" value="${viewConfig[field + '_order']}"/>
            <g:set var="label" value="${viewConfig[field + '_label']}"/>
            <g:if test="${active != null}">
                <g:if test="${viewConfig.wish_list_active == "true"  && field == 'wish_list'}">
                    <div class="bmui-tab-header top" data-tabify-tab-id="${field}">
                        <span class="title"> ${label}</span>
                    </div>
                </g:if>
                <g:if test="${viewConfig.gift_registry_active == "true"  && field == 'gift_registry'}">
                    <div class="bmui-tab-header top" data-tabify-tab-id="${field}">
                        <span class="title"> ${label}</span>
                    </div>
                </g:if>
            </g:if>
        </g:each>
    </div>
    <div class="bmui-tab-body-container">
        <g:each in="${fields.getSortedFields(viewConfig)}" var="field">
            <g:set var="active" value="${viewConfig[field + '_active'].toBoolean(null)}"/>
            <g:set var="order" value="${viewConfig[field + '_order']}"/>
            <g:set var="label" value="${viewConfig[field + '_label']}"/>
            <g:if test="${viewConfig.wish_list_active == "true"  && field == 'wish_list'}">
                <div id="bmui-tab-${field}">
                    <div class="wish_list">
                        <plugin:hookTag hookPoint="customerProfileWishLists" attrs="${[:]}"/>
                    </div>
                </div>
            </g:if>
            <g:if test="${viewConfig.gift_registry_active == "true"  && field == 'gift_registry'}">
                <div id="bmui-tab-${field}">
                    <div class="gift_registry">
                        <plugin:hookTag hookPoint="customerProfileGiftRegistry" attrs="${[:]}"/>
                    </div>
                </div>
            </g:if>
        </g:each>
    </div>
</div>
