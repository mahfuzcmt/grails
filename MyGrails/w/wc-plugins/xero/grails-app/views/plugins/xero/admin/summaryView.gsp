<div>
    <h3><g:message code="operation.summary"/></h3>
    <g:if test="${emptyTask}">
        <span class="message-block warning"><g:message code="status.log.cleared"/></span>
    </g:if>
    <g:else>
        <div class="bmui-tab">
            <div class="bmui-tab-header-container">
                <g:each in="${logs}" var="log">
                    <div class="bmui-tab-header" data-tabify-tab-id="${log.key}">
                        <span class="title"><g:message code="${log.key}"/></span>
                    </div>
                </g:each>
            </div>

            <div class="bmui-tab-body-container">
                <g:each in="${logs}" var="log">
                    <div id="bmui-tab-${log.key}" style="overflow-y: auto; max-height: 400px;">
                        <g:include view="/admin/item/import/summaryTable.gsp"
                                   model="[logs: log.value]"/>
                    </div>
                </g:each>
            </div>
        </div>
    </g:else>
</div>