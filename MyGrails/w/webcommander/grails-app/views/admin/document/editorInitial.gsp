<%@ page import="com.webcommander.constants.NamedConstants; com.webcommander.manager.PathManager; com.webcommander.constants.DomainConstants" %>
<div class="header">
    <span class="item-group entity-count title">
        <g:message code="documents"/>
    </span>
    <div class="toolbar toolbar-right">
    </div>
</div>
<div class="app-tab-content-container layout-panel">
    <g:if test="${!params.id}">
        <div class="sample-layouts-container">
            <div class="header">
                <span class="doc-title">
                    <g:message code="select.layout"/>
                </span>
                <div class="create-new floating-popup">
                    <button class="floating-action-dropper start-button" type="button"><g:message code="start.blank"/></button>
                    <div class="popup-body context-menu">
                        <g:each in="${DomainConstants.DOCUMENT_TYPES}" var="type">
                            <div class="action-item close-popup" data-action="${type.value}"><g:message code="${type.value}"/></div>
                        </g:each>
                    </div>
                </div>
            </div>
            <div class="body">
                <g:each in="${DomainConstants.DOCUMENT_TYPES}" var="type">
                    <div class="layout-type ${type.value}">
                        <div class="layout-title"><span><g:message code="${type.value}"/></span></div>
                        <div class="layout-content">
                            <g:set var="layoutGroups" value="${layouts.findAll {it.type == type.value}}"/>
                            <g:each in="${layoutGroups}" var="layout">
                                <div class="layout" data-id="${layout.id}" data-type="${layout.type}">
                                    <div class="thumb">
                                        <img src="${appResource.getDocumentPath(name: "${layout.name}.png")}">
                                    </div>
                                    <div class="name">${NamedConstants.DOCUMENT_LAYOUT_NAME_MAP[layout.name]}</div>
                                </div>
                            </g:each>
                        </div>
                    </div>
                </g:each>
            </div>
        </div>
    </g:if>
</div>