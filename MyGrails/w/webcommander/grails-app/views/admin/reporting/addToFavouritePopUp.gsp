<form action="${app.relativeBaseUrl()}commanderReporting/saveFavourite" method="post" class="edit-popup-form">
    <g:if test="${report}">
        <input type="hidden" name="id" value="${report.id}">
    </g:if>
    <div class="form-row">
        <label><g:message code="enter.name.favourite.report"/></label>
        <input type="text" class="report-name" validation="required" name="reportName" value="${report?.name}">
    </div>
    <div class="button-line">
        <button type="submit" class="submit-button"><g:message code="save"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>