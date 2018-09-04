<form action="${app.relativeBaseUrl()}dashboard/saveReportConfig" method="post" class="edit-popup-form two-column">
    <input type="hidden" name="dashletId" value="${params.dashletId}">
    <g:if test="${favouriteReport}">
        <g:radioGroup name="fvrtReportGroup" values="${favouriteReport.id}" labels="${favouriteReport.name}" value="${activeStat}">
            <div class="form-row">
                ${it.radio}<label class="value">${message(code: it.label)}</label>
            </div>
        </g:radioGroup>
        <div class="button-line">
            <button type="submit" class="submit-button"><g:message code="save"/></button>
            <button type="button" class="cancel-button"><g:message code="cancel"/></button>
        </div>
    </g:if>
    <g:else>
        <span class="message"><g:message code="no.favourite.report.crated"/></span>
        <div class="button-line">
            <button type="submit" disabled class="submit-button"><g:message code="save"/></button>
            <button type="button" class="cancel-button"><g:message code="cancel"/></button>
        </div>
    </g:else>
</form>