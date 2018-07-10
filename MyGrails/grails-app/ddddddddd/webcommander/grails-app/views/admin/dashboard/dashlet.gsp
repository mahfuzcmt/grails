<%@ page import="com.webcommander.util.AppUtil; com.webcommander.constants.DomainConstants" %>
<div class="dashlet ${dashlet.uniqueName}" data-id="${dashlet.id}" data-name="${dashlet.uniqueName}">
    <g:set var="ecommerce" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce")}"/>
    <div class="dashlet-content-wrapper">
        <div class="header">
            <span class="title">
                <g:if test="${!((dashlet.title == "latest.order") && (ecommerce == 'false'))}">
                    <g:message code="${(ecommerce == 'false') ? dashlet.title.replace("customer","member") : dashlet.title}"/>
                </g:if>
            </span>
            <span class="icon action" title="Configure" dashlet-id="${dashlet.id}" data-name="${dashlet.uniqueName}"></span>
        </div>
        <div class="content">
            <g:include controller="${dashlet.controller}" action="${dashlet.action}" params="${[dashlet: dashlet.id]}"/>
        </div>
    </div>
</div>