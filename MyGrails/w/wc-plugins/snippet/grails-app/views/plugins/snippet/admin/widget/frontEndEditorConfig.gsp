<%@ page import="com.webcommander.plugin.snippet.constants.SnippetConstants; com.webcommander.plugin.snippet.Snippet" %>
<div class="fee-widget-config-panel fee-snippet-wrapper">
    <g:form controller="frontEndEditor" action="saveWidget" class="config-form">
        <input type="hidden" id="existAutoSnippetId" value="${widget?.id ? contentId?.getAt(0) : ''}">
        <input type="hidden" name="widgetType" value="${widget.widgetType}">
        <div class="fee-pu-content-header">
            <div class="category-wrapper">
                <div class="fee-title">Show All</div>
                <div class="template-category-list">
                    <div class="category all" data-category="all">
                        <span class="icon all"></span>
                        <span class="label">Show All</span>
                    </div>
                    <g:each in="${SnippetConstants.SNIPPET_TEMPLATE_CATEGORY_NAMES}" var="template">
                        <div class="category ${template.key}" data-category="${template.key}">
                            <span class="icon ${template.key}"></span>
                            <span class="label ${template.key}"><g:message code="${template.value}"/></span>
                        </div>
                    </g:each>
                </div>
            </div>
        </div>
        <div class="fee-config-body fee-pu-content-body">
            <div class="fee-template-wrapper">
                <div class="fee-snippet-templates">
                    <g:each in="${templates}" var="template">
                        <div class="fee-snippet-template ${template.category}" uuid="${template.uuid}">
                            <img src="${template.thumb}"/>
                        </div>
                    </g:each>
                </div>
            </div>
        </div>
    </g:form>
</div>