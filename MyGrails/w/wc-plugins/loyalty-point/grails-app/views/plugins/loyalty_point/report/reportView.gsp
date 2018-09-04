<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.util.AppUtil" %>
<div class="header">
    <span class="item-group entity-count title">
        <g:message code="loyalty.point.log"/> (<span class="count">${count}</span>)
    </span>
    <div class="toolbar toolbar-right">
        <a href="${app.relativeBaseUrl()}loyaltyPointAdmin/exportLog" title="<g:message code="download"/>">
            <span class="tool-group toolbar-btn export" disabled><g:message code="export"/></span>
        </a>
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
            <col class="customer-name-column">
            <col class="points-column">
            <col class="sources-column">
            <col class="date-column">
        </colgroup>
        <tr>
            <th><g:message code="customer.name" args="${[(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce") == 'true')?"Customer":"Member"]}"/></th>
            <th><g:message code="points"/></th>
            <th><g:message code="sources"/></th>
            <th><g:message code="date"/></th>
        </tr>
        <g:if test="${pointHistories}">
            <g:each in="${pointHistories}" var="history">
                <tr>
                    <td>${history.customer.fullName()}</td>
                    <td>${history.pointCredited}</td>
                    <td><g:message code="${history.type}"/></td>
                    <td>${history.created}</td>
                </tr>
            </g:each>
        </g:if>
    </table>
</div>

<div class="footer">
    <ui:perPageCountSelector/>
    <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
</div>