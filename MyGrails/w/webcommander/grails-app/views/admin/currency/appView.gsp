<%@ page import="com.webcommander.admin.Country" %>
<div class="header">
    <span class="item-group entity-count title">
        <g:message code="currencies"/> (<span class="count">${count}</span>)
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
    <table class="content">
        <colgroup>
            <col class="select-column">
            <col class="name-column">
            <col class="code-column">
            <col class="symbol-column">
            <col class="conversion-type-column">
            <col class="conversion-rate-column">
            <col class="update-column">
            <col class="status-column">
            <col class="actions-column">
        </colgroup>
        <tr>
            <th class="select-column"><input class="check-all multiple" type="checkbox"></th>
            <th><g:message code="name"/></th>
            <th><g:message code="code"/></th>
            <th colspan="4"><g:message code="symbol"/></th>

            %{--<th><g:message code="conversion.type"/></th>
            <th><g:message code="conversion.rate"/></th>
            <th><g:message code="updated"/></th>--}%

            <th class="status-column"><g:message code="status"/></th>
            <th class="actions-column"><g:message code="actions"/></th>

        </tr>
        <g:if test="${currencies}">
            <g:each in="${currencies}" var="currency">
                <tr class="${currency.base ? "base-currency highlighted" : "" + (currency.active ? "" : "inactive")}">
                    <td class="select-column"><input entity-id="${currency.id}" type="checkbox" class="multiple"></td>
                    <td>
                        <g:if test="${currency.country}">
                            <g:set var="code" value="${currency.country.code.toLowerCase()}"/>
                            <span class="flag-icon ${code}"></span>
                        </g:if>
                        ${currency.name.encodeAsBMHTML()}
                    </td>
                    <td>${currency.code.encodeAsBMHTML()}</td>
                    <td colspan="4">${currency.symbol.encodeAsBMHTML()}</td>

                    %{--<td><g:message code="${currency.manualConversion ? "manual.conversion" : "automatic.conversion" }"/></td>
                    <td>${currency.conversionRate}</td>
                    <td>${currency.rateUpdated.toAdminFormat(false, false, session.timezone)}</td>--}%

                    <td class="status-column">
                        <span class="status ${currency.active ? 'positive' : 'negative'}" title="${currency.active ? g.message(code: 'active') : g.message(code: 'inactive')}"></span>
                    </td>
                    <td class="actions-column">
                        <span class="action-navigator collapsed" entity-id="${currency.id}" entity-name="${currency.name.encodeAsBMHTML()}"></span>
                    </td>
                </tr>
            </g:each>
        </g:if>
        <g:else>
            <tr class="table-no-entry-row">
                <td colspan="9"><g:message code="no.currency.created"/></td>
            </tr>
        </g:else>
    </table>
</div>

<div class="footer">
    <ui:perPageCountSelector/>
    <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
</div>