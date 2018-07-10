<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.util.AppUtil" %>
<g:set var="fields" bean="configService"/>
<div id="customer-profile-tabs" class="bmui-tab left-side-header">
    <g:set var="welcomeHtml" value="${customWelcomeHtml.trim()}" />
    <div class="welcome-profile">
        <g:if test="${isWelcomeMessageActive == "true"}">
            <g:if test="${welcomeHtml}">
                <span class="welcome-message">${welcomeHtml}</span>
            </g:if>
            <g:else>
                <g:message code="welcome"/> <span>${customer.fullName().encodeAsBMHTML()}</span> to <span>${storeName.encodeAsBMHTML()}</span>
            </g:else>
        </g:if>
        <span class="button profileLogout"><a href="${app.relativeBaseUrl()}customer/logout"><g:message code="logout"/></a></span>
    </div>
    <div class="bmui-tab-header-container">
        <plugin:hookTag hookPoint="customerProfilePluginsJS" attrs="${[:]}"/>
        <g:each in="${fields.getSortedFields(viewConfig)}" var="field">
            <g:set var="active" value="${viewConfig[field + '_active'].toBoolean(null)}"/>
            <g:set var="order" value="${viewConfig[field + '_order']}"/>
            <g:set var="label" value="${viewConfig[field + '_label']}"/>
            <license:allowed id="product_limit">
                <g:if test="${viewConfig.overview_active == "true"  && field == 'overview' && (AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce") == 'true')}">
                    <div class="bmui-tab-header left" data-tabify-tab-id="overview" data-tabify-url="${app.relativeBaseUrl() + 'customer/loadOverview'}">
                        <span class="title">${label}</span>
                    </div>
                </g:if>
            </license:allowed>
            <g:if test="${viewConfig.manage_my_account_active == "true"  && field == 'manage_my_account'}">
                <div class="bmui-tab-header left" data-tabify-tab-id="manage-account" data-tabify-url="${app.relativeBaseUrl() + 'customer/loadManageAccount'}">
                    <span class="title">${label}</span>
                </div>
            </g:if>
            <g:if test="${viewConfig.my_eorders_active == "true"  && field == 'my_eorders' && (AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce") == 'true')}">
                <div class="bmui-tab-header left" data-tabify-tab-id="my-orders" data-tabify-url="${app.relativeBaseUrl() + 'customer/loadOrdersView'}">
                    <span class="title">${label}</span>
                </div>
            </g:if>
            <g:if test="${viewConfig.my_carts_active == "true"  && field == 'my_carts' && (AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce") == 'true')}">
                <g:if test="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.MY_CARTS).size() > 0 }">
                    <div class="bmui-tab-header left" data-tabify-tab-id="my-carts" data-tabify-url="${app.relativeBaseUrl() + 'customer/loadMyCarts'}">
                        <span class="title">${label}</span>
                    </div>
                </g:if>
            </g:if>
            <g:if test="${viewConfig.my_lists_active == "true"  && field == 'my_lists' && (AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce") == 'true')}">
                <g:if test="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.MY_LISTS).size() > 0 }">
                    <div class="bmui-tab-header left" data-tabify-tab-id="my-list" data-tabify-url="${app.relativeBaseUrl() + 'customer/loadMyLists'}">
                        <span class="title">${label}</span>
                    </div>
                </g:if>
            </g:if>
            <g:if test="${viewConfig.my_entitlements_active == "true"  && field == 'my_entitlements' && (AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce") == 'true')}">
                <g:if test="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.MY_ENTITLEMENTS).size() > 0 }">
                    <div class="bmui-tab-header left" data-tabify-tab-id="my-entitlements" data-tabify-url="${app.relativeBaseUrl() + 'customer/loadMyEntitlements'}">
                        <span class="title">${label}</span>
                    </div>
                </g:if>
            </g:if>
            <g:if test="${viewConfig.enableWallet && viewConfig.my_wallet_active == "true"  && field == 'my_wallet' && (AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce") == 'true')}">
                <div class="bmui-tab-header left" data-tabify-tab-id="my-wallet" data-tabify-url="${app.relativeBaseUrl() + 'customer/loadStoreWallet'}">
                    <span class="title">${label}</span>
                </div>
            </g:if>
        </g:each>
    </div>
    <div class="bmui-tab-body-container">
        <plugin:hookTag hookPoint="customerProfileTabBody" attrs="${[:]}">
            <license:allowed id="product_limit">
                <g:if test="${(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce") == 'true')}">
                    <div id="bmui-tab-overview"></div>
                </g:if>
            </license:allowed>
            <div id="bmui-tab-manage-account"></div>
            <g:if test="${(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce") == 'true')}">
                <div id="bmui-tab-my-orders"></div>
            </g:if>
            <g:if test="${(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce") == 'true')}">
                <div id="bmui-tab-my-wallet"></div>
            </g:if>
        </plugin:hookTag>
    </div>
</div>