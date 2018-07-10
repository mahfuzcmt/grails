<%@ page import="com.webcommander.webcommerce.Product" %>
<div class="shipment-history">
    <g:if test="${historyList && historyList.size() > 0}">
        <div class="label-selector">
            <g:select name="changeLabel" from="${labelMap}" optionKey="key" optionValue="value"/>
        </div>

        <div class="change-note">
            <span class="title"><g:message code="change.note"/></span>
            <div class="note">
                ${historyList.first().changeNote}
            </div>
        </div>
        <g:include view="admin/order/historyTable.gsp"/>
    </g:if>
    <g:else>
        <span class="no-history">
            <g:message code="no.history.found"/>
        </span>
    </g:else>
</div>