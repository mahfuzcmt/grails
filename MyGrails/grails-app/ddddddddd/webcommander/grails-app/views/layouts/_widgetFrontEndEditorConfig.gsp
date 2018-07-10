<g:if test="${!params?.noLayout}">
    <div class="fee-widget-config-panel">
        <g:form controller="frontEndEditor" action="saveWidget" class="config-form">
            <input type="hidden" name="widgetType" value="${widget.widgetType}">
            <div class="fee-config-body">
                <g:layoutBody/>
            </div>
            <div class="fee-button-wrapper fee-config-footer">
                <button class="fee-save" type="submit"><g:message code="save"/></button>
                <button class="fee-cancel fee-common" type="button"><g:message code="cancel"/></button>
            </div>
        </g:form>
    </div>
</g:if>
<g:else>
    <g:layoutBody/>
</g:else>