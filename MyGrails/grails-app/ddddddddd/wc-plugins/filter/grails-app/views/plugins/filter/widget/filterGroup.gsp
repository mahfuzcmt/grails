<g:applyLayout name="_productwidget">
    <g:if test="${isAdmin}">
        <div class="widget-text">
            <span><g:message code="filter.group.widget"/></span>
        </div>
    </g:if>
    <g:elseif test="${productAssocList}">
        <g:each in="${productAssocList}" var="productAssoc">
            <g:if test="${productAssoc.item.filterGroup.isActive}">
                <div class="info-row filter-group">
                    <label>${productAssoc.item.filterGroup.name}:</label>
                    <a href="${app.relativeBaseUrl() + 'filter/' + productAssoc.item.url}">
                        <span class="name">${productAssoc.item.heading.encodeAsBMHTML()}</span>
                    </a>
                </div>
            </g:if>
        </g:each>
    </g:elseif>
</g:applyLayout>

%{--<g:applyLayout name="_productwidget">
    <g:if test="${isAdmin}">
        <div class="widget-text">
            <span><g:message code="filter.group.widget"/></span>
        </div>
    </g:if>
    <g:elseif test="${selectedFilterGroupItem}">
        <div class="info-row filter-group">
            <label><g:message code="filter.group"/>:</label>
            <a href="${app.relativeBaseUrl() + 'filter/' + selectedFilterGroupItem.url}">
                <span class="name">${selectedFilterGroupItem.title.encodeAsBMHTML()}</span>
            </a>
        </div>
    </g:elseif>
</g:applyLayout>--}%
