<div class="custom-fields-details">
    <span class="header"><g:message code="custom.field.details"/></span>
    <div class="order-row-detail-content">
        <table>
            <tfoot>
            <g:each in="${customData}" var="data">
                <tr>
                    <td>${data.fieldName.encodeAsBMHTML()}</td>
                    <td>
                        <span>${data.fieldValue.encodeAsBMHTML()}</span>
                    </td>
                </tr>
            </g:each>
            </tfoot>
        </table>
    </div>
</div>