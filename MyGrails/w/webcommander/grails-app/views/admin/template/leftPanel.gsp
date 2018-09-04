<%@ page import="com.webcommander.AppResourceTagLib; grails.util.Holders; com.webcommander.constants.NamedConstants; com.webcommander.constants.DomainConstants; com.webcommander.util.AppUtil" %>
<div class="left-panel">
    <div class="header">
        <div class="with-selector chosen-wrapper">
            <label><g:message code="width" /></label>
            <ui:namedSelect key="${NamedConstants.TEMPLATE_CONTAINER_CLASS}" name="templateContainerClass" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "template_container_class")}"/>
        </div><div class="installed-colors colors">
        <g:each in="${installedColors}" var="color">
            <span class="color ${color.fullname == installedColor ? "selected" : ""}" title="${color.name}" name="${color.fullname}" style="background-color: ${color.code}"></span>
        </g:each>
    </div>
    </div>

    <div class="template-image body">
        <g:if test="${appResource.isExistTemplateLeftPanelImage()}">
            <img src="${appResource.getTemplateLeftPanelImageURL()}" alt="<g:message code="no.preview.found"/>"/>
        </g:if>
        <g:else>
            <div class="message no-preview"><g:message code="no.preview.found"/></div>
        </g:else>
    </div>
</div>