<div class="right-panel grid-view panel">
    <div class="body">
        <div class="assetLibrary">
            <g:if test="${folders.size() > 0}">
                <h4 class="group-label"><g:message code="folders"/></h4>
                <g:each in="${folders}" var="folder">
                    <div class="grid-item folder" content-type="folder" content-name="${folder.name.encodeAsBMHTML()}" content-path="${folder.path.encodeAsBMHTML()}"
                            content-parent="${folder.parent.encodeAsBMHTML()}" title="${folder.name.encodeAsBMHTML()}">
                        <span class="float-menu-navigator" content-type="folder"></span>
                        <span class="image"></span>
                        <div class="title">${folder.name.encodeAsBMHTML()}</div>
                    </div>
                </g:each>
            </g:if>
            <g:if test="${files.size() > 0}">
                <h4 class="group-label"><g:message code="files"/></h4>
                <g:each in="${files}" var="file">
                    <div class="grid-item file ${file.clazz}" content-type="file" content-clazz="${file.clazz}" content-name="${file.name.encodeAsBMHTML()}" content-path="${file.path.encodeAsBMHTML()}"
                            content-parent="${file.parent.encodeAsBMHTML()}" title="${file.name.encodeAsBMHTML()}">
                        <span class="float-menu-navigator" content-type="file"></span>
                        <div class="image"></div>
                        <span class="title">${file.name.encodeAsBMHTML()}</span>
                    </div>
                </g:each>
            </g:if>
        </div>
    </div>
    <div class="footer">
        <ui:perPageCountSelector/>
        <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
    </div>
    <div class="status-container-wrapper">
        <table class="status-container">
            <colgroup>
                <col style="width: 15%">
                <col style="width: 20%">
                <col style="width: 34%">
                <col style="width: 10%">
                <col style="width: 8%">
                <col style="width: 5%">
            </colgroup>
            <tr>
                <th><g:message code="file.name"/></th>
                <th><g:message code="copied.to"/></th>
                <th><g:message code="progress"/></th>
                <th class="status-column"><g:message code="status"/></th>
                <th><g:message code="size"/></th>
                <th class="action-column">
                    <span class="tool-icon cancel-all" title="<g:message code="cancel.all.pending.request"/>"></span>
                    <span class="tool-icon clear-list" title="<g:message code="remove.completed.request"/>"></span>
                </th>
            </tr>
        </table>
    </div>
</div>