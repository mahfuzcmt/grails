<%@ page import="com.webcommander.plugin.embedded_page.EmbeddedPage;" %>
<div class="fee-widget-config-panel">
    <g:form controller="frontEndEditor" action="saveTabAccordionWidget" class="config-form">
        <input type="hidden" name="widgetType" value="${widget.widgetType}">
        <div class="fee-config-body fee-noPadding">
            <div class="fee-header-top fee-padding-10">
                <div class="fee-row">
                    <div class="fee-col fee-col-33 fee-form-row fee-padding-5">
                        <label for="widgetTitle"><g:message code="widget.title"/></label>
                        <input type="text" name="title" id="widgetTitle" maxlength="255" value="${widget.title}">
                    </div>
                    <div class="fee-col fee-col-33 fee-form-row fee-padding-5">
                        <label for="widgetType"><g:message code="type"/></label>
                        <g:select class="sidebar-input" name="type" id="widgetType" from="${[g.message(code: "tab"), g.message(code: "accordion")]}" keys="${['tab', 'accordion']}" toggle-target="transition" value="${widgetParams.type ?: params.config['type']}"/>
                    </div>
                    <div class="fee-col fee-col-33 fee-form-row fee-padding-5">
                        <label for="widgetAxis"><g:message code="axis"/></label>
                        <div>
                            <span class="fee-auto-width"><input type="radio" id="widgetHrAxis" ${widgetParams.axis == 'h' || !widgetParams.axis ? 'checked' : ''} name="axis" value="h"><label class="fee-auto-width"><g:message code="horizontal"/></label></span>
                            <span class="fee-auto-width"><input type="radio" name="axis" ${widgetParams.axis == 'v' ? 'checked' : ''} id="widgetAxis" value="v"><label class="fee-auto-width"><g:message code="vertical"/></label></span>
                        </div>
                    </div>
                </div>
            </div>
            <div class="fee-scroll-item-wrapper fee-padding-10">
                <div class="fee-body">
                    <table class="fee-table fee-tab-add-table">
                        <colgroup>
                            <col style="width: 40%">
                            <col style="width: 40%">
                            <col style="width: 20%">
                        </colgroup>
                        <tr class="fee-ignore">
                            <th><g:message code="${config.type}.name"/></th>
                            <th><g:message code="embedded.page"/></th>
                            <th class="actions-column"><g:message code="actions"/></th>
                        </tr>
                        <g:each in="${widget.widgetContent}" var="wiContent" status="i">
                            <tr class="fee-item">
                                <td class="fee-editable">
                                    <span class="fee-text">${wiContent.extraProperties.encodeAsBMHTML()}</span>
                                    <input type="text" class="fee-hidden" name="contentName" validation="required maxlength[100]" value="${wiContent.extraProperties.encodeAsBMHTML()}">
                                    <span class="tool-icon edit fee-edit-icon"></span>
                                </td>
                                <td class="fee-editable fee-noOverflow">
                                    <ui:domainSelect name="contentId" domain="${EmbeddedPage}" class="content-id" value="${wiContent.contentId}"/>
                                </td>
                                <td class="actions-column">
                                    <span class="tool-icon remove fee-remove"></span>
                                </td>
                            </tr>
                        </g:each>
                    </table>
                </div>
            </div>
        </div>
        <div class="fee-button-wrapper fee-config-footer">
            <button class="fee-save" type="submit"><g:message code="save"/></button>
            <button class="fee-cancel fee-common" type="button"><g:message code="cancel"/></button>
        </div>
    </g:form>
    <table class="fee-hidden fee-add-new-entry">
        <tr class="fee-item">
            <td class="fee-editable">
                <input type="text" class="content-name" maxlength="100" validation="required maxlength[100]">
                <span class="tool-icon edit fee-edit-icon fee-hidden"></span>
            </td>
            <td class="fee-editable fee-noOverflow">
                <ui:domainSelect name="" class="content-id" validation="required" domain="${EmbeddedPage}"/>
            </td>
            <td class="actions-column">
                <button class="submit-button fee-add-content-btn" type="button"><g:message code="add"/></button>
                <span class="tool-icon remove fee-remove fee-hidden"></span>
            </td>
        </tr>
    </table>
</div>