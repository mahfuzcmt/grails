<%@ page import="com.webcommander.util.AppUtil; com.webcommander.constants.DomainConstants; com.webcommander.constants.NamedConstants; com.webcommander.config.EmailTemplate" %>
<div class="accordion-panel">
    <div class="label-bar label-bar-email-settings expanded"><a class="toggle-icon"></a>
        <g:message code="email.settings"/>
    </div>
    <div class="accordion-item expanded">
        <g:include view="admin/setting/emailSettings.gsp"/>
    </div>
    <g:set var="emailTypes" value="${DomainConstants.EMAIL_TYPE}"/>
    <g:set var="ecommerce" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce")}"/>
    <g:each in="${emailTypes.values()}" var="type">
        <g:if test="${((DomainConstants.ECOMMERCE_EMAIL_TYPE_CHECKLIST[type] == true) && (ecommerce == 'true')) || (DomainConstants.ECOMMERCE_EMAIL_TYPE_CHECKLIST[type] == null)}">
            <div class="label-bar label-bar-${type} collapsed"><a class="toggle-icon"></a>
                <input type="hidden" name="labelBarIdentifier" value="label-bar-${type}">
                <g:message code="${(ecommerce == 'false') ? NamedConstants.EMAIL_SETTING_MESSAGE_KEYS[type].replace("customer","member") : NamedConstants.EMAIL_SETTING_MESSAGE_KEYS[type]}"/>
            </div>
            <div class="accordion-item collapsed">
                <g:set var="templates" value="${EmailTemplate.findAllByType(type)}"/>
                <div class="email-table">
                    <table class="content">
                        <colgroup>
                            <col class="email-column">
                            <col class="type-column">
                            <col class="subject-column">
                            <col class="status-column">
                            <col class="actions-column">
                        </colgroup>
                        <tr>
                            <th><g:message code="email"/></th>
                            <th><g:message code="type"/></th>
                            <th><g:message code="subject"/></th>
                            <th class="status-column"><g:message code="status"/></th>
                            <th class="actions-column"><g:message code="actions"/></th>
                        </tr>
                        <g:each in="${templates}" var="template">
                            <g:set var="identifier" value="${template.identifier.replaceAll("-", "_")}"/>
                            <g:if test="${((DomainConstants.ECOMMERCE_EMAIL_TEMPLATE_CHECKLIST[identifier] == true) && (ecommerce == 'true')) || (DomainConstants.ECOMMERCE_EMAIL_TEMPLATE_CHECKLIST[identifier] == null)}">
                                <tr>
                                    <td><g:message code="${(ecommerce == 'false') ? template.label.replace("customer","member") : template.label}"/></td>
                                    <td>${template.contentType}</td>
                                    <td>${(ecommerce == 'false') ? template.subject.replace("Customer","Member").encodeAsBMHTML() : template.subject.encodeAsBMHTML()}</td>
                                    <td class="status-column">
                                        <span class="status ${template.active ? "positive" : "negative"}" title="${template.active ? g.message(code: "active") : g.message(code: "inactive")}"></span>
                                    </td>
                                    <td class="actions-column">
                                        <span class="action-navigator collapsed" entity-id="${template.id}" entity-name="${template.label}"></span>
                                    </td>
                                </tr>
                            </g:if>
                        </g:each>
                    </table>
                </div>
            </div>
        </g:if>
    </g:each>
</div>