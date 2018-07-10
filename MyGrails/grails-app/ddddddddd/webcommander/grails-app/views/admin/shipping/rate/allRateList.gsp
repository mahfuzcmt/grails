<div class="right-panel">
    <div class="header">
        <div class="left-header">
            <h3 class="title"><g:message code="rates"/></h3>
            <p class="description"><g:message code="rates.description"/></p>
        </div>
        <div class="toolbar toolbar-right">
            <div class="tool-group action-header" style="display: none">
                <button type="button" class="rate-bulk-edit"><g:message code="edit"/></button>
            </div>
            <div class="bulk-edit-buttons" style="display: none">
                <div class="tool-group">
                    <button type="button" class="rate-bulk-save"><g:message code="save"/></button>
                </div>
                <div class="tool-group">
                    <button type="button" class="rate-bulk-cancel"><g:message code="cancel"/></button>
                </div>
            </div>
            <div class="filter-group">
                <div class="advance-filter-btn"><g:message code="filter.by"/></div>
                <form class="search-form tool-group">
                    <input type="text" class="search-text" placeholder="<g:message code="search"/>"><button type="submit" class="icon-search"></button>
                </form>
            </div>

        </div>
    </div>
    <div class="rate-data-list app-tab-content-container">
        <table class="content">
            <colgroup>
                <col style="width: 5%">
                <col style="width: 22%">
                <col style="width: 21%">
                <col style="width: 21%">
                <col style="width: 21%">
                <col style="width: 10%">
            </colgroup>
            <tr>
                <th class="select-column"><input type="checkbox" class="check-all multiple"></th>
                <th><g:message code="rate.name"/></th>
                <th><g:message code="method"/></th>
                <th><g:message code="shipping.cost"/></th>
                <th><g:message code="handling.cost"/></th>
                <th class="action-col"></th>
            </tr>
            <g:each in="${rateList}" var="rate">
                <g:include view="admin/shipping/rate/rateRow.gsp" model="${[rate: rate]}"/>
            </g:each>
        </table>
    </div>
    <div class="footer">
        <ui:perPageCountSelector/>
        <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
    </div>
</div>
