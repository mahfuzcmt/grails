<%@ page import="com.webcommander.constants.DomainConstants"%>
<div class="bmui-tab create-edit-panel">
    <div class="bmui-tab-header-container top-side-header left">
        <div class="bmui-tab-header" data-tabify-tab-id="overview" data-tabify-url="${app.relativeBaseUrl()}myAccount/loadCustomProjectProperties?id=${projectId}&property=overview">
            <span class="title"><g:message code="overview"/></span>
        </div>
        <div class="bmui-tab-header" data-tabify-tab-id="projectDetails" data-tabify-url="${app.relativeBaseUrl()}myAccount/loadCustomProjectProperties?id=${projectId}&property=projectDetails">
            <span class="title"><g:message code="project.details"/></span>
        </div>
        <div class="bmui-tab-header" data-tabify-tab-id="projectFiles" data-tabify-url="${app.relativeBaseUrl()}myAccount/loadCustomProjectProperties?id=${projectId}&property=projectFiles">
            <span class="title"><g:message code="files"/></span>
        </div>
        <div class="bmui-tab-header" data-tabify-tab-id="sitemap" data-tabify-url="${app.relativeBaseUrl()}myAccount/loadCustomProjectProperties?id=${projectId}&property=sitemap">
            <span class="title"><g:message code="sitemap"/></span>
        </div>
        <div class="bmui-tab-header" data-tabify-tab-id="projectMessages" data-tabify-url="${app.relativeBaseUrl()}myAccount/loadCustomProjectProperties?id=${projectId}&property=projectMessages">
            <span class="title"><g:message code="messages"/></span>
        </div>
    </div>
    <div class="bmui-tab-body-container product-editor-body">
        <div id="bmui-tab-overview">
        </div>
        <div id="bmui-tab-projectDetails">
        </div>
        <div id="bmui-tab-sitemap">
        </div>
        <div id="bmui-tab-projectFiles">
        </div>
        <div id="bmui-tab-projectMessages">
        </div>
    </div>
</div>