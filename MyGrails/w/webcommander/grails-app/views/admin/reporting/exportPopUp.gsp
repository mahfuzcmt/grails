<g:form action="${app.relativeBaseUrl()}commaderReporting/saveExport" method="post" class="edit-popup-form">
    <div class="title">
        <label>Please chose a report format</label>
        <g:select name="report-formate" from="${['csv','pdf','excel'].collectEntries({[(it): g.message(code: it)]})}" optionKey="${{it.key}}" optionValue="${{it.value}}" value=""/>
    </div>
    <div class="title">
        <label>Enter a name for your report</label></br>
        <input type="text"class="large report-name"/>
    </div>
    <div class="button-line">
        <button type="submit" class="submit-button"><g:message code="export.report"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</g:form>