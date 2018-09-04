<%@ page import="com.webcommander.util.AppUtil; com.webcommander.constants.DomainConstants" %>
<div class="header">
    <span class="item-group entity-count title">
        <g:message code="items.in.trash"/> (<span class="count">${count}</span>)
    </span>
    <div class="toolbar toolbar-right">
        <g:set var="ecommerce" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce").toBoolean()}"/>

        <div class="tool-group action-header" style="display: none">
            <g:select class="action-on-selection" name="action" from="${[g.message(code: "with.selected"), g.message(code: "restore"), g.message(code: "remove")]}" keys="['', 'restore', 'delete']"/>
        </div>
        <div class="tool-group chosen-wrapper">
            <g:select class="selected-domain small" name="domain" from="${domains.collect { it = (domainNames[it] ? domainNames[it] : ecommerce? it : it.replace("Customer", "Member")) }}" keys="${domains}" noSelection="['': 'All']"/>
        </div>
        <form class="search-form tool-group">
            <input type="text" class="search-text" placeholder="<g:message code="search"/>"><button type="submit" class="icon-search"></button>
        </form>
        <div class="tool-group">
            <span class="toolbar-item add-filter" title="<g:message code="advance.search"/>"><i></i></span>
            <span class="toolbar-item remove-filter disabled" title="<g:message code="remove.search"/>"><i></i></span>
        </div>
        <div class="tool-group">
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>
<div class="app-tab-content-container">
    <table class="content">
        <colgroup>
            <col style="width: 3%">
            <col style="width: 10%">
            <col style="width: 58%">
            <col style="width: 22%">
            <col style="width: 7%">
        </colgroup>
        <tr>
            <th class="select-column"><input class="check-all multiple" type="checkbox"></th>
            <th><g:message code="entity.type"/></th>
            <th><g:message code="name"/></th>
            <th><g:message code="deleted"/></th>
            <th class="actions-column"><g:message code="actions"/></th>
        </tr>
        <g:if test="${trashMap}">
            <g:each in="${trashMap}" var ="trash">
                <g:each in="${trash.value}" var="entity">
                    <tr>
                        <td class="select-column"><input type="checkbox" class="multiple" entity-id="${entity.id}" entity-type="${trash.key}" name="${trashMap.key}"></td>
                        <td>${trash.key}</td>
                        <td>${entity.name.encodeAsBMHTML()}</td>
                        <td>${entity.updated.toAdminFormat(true, false, session.timezone)}</td>
                        <td class="actions-column">
                            <span class="action-navigator collapsed" entity-id="${entity.id}" entity-type="${trash.key}" entity-name="${entity.name.encodeAsBMHTML()}"></span>
                        </td>
                    </tr>
                </g:each>
            </g:each>
        </g:if>
        <g:else>
            <tr class="table-no-entry-row">
                <td colspan="5"><g:message code="no.content.trash"/></td>
            </tr>
        </g:else>
    </table>
</div>
<div class="footer">
    <ui:perPageCountSelector/>
    <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
</div>