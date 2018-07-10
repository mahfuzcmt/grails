<%@ page import="com.webcommander.constants.DomainConstants" %>
<div class="profile-editor-panel">
    <div class="edit-popup-form">
        <div class="app-tab-content-container">
            <div class="header">
                <div class="left-header">
                    <span class="title"><g:message code="rules"/></span>
                </div>
                <div class="toolbar toolbar-right">
                    <g:if test="${classEnabled}">
                        <div class="filter-select tool-group">
                            <g:select noSelection="${['':"${g.message(code: 'all')}"]}" class="medium" name="shippingClass" from="${classList}" optionKey="id" optionValue="name"/>
                        </div>
                    </g:if>
                    <div class="filter-select tool-group">
                        <ui:namedSelect class="medium" name="policyType" key="${methodKeys}"/>
                    </div>
                    <form class="search-form tool-group">
                        <input type="text" class="search-text" placeholder="<g:message code="search"/>"><button type="submit" class="icon-search"></button>
                    </form>
                </div>
            </div>
            <table class="content">
                <tr>
                    <th class="select-column"></th>
                    <th><g:message code="rule.name"/></th>
                    <th><g:message code="method"/></th>
                    <g:if test="${classEnabled}"><th><g:message code="shipping.class"/></th></g:if>
                    <th><g:message code="shipping.cost"/></th>
                    <th><g:message code="handling.cost"/></th>
                </tr>
                <g:if test="${ruleList}">
                    <g:each in="${ruleList}" var="rule">
                        <g:include view="admin/shipping/rule/ruleRow.gsp" model="[rule: rule, selectRulePopup: true, radio: true]"/>
                    </g:each>
                </g:if>
                <g:else>
                    <tr class="table-no-entry-row">
                        <td colspan="6"><g:message code="no.rule.created"/></td>
                    </tr>
                </g:else>
            </table>
        </div>

        <div class="button-line">

            <div class="footer">
                <ui:perPageCountSelector prepand="${["5": "5"]}"/>
                <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
            </div>

            <button type="submit" class="submit-button next"><g:message code="next"/></button>
            <button type="button" class="cancel-button"><g:message code="cancel"/></button>
        </div>
    </div>
</div>