<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.util.AppUtil" %>
<div class="header">
    <span class="item-group entity-count title">
        <g:message code="customer.groups" args="${[(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce") == 'true')?"Customer":"Member"]}"/> (<span class="count">${count}</span>)
    </span>
    <div class="toolbar toolbar-right">
        <div class="tool-group action-header" style="display: none">
            <g:select class="action-on-selection" name="action" from="${[ g.message(code: "with.selected"), g.message(code: "assign.customer", args: (AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce") == 'true')?"Customer":"Member"), g.message(code: "status"), g.message(code: "remove")]}" keys="['', 'assign_customer', 'status', 'remove']"/>
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
            <span class="toolbar-item switch-menu collapsed"><i></i></span>
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>
<div class="app-tab-content-container">
    <table class="content">
        <colgroup>
            <col style="width: 3%">
            <col style="width: 20%">
            <col style="width: 40%">
            <col style="width: 10%">
            <col style="width: 20%">
            <col style="width: 7%">
        </colgroup>
        <tr>
            <th class="select-column"><input class="check-all multiple" type="checkbox"></th>
            <th><g:message code="name"/></th>
            <th><g:message code="description"/></th>
            <th class="status-column"><g:message code="status"/></th>
            <th><g:message code="created"/></th>
            <th class="actions-column"><g:message code="actions"/></th>
        </tr>
        <g:if test="${customerGroups}">
            <g:each in="${customerGroups}" var="group">
                <tr>
                    <td class="select-column"><input entity-id="${group.id}" type="checkbox" class="multiple"></td>
                    <td>${group.name.encodeAsBMHTML()}</td>
                    <td>${group.description.encodeAsBMHTML()}</td>
                    <td class="status-column">
                        <g:if test="${group.status == 'A'}">
                            <span class="status positive" title="${g.message(code: 'active')}"></span>
                        </g:if>
                        <g:else>
                            <span class="status negative" title="${g.message(code: 'inactive')}"></span>
                        </g:else>
                    </td>
                    <td>${group.created.toAdminFormat(true, false, session.timezone)}</td>
                    <td class="actions-column">
                        <span class="action-navigator collapsed" entity-id="${group.id}" entity-name="${group.name.encodeAsBMHTML()}"></span>
                    </td>
                </tr>
            </g:each>
        </g:if>
        <g:else>
            <tr class="table-no-entry-row">
                <td colspan="6"><g:message code="no.customer.group.created"/></td>
            </tr>
        </g:else>
    </table>
</div>
<div class="footer">
    <ui:perPageCountSelector/>
    <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
</div>