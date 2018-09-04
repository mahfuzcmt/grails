<%@ page import="com.webcommander.constants.DomainConstants" %>
<div class="profile-editor-panel">
    <div class="edit-popup-form">
        <div class="app-tab-content-container">
            <div class="header">
                <div class="left-header">
                    <span class="title"><g:message code="rules"/></span>
                </div>
                <div class="toolbar toolbar-right">
                    <form class="search-form tool-group">
                        <input type="text" class="search-text" placeholder="<g:message code="search"/>"><button type="submit" class="icon-search"></button>
                    </form>
                </div>
            </div>
            <g:include view="admin/tax/new/rule/ruleTable.gsp" model="[showSelectCol: true, hideActionCol: true]"/>
        </div>
        <div class="footer">
            <ui:perPageCountSelector prepand="${["5": "5"]}"/>
            <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
        </div>
        <div class="button-line">
            <button type="submit" class="submit-button next"><g:message code="next"/></button>
            <button type="button" class="cancel-button"><g:message code="cancel"/></button>
        </div>
    </div>
</div>