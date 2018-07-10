<%@ page import="com.webcommander.util.AppUtil" %>
<div class="dashlet-latest-statistics">
    <div class="latest-activity-data">
        <g:if test="${latestActivities.size() > 0}">
            <g:include view="admin/reporting/latestActivity.gsp" model="${[latestActivities: latestActivities]}"/>
        </g:if>
        <g:else>
            <tr class="table-no-entry-row"><td colspan="5" ><g:message code="no.activity.found"/></td></tr>
        </g:else>
    </div>
</div>