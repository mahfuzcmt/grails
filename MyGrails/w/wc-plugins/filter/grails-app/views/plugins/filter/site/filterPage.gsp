<div class="filter-details">
    <div class="filter-title">
        <h1 class="title page-heading"><g:message code="search.result"/></h1>
    </div>
    <div class="product-view">

        <g:if  test="${!productList}" >
            <div class="total-result"><g:message args="0" code="total.result.found"/></div>
        </g:if>
        <g:else>
            <g:include view="widget/productListings.gsp" model="[url_prefix: 'bdpr']"/>
        </g:else>

    </div>
</div>