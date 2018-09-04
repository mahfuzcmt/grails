<%@ page import="com.webcommander.constants.DomainConstants" %>
<div class="header multi-tab-shared-header">
    <span class="header-title"></span>
    <div class="toolbar toolbar-right">
        <span class="tool-group toolbar-btn save save-all"><g:message code="save.all"/></span>
        <span class="tool-group toolbar-btn cancel"><g:message code="cancel"/></span>
    </div>
</div>
<div class="bmui-tab left-side-header">
    <div class="bmui-tab-header-container">
        <div class="bmui-tab-header" data-tabify-tab-id="basic" data-tabify-url="${app.relativeBaseUrl()}productAdmin/loadProductBulkProperties?ids=${productIds}&property=basic">
            <span class="title"><g:message code="basic"/></span>
        </div>
        <div class="bmui-tab-header" data-tabify-tab-id="priceStock" data-tabify-url="${app.relativeBaseUrl()}productAdmin/loadProductBulkProperties?ids=${productIds}&property=price-stock">
            <span class="title"><g:message code="price.stock"/></span>
        </div>
        <div class="bmui-tab-header" data-tabify-tab-id="advanced" data-tabify-url="${app.relativeBaseUrl()}productAdmin/loadProductBulkProperties?ids=${productIds}&property=advanced">
            <span class="title"><g:message code="advanced"/></span>
        </div>
        <div class="bmui-tab-header" data-tabify-tab-id="webtool" data-tabify-url="${app.relativeBaseUrl()}productAdmin/loadProductBulkProperties?ids=${productIds}&property=webtool">
            <span class="title"><g:message code="webtool"/></span>
        </div>
    </div>
    <div class="bmui-tab-body-container">
        <div id="bmui-tab-basic">
        </div>
        <div id="bmui-tab-priceStock">
        </div>
        <div id="bmui-tab-advanced">
        </div>
        <div id="bmui-tab-webtool">
        </div>
    </div>
</div>