<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.util.AppUtil" %>
<div class="dashlet-latest-statistics">
    <div class="customer-data-table">
        <table class="content">
            <colgroup>
                <col style="width: 25%">
                <col style="width: 10%">
                <col style="width: 35%">
                <col style="width: 30%">
            </colgroup>
            <tr>
                <th><g:message code="name"/></th>
                <th><g:message code="status"/></th>
                <th><g:message code="join.date"/></th>
                <th><g:message code="email"/></th>
            </tr>
            <g:if test="${components.size() > 0}">
                <g:each in="${components}" var="customer">
                    <tr>
                        <td>${customer.firstName.encodeAsBMHTML()} ${customer.lastName.encodeAsBMHTML()}</td>
                        <g:set var="tooltipNotification" value="${[A: 'ACTIVE', I: 'INACTIVE']}"/>
                        <td><span title="${tooltipNotification[customer.status]}" class="status ${customer.status == DomainConstants.CUSTOMER_STATUS.ACTIVE ? DomainConstants.STATUS.POSITIVE : (DomainConstants.CUSTOMER_STATUS.INACTIVE ? DomainConstants.STATUS.NEGATIVE : DomainConstants.STATUS.DIPLOMATIC)}"></span></td>
                        <td>${customer.created.toAdminFormat(true, false, session.timezone)}</td>
                        <td>${customer.address.email}</td>
                    </tr>
                </g:each>
            </g:if>
            <g:else>
                <tr class="table-no-entry-row"><td colspan="4"><g:message code="no.customer.found" args="${[(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce") == 'true')?"Customer":"Member"]}"/></td></tr>
            </g:else>
        </table>
    </div>
</div>