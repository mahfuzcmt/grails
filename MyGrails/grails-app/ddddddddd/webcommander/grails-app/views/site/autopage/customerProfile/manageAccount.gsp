<%@ page import="com.webcommander.util.AppUtil; com.webcommander.constants.DomainConstants; com.webcommander.manager.HookManager; com.webcommander.webcommerce.PaymentGatewayMeta" %>
<g:set var="fields" bean="configService"/>
<div class="bmui-tab">
    <div class="bmui-tab-header-container top-box">
        <g:each in="${fields.getSortedFields(viewConfig)}" var="field">
            <g:set var="active" value="${viewConfig[field + '_active'].toBoolean(null)}"/>
            <g:set var="order" value="${viewConfig[field + '_order']}"/>
            <g:set var="label" value="${viewConfig[field + '_label']}"/>
            <g:if test="${viewConfig.account_information_active == "true"  && field == 'account_information'}">
                <div class="bmui-tab-header top" data-tabify-tab-id="${field}">
                    <span class="title"> ${label}</span>
                </div>
            </g:if>
            <license:allowed id="product_limit">
                <g:if test="${viewConfig.billing_address_active == "true"  && field == 'billing_address' && (AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce") == 'true')}">
                    <div class="bmui-tab-header top" data-tabify-tab-id="${field}">
                        <span class="title"> ${label}</span>
                    </div>
                </g:if>
                <g:if test="${viewConfig.shipping_address_active == "true"  && field == 'shipping_address' && (AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce") == 'true')}">
                    <div class="bmui-tab-header top" data-tabify-tab-id="${field}">
                        <span class="title"> ${label}</span>
                    </div>
                </g:if>
            </license:allowed>
        </g:each>
    </div>
    <div class="bmui-tab-body-container">
        <license:allowed id="product_limit">
            <g:each in="${fields.getSortedFields(viewConfig)}" var="field">
                <g:set var="active" value="${viewConfig[field + '_active'].toBoolean(null)}"/>
                <g:set var="order" value="${viewConfig[field + '_order']}"/>
                <g:set var="label" value="${viewConfig[field + '_label']}"/>
                <g:if test="${viewConfig.account_information_active == "true"  && field == 'account_information'}">
                    <div id="bmui-tab-${field}">
                        <div class="account_information">
                            <g:include view="/site/autopage/customerProfile/accountInfoView.gsp" model="${[address: address]}"/>
                        </div>
                    </div>
                </g:if>
                <g:if test="${viewConfig.billing_address_active == "true"  && field == 'billing_address' && (AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce") == 'true')}">
                    <div id="bmui-tab-${field}">
                        <div class="billing_address">
                            <g:include view="/site/autopage/customerProfile/addressListView.gsp" model="[addresses: billingAddresses, activeAddress: activeBillingAddress]"/>
                        </div>
                    </div>
                </g:if>
                <g:if test="${viewConfig.shipping_address_active == "true"  && field == 'shipping_address' && (AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce") == 'true')}">
                    <div id="bmui-tab-${field}">
                        <div class="shipping_address">
                            <g:include view="/site/autopage/customerProfile/addressListView.gsp" model="[addresses: shippingAddresses,activeAddress: activeShippingAddress]"/>
                        </div>
                    </div>
                </g:if>
            </g:each>
        </license:allowed>
    </div>
</div>
