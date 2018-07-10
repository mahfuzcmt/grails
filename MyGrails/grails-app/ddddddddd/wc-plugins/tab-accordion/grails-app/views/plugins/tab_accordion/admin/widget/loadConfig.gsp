<%@ page import="com.webcommander.widget.Widget; com.webcommander.plugin.embedded_page.EmbeddedPage" %>
<g:form class="create-edit-form" method="post" controller="widget" action="saveTabAccordionWidget">
    <span class="configure-btn" title="<g:message code="configuration"/>"><i></i></span>
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="tab.accordion.selection.info"/></h3>
            <div class="info-content"><g:message code="section.text.tab.accordion.selection"/></div>
        </div>
        <div class="form-section-container">
            <div class="widget-config-panel">
                <div>
                    <div class="form-row">
                        <label><g:message code="title"/></label>
                        <input type="text" class="medium" name="title" value="${widget.title}">
                    </div>
                </div>
            </div>

            <div class="div-table basic-filters">
                <input type="hidden" name="type" value="${config.type}">
                <input type="hidden" name="axis" value="${config.axis}">
                <div class="div-table-header">
                    <div class="div-table-row  col-3">
                        <div class="div-table-cell-heading"><g:message code="${config.type}.name"/></div>
                        <div class="div-table-cell-heading"><g:message code="embedded.page"/></div>
                        <div class="div-table-cell-heading action-column"><g:message code="actions"/></div>
                    </div>
                </div>
                <g:set var="widget" value="${widget as Widget}"/>
                <div class="div-table-body">
                    <g:each in="${widget.widgetContent}" var="wiContent" status="i">
                        <div class="div-table-row col-3">
                            <div class="div-table-cell tab-name editable" validation="maxlength[100]">

                                <span class="value">${wiContent.extraProperties.encodeAsBMHTML()}</span>
                                <input type="hidden" class="name" name="contentName" value="${wiContent.extraProperties.encodeAsBMHTML()}">
                            </div>
                            <div class="div-table-cell content-cell editable">

                                <ui:domainSelect name="contentId" domain="${EmbeddedPage}" class="content-id" value="${wiContent.contentId}"/>
                            </div>
                            <div class="div-table-cell">

                                <span class="tool-icon remove"></span>
                            </div>
                        </div>
                    </g:each>

                    <div class="div-table-row  col-3 add-new-entry">
                        <div class="div-table-cell">

                            <input type="text" class="content-name" maxlength="100" validation="maxlength[100]">
                        </div>
                        <div class="div-table-cell content-cell">

                            <ui:domainSelect name="${""}" class="content-id" domain="${EmbeddedPage}"/>
                            <input type="hidden" class="content-id">
                        </div>
                        <div class="div-table-cell">
                            <button class="submit-button add-content-btn" type="button"><g:message code="add"/></button>
                        </div>
                    </div>
                </div>
            </div>
            <div class="form-row btn-row">
                <button type="submit" class="submit-button edit-popup-form-submit"><g:message code="update"/></button>
                <button type="button" class="cancel-button"><g:message code="cancel"/></button>
            </div>
        </div>
    </div>
</g:form>