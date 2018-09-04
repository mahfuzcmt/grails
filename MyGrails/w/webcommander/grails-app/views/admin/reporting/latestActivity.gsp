<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.util.AppUtil" %>
<g:set var="ecommerce" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce")}"/>
<g:each in="${latestActivities}" var="activity">
    <div class="report-block-item">
        <div class="image-container">
            <span class="${activity.type}-icon"></span>
        </div>
        <g:if test="${(activity.type == 'order') && (ecommerce == "false")}">
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
                    <g:if test="${ecommerce == "true"}">
                        <g:message code="customer.registration"/>
                    </g:if>
                    <g:else>
                        <g:message code="member.registration"/>
                    </g:else>
                    - <g:message code="x.joined" args="${[activity.fname + (activity.lname ? " " + activity.lname : "")]}"/>
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