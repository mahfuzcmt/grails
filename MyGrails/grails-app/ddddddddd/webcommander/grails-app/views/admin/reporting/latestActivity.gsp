<%@ page import="com.webcommander.util.AppUtil" %>
<g:each in="${latestActivities}" var="activity">
    <div class="report-block-item">
        <div class="image-container">
            <span class="${activity.type}-icon"></span>
        </div>
        <g:if test="${activity.type == 'order'}">
            <div class="title-n-summary">
                <div class="title">
                    <g:message code="n.items.for.n" args="${[activity.count, AppUtil.baseCurrency.symbol + activity.amount.toAdminPrice()]}"/>
                </div>
                <div class="timestamp">
                    <reporting:timestamp time="${activity.time}" state="${activity.state}" country="${activity.country}"/>
                </div>
            </div>
        </g:if>
        <g:elseif test="${activity.type == 'customer'}">
            <div class="title-n-summary">
                <div class="title">
                    <g:message code="customer.registration"/> - <g:message code="x.joined" args="${[activity.fname + (activity.lname ? " " + activity.lname : "")]}"/>
                </div>
                <div class="timestamp">
                    <reporting:timestamp time="${activity.time}" state="${activity.state}" country="${activity.country}"/>
                </div>
            </div>
        </g:elseif>
        <g:elseif test="${activity.type == 'subscriber'}">
            <div class="title-n-summary">
                <div class="title">
                    <g:message code="newsletter"/> - <g:message code="x.subscribed.for.newsletter" args="${[activity.fname ? activity.fname + (activity.lname ? " " + activity.lname : "") : activity.email]}"/>
                </div>
                <div class="timestamp">
                    <reporting:timestamp time="${activity.time}" state="${activity.state}" country="${activity.country}"/>
                </div>
            </div>
        </g:elseif>
    </div>
</g:each>