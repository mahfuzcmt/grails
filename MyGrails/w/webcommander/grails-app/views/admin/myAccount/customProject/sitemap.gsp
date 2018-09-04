<div class="app-tab-content-container">
    <div class="header">
        <span class="heading-title"><g:message code="files"/> </span>
        <div class="tool-bar right">
            <span class="toolbar-btn save"><g:message code="save"/></span>
        </div>
    </div>
    <div class="table-like-tree-sortable">
        <div class="scroll-item-wrapper">
            <g:set var="loop" value="${items.size()}"/>
            <g:set var="currentList" value="${items}"/>
            <g:set var="currentIndex" value="${0}"/>
            <g:set var="currentListStack" value="${[]}"/>
            <g:set var="currentIndexStack" value="${[]}"/>
            <g:while test="${loop}">
                <g:set var="currentItem" value="${currentList.get(currentIndex)}"/>
                <g:set var="hasChild" value="${currentItem.children?.size()}"/>
                <div class="bmui-stl-entry-container">
                <div class="bmui-stl-entry">
                    <div class="table-actions action-column">
                        <span class="tool-icon edit" title="<g:message code="edit"/>"></span>
                        <span class="tool-icon remove" title="<g:message code="delete"/>"></span>
                    </div>
                    <div class="name-column">
                        ${currentItem.name.encodeAsHTML()}
                    </div>
                </div>
                <g:if test="${hasChild}">
                    <%
                        currentListStack.push(currentList);
                        currentIndexStack.push(currentIndex);
                    %>
                    <g:set var="currentList" value="${currentItem.children}"/>
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
        </div>
        <div class="action-row">
            <span class="action create-new"><g:message code="add.new"/> </span>
        </div>
    </div>
</div>