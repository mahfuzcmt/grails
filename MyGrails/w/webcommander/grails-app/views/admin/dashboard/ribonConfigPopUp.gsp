<form action="${app.relativeBaseUrl()}dashboard/saveRibbonConfiguration" method="post" class="edit-popup-form two-column">
    <input type="hidden" name="configType" value="${params.configType}">
    <g:each in="${dashletItemList}" var="item">
        <div class="form-row" item-it="${item.id}">
            <input type="checkbox" class="multiple"  value="${item.uiClass}" name="name" ${item.isVisible == true ? "checked" : ""}>
            <label><g:message code="${item.title}"/></label>
        </div>
    </g:each>
    <div class="button-line">
        <button type="submit" class="submit-button"><g:message code="save"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>
