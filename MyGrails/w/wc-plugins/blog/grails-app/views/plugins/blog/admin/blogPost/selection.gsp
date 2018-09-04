<div class="filter-block">
    <div class="toolbar toolbar-right">
        <div class="search-form tool-group">
            <input type="text" class="search-text" placeholder="<g:message code="search"/>"><button type="button" class="icon-search search-button submit-button"></button>
        </div>
    </div>
</div>

<div class="left-right-selector-panel">
    <div class="multi-column two-column">
        <div class="columns first-column">
            <div class="column-content selection-panel table-view">
                <g:include controller="blogAdmin" action="loadBlogPostForSelection"/>
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
                        <th><g:message code="post.title"/></th>
                        <th class="actions-column">
                            <span class="tool-icon remove-all"></span>
                        </th>
                    </tr>
                    <g:each in="${posts}" var="post">
                        <tr>
                            <td>${post.name.encodeAsBMHTML()}</td>
                            <td class="actions-column" item="${post.id}" type="post">
                                <g:if test="${!preventSort}">
                                    <span class="tool-icon move-controls">
                                        <span class="move-up"></span>
                                        <span class="move-down"></span>
                                    </span>
                                </g:if>
                                <span class="tool-icon remove-item remove"></span>
                                <input type="hidden" name="${fieldName}" value="${post.id}">
                            </td>
                        </tr>
                    </g:each>
                </table>
            </div>
            <div class="footer">
                <paginator total="${posts.size()}" offset="0" max="${10}"></paginator>
            </div>
            <div class="hidden">
                <div class="action-column-dice-content">
                    <table>
                        <tr>
                            <td></td>
                            <td class="actions-column">
                                <g:if test="${!preventSort}">
                                    <span class="tool-icon move-controls">
                                        <span class="move-up"></span>
                                        <span class="move-down"></span>
                                    </span>
                                </g:if>
                                <span class="tool-icon remove-item remove"></span>
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