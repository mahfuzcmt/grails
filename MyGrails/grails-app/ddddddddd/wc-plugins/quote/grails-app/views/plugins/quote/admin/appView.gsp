<%--
  Created by IntelliJ IDEA.
  User: sajedur
  Date: 6/15/2015
  Time: 11:06 AM
--%>
<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.util.AppUtil" %>
<div class="header">
    <span class="item-group entity-count title">
        <g:message code="quotes"/> (<span class="count">${count}</span>)
    </span>
    <div class="toolbar toolbar-right">
        <div class="tool-group action-header" style="display: none">
            <g:select class="action-on-selection" name="action" from="${[ g.message(code: "with.selected"), g.message(code: "remove")]}" keys="['', 'remove']"/>
        </div>
        <form class="search-form tool-group">
            <input type="text" class="search-text" placeholder="<g:message code="search"/>"><button type="submit" class="icon-search"></button>
        </form>
        <div class="tool-group">
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>

<div class="app-tab-content-container">
    <table class="content">
        <colgroup>
            <col class="id-column">
            <col class="name-column">
            <col class="date-column">
            <col class="total-column">
            <col class="actions-column">
        </colgroup>
        <tr>
            <th><g:message code="quote.id"/></th>
            <th><g:message code="customer.name" args="${[(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce") == 'true')?"Customer":"Member"]}"/></th>
            <th><g:message code="quote.date"/></th>
            <th><g:message code="quote.total"/></th>
            <th class="actions-column"><g:message code="actions"/></th>
        </tr>
        <g:if test="${quotes}">
            <g:each in="${quotes}" var="quote">
                <tr>
                    <td>${quote.id}</td>
                    <td>${quote.customer.fullName().encodeAsBMHTML()}</td>
                    <td>${quote.created.toAdminFormat(true, false, session.timezone)}</td>
                    <td>${quote.grandTotal.toPrice()}</td>
                    <td class="actions-column">
                        <span class="action-navigator collapsed" entity-id="${quote.id}"></span>
                    </td>
                </tr>
            </g:each>
        </g:if>
        <g:else>
            <tr class="table-no-entry-row">
                <td colspan="7"><g:message code="quote.not.found"/></td>
            </tr>
        </g:else>
    </table>
</div>

<div class="footer">
    <ui:perPageCountSelector/>
    <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
</div>