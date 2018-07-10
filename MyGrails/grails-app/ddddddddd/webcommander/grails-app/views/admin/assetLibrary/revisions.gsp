<form class="edit-popup-form" action="" method="post">
    <input type="hidden" name="originalFilePath" value="${filePath}">
    <div class="file-revision">
        <table>
            <colgroup>
                <col style="width: 30%">
                <col style="width: 40%">
                <col style="width: 15%">
                <col style="width: 10%"
            </colgroup>
            <tr>
                <th><g:message code="revision"/></th>
                <th><g:message code="operator"/></th>
                <th><g:message code="storage.used"/></th>
                <th class="actions-column">
                </th>
            </tr>
            <g:each in="${files}" var="file">
                <tr>
                    <td>${file.time}</td>
                    <td>${file.user}</td>
                    <td>${file.size}</td>
                    <td class="actions-column">
                        <span class="tool-icon replace" title="<g:message code="replace.with.it"/>" data-path="${file.path}"></span>
                    </td>
                </tr>
            </g:each>
        </table>
        <div class="form-row total-usage">
            <label><g:message code="total.storage.used"/></label>
            <label>${total}</label>
        </div>
        <div class="clear-all">
            <span class="tool-icon clear-all" title="<g:message code="clear.all"/>"></span>
        </div>
    </div>
    <div class="button-line">
        <button type="button" class="cancel-button"><g:message code="close"/></button>
    </div>
</form>