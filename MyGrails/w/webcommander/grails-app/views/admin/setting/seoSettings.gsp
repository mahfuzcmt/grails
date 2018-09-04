<%@ page import="com.webcommander.constants.NamedConstants; com.webcommander.util.AppUtil; com.webcommander.constants.DomainConstants" %>
<div class="toolbar toolbar-share">
    <div class="toolbar toolbar-right">
        <div class="tool-group action-tool action-menu">
            <span class="tool-text"><g:message code="actions"/></span><span class="action-dropper collapsed"></span>
        </div>
    </div>
</div>
<form class="seo-setting-form create-edit-form" action="${app.relativeBaseUrl()}setting/saveConfigurations" method="post">
    <input type="hidden" name="type" value="webtool">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="google.tracking"/></h3>
            <div class="info-content"><g:message code="section.text.google.tracking.info"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row">
                <input type="checkbox" class="single" toggle-target="tracking-id" name="webtool.tracking_enabled" value="true" uncheck-value="false" ${config.tracking_enabled == "true" ? "checked='checked'" + "" : ""}>
                <span><g:message code="enable.tracking"/></span>
            </div>
            <div class="tracking-id">
                <div class="form-row mandatory">
                    <label><g:message code="tracking.id"/><span class="suggestion">e. g. Your Google Tracking ID</span></label>
                    <input type="text" class="large" name="webtool.tracking_id" value="${config.tracking_id}" validation="skip@if{self::hidden} required">
                </div>
                <div class="double-input-row">
                    <div class="form-row">
                        <input type="checkbox" class="single" name="webtool.page_tracking" value="on" ${config.page_tracking == "on" ? "checked='checked'" : ""} uncheck-value="off">
                        <span><g:message code="page.tracking.enabled"/></span>
                    </div><div class="form-row">
                        <input type="checkbox" class="single" name="webtool.google_e_commerce_tracking" value="on" ${config.google_e_commerce_tracking == "on" ? "checked='checked'" : ""} uncheck-value="off">
                        <span><g:message code="ecommerce.tracking.enabled"/></span>
                    </div>
                </div>
                <g:set var="type" value="${DomainConstants.SITE_CONFIG_TYPES.GOOGLE_EVENT_TRACKING}"/>
                <input type="hidden" name="type" value="${type}">
                <g:set var="eConfig" value="${AppUtil.getConfig(type)}"/>
                <div class="form-row">
                    <input type="checkbox" class="single" name="${type}.tracking" value="on" toggle-target="event-tracking" ${eConfig.tracking == "on" ? "checked='checked'" : ""} uncheck-value="off">
                    <span><g:message code="event.tracking.enabled"/></span>
                </div>
            </div>
            <div class="event-tracking">
                <g:set var="message" value="${NamedConstants.EVENT_TRACKING_TYPE}"/>
                <g:each in="${eConfig.findAll {it.key.startsWith("event_")}.keySet()}" var="event">
                    <div class="form-row">
                        <input type="checkbox" class="single" name="${type}.${event}" value="on" toggle-target="${event}" ${eConfig[event] == "on" ? "checked='checked'" : ""} uncheck-value="off">
                        <g:set var="last" value="${event.substring(event.indexOf("_") + 1)}"/>
                        <span><g:message code="${message[last]}"/></span>
                    </div>
                    <div class="${event}">
                    %{--dom alignment should not change--}%
                        <g:set var="values" value="${eConfig.findAll {it.key.startsWith(last + "_")}.keySet()}"/>
                        <g:each status="i" in="${values}" var="key"><g:if test="${i%2 == 0}">
                            <div class="double-input-row">
                            </g:if><div class="form-row">
                                <input type="checkbox" class="single" name="${type}.${key}" value="on" ${eConfig[key] == "on" ? "checked='checked'" : ""} uncheck-value="off">
                                <span>${i} <g:message code="${key.replace(last + "_", "").replaceAll("_", ".")}"/></span>
                            </div><g:if test="${i%2 != 0 || i == (values.size() - 1)}"></div></g:if></g:each>
                    </div>
                </g:each>
            </div>
            <div class="form-row">
                <button type="submit" class="submit-button seo-setting-form-submit"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>
<div class="section-separator"></div>
<div class="create-edit-form">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="root.upload"/></h3>
            <div class="info-content"><g:message code="section.text.root.upload.info"/></div>
        </div>
        <div class="form-section-container">
            <div class="seo-upload-container">
                <div class="drop-zone">
                    <input name="seoUpload" type="file" size-limit="51200" class="medium" reset-after-submit="true" reset-support="false" remove-support="false" text-helper="no" submit="auto" ajax-url="${
                        app.relativeBaseUrl()}setting/seoUpload">
                </div>
                <div class="seo-file-list">
                    <g:each in="${seoRootFiles}" var="file">
                        <div class="item">
                            <div class="name">${file}</div><div class="action"><span class="tool-icon remove" file-name="${file}"></span></div>
                        </div>
                    </g:each>
                </div>
            </div>
        </div>
    </div>
</div>
<div class="section-separator"></div>
<div class="create-edit-form">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="301.redirects"/></h3>
            <div class="info-content"><g:message code="section.text.301.redirects.info"/></div>
        </div>
        <div class="form-section-container">
            <g:include controller="setting" action="load301Redirect"/>
        </div>
    </div>
</div>