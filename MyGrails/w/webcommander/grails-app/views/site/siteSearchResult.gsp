<%@ page import="com.webcommander.constants.NamedConstants" %>
<div class="entire-search">
    <h2><div class="title"><g:message args="${[searchCriteria]}" code="search.result.for"/></div></h2>
    <div class="total-result"><g:message args="${[totalResult]}" code="total.result.found"/></div>
    <g:if test="${totalResult > 0}">
        <div class="search-results">
            <plugin:hookTag hookPoint="siteSearchResult">
                <g:if test="${productData}">
                    <%
                        Map productConfig = new LinkedHashMap(searchConfig)
                        productConfig["display-type"] = searchConfig.search_result_view
                        productConfig['show-pagination'] = NamedConstants.PAGINATION_TYPE.BOTTOM
                    %>
                    <div class="search-result products">
                        <div class="title"><g:message code="product"/></div>
                        <g:include view="widget/productListings.gsp" model="[productList: productData, config: productConfig, totalCount:  results.totalProduct, offset:  siteSearchConfig.product.offset, max:  siteSearchConfig.product.max , url_prefix: 'product-search']"/>
                    </div>
                </g:if>
                <g:if test="${categoryData}">
                    <div class="title"><g:message code="category"/></div>
                    <div class="search-result categories">
                        <g:include view="widget/categoryImageView.gsp" model="[categoryList: categoryData, url_prefix: 'category-search', config: [description: true, 'show-pagination': NamedConstants.PAGINATION_TYPE.BOTTOM ], totalCount: results.totalCategory, offset: siteSearchConfig.category.offset, max: siteSearchConfig.category.max]"/>
                    </div>
                </g:if>
                <g:if test="${pageData}">
                    <div class="search-result pages">
                        <div class="title"><g:message code="page"/></div>
                        <g:each in="${pageData}" var="pData">
                            <div class="page">
                                <a href="${app.relativeBaseUrl()}${pData.page.url}"><span class="title">${pData.page.name.encodeAsBMHTML()}</span></a>
                                <g:if test="${pData.pageContent}">
                                    <div class="summary">${pData.pageContent.textify().truncate(200)}</div>
                                    <a class="page-read-more" href="${app.relativeBaseUrl()}${pData.page.url}" target="_self"><site:message code="s:read.more"/></a>
                                </g:if>
                            </div>
                        </g:each>
                        <paginator data-urlprefix="page-search" total="${results.totalPage}" offset="${siteSearchConfig.page.offset}" max="${siteSearchConfig.page.max}"></paginator>
                    </div>
                </g:if>
            </plugin:hookTag>
        </div>
    </g:if>
</div>