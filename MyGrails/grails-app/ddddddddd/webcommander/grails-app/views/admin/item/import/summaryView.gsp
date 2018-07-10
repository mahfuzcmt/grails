<div>
    <h3>Import Summary</h3>
    <g:if test="${emptyTask}">
        <span class="message-block warning"><g:message code="import.status.log.cleared"/></span>
    </g:if>
    <g:else>
        <div class="bmui-tab">
            <div class="bmui-tab-header-container">
                <div class="bmui-tab-header" data-tabify-tab-id="category">
                    <span class="title"><g:message code="category"/></span>
                </div>
                <div class="bmui-tab-header" data-tabify-tab-id="product">
                    <span class="title"><g:message code="product"/></span>
                </div>
            </div>
            <div class="bmui-tab-body-container">
                <div id="bmui-tab-category" style="overflow-y: auto; max-height: 400px;">
                    <g:include view="/admin/item/import/summaryTable.gsp"
                               model="[logs: categoryImportLogs]"/>
                </div>
                <div id="bmui-tab-product" style="overflow-y: auto; max-height: 400px;">
                    <g:include view="/admin/item/import/summaryTable.gsp"
                               model="[logs: productImportLogs]"/>
                </div>
            </div>
        </div>
    </g:else>
</div>