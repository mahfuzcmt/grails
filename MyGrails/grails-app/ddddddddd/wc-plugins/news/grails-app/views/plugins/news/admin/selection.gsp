%{--news selection--}%
<%@ page import="com.webcommander.webcommerce.Category" %>
<div class="filter-block">
    <input type="text" class="search-text" placeholder="<g:message code="search"/>">
    <select id="news-selecting" class="news-selecting medium">
        <option value=""><g:message code="all.news"/></option>
        <option value="lastWeek"><g:message code="last.one.week"/></option>
        <option value="lastMonth"><g:message code="last.one.month"/></option>
    </select>

    <div class="form-row datefield-between date-range">
        <label><g:message code="news.between"/></label>
        <input type="text" class="datefield-from smaller" name="newsFrom"> &nbsp; &nbsp; - &nbsp;<input type="text" class="datefield-to smaller" name="newsTo"/>
        &nbsp; &nbsp; <button  class="small submit"><g:message code="search"/></button>
    </div>

</div>

<div class="left-right-selector-panel">
    <div class="multi-column two-column">
        <div class="columns first-column">
            <div class="column-content selection-panel table-view">
                <g:include controller="news" action="loadNewsesForSelection"/>
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
                        <th><g:message code="product.name"/></th>
                        <th class="actions-column">
                            <span class="tool-icon remove-all"></span>
                        </th>
                    </tr>
                    <g:each in="${newses}" var="news">
                        <tr>
                            <td>${news.title.encodeAsBMHTML()}</td>
                            <td class="actions-column" item="${product.id}" type="product">
                                <g:if test="${!preventSort}">
                                    <span class="tool-icon move-controls">
                                        <span class="move-up"></span>
                                        <span class="move-down"></span>
                                    </span>
                                </g:if>
                                <span class="tool-icon remove-item remove"></span>
                                <input type="hidden" name="${fieldName}" value="${news.id}">
                            </td>
                        </tr>
                    </g:each>
                </table>
            </div>
            <div class="footer">
                <paginator total="${newses.size()}" offset="0" max="${10}"></paginator>
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