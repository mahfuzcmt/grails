<div>
    <h3><g:message code="${operation}.summary"/></h3>
    <g:if test="${emptyTask}">
        <span class="message-block warning"><g:message code="status.log.cleared"/></span>
    </g:if>
    <g:else>
        <div class="bmui-tab">
            <div class="bmui-tab-header-container">
                <g:each in="${task.loggers}" var="logger">
                    <div class="bmui-tab-header" data-tabify-tab-id="logger-${logger.key}">
                        <span class="title"><g:message code="${logger.key.dotCase()}"/></span>
                    </div>
                </g:each>
            </div>
            <div class="bmui-tab-body-container">
                <g:each in="${task.loggers}" var="logger">
                    <div id="bmui-tab-logger-${logger.key}" style="overflow-y: auto; max-height: 400px;">
                        <g:include view="/admin/customerExportImport/summaryTable.gsp" model="[logs: logger.value.getLogs(operation)]"/>
                    </div>
                </g:each>
            </div>
        </div>
    </g:else>
</div>