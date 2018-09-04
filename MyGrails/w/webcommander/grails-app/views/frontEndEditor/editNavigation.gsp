<%@ page import="com.webcommander.content.Navigation; grails.converters.JSON" %>
<g:if test="${!params.navigationId}">
    <div class="table-like-tree-sortable fee-common-popup-content">
    <form action="${app.relativeBaseUrl()}frontEndEditor/saveNavigationItem" method="post" class="config-form">
    <input type="hidden" id="navigationId" value="${navigationId}">
    <g:set var="newitemid_incrementor" value="${1}"/>
    <div class="header-line">
        <div class="fee-header-top fee-padding-10">
            <div class="fee-row fee-navigation-create-panel" style="display: none;">
                <div class="fee-col fee-col-60 fee-inlineInput mandatory form-row">
                    <label for="navigationTitle"><g:message code="navigation"/></label>
                    <input type="text" class="create-navigation-inp" name="navigationTitle" id="navigationTitle" maxlength="100">
                </div>
                <div class="fee-col fee-col-40 form-row">
                    <button class="fee-navigation-save create-navigation-inp fee-row-button" type="button"><g:message code="save"/></button>
                    <button class="fee-navigation-cancel create-navigation-inp fee-row-button" type="button"><g:message code="cancel"/></button>
                </div>
            </div>
            <div class="fee-navigation-panel">
                <div class="fee-row">
                    <div class="fee-col fee-col-33 form-row">
                        <label><g:message code="navigation"/></label>
                        <ui:domainSelect name="navigationId" domain="${Navigation}" value="${navigationId}" class="navigation-selector select-navigation-inp always-bottom"/>
                    </div>
                    <div class="fee-col fee-col-33 form-row">
                        <label for="widgetTitle"><g:message code="widget.title"/></label>
                        <input type="text" name="title" id="widgetTitle" maxlength="255" value="${widget.title}">
                    </div>
                    <div class="fee-col fee-col-33 form-row">
                        <label for="widgetOrientation"><g:message code="orientation"/></label>
                        <div>
                            <span class="fee-auto-width"><input type="radio" id="widgetHrOrientation" ${widgetParams.orientation == 'H' ? 'checked' : ''} name="orientation" value="H"><label class="fee-auto-width"><g:message code="horizontal"/></label></span>
                            <span class="fee-auto-width"><input type="radio" name="orientation" ${widgetParams.orientation == 'V' || !widgetParams.orientation ? 'checked' : ''} id="widgetOrientation" value="V"><label class="fee-auto-width"><g:message code="vertical"/></label></span>
                        </div>
                    </div>
                </div>
                <a class="fee-navigation-create" href="#" type="button">+ <g:message code="create.navigation"/></a>
            </div>
        </div>
        <span class="title">Manage Navigation - <g:message code="navigation"/></span>
        <span class="icon close"></span>
    </div>
    <div class="body fee-table-sorting">
</g:if>

<g:set var="loop" value="${items.size()}"/>
<g:set var="currentList" value="${items}"/>
<g:set var="currentIndex" value="${0}"/>
<g:set var="currentListStack" value="${[]}"/>
<g:set var="currentIndexStack" value="${[]}"/>
<g:while test="${loop}">
    <g:set var="currentItem" value="${currentList.get(currentIndex)}"/>
    <g:set var="hasChild" value="${currentItem.childItems?.size()}"/>
    <div class="bmui-stl-entry-container">
    <g:if test="${!currentItem.id}">
        <%
            currentItem.id = -1 * newitemid_incrementor++;
        %>
    </g:if>
    <div class="bmui-stl-entry" parent="${currentItem.parent?.id}" item_id="${currentItem.id}" update_cache="${currentItem.id > 0 ? '' : currentItem.toJSON().encodeAsBMHTML()}">
        <div class="table-actions action-column">
            <span class="tool-icon edit" title="<g:message code="edit"/>"></span>
            <span class="tool-icon remove" title="<g:message code="delete"/>"></span>
        </div>
        <div class="name-column">
            ${currentItem.label.encodeAsHTML()}
        </div>
    </div>
    <g:if test="${hasChild}">
        <%
            currentListStack.push(currentList);
            currentIndexStack.push(currentIndex);
        %>
        <g:set var="currentList" value="${currentItem.childItems}"/>
        <g:set var="currentIndex" value="${-1}"/>
        <div class="bmui-stl-sub-container">
    </g:if>
    <g:else>
        </div>
    </g:else>
    <g:set var="currentIndex" value="${currentIndex + 1}"/>
    <g:set value="${currentIndex < currentList.size()}" var="loop"/>
    <g:while test="${!loop && currentListStack.size()}">
        </div>
    </div>
        <g:set var="currentList" value="${currentListStack.pop()}"/>
        <g:set var="currentIndex" value="${currentIndexStack.pop() + 1}"/>
        <g:set value="${currentIndex < currentList.size()}" var="loop"/>
    </g:while>
</g:while>

<input type="hidden" class="freeFirstNegativeId fee-remove-panel value="${newitemid_incrementor}">
<div class="removed-repository fee-remove-panel" style="display: none">
    <g:each in="${removedItems}" var="removedItem">
        <input value="${removedItem}">
    </g:each>
</div>
<g:if test="${!params.navigationId}">
    <div class="fee-create-panel fee-non-remove">
        <div class="fee-clickable-panel">
            <a href="#" class="fee-add-button">+ <g:message code="add.items"/></a>
        </div>
        <div class="bmui-stl-entry-container fee-non-remove" style="display: none;">
            <div class="fee-add-panel"></div>
        </div>
    </div>
    </div>
    <div class="button-line fee-button-wrapper fee-config-footer">
        <button class="fee-cancel fee-common" type="button"><g:message code="cancel"/></button>
        <button class="fee-save" type="submit"><g:message code="update"/></button>
    </div>
    </form>
</div>
</g:if>

