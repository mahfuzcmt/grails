<%@ page import="com.webcommander.plugin.filter.FilterGroup" %>
<div class="filter-block">
    <div class="search-form tool-group">
        <ui:domainSelect name="filterGroup" domain="${FilterGroup}" id="id" prepend="${['': g.message(code: "all")]}" class="always-top filter-group-selector" value="${params.filterGroup ? params.filterGroup.toLong() : 0}"/>
        <select id="filter-sorting" class="filter-group-item-sorting medium">
            <option value=""><g:message code="sort.by"/></option>
            <option value="ALPHA_ASC"><g:message code="alphabetic.a.z"/></option>
            <option value="ALPHA_DESC"><g:message code="alphabetic.z.a"/></option>
            <option value="CREATED_ASC"><g:message code="creation.date.oldest.first"/></option>
            <option value="CREATED_DESC"><g:message code="creation.date.latest.first"/></option>
        </select>
        <input type="text" class="search-text" placeholder="<g:message code="search"/>"><button type="button" class="submit-button search-form-submit icon-search"><g:message code="search"/></button>
    </div>
</div>

<div class="left-right-selector-panel">
    <div class="multi-column two-column">
        <div class="columns first-column">
            <div class="column-content selection-panel table-view">
                <g:include controller="filterGroup" action="loadFilterForSelection"/>
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
                        <th><g:message code="filter.name"/></th>
                        <th class="actions-column">
                            <span class="tool-icon remove-all"></span>
                        </th>
                    </tr>
                    <g:each in="${filters}" var="filter">
                        <tr>
                            <td>${filter.title.encodeAsBMHTML()}</td>
                            <td class="actions-column" item="${filter.id}" type="filterGroupItem">
                                <g:if test="${!preventSort}">
                                    <span class="tool-icon move-controls">
                                        <span class="move-up"></span>
                                        <span class="move-down"></span>
                                    </span>
                                </g:if>
                                <span class="action-navigator collapsed"></span>
                                <input type="hidden" name="${fieldName}" value="${filter.id}">
                            </td>
                        </tr>
                    </g:each>
                </table>
            </div>
            <div class="footer">
                <paginator total="${filters.size()}" offset="0" max="${10}"></paginator>
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