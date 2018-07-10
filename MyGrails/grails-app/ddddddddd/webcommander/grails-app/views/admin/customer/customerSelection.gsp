<div class="left-right-selector-panel">
    <div class="header">
        <div class="toolbar toolbar-right">
            <div class="search-form tool-group">
                <input type="text" class="search-text" placeholder="<g:message code="search"/>"><button type="button" class="icon-search search-button submit-button"></button>
            </div>
        </div>
    </div>
    <div class="multi-column two-column">
        <div class="columns first-column">
            <div class="column-content selection-panel table-view">
                <g:include controller="customerAdmin" action="loadCustomerForMultiSelect"/>
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
                    <g:each in="${customers}" var="customer">
                        <tr>
                            <td>${customer.fullName.encodeAsBMHTML()}</td>
                            <td class="actions-column" type="customer" item="${customer.id}">
                                <span class="tool-icon remove-item remove"></span>
                                <input type="hidden" name="customer" value="${customer.id}">
                            </td>
                        </tr>
                    </g:each>
                </table>
            </div>
            <div class="footer">
                <paginator total="${customers?.size()}" offset="0" max="${10}"></paginator>
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