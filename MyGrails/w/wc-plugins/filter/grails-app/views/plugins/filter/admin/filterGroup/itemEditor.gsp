<%@ page import="grails.converters.JSON" %>
<div class="header">
    <div class="toolbar toolbar-right">
        <span class="tool-group toolbar-btn save"><g:message code="save"/></span>
        <div class="tool-group action-tool action-menu">
            <span class="tool-text"><g:message code="actions"/></span><span class="action-dropper collapsed"></span>
        </div>
    </div>
</div>
<div class="app-tab-content-container">
    <div class="left-panel panel">
    </div>
    <div class="right-panel panel">
        <div class="table-like-tree-sortable advance-filter-sortable">
            <input type="hidden" id="filterGroupId" value="${filterGroupId}">
            <g:set var="newitemid_incrementor" value="${1}"/>
            <div class="scroll-item-wrapper">
                <g:set var="loop" value="${items.size()}"/>
                <g:set var="currentList" value="${items}"/>
                <g:set var="currentIndex" value="${0}"/>
                <g:while test="${loop}">
                    <g:set var="currentItem" value="${currentList.get(currentIndex)}"/>
                    <div class="bmui-stl-entry-container">
                        <g:if test="${!currentItem.id}">
                            <%
                                currentItem.id = -1 * newitemid_incrementor++;
                            %>
                        </g:if>
                        <div class="bmui-stl-entry" item_id="${currentItem.id}" update_cache="${currentItem.id > 0 ? '' : currentItem.toJSON().encodeAsBMHTML()}">
                            <div class="table-actions action-column">
                                <span class="tool-icon edit" title="<g:message code="edit"/>"></span>
                                <span class="tool-icon remove" title="<g:message code="delete"/>"></span>
                            </div>
                            <div class="preview-image">
                                <g:set var="logoPath" value="${appResource.getFilterGroupItemImageURL(filterGroupItem: currentItem, imageSize: "thumb")}"/>
                                <img id="filter-group-item-logo-preview-${currentItem.id}" src="${logoPath}" alt="${currentItem.imageAlt}">
                            </div>
                            <div class="name-column">
                                ${currentItem.heading.encodeAsHTML()}
                            </div>
                        </div>
                    </div>

                    <g:set var="currentIndex" value="${currentIndex + 1}"/>
                    <g:set value="${currentIndex < currentList.size()}" var="loop"/>

                </g:while>
            </div>

            <input type="hidden" class="freeFirstNegativeId" value="${newitemid_incrementor}">
            <div class="removed-repository" style="display: none">
                <g:each in="${removedItems}" var="removedItem">
                    <input value="${removedItem}">
                </g:each>
            </div>

        </div>
    </div>
</div>