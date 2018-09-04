<%@ page import="com.webcommander.constants.NamedConstants" %>
<div class="header">
    <span class="item-group entity-count title">
        <g:message code="payment.gateways"/> (<span class="count">${count}</span>)
    </span>
    <div class="toolbar toolbar-right">
        <div class="tool-group action-header" style="display: none">
            <g:select class="action-on-selection" name="action" from="${[ g.message(code: "with.selected"), g.message(code: "status")]}" keys="['', 'status']"/>
        </div>

        <div class="tool-group">
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>

<div class="app-tab-content-container payment-gateway-table">
    <table class="content">
        <colgroup>
            <col style="width: 3%">
            <col style="width: 150px">
            <col>
            <col style="width: 250px">
            <col style="width: 100px">
            <col style="width: 100px">
        </colgroup>
        <tr>
            <th class="select-column"><input class="check-all multiple" type="checkbox"></th>
            <th><g:message code="name"/></th>
            <th><g:message code="information"/></th>
            <th><g:message code="surcharge.type"/></th>
            <th><g:message code="status"/></th>
            <th class="actions-column"><g:message code="actions"/></th>
        </tr>
        <g:if test="${paymentGateways}">
            <g:each in="${paymentGateways}" var="pg">
                <tr class="${pg.isDefault ? 'default-payment-gateway highlighted' : ''}">
                    <td class="select-column"><input entity-id="${pg.id}" type="checkbox" class="multiple"></td>
                    <td><g:message code="${pg.name}"/></td>
                    <td>${pg.information.encodeAsBMHTML()}</td>
                    <td><g:message code="${NamedConstants.SURCHARGE_TYPE[pg.surchargeType]}"/></td>
                    <td class="status-column">
                        <span class="status ${pg.isEnabled ? 'positive' : 'negative'}" title="${pg.isEnabled ? g.message(code: 'active') : g.message(code: 'inactive')}"></span>
                    </td>
                    <td class="actions-column">
                        <g:if test="${pg.code != com.webcommander.constants.DomainConstants.PAYMENT_GATEWAY_CODE.API}">
                            <span class="action-navigator collapsed" entity-id="${pg.id}" entity-name="${pg.name.encodeAsBMHTML()}"></span>
                        </g:if>
                    </td>
                </tr>
            </g:each>
        </g:if>
        <g:else>
            <tr class="table-no-entry-row">
                <td colspan="5"><g:message code="no.payment.gateway"/></td>
            </tr>
        </g:else>
    </table>
<g:if test="${params.wizard}">
    <div class="footer">
        <ui:perPageCountSelector/>
        <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
    </div>
</g:if>
</div>
<g:if test="${!params.wizard}">
    <div class="footer">
        <ui:perPageCountSelector/>
        <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
    </div>
</g:if>

