<%@ page import="com.webcommander.plugin.embedded_page.EmbeddedPage" %>
<g:applyLayout name="_widgetShortConfig">
    <div class="sidebar-group">
        <div class="sidebar-group-label"><g:message code="height"/></div>
        <div class="sidebar-group-body">
            <input validation="digits" restrict="numeric" type="text" class="sidebar-input" name="height" value="${config.height}">
        </div>
    </div>
    <div class='sidebar-group visible-section-selector'>
        <span class="sidebar-group-label"><g:message code="section"/></span>
        <div class='sidebar-group-body'>
            <g:select class="sidebar-input" name="slidable_section_list" from="${EmbeddedPage.findAllByIsDisposable(false)}" optionKey="id" optionValue="name" value="${widget.widgetContent.contentId}" multiple="true"/>
        </div>
    </div>
</g:applyLayout>