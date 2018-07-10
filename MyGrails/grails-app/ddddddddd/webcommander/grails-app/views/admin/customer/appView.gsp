<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.util.AppUtil" %>
<div class="header">
    <span class="item-group entity-count title">
        <g:message code="${(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce") == 'true')?"customers":"members"}"/> (<span class="count">${count}</span>)
    </span>
    <div class="toolbar toolbar-right">
        <div class="tool-group action-header" style="display: none">
            <g:select class="action-on-selection" name="action" from="${[ g.message(code: "with.selected"), g.message(code: "remove"), g.message(code: "bulk.edit")]}" keys="['', 'remove', 'bulkEdit']"/>
        </div>
        <form class="search-form tool-group">
            <input type="text" class="search-text" placeholder="<g:message code="search"/>"><button type="submit" class="icon-search"></button>
        </form>
        <div class="tool-group">
            <span class="toolbar-item add-filter" title="<g:message code="advance.search"/>"><i></i></span>
            <span class="toolbar-item remove-filter disabled" title="<g:message code="remove.search"/>"><i></i></span>
        </div>
        <div class="tool-group action-tool action-menu collapsed">
            <span class="tool-text"><g:message code="actions"/></span><span class="action-dropper"></span>
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
            <col class="select-column"/>
            <col class="first-name-column"/>
            <col class="last-name-column"/>
            <col class="email-column">
            <col class="store-credit-column"/>
            <col class="status-column"/>
            <col class="customer-since-column"/>
            <col class="actions-column"/>
        </colgroup>
        <tr>
            <th class="select-column"><input class="check-all multiple" type="checkbox"></th>
            <th><g:message code="first.name"/></th>
            <th><g:message code="last.name.surname"/></th>
            <th><g:message code="email"/></th>
            <th><g:message code="store.credit"/></th>
            <th class="status-column"><g:message code="status"/></th>
            <th><g:message code="customer.since" args="${[(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce") == 'true')?"Customer":"Member"]}"/></th>
            <th class="actions-column"><g:message code="actions"/></th>
        </tr>
        <g:if test="${customers}">
            <g:each in="${customers}" var="customer">
                <tr>
                    <td class="select-column"><input entity-id="${customer.id}" type="checkbox" class="multiple"></td>
                    <td>${customer.firstName.encodeAsBMHTML()} &nbsp; ${customer.isCompany ? "<span class='mark-icon company'></span>" : ""}</td>
                    <td>${customer.lastName.encodeAsBMHTML()}</td>
                    <td>${customer.userName.encodeAsBMHTML()}</td>
                    <td>${customer.storeCredit.toAdminPrice()}</td>
                    <g:set var="status" value="${[A: "positive", I: "negative"]}"/>
                    <td class="status-column"><span class="status ${status[customer.status] ?: 'diplomatic'}" title="${customer.status == 'A' ? g.message(code: 'active') :  customer.status == 'I' ? g.message(code: 'inactive') : g.message(code: 'awaiting') }">
                        </span></td>
                    <td>${customer.created.toAdminFormat(true, false, session.timezone)}</td>
                    <td class="actions-column">
                        <span class="action-navigator collapsed" entity-id="${customer.id}" entity-name="${customer.firstName.encodeAsBMHTML()}"></span>
                    </td>
                </tr>
            </g:each>
        </g:if>
        <g:else>
            <tr class="table-no-entry-row">
                <td colspan="8"><g:message code="no.customer.created"/></td>
            </tr>
        </g:else>
    </table>
</div>
<div class="footer">
    <ui:perPageCountSelector/>
    <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
</div>