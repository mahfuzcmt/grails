<%@ page import="com.webcommander.util.AppUtil; com.webcommander.constants.DomainConstants" %>
<div class="header multi-tab-shared-header">
    <span class="item-group entity-count title">
        <g:message code="${(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce") == 'true')?"customers":"members"}"/> (<span class="count">${customerIds.size()}</span>)
    </span>
    <div class="toolbar toolbar-right">
        <span class="tool-group toolbar-btn save save-all"><g:message code="save.all"/></span>
        <span class="tool-group toolbar-btn cancel"><g:message code="cancel"/></span>
    </div>
</div>
<div class="bmui-tab header">
    <div class="bmui-tab-header-container" style="display: none;">
        <div class="bmui-tab-header" data-tabify-tab-id="basic" data-tabify-url="${app.relativeBaseUrl()}customerAdmin/loadCustomerBulkProperties?ids=${customerIds}&property=basic">
            <span class="title"><g:message code="basic"/></span>
        </div>
    </div>
    <div class="bmui-tab-body-container">
        <div id="bmui-tab-basic">
        </div>
    </div>
</div>