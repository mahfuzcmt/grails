<form class="edit-popup-form">
    <div class="header filter-block">
        <g:select name="selection-type" class="medium" from="[g.message(code: 'brands'), g.message(code: 'manufacturers')]" keys="['brand', 'manufacturer']"/>
        <div class="toolbar toolbar-right">
            <div class="search-form tool-group">
                <input type="text" class="search-text" placeholder="<g:message code="search"/>"><button type="button" class="icon-search search-button submit-button search-form-submit"></button>
            </div>
        </div>
    </div>
    <div class="left-right-selector-panel">
        <div class="multi-column two-column">
            <div class="columns first-column">
                <div class="column-content selection-panel table-view">
                    <g:include controller="brandAdmin" action="loadBrandForSelection"/>
                </div>
            </div><div class="columns last-column">
            <div class="column-content selected-panel table-view">
                <div class="body">
                    <table>
                        <colgroup>
                            <col style="width: 80%">
                            <col style="width: 20%">
                        </colgroup>
                        <tr>
                            <th><g:message code="name"/></th>
                            <th class="actions-column">
                                <span class="tool-icon remove-all"></span>
                            </th>
                        </tr>
                        <g:each in="${brands}" var="brand">
                            <tr>
                                <td>${brand.name.encodeAsBMHTML()}</td>
                                <td class="actions-column" type="brand" item="${brand.id}">
                                    <span class="tool-icon remove-item remove"></span>
                                    <input type="hidden" name="${params.brandField ?: "brand"}" value="${brand.id}">
                                </td>
                            </tr>
                        </g:each>
                        <g:each in="${manufacturers}" var="manufacturer">
                            <tr>
                                <td>${manufacturer.name.encodeAsBMHTML()}</td>
                                <td class="actions-column" type="manufacturer" item="${manufacturer.id}">
                                    <span class="tool-icon remove-item remove"></span>
                                    <input type="hidden" name="${params.manufacturerField ?: "manufacturer"}" value="${manufacturer.id}">
                                </td>
                            </tr>
                        </g:each>
                    </table>
                </div>
                <div class="footer">
                    <paginator total="${(brands?.size() ?: 0) + (manufacturers?.size() ?: 0)}" offset="0" max="${10}"></paginator>
                </div>
                <div class="hidden">
                    <div class="action-column-dice-content">
                        <table>
                            <tr>
                                <td></td>
                                <td class="actions-column">
                                    <span class="tool-icon remove-item remove"></span>
                                    <input type="hidden">
                                </td>
                            </tr>
                        </table>
                    </div>
                </div>
            </div>
        </div>
        </div>
    </div>
    <div class="button-line">
        <button type="submit" class="submit-button edit-popup-form-submit"><g:message code="done"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>