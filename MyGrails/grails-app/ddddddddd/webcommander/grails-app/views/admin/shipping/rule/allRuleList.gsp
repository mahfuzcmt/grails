<%@ page import="com.webcommander.constants.NamedConstants" %>
<div class="right-panel table-view">
    <div class="header">
        <div class="left-header">
            <h3 class="title"><g:message code="rules"/></h3>
            <p class="description"><g:message code="rules.description"/></p>
        </div>
        <div class="toolbar toolbar-right">
            <div class="advance-filter-btn"><g:message code="filter.by"/></div>
            <form class="search-form tool-group">
                <input type="text" class="search-text" placeholder="<g:message code="search"/>"><button type="submit" class="icon-search"></button>
            </form>
            <div class="tool-group action-tool action-menu">
                <span class="tool-text"></span><span class="rule-actions"></span>
            </div>
        </div>
    </div>
    <div class="app-tab-content-container">
        <table class="content">
            <tr>
                <th><g:message code="rule.name"/></th>
                <th><g:message code="method"/></th>
                <g:if test="${classEnabled}"><th><g:message code="class"/></th></g:if>
                <th><g:message code="shipping.cost"/></th>
                <th><g:message code="handling.cost"/></th>
                <th><g:message code="zone"/></th>
                <th class="action-col"></th>
            </tr>
            <g:each in="${ruleList}" var="rule">
                <g:include view="admin/shipping/rule/ruleRow.gsp" model="${[rule: rule]}"/>
            </g:each>
        </table>
    </div>
    <div class="footer">
        <ui:perPageCountSelector/>
        <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
    </div>
</div>
