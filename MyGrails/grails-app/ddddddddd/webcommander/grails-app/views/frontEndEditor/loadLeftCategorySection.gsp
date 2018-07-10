<div class="fee-body">
    <table class="fee-table left-table">
        <colgroup>
            <col style="width: 70%">
            <col style="width: 30%">
        </colgroup>
        <tr class="fee-ignore">
            <th><g:message code="category.name"/></th>
            <th class="actions-column check-all"><input class="multiple" type="checkbox"></th>
        </tr>
        <g:if test="${allCategories.size() > 0}">
            <g:each in="${allCategories}" var="category">
                <tr class="item">
                    <td>${category.name.encodeAsBMHTML()}</td>
                    <td class="actions-column" item="${category.id}" type="category">
                        <input type="checkbox" class="multiple" value="${category.id}">
                    </td>
                </tr>
            </g:each>
        </g:if>
        <g:else>
            <tr class="table-no-entry-row">
                <td colspan="2"><g:message code="no.category.found"/></td>
            </tr>
        </g:else>
    </table>
</div>
<div class="fee-footer">
    <paginator total="${count}" offset="${params.offset}" url="frontEndEditor/categoryConfig?onlyLeft=true" max="${params.max}"></paginator>
</div>
