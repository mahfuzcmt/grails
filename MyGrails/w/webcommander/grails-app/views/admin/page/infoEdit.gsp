<%@ page import="com.webcommander.util.AppUtil; com.webcommander.content.Navigation; com.webcommander.design.Layout; com.webcommander.constants.DomainConstants" %>
<g:set var="addPageForStore" value="${isMultiModelEnabled && params.addPageForStore}"/>
<g:set var="actionUrl" value="${addPageForStore ? "${app.relativeBaseUrl()}pageAdmin/addPageForStore" : "${app.relativeBaseUrl()}pageAdmin/save"}"/>

<form action="${actionUrl}" method="post" class="create-edit-form">
    <div class="bmui-tab">
        <div class="bmui-tab-header-container top-side-header">
            <plugin:hookTag hookPoint="pageInfoEditTabHeader">
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
            <plugin:hookTag hookPoint="pageInfoEditTabBody">
                <div id="bmui-tab-general">
                    <input type="hidden" name="id" value="${page.id}">
                    <g:if test="${addPageForStore}">
                        <input type="hidden" name="storeId" value="${storeId}">
                    </g:if>
                    <g:if test="${addPageForStore && storeId}">
                        <input type="hidden" name="storeId" value="${storeId}">
                    </g:if>
                    <g:if test="${addPageForStore && params.parentId}">
                        <input type="hidden" name="parentId" value="${parentId}">
                    </g:if>
                    <div class="form-section">
                        <div class="form-section-info">
                            <h3><g:message code="general.info"/></h3>
                            <div class="info-content"><g:message code="section.text.general.info"/></div>
                        </div>
                        <div class="form-section-container">
                            <div class="double-input-row">
                                <div class="form-row mandatory">
                                    <label><g:message code="page.name"/><span class="suggestion"><g:message code="suggestion.page.name"/></span></label>
                                    <input type="text" name="name" class="large unique" value="${page.name}" validation="required rangelength[2, 100]" maxlength="100">
                                </div><div class="form-row mandatory">
                                    <label><g:message code="page.title"/><span class="suggestion"><g:message code="suggestion.page.title"/></span></label>
                                    <input type="text" name="title" class="large" value="${page.title}" validation="required rangelength[2, 250]" maxlength="250">
                                </div>
                            </div>
                            <div class="form-row trash-row" style="display: none;">
                                <label><g:message code="what.to.do"/></label>
                                <span><a onclick="return false" class="trash-duplicate-restore fake-link"><g:message code="restore"/></a> <g:message code="restore.and.close.window"/> <g:message code="or"/></span>
                                <span><input type="checkbox" name="deleteTrashItem.name" class="trash-duplicate-delete multiple"> <g:message code="delete.and.save"/></span>
                            </div>
                            <g:if test="${addPageForStore}">
                                <div class="form-row  mandatory">
                                    <label><g:message code="store.name"/></label>
                                    <g:select name="store" class="large store" id="store" from="${stores}" dataAttrs="[storeIdentifier: 'identifire']" optionKey="id" optionValue="location" value="${store}"/>
                                </div>
                            </g:if>
                            <div class="double-input-row">
                                <div class="form-row">
                                    <label><g:message code="url.identifier"/><span class="suggestion"><g:message code="suggestion.page.url.identifire"/></span></label>
                                    <input type="text" name="url" class="large unique" value="${page.url}" validation="rangelength[2, 50] ${addPageForStore ? "" : 'url_folder'}" ${addPageForStore ? "readonly" :""}>
                                </div><div class="form-row chosen-wrapper">
                                    <label><g:message code="administrative.status"/><span class="suggestion">e.g.  Active/Inactve</span></label>
                                    <g:select name="active" from="${['active', 'inactive'].collect {g.message(code: it)}}" keys="${['true', 'false']}" class="medium" value="${page.isActive}"/>
                                </div>
                            </div>
                            <div class="double-input-row">
                                <div class="form-row chosen-wrapper" >
                                    <label><g:message code="visibility"/><span class="suggestion"><g:message code="suggestion.page.visibility"/></span></label>
                                    <g:set var="visibilities" value="['open', 'hidden', 'restricted']"/>
                                    <select name="visibility" class="large" toggle-target="visibility">
                                        <g:each in="${visibilities}" var="opt">
                                            <option value="${opt}" ${opt == page.visibility ? "selected" : ""}><g:message code="${opt}"/></option>
                                        </g:each>
                                    </select>
                                </div><div class="form-row chosen-wrapper">
                                    <label class="medium"><g:message code="layout"/><span class="suggestion"><g:message code="suggestion.page.layout"/> </span> </label>
                                    <ui:domainSelect name="layoutid" class="large" domain="${Layout}" value="${page.layout?.id}" prepend="${[(''): g.message(code: 'no.layout')]}"/>
                                </div>
                            </div>
                            <div class="form-row restricted-visibility-row visibility-restricted" ${page.visibility == DomainConstants.PAGE_VISIBILITY.RESTRICTED ? '' : 'style="display:none"' }>
                                <label><g:message code="visible.to"/></label>
                                <input type="radio" name="visibleTo" class="radio" value="all" ${page.visibleTo ? (page.visibleTo == DomainConstants.PAGE_VISIBLE_TO.ALL? "checked" : "") : "checked"}>
                                <g:message code="all.customer" args="${[(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce") == 'true')?"Customer":"Member"]}"/>&nbsp;&nbsp;
                                <input type="radio" name="visibleTo" toggle-target="choose-customer" class="radio" value="selected"  ${page.visibleTo ? (page.visibleTo == DomainConstants.PAGE_VISIBLE_TO.SELECTED ? "checked" : "") : ""}>
                                <g:message code="selected.customers.or.groups" args="${[(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce") == 'true')?"Customer":"Member"]}"/>
                                <span class="tool-icon choose choose-customer"></span>
                                <g:each in="${page.customers}" var="customer">
                                    <input type="hidden" name="customer" value="${customer.id}">
                                </g:each>
                                <g:each in="${page.customerGroups}" var="customerGroup">
                                    <input type="hidden" name="customerGroup" value="${customerGroup.id}">
                                </g:each>
                            </div>
                            <g:if test="${page.id == null}">
                                <div class="form-row">
                                    <label><g:message code="navigation"/></label>
                                    <ui:domainSelect domain="${Navigation}" custom-attrs="${[multiple: 'true']}" name="linkedNavigations"/>
                                </div>
                            </g:if>
                        </div>
                    </div>
                </div>
                <div id="bmui-tab-metatag">
                    <div class="form-section">
                        <div class="form-section-info">
                            <h3><g:message code="meta.tag.info"/></h3>
                            <div class="info-content"><g:message code="section.text.meta.tag.info"/></div>
                        </div>
                        <div class="form-section-container">
                            <g:include view="/admin/metatag/metaTagEditor.gsp" model="${[metaTags: page.metaTags]}"/>
                        </div>
                    </div>
                </div>
                <div id="bmui-tab-webtool">
                    <div class="form-section">
                        <div class="form-section-info">
                            <h3><g:message code="webtool.info"/></h3>
                            <div class="info-content"><g:message code="section.text.webtool.info"/></div>
                        </div>
                        <div class="form-section-container">
                            <div class="form-row">
                                <label><g:message code="disable.tracking"/></label>
                                <input type="checkbox" class="single" name="disableTracking" value="true" uncheck-value="false" ${page.disableGooglePageTracking ? "checked=checked" : ""}/>
                            </div>
                        </div>
                    </div>
                </div>
            </plugin:hookTag>
        </div>
    </div>
    <div class="form-row wcui-horizontal-tab-button">
        <button type="submit" class="submit-button edit-popup-form-submit"><g:message code="${page.id ? "update" : "save"}"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>