<%@ page import="com.webcommander.constants.NamedConstants" %>
<div class="right-panel table-view">
    <div class="header">
        <div class="left-header">
            <h3 class="title"><g:message code="rules"/></h3>
            <p class="description"><g:message code="tax.rules.description"/></p>
        </div>
        <div class="toolbar toolbar-right">
            <form class="search-form tool-group">
                <input type="text" class="search-text" placeholder="<g:message code="search"/>"><button type="submit" class="icon-search"></button>
            </form>
        </div>
    </div>
    <div class="app-tab-content-container">
        <g:include view="admin/tax/new/rule/ruleTable.gsp"/>
    </div>
    <div class="footer">
        <ui:perPageCountSelector/>
        <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
    </div>
</div>
