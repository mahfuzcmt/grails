<%@ page import="com.webcommander.design.Layout" %>
<div class="header">
    <span class="layout-icon"></span>
    <span class="item-group entity-count title">
        <g:message code="layouts"/> (<span class="count">${count}</span>)
    </span>
    <div class="toolbar toolbar-right">
        <div class="tool-group">
           <label><g:message code="layout.type"/>:</label>
            <g:select name="layoutType" from="${[g.message(code: "none"), g.message(code: "disposable"), g.message(code:  "template")]}" keys="${["all", "disposable", "template"]}" class="small"/>
        </div>
        <div class="tool-group toolbar-btn create"><i></i><g:message code="create"/></div>
        <div class="tool-group">
            <span class="toolbar-item reload right" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>
<div class="app-tab-content-container">
    <div class="left-panel">
        <div class="body layout-list blocktype-list">
            <g:include view="admin/layout/leftPanel.gsp" model="[layoutList: layoutList, selected: selected, defaultLayout: defaultLayout]"></g:include>
        </div>
    </div>
    <div class="right-panel">
        <div class="body">
            <g:if test="${selected}">
                <iframe class="layout-render-frame" src="${createLink(controller: "layout", action: "renderLayout", params: [id: selected, viewMode: true])}"></iframe>
            </g:if>
            <g:else>
                <iframe class="layout-render-frame"></iframe>
            </g:else>
        </div>
    </div>
</div>