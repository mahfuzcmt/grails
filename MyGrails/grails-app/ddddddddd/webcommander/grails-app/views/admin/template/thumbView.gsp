<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.util.AppUtil" %>
<g:set var="installedTemplate" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "template_domain")}"/>
<div class="body">
    <div class="template-container">
        <g:if test="${!templateList.size()}">
            <div class="no-entry-block"><span class="message"><g:message code="no.templates.available"/></span></div>
        </g:if>
        <g:each in="${templateList}" var="template">
            <div class="template-thumb-wrapper">
                <div class="template-thumb ${template.liveURL == installedTemplate ? "installed" : ""}" template-name="${template.name.encodeAsBMHTML()}" template-id="${template.uuid}">
                <div class="template-image-block">
                    <span class="vertical-aligner" style='background-image: url("${template.imageURL}");'></span>
                </div>
                <div class="template-info">
                    <div class="template-name">${template.name.encodeAsBMHTML()}</div>
                    <div class="template-id">${template.uuid}</div>
                    <div class="colors">
                        <g:each in="${template.colors}" var="color">
                            <span class="color-block" name="${color.name}" color="${color.code}" style="background-color: ${color.code}"></span>
                        </g:each>
                    </div>
                    <g:if test="${template.liveURL != installedTemplate}">
                        <span class='button-group'>
                            <a target='_blank' href='${template.liveURL}' class='demo-link'><span class='demo action'><g:message code="demo"/></span></a>
                            <span class='install action'><g:message code="install"/></span>
                        </span>
                    </g:if>
                    <g:else>
                        <span class='button-group active-template'>
                            <g:message code="active.template"/>
                        </span>
                    </g:else>
                </div>
            </div>
            </div>
        </g:each>
    </div>
</div>
<div class="footer">
    <paginator total="${totalCount}" offset="${offset}" max="${max}"></paginator>
</div>