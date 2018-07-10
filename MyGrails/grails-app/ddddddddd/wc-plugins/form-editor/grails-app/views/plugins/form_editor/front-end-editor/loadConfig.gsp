<g:applyLayout name="_widgetFrontEndEditorConfig">
    <div class="fee-row fee-configure-panel fee-padding-10">
        <div class="fee-col fee-col-50 form-row fee-padding-5">
            <label for="widgetTitle"><g:message code="widget.title"/></label>
            <input type="text" name="title" id="widgetTitle" maxlength="255" value="${widget.title}">
        </div>
        <div class="fee-col fee-col-50 form-row fee-padding-5">
            <label for="widgetForm"><g:message code="form"/></label>
            <g:select name="form" id="widgetForm" class="sidebar-input" from="${forms}" optionKey="id" optionValue="name" value="${form}"/>
        </div>
    </div>
</g:applyLayout>