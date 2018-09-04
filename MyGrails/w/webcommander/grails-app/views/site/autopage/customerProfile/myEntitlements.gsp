<%@ page import="com.webcommander.util.AppUtil; com.webcommander.constants.DomainConstants; com.webcommander.manager.HookManager; com.webcommander.webcommerce.PaymentGatewayMeta" %>
<g:set var="fields" bean="configService"/>
<div class="bmui-tab top-box">
    <div class="bmui-tab-header-container top-box">
        <g:each in="${fields.getSortedFields(viewConfig)}" var="field">
            <g:set var="active" value="${viewConfig[field + '_active'].toBoolean(null)}"/>
            <g:set var="order" value="${viewConfig[field + '_order']}"/>
            <g:set var="label" value="${viewConfig[field + '_label']}"/>
            <g:if test="${active != null}">
                <g:if test="${viewConfig.store_credit_active == "true"  && field == 'store_credit'}">
                    <div class="bmui-tab-header top" data-tabify-tab-id="${field}">
                        <span class="title"> ${label}</span>
                    </div>
                </g:if>
                <g:if test="${viewConfig.loyalty_point_active == "true"  && field == 'loyalty_point'}">
                    <g:set var="configs_loyality_points" value="${AppUtil.getConfig(com.webcommander.constants.DomainConstants.SITE_CONFIG_TYPES.LOYALTY_POINT)}"/>
                    <g:if test="${configs_loyality_points.is_enabled == "true"}">
                    <div class="bmui-tab-header top" data-tabify-tab-id="${field}">
                        <span class="title"> ${label}</span>
                    </div>
                    </g:if>
                    <g:if test="${configs_loyality_points.enable_referral == "true"}">
                        <div class="bmui-tab-header top" data-tabify-tab-id="referral">
                            <span class="title">Referral </span>
                        </div>
                    </g:if>
                </g:if>
                <g:if test="${viewConfig.gift_card_active== "true"  && field == 'gift_card'}">
                    <g:set var="configs_gift_card" value="${AppUtil.getConfig(com.webcommander.constants.DomainConstants.SITE_CONFIG_TYPES.GIFT_CARD)}"/>
                    <g:if test="${configs_gift_card.is_enabled == "true"}">
                        <div class="bmui-tab-header top" data-tabify-tab-id="${field}">
                            <span class="title"> ${label}</span>
                        </div>
                    </g:if>
                </g:if>
            </g:if>
        </g:each>
    </div>
    <div class="bmui-tab-body-container">
        <g:each in="${fields.getSortedFields(viewConfig)}" var="field">
            <g:set var="active" value="${viewConfig[field + '_active'].toBoolean(null)}"/>
            <g:set var="order" value="${viewConfig[field + '_order']}"/>
            <g:set var="label" value="${viewConfig[field + '_label']}"/>
            <g:if test="${viewConfig.store_credit_active  == "true"  && field == 'store_credit'}">
                <license:allowed id="allow_store_credit_feature">
                    <div id="bmui-tab-${field}">
                        <div class="store_credit">
                            <g:include controller="customer" action="loadStoreCredit"/>
                        </div>
                    </div>
                </license:allowed>
            </g:if>
            <g:if test="${viewConfig.loyalty_point_active == "true"  && field == 'loyalty_point'}">
                <div id="bmui-tab-${field}">
                    <div class="loyalty_point">
                        <plugin:hookTag hookPoint="customerProfileLoyaltyPoints" attrs="${[:]}"/>
                    </div>
                </div>
            </g:if>
            <g:if test="${viewConfig.loyalty_point_active == "true" && field == 'loyalty_point'}">
                <g:set var="configs_loyality_points" value="${AppUtil.getConfig(com.webcommander.constants.DomainConstants.SITE_CONFIG_TYPES.LOYALTY_POINT)}"/>
                <g:if test="${configs_loyality_points.enable_referral == "true"}">
                <div id="bmui-tab-referral">
                    <div class="referral">
                        <plugin:hookTag hookPoint="customerProfilerReferral" attrs="${[:]}"/>
                    </div>
                </div>
               </g:if>
            </g:if>
            <g:if test="${viewConfig.gift_card_active== "true"  && field == 'gift_card'}">
                <div id="bmui-tab-${field}">
                    <div class="gift_card">
                        <plugin:hookTag hookPoint="customerProfileGiftCard" attrs="${[:]}"/>
                    </div>
                </div>
            </g:if>
        </g:each>
    </div>
</div>
