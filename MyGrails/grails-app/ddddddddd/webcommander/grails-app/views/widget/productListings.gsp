<%@ page import="com.webcommander.manager.HookManager; com.webcommander.constants.NamedConstants; com.webcommander.util.AppUtil;" %>
<g:set var="productViewClazz" value="${['product-view', config["display-type"] + '-view']}"/>
<%
    productViewClazz = HookManager.hook("productViewClazz", productViewClazz, config)
%>
<g:set var="productViewClazz" value="${productViewClazz.join(" ")}"/>
<div class="${productViewClazz}" id="${config['product_listing_id']}" display-type="${config["display-type"]}"  show-on-hover="${config.show_on_hover}">
    <g:if test="${config["display-type"] == NamedConstants.PRODUCT_WIDGET_VIEW.SCROLLABLE}">
        <div class="header">
            <div class="scroller">
                <span class="scroll-left"></span>
                <span class="scroll-right"></span>
            </div>
        </div>
    </g:if>
    <g:elseif test="${config["show_view_switcher"] == "true"|| config["show-pagination"] == NamedConstants.PAGINATION_TYPE.TOP || config["show-pagination"] == NamedConstants.PAGINATION_TYPE.TOP_AND_BOTTOM || config["sortable"] == "true"}">
        <div class="header">
            <g:if test="${config["sortable"] == "true"}">
                <div class="sortable">
                    <select id="product-sorting" class="product-sorting medium" urlprefix="${url_prefix}">
                        <plugin:hookTag hookPoint="productSortingTypes" attrs="[config: config]">
                            <option value=""><g:message code="sort.by"/></option>
                            <option value="ALPHA_ASC" ${config["product-sorting"] == "ALPHA_ASC" ? "selected" : ""}><g:message code="alphabetic.a.z"/></option>
                            <option value="ALPHA_DESC" ${config["product-sorting"] == "ALPHA_DESC" ? "selected" : ""}><g:message code="alphabetic.z.a"/></option>
                            <g:if test="${config["price"] == "true"}">
                                <option value="PRICE_DESC" ${config["product-sorting"] == "PRICE_DESC" ? "selected" : ""}><g:message code="price.high.low"/></option>
                                <option value="PRICE_ASC" ${config["product-sorting"] == "PRICE_ASC" ? "selected" : ""}><g:message code="price.low.high"/></option>
                            </g:if>
                        </plugin:hookTag>
                    </select>
                </div>
            </g:if>
            <g:if test="${(config["show-pagination"] == NamedConstants.PAGINATION_TYPE.TOP || config["show-pagination"] == NamedConstants.PAGINATION_TYPE.TOP_AND_BOTTOM)}">
                <g:if test="${config["item-per-page-selection"] == 'true'}">
                    <ui:perPageCountSelector value="${max}"/>
                </g:if>
                <paginator data-urlprefix="${url_prefix}" total="${totalCount}" offset="${offset}" max="${max}"></paginator>
            </g:if>
            <g:if test="${config["show_view_switcher"] == "true"}">
                <div class="btn-group view-switchers">
                    <g:if test="${config["display-type"] == NamedConstants.PRODUCT_WIDGET_VIEW.LIST}">
                        <button class="list-view-switcher active" value="list" type="button"></button>
                        <button class="image-view-switcher" value="image" type="button"></button>
                    </g:if>
                    <g:else>
                        <button class="list-view-switcher" value="list" type="button"></button>
                        <button class="image-view-switcher active" value="image" type="button"></button>
                    </g:else>
                </div>
            </g:if>
        </div>
    </g:elseif>
    <div class="content">
        <g:include view="site/productImageView.gsp"/>
    </div>
    <g:if test="${(config["show-pagination"] == NamedConstants.PAGINATION_TYPE.BOTTOM || config["show-pagination"] == NamedConstants.PAGINATION_TYPE.TOP_AND_BOTTOM) && config["display-type"] != NamedConstants.PRODUCT_WIDGET_VIEW.SCROLLABLE}">
        <div class="footer">
            <span class="pagination-literal-status"><site:message code="s:pagination.status.text" args="${[offset + 1, offset + max < totalCount ? offset + max : totalCount, totalCount, Math.ceil(totalCount / max)]}"/></span>
            <g:if test="${config["item-per-page-selection"] == 'true'}">
                <ui:perPageCountSelector value="${max}"/>
            </g:if>
            <paginator data-urlprefix="${url_prefix}" total="${totalCount}" offset="${offset}" max="${max}"></paginator>
        </div>
    </g:if>
</div>