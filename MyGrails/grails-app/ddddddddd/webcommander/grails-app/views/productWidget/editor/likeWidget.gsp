<%@ page import="com.webcommander.util.AppUtil; com.webcommander.constants.DomainConstants" %>
<g:set var="config" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.PRODUCT)}"/>
<g:set var="like_url" value="${app.currentURL()}"/>
<g:applyLayout name="_productwidget">
    <g:each var="profile" status="i"
            in="${['facebook', 'twitter', 'googleplus', 'pinterest']}">
        <g:set var="profileName" value="like_${profile}"/>
        <g:if test="${config[profileName].toBoolean()}">
            <span class="${profile}-like-us-dummy-icon">
            </span>
        </g:if>
    </g:each>
    <g:if test="${config.tell_friend.toBoolean()}">
        <span class="tell-friend" title="<g:message code='tell.friend'/>"></span>
    </g:if>
</g:applyLayout>