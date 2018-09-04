<div>
    <h3><g:message code="${operation}.summary"/></h3>
    <g:if test="${emptyTask}">
        <span class="message-block warning"><g:message code="status.log.cleared"/></span>
    </g:if>
    <g:else>
        <g:include view="/admin/customerExportImport/summaryTable.gsp" model="[logs: logs]"/>
    </g:else>
</div>