<%@ page import="com.webcommander.util.StringUtil" %>
<div class="tab-accordion-tab bmui-tab ${config.axis == 'v' ? 'left-side-header': ''}">
    <g:set var="tabId" value="${StringUtil.uuid}"/>
    <div class="bmui-tab-header-container">
        <g:each in="${widget.widgetContent}" var="content" status="i">
            <div class="bmui-tab-header" data-tabify-tab-id="${tabId + "-" + i}">
                <span class="title">${content.extraProperties?.encodeAsBMHTML()}</span>
            </div>
        </g:each>
    </div>
    <div class="bmui-tab-body-container">
        <g:each in="${widget.widgetContent}" var="content" status="i">
            <div id="bmui-tab-${tabId + "-" + i}" content-id="${content.contentId}">
                <render:renderPageContent value="${pages[i].body}"/>
            </div>
        </g:each>
    </div>
</div>