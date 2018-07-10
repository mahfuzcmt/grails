<%@ page import="com.webcommander.admin.Country" %>
<div class="header">
    <span class="item-group entity-count title">
        <g:message code="documents"/> (<span class="count">${count}</span>)
    </span>
    <div class="toolbar toolbar-right">
        <div class="tool-group action-header" style="display: none">
            <g:select class="action-on-selection" name="action" from="${[ g.message(code: "with.selected"), g.message(code: "remove")]}" keys="['', 'remove']"/>
        </div>
        <form class="search-form tool-group">
            <input type="text" class="search-text" placeholder="<g:message code="search"/>"><button type="submit" class="icon-search"></button>
        </form>
        <div class="tool-group toolbar-btn create"><i></i><g:message code="create"/></div>
        <div class="tool-group">
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>

<div class="app-tab-content-container">
    <table class="content document-list">
        <colgroup>
            <col class="select-column">
            <col class="name-column">
            <col class="type-column">
            <col class="created-column">
            <col class="status-column">
            <col class="actions-column">
        </colgroup>
        <tr>
            <th class="select-column"><input class="check-all multiple" type="checkbox"></th>
            <th><g:message code="name"/></th>
            <th><g:message code="type"/></th>
            <th><g:message code="created"/></th>
            <th><g:message code="status"/></th>
            <th class="actions-column"><g:message code="actions"/></th>
        </tr>
        <g:if test="${documents}">
            <g:each in="${documents}" var="document">
                <tr class="${document.isLayout ? "default" : ""}">
                    <td class="select-column"><input entity-id="${document.id}" type="checkbox" class="multiple"></td>
                    <td>
                        ${document.name.encodeAsBMHTML()}
                    </td>
                    <td><g:message code="${document.type}"/></td>
                    <td>${document.created.toAdminFormat(true, false, session.timezone)}</td>
                    <td class="status-column">
                        <span class="status ${document.active ? 'positive' : 'positive-minus'}" title="${document.active ? g.message(code: 'active') : g.message(code: 'inactive')}"></span>
                    </td>
                    <td class="actions-column">
                        <span class="action-navigator collapsed" entity-id="${document.id}" entity-name="${document.name.encodeAsBMHTML()}"></span>
                    </td>
                </tr>
            </g:each>
        </g:if>
        <g:else>
            <tr class="table-no-entry-row">
                <td colspan="9"><g:message code="no.document.created"/></td>
            </tr>
        </g:else>
    </table>
</div>

<div class="footer">
    <ui:perPageCountSelector/>
    <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
</div>