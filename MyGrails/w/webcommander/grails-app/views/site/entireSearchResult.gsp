<div class="entire-search">
    <h2><div class="search-result"><g:message args="${[searchCriteria]}" code="search.result.for"/></div></h2>
    <div class="total-result"><g:message args="${[count]}" code="total.result.found"/></div>
    <g:if test="${count}">
        <div class="header">
            <paginator data-urlprefix="spx" total="${count}" offset="${offset}" max="${max}"></paginator>
        </div>
        <div class="search-item">
            <g:each in="${article}" var="art">
                <div class="article">
                    <span class="title">${art.name}</span> <span class="search-type"><g:message code="search.result.article"/></span>
                    ${art.summary}<span class="view-detail"><a href="${app.relativeBaseUrl()}article/${art.url}"><g:message code="view.details"/></a></span>
                </div>
            </g:each>
            <g:each in="${product}" var="pro">
                <div class="product">
                    <span class="title">${pro.name}</span><span class="search-type"><g:message code="search.result.product"/></span>
                    <span class="view-detail"><a href="${app.relativeBaseUrl()}product/${pro.url}"><g:message code="view.details"/></a></span>
                </div>
            </g:each>
            <g:each in="${category}" var="cat">
                <div class="category">
                    <span class="title">${cat.name}</span><span class="search-type"><g:message code="search.result.category"/></span>
                    <span class="view-detail"><a href="${app.relativeBaseUrl()}category/${cat.url}"><g:message code="view.details"/></a></span>
                </div>
            </g:each>
            <g:each in="${collection}" var="col">
                <div class="collection">
                    <span class="title">${col.name}</span><span class="search-type"><g:message code="search.result.collection"/>
                </div>
            </g:each>
        </div>
        <div class="footer">
            <paginator data-urlprefix="spx" total="${count}" offset="${offset}" max="${max}"></paginator>
        </div>
    </g:if>
</div>