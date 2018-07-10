<%@ page import="com.webcommander.admin.Operator; com.webcommander.manager.LicenseManager" %>
<div class="header">
    <input type="hidden" id="session-owner-id" value="${session.admin}"/>
    <span class="item-group entity-count title">
        <g:message code="operators"/> (<span class="count">${count}</span>)
    </span>
    <div class="toolbar toolbar-right">
        <div class="tool-group action-header" style="display: none">
            <g:select class="action-on-selection" name="action" from="${[g.message(code: "with.selected"), g.message(code: "remove"), g.message(code: "status"), g.message(code: "manage.permission"), g.message(code: "role.assign"), g.message(code: "api.access")]}"
                      keys="['', 'delete', 'status', 'manage_permission', 'role_assign', 'api_access']"/>
        </div>
        <form class="search-form tool-group">
            <input type="text" class="search-text" placeholder="<g:message code="search"/>"><button type="submit" class="icon-search"></button>
        </form>
        <div class="tool-group">
            <span class="toolbar-item add-filter" title="<g:message code="advance.search"/>"><i></i></span>
            <span class="toolbar-item remove-filter disabled" title="<g:message code="remove.search"/>"><i></i></span>
        </div>
        <div class="tool-group toolbar-btn create"><i></i><g:message code="create"/></div>
        <div class="tool-group">
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>
<div class="app-tab-content-container">
    <table class="content">
        <colgroup>
            <g:if test="${!LicenseManager.isProvisionActive()}">
                <col class="select-column">
            </g:if>
            <col class="name-column">
            <col class="email-column">
            <col class="created-column">
            <col class="updated-column">
            <col class="status-column">
            <g:if test="${LicenseManager.isProvisionActive()}">
                <col class="approved-column">
            </g:if>
            <col class="actions-column">
        </colgroup>
        <tr>
            <g:if test="${!LicenseManager.isProvisionActive()}">
                <th class="select-column"><input class="check-all multiple" type="checkbox"></th>
            </g:if>
            <th class="name-column"><g:message code="full.name"/></th>
            <th class="name-column"><g:message code="email"/></th>
            <th class="created-column"><g:message code="created"/></th>
            <th class="updated-column"><g:message code="updated"/></th>
            <th class="status-column"><g:message code="status"/></th>
            <g:if test="${LicenseManager.isProvisionActive()}">
                <th class="approved-column"><g:message code="is.approved"/></th>
            </g:if>
            <th class="actions-column"><g:message code="actions"/></th>
        </tr>
        <g:if test="${users}">
            <g:if test="${LicenseManager.isProvisionActive()}">
                <g:each in="${users}" var="entity">
                    <g:set var="user" value="${Operator.findByUuid(entity.uuid)}"/>
                    <tr>
                        <td>${user?.fullName?.encodeAsBMHTML()}</td>
                        <td>${user?.email?.encodeAsBMHTML() ?: entity.email}</td>
                        <td>${user?.created?.toAdminFormat(true, false, session.timezone)}</td>
                        <td>${user?.updated?.toAdminFormat(true, false, session.timezone)}</td>
                        <td class="status-column">
                            <span class="status ${user?.isActive ? 'positive' : 'negative'}" title="${user?.isActive ? g.message(code: 'active') : g.message(code: 'inactive')}"></span>
                        </td>
                        <td class="approved-column">
                            <span class="status ${entity.approved ? 'positive' : 'negative'}" title="${entity.approved ? g.message(code: 'approved') : g.message(code: 'pending')}"></span>
                        </td>
                        <td class="actions-column"><span class="action-navigator collapsed" entity-id="${user?.id}" entity-name="${user?.fullName?.encodeAsBMHTML()}"></span></td>
                    </tr>
                </g:each>
            </g:if>
            <g:else>
                <g:each in="${users}" var="user">
                    <tr>
                        <td class="select-column"><input type="checkbox" class="multiple" entity-id="${user.id}" entity-name="${user.email}" ></td>
                        <td>${user.fullName.encodeAsBMHTML()}</td>
                        <td>${user.email.encodeAsBMHTML()}</td>
                        <td>${user.created.toAdminFormat(true, false, session.timezone)}</td>
                        <td>${user.updated.toAdminFormat(true, false, session.timezone)}</td>
                        <td class="status-column">
                            <span class="status ${user.isActive ? 'positive' : 'negative'}" title="${user.isActive ? g.message(code: 'active') : g.message(code: 'inactive')}"></span>
                        </td>
                        <td class="actions-column"><span class="action-navigator collapsed" entity-id="${user.id}" entity-name="${user.fullName.encodeAsBMHTML()}"></span></td>
                    </tr>
                </g:each>
            </g:else>
        </g:if>
        <g:else>
            <tr class="table-no-entry-row">
                <td colspan="8"><g:message code="no.operator.created"/></td>
            </tr>
        </g:else>
    </table>
</div>
<div class="footer">
    <ui:perPageCountSelector/>
    <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
</div>