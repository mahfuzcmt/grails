<div class="left-panel">
    <span class="item-group entity-count title">
        <g:message code="profile"/> (<span class="count">${profiles?.size()}</span>)
    </span>

    <div class=left-panel-header>
        <div class="toolbar">
            <g:select name="shippingProfile" class="tax-profile-selector root-selector medium" from="${profiles}" optionValue="name" optionKey="id" value="${profile?.id}"/>
            <div class="tool-group toolbar-btn create-profile"><i></i><g:message code="new"/></div>
        </div>
    </div>

    <div class="body explorer-items blocktype-list sortable-container">
        <g:each in="${profile?.rules}" var="rule">
            <div class="explorer-item blocklist-item ${rule.id == selectedRuleId ? "selected" : ""}" entity-id="${rule.id}" entity-name="${rule.name}">
                <span class="float-menu-navigator"></span>
                <span class="title listitem-title">${rule.name.encodeAsBMHTML()}</span>
                <span class="listitem-count blocklist-subitem-summary-view">${rule.description.encodeAsBMHTML()}</span>
            </div>
        </g:each>
    </div>

    <div class="navigation-buttons">
        <div class="navigation-button rule-button" item-type="rule">
            <span class="icon"></span><span class="button-text"><g:message code="rule"/></span>
        </div>
        <div class="navigation-button zone-button" item-type="zone">
            <span class="icon"></span><span class="button-text"><g:message code="zone"/></span>
        </div>
        <div class="navigation-button code-button" item-type="code">
            <span class="icon"></span><span class="button-text"><g:message code="code"/></span>
        </div>
    </div>
</div>


