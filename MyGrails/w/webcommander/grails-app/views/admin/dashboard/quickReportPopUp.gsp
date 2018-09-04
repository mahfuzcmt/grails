<form action="${app.relativeBaseUrl()}dashboard/saveQuickReportConfiguration" method="post" class="edit-popup-form">
    <g:each in="${dashletItemList}" var="item">
        <div class="form-row" item-it="${item.id}">
            <label><g:message code="${item.title}"/></label>
            <input type="checkbox" class="single" value="${item.id}" name="${item.uiClass}" ${item.isVisible == true ? "checked" : ""}>
        </div>
    </g:each>
    <div class="button-line">
        <button type="submit" class="submit-button"><g:message code="save"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>
