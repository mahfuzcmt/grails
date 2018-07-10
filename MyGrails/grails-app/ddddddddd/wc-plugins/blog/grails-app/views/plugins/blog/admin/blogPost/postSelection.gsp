<form action="" class="edit-popup-form">
    <div class="widget-toolbar">
        <div class="toolbar toolbar-right">
            <div class="search-form tool-group">
             <span class="tool-icon remove-search" title="<g:message code="remove.search"/>" style="display: none">
            </span><input type="text" class="search-text" placeholder="<g:message code="search"/>"><button type="button" class="icon-search search-button submit-button search-form-submit"></button>
            </div>
        </div>
    </div>
    <div class="left-right-selector-panel">
        <div class="multi-column two-column">
            <div class="columns first-column">
                <div class="column-content selection-panel table-view">
                    <g:include controller="blogAdmin" action="loadPostForMultiSelect"/>
                </div>
            </div><div class="columns last-column">
            <div class="column-content selected-panel table-view">
                <div class="body">
                    <table>
                            <colgroup>
                                <col style="width: 80%">
                                <col style="width: 20%">
                            </colgroup>
                            <tr>
                                <th><g:message code="name"/></th>
                                <th class="actions-column">
                                    <span class="tool-icon remove-all"></span>
                                </th>
                            </tr>
                            <g:each in="${posts}" var="post">
                                <tr>
                                    <td>${post.name.encodeAsBMHTML()}</td>
                                    <td class="actions-column" type="post" item="${post.id}">
                                        <span class="action-navigator collapsed"></span>
                                        <input type="hidden" name="post" value="${post.id}">
                                    </td>
                                </tr>
                            </g:each>
                        </table>
                    </div>
                    <div class="footer">
                        <paginator total="${post?.size()}" offset="0" max="${10}"></paginator>
                    </div>
                    <div class="hidden">
                        <div class="action-column-dice-content">
                            <table>
                                <tr>
                                    <td></td>
                                    <td class="actions-column">
                                        <span class="action-navigator collapsed"></span>
                                        <input type="hidden">
                                    </td>
                                </tr>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div class="button-line">
        <button type="submit" class="submit-button edit-popup-form-submit"><g:message code="done"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>
