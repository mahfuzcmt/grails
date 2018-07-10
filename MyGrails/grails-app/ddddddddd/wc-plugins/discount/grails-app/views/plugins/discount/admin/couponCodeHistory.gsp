<%@ page import="com.webcommander.constants.NamedConstants" %>
<div class="header">
    <span class="item-group entity-count title">
        ${discount.name} - <g:message code="coupon.codes"/> (<span class="count">${count}</span>)
    </span>
    <div class="toolbar toolbar-right">
        <form class="search-form tool-group">
            <input type="text" class="search-text" placeholder="<g:message code="search"/>"><button type="submit" class="icon-search"></button>
        </form>
        <div class="tool-group">
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>
<div class="app-tab-content-container">
    <table class="content order-app-view">
        <colgroup>
            <col style="width: 20%">
            <col style="width: 20%">
            <col style="width: 20%">
            <col style="width: 20%">
            <col style="width: 20%">
            <col style="width: 20%">
        </colgroup>
        <tr>
            <th><g:message code="code"/></th>
            <th><g:message code="creation.date"/></th>
            <th><g:message code="created.by"/></th>
            <th><g:message code="link.to.usage.counter.each.customer"/></th>
            <th class="status-column"><g:message code="status"/></th>
            <th class="actions-column"><g:message code="actions"/></th>
        </tr>
        <g:if test="${codeList}">
            <g:each in="${codeList}" var="code">
                <tr>
                    <td>${code.code}</td>
                    <td>${coupon.created.toAdminFormat(true, false, session.timezone)}</td>
                    <td>${coupon.createdBy.fullName.encodeAsBMHTML()}</td>
                    <td>${code.usageCounter}</td>
                    <td class="status-column order-status">
                        <span class="status ${code.isActive ? 'positive' : 'negative'}" title="${g.message(code: 'code.status' )}"></span>
                    </td>
                    <td class="actions-column">
                        <span class="action-navigator collapsed " entity-id="${code.id}"></span>
                    </td>
                </tr>
            </g:each>
        </g:if>
        <g:else>
            <tr class="table-no-entry-row">
                <td colspan="2"><g:message code="no.coupon.codes"/></td>
            </tr>
        </g:else>
    </table>
</div>
<div class="footer">
    <ui:perPageCountSelector/>
    <paginator total="${count}" max="${params.max}" offset="${params.offset}"></paginator>
</div>