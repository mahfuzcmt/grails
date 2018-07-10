<div class="header multi-tab-shared-header">
    <span class="item-group entity-count title"></span>
    <span class="header-title"></span>
    <div class="toolbar toolbar-right">
        <span class="tool-group toolbar-btn create-rule-top">+ <g:message code="rule"/></span>
        <span class="tool-group toolbar-btn create-rate-top hidden">+ <g:message code="rate"/></span>
    </div>
</div>
<div class="app-tab-content-container two-panel-explorer">
    <g:include controller="shippingAdmin" action="loadLeftPanel"/>
    <g:include controller="shippingAdmin" action="explorerView"/>
</div>