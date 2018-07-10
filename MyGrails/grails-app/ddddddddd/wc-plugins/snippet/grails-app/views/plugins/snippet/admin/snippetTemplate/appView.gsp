<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.content.Section" %>
<div class="header">
    <span class="item-group entity-count title">
        <g:message code="snippet.templates"/> (<span class="count">${count}</span>)
    </span>
    <div class="toolbar toolbar-right">
        <div class="tool-group toolbar-btn create"><i></i><g:message code="create"/></div>
        <div class="tool-group">
            <span class="toolbar-item switch-menu collapsed"><i></i></span>
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>
<div class="app-tab-content-container">
    <table class="content">
        <colgroup>
            <col class="name-column">
            <col class="image-column">
            <col class="actions-column">
        </colgroup>
        <tr>
            <th><g:message code="snippet.name"/></th>
            <th><g:message code="thumbnail.image"/></th>
            <th class="actions-column"><g:message code="actions"/></th>
        </tr>
        <g:if test="${templates}">
            <g:each in="${templates}" var="template">
                <tr>
                   <td class="name-column">${template.name}</td>
                    <td><img class="item-logo" src="${template.thumb}"></td>
                    <td class="actions-column">
                        <span class="action-navigator collapsed" entity-uuid="${template.uuid}" entity-name="${template.name.encodeAsBMHTML()}" entity-type="snippet-template"></span>
                    </td>
                </tr>
            </g:each>
        </g:if>
        <g:else>
            <tr class="table-no-entry-row">
                <td colspan="3"><g:message code="no.snippet.template.uploaded"/> </td>
            </tr>
        </g:else>
    </table>
</div>
<div class="footer">
    <ui:perPageCountSelector/>
    <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
</div>