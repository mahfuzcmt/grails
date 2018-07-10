<%@ page import="com.webcommander.admin.Customer" %>
<div class="toolbar-share">
    <div class="toolbar toolbar-right">
        <div class="tool-group">
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>
<div class="table-view">
    <div class="body">
        <table class="content">
            <colgroup>
                <col>
                <col>
            </colgroup>
            <tr>
                <th><g:message code="ip.address"/></th>
                <th><g:message code="name"/></th>
            </tr>
            <g:each in="${visitors}" var="visitor" status="i">
                <tr>
                    <td>${visitor.ip_address}</td>
                    <g:set var="customer" value="${visitor.customer ? Customer.read(visitor.customer) : null}"/>
                    <td>${customer ? (customer.firstName + (customer.lastName ?: "")).encodeAsBMHTML() : "N/A"}</td>
                </tr>
            </g:each>
        </table>
    </div>
    <div class="footer">
        <ui:perPageCountSelector/>
        <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
    </div>
</div>
