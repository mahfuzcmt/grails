<%@ page import="com.webcommander.webcommerce.ShippingClass" %>
<div class="left-panel">
    <span class="item-group entity-count title">
        <g:message code="profile"/> (<span class="count">${profiles?.size()}</span>)
    </span>

    <div class=left-panel-header>
        <div class="toolbar">
            <g:select name="shippingProfile" class="shipping-profile-selector root-selector medium" from="${profiles}" optionValue="name" optionKey="id" value="${profile?.id}"/>
            <div class="tool-group toolbar-btn create-profile"><i></i><g:message code="new"/></div>
        </div>
    </div>

    <div class="body explorer-items blocktype-list sortable-container">
        <g:if test="${classEnabled}">
            <div class="toolbar">
                <ui:domainSelect domain="${ShippingClass}" name="shippingClass" class="medium class-filter-selector" prepend="${['': g.message(code: 'filter.by.class')]}"  value="${selectedClassId}"/>
            </div>
        </g:if>
        <g:each in="${profile?.shippingRules}" var="shippingRule">
            <g:if test="${!classEnabled || selectedClassId == null || shippingRule.shippingClass?.id == selectedClassId}">
                <div class="explorer-item blocklist-item ${shippingRule.id == selectedRuleId ? "selected" : ""}" entity-id="${shippingRule.id}" entity-name="${shippingRule.name}">
                    <span class="float-menu-navigator"></span>
                    <span class="title listitem-title">${shippingRule.name.encodeAsBMHTML()}</span>
                    <span class="listitem-count blocklist-subitem-summary-view">${shippingRule.description.encodeAsBMHTML()}</span>
                </div>
            </g:if>
        </g:each>
    </div>

    <div class="navigation-buttons">
        <div class="navigation-button rule-button" item-type="rule">
            <span class="icon"></span><span class="button-text"><g:message code="rule"/></span>
        </div>
        <div class="navigation-button zone-button" item-type="zone">
            <span class="icon"></span><span class="button-text"><g:message code="zone"/></span>
        </div>
        <div class="navigation-button rate-button" item-type="rate">
            <span class="icon"></span><span class="button-text"><g:message code="rate"/></span>
        </div>
    </div>
</div>


