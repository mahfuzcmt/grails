<%@ page import="com.webcommander.manager.HookManager" %>
<g:set var="productViewClazz" value="${['product-search', 'product-view', config.search_result_view + '-view']}"/>
<%
    productViewClazz = HookManager.hook("productViewClazz", productViewClazz, config)
%>
<g:set var="productViewClazz" value="${productViewClazz.join(" ")}"/>
<div class="${productViewClazz}">
    <div class="search-result"><g:message args="${[searchCriteria]}" code="search.result.for"/></div>
    <div class="total-result"><g:message args="${[count]}" code="total.result.found"/></div>
    <g:if test="${count}">
        <div class="header">
            <paginator data-urlprefix="spx" total="${count}" offset="${offset}" max="${max}"></paginator>
        </div>
    </g:if>
    <div class="content">
        <g:if test="${config.search_result_view == 'image'}">
            <g:include view="site/productImageView.gsp"/>
        </g:if>
        <g:else>
            <g:include view="site/productListView.gsp"/>
        </g:else>
    </div>
    <div class="footer">
        <paginator data-urlprefix="spx" total="${count}" offset="${offset}" max="${max}"></paginator>
    </div>
</div>