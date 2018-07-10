<form class="edit-popup-form">
    <div class="header filter-block">
        <g:select name="selection-type" class="medium" from="[g.message(code: 'product'), g.message(code: 'category')]" keys="['product', 'category']"/>
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
                    <g:include controller="productAdmin" action="loadProductsForSelection"/>
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
                        <g:each in="${selectedProducts}" var="product">
                            <tr>
                                <td>${product.name.encodeAsBMHTML()}</td>
                                <td class="actions-column" type="product" item="${product.id}">
                                    <span class="action-navigator collapsed"></span>
                                    <input type="hidden" name="${params.productField ?: 'product'}" value="${product.id}">
                                </td>
                            </tr>
                        </g:each>
                        <g:each in="${selectedCategories}" var="category">
                            <tr>
                                <td>${category.name.encodeAsBMHTML()}</td>
                                <td class="actions-column" type="category" item="${category.id}">
                                    <span class="action-navigator collapsed"></span>
                                    <input type="hidden" name="${params.categoryField ?: 'category'}" value="${category.id}">
                                </td>
                            </tr>
                        </g:each>
                    </table>
                </div>
                <div class="footer">
                    <paginator total="${(selectedProducts?.size() ?: 0) + (selectedCategories?.size() ?: 0)}" offset="0" max="${10}"></paginator>
                </div>
                <div class="hidden">
                    <div class="action-column-dice-content">
                        <table>
                            <tr>
                                <td></td>
                                <td class="actions-column">
                                    <span class="action-navigator collapsed"></span>
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