<div class="multi-column two-column left-right-selector-panel">
    <div class="columns first-column">
        <div class="column-content metatag-entry-panel" validation-attr="metatag-validation">
            <div class="body">
                <div class="form-row mandatory">
                    <label><g:message code="name"/></label>
                    <input type="text" class="medium tagName small" metatag-validation="required maxlength[100]"/>
                </div>
                <div class="form-row mandatory">
                    <label><g:message code="value"/></label>
                    <input type="text" class="medium tagValue small" metatag-validation="required maxlength[1000]"/>
                </div>
            </div>
            <div class="form-row">
                <label>&nbsp</label>
                <button type="button" class="submit-button addMetatag"><g:message code="add"/></button>
            </div>
        </div>
    </div><div class="columns last-column">
    <div class="column-content selected-panel table-view">
            <table>
                <colgroup>
                    <col style="width: 80%">
                    <col style="width: 20%">
                </colgroup>
                <tr>
                    <th><g:message code="name"/></th>
                    <th class="actions-column"><span class="tool-icon remove-all"></span></th>
                </tr>
                <g:each in="${metatags}" var="tag">
                    <tr data-id="${tag.id}">
                        <td>${tag.name}</td>
                        <td class="actions-column" type="metatag" item= ${tag.name}>
                            <span class="action-navigator collapsed"></span>
                            <input type="hidden" name="tag_name" value="${tag.name}"/>
                            <input type="hidden" name="tag_content" value="${tag.value}"/>
                        </td>
                    </tr>
                </g:each>
            </table>
            <div class="pagination-line">
                <paginator total="${metatags.size()}" offset="0" max="${10}"></paginator>
            </div>
            <div class="hidden">
                <div class="action-column-dice-content">
                    <table>
                        <tr>
                            <td></td>
                            <td class="actions-column">
                                <span class="action-navigator collapsed"></span>
                                <input type="hidden"/>
                                <input type="hidden"/>
                            </td>
                        </tr>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>