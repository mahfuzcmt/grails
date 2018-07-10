<div class="body">
    <table class="content">
        <colgroup>
            <col style="width: 70%">
            <col style="width: 30%">
        </colgroup>
        <tr>
            <th><g:message code="product.name"/></th>
            <th class="actions-column"><input class="check-all multiple" type="checkbox"></th>
        </tr>
        <g:if test="${products.size() > 0}">
            <g:each in="${products}" var="product">
                <tr>
                    <td>
                        <plugin:hookTag hookPoint="productSelectionColumn" attrs="${[productId: product.id]}">
                            ${product.name.encodeAsBMHTML()}
                        </plugin:hookTag>
                    </td>
                    <td class="actions-column" ${params.fieldName ?: fieldName ?: "product"}="${product.id}">
                        <input type="checkbox" class="multiple">
                        <input type="hidden" value="${product.id}">
                    </td>
                </tr>
            </g:each>
        </g:if>
        <g:else>
            <tr class="table-no-entry-row">
                <td colspan="2"><g:message code="no.product.found"/> </td>
            </tr>
        </g:else>
    </table>
</div>
<div class="footer">
    <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
</div>