<%@ page import="com.webcommander.util.AppUtil; com.webcommander.constants.DomainConstants; com.webcommander.constants.NamedConstants" %>
<div class="header">
    <span class="active-template"></span>
    <div class="title"><g:message code="installed.template"/></div>
    <div class="toolbar toolbar-right">
        <div class="tool-group">
            <label><g:message code="category" /></label>
            <ui:namedSelect name="template_category" prepend="${["": "all"]}"   key="${typesAndCategories.categories}" optionKey="id" optionLabel="displayName" class="medium filter"/>
        </div>
        <div class="tool-group">
            <label><g:message code="type" /></label>
            <ui:namedSelect name="template_type" prepend="${["": "all"]}" key="${typesAndCategories.types}" optionKey="name" optionLabel="displayName" class="small filter"/>
        </div>
    </div>
</div>
<div class="app-tab-content-container">
    <g:include controller="templateAdmin" action="leftPanel"/>
   <div class="right-panel">
        <g:include view="admin/template/thumbView.gsp" model="[templateList: templates.templateList, totalCount: templates.totalCount, offset: offset, max: max]"></g:include>
    </div>
</div>