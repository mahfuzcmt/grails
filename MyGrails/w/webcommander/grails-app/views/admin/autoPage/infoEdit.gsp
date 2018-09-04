<%@ page import="com.webcommander.design.Layout" %>
<form action="${app.relativeBaseUrl()}autoPage/save" method="post" class="edit-popup-form create-edit-form">
    <input type="hidden" name="id" value="${fixedPage.id}">
    <div class="bmui-tab">
        <div class="bmui-tab-header-container  top-side-header">
            <plugin:hookTag hookPoint="autoPageInfoEditTabHeader">
                <div class="bmui-tab-header" data-tabify-tab-id="general">
                    <span class="title"><g:message code="general"/></span>
                </div>
                <div class="bmui-tab-header" data-tabify-tab-id="metatag">
                    <span class="title"><g:message code="metatag"/></span>
                </div>
                <div class="bmui-tab-header" data-tabify-tab-id="webtool">
                    <span class="title"><g:message code="webtool"/></span>
                </div>
            </plugin:hookTag>
        </div>
        <div class="bmui-tab-body-container">
            <plugin:hookTag hookPoint="autoPageInfoEditTabBody">
                <div id="bmui-tab-general">
                    <div class="form-section">
                        <div class="form-section-info">
                            <h3><g:message code="general.info"/></h3>
                            <div class="info-content"><g:message code="section.text.general.info"/></div>
                        </div>
                        <div class="form-section-container">
                            <div class="double-input-row">
                                <div class="form-row">
                                    <label><g:message code="page.title"/><span class="suggestion">Set a specific Article Name instead of pre defined one</span></label>
                                    <input class="medium" type="text" name="title" value="${fixedPage.title.encodeAsBMHTML()}">
                                </div><div class="form-row">
                                    <label><g:message code="https.only"/></label>
                                    <input type="checkbox" name="isHttps" class="single" value="true" uncheck-value="false" ${fixedPage.isHttps ? "checked" : ""}>
                                </div>
                            </div>
                            <div class="form-row chosen-wrapper">
                                <label class="medium"><g:message code="layout"/><span class="suggestion"> Choose defined layout or layout that you have created earlier. For example, Web Commander Default Layout. </span></label>
                                <ui:domainSelect name="layout-id" class="medium" domain="${Layout}" value="${fixedPage.layout.id}"/>
                            </div>
                        </div>
                    </div>
                </div>
                <div id="bmui-tab-metatag">
                    <div class="form-section">
                        <div class="form-section-info">
                            <h3><g:message code="meta.tag"/></h3>
                            <div class="info-content"><g:message code="section.text.metatag.info"/></div>
                        </div>
                        <div class="form-section-container">
                            <g:include view="admin/metatag/metaTagEditor.gsp" model="[metaTags: fixedPage.metaTags]"/>
                        </div>
                    </div>
                </div>
                <div id="bmui-tab-webtool">
                    <div class="form-section">
                        <div class="form-section-info">
                            <h3><g:message code="webtool.info"/></h3>
                            <div class="info-content"><g:message code="section.text.product.info"/></div>
                        </div>
                        <div class="form-section-container">
                            <div class="form-row">
                                <label><g:message code="disable.tracking"/></label>
                                <input type="checkbox" name="disableTracking" value="true" class="single" uncheck-value="false" ${fixedPage.disableGooglePageTracking ? "checked=checked" : ""}/>
                            </div>
                        </div>
                    </div>
                </div>
            </plugin:hookTag>
        </div>
    </div>
    <div class="form-row wcui-horizontal-tab-button">
        <button type="submit" class="submit-button"><g:message code="update"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>