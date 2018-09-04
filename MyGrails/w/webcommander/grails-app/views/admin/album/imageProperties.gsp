<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.constants.NamedConstants;" %>
<%@ page import="com.webcommander.Page; com.webcommander.webcommerce.Product;" %>
<%@ page import="com.webcommander.webcommerce.Category" %>
<g:set var="itemTypes" value="${DomainConstants.NAVIGATION_ITEM_TYPE}" />
<div class="bmui-tab">
    <div class="bmui-tab-header-container top-side-header">
        <div class="bmui-tab-header" data-tabify-tab-id="basic">
            <span class="title"><g:message code="basic"/></span>
        </div>
        <plugin:hookTag hookPoint="albumImagePropertiesTabHeader"/>
    </div>
    <div class="bmui-tab-body-container">
        <div id="bmui-tab-basic" class="body">
            <form class="edit-popup-form edit-properties-form" action="${app.relativeBaseUrl()}album/saveImageProperties" method="post">
                <input type="hidden" name="id" value="${albumImage?.id}">
                <div class="form-row">
                    <label><g:message code="alt.text"/></label>
                    <input type="text" name="altText" class="medium" validation="maxlength[255]" maxlength="255" value="${albumImage?.altText}">
                </div>
                <div class="form-row">
                    <label><g:message code="description"/></label>
                    <input type="text" name="description" class="medium" validation="maxlength[255]" maxlength="255" value="${albumImage?.description}">
                </div>
                <div class="form-row">
                    <label><g:message code="link.to.url"/></label>
                    <ui:namedSelect class="medium link-type" name="linkType" key="${NamedConstants.NAVIGATION_ITEM_MESSAGE_KEYS}" value="${albumImage.linkType}"/>
                </div>
                <g:if test="${albumImage?.linkType && albumImage?.linkType != 'custom_link'}">
                    <g:include controller="album" action="loadReferenceSelectorBasedOnType" params="${[linkType: albumImage?.linkType, linkTo: albumImage?.linkTo]}"/>
                </g:if>
                <div id="link-target" class="form-row chosen-wrapper">
                    <label><g:message code="link.target"/></label>
                    <g:select class="medium" name="linkTarget" from="${['_self', '_blank', '_parent', '_top']}" value="${albumImage?.linkTarget}" keys="${['_self', '_blank', '_parent', '_top']}" />
                </div>
                <div class="form-row">
                    <button type="submit" class="submit-button edit-properties-form-submit"><g:message code="save"/></button>
                    <button type="button" class="cancel-button"><g:message code="cancel"/></button>
                </div>
            </form>
        </div>
        <plugin:hookTag hookPoint="albumImagePropertiesTabBody" attrs="[albumImage: albumImage]"/>
    </div>
</div>