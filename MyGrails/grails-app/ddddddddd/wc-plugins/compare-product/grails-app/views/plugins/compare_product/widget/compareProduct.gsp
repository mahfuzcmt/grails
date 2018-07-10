<g:applyLayout name="_widget">
    <app:enqueueSiteJs src="plugins/compare-product/js/compare-product.js" scriptId="compare-product" />
    <div class="compare-quantity"><g:message code="compare.products"/>(${dataList.size()})</div>
    <g:if test="${dataList.size()}">
        <div class="compare-item-container">
            <g:each in="${dataList}" var="item" status="i">
                <div class="row ${ i%2 == 0 ? "even" : "odd"}">
                    <span class="product-name">${item.name.encodeAsHTML()}</span>
                    <span class="close-icon" product-id="${item.id}"></span>
                </div>
            </g:each>
        </div>
        <g:if test="${dataList.size() > 1}">
            <div class="row btn-row">
                <span class="clear-all-btn button"><g:message code="clear.all"/></span>
                <span class="compare-btn button">
                    <a href="${app.relativeBaseUrl()}compareProduct/details"><g:message code="compare"/></a>
                </span>
            </div>
        </g:if>
    </g:if>
</g:applyLayout>