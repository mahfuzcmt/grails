<div class="header multi-tab-shared-header">
    <span class="header-title"></span>
    <div class="toolbar toolbar-right">
        <span class="tool-group toolbar-btn save save-all"><g:message code="save.all"/></span>
        <span class="tool-group toolbar-btn cancel"><g:message code="cancel"/></span>
    </div>
</div>
<div class="bmui-tab left-side-header">
    <div class="bmui-tab-header-container">
        <div class="bmui-tab-header" data-tabify-tab-id="basic" data-tabify-url="${app.relativeBaseUrl()}ebayListingAdmin/loadProfileProperties?profileId=${profileId}&property=basic">
            <span class="title"><g:message code="basic"/></span>
        </div>
        <div class="bmui-tab-header" data-tabify-tab-id="pricing" data-tabify-url="${app.relativeBaseUrl()}ebayListingAdmin/loadProfileProperties?profileId=${profileId}&property=pricing">
            <span class="title"><g:message code="pricing"/></span>
        </div>
        <div class="bmui-tab-header" data-tabify-tab-id="paymentMethod" data-tabify-url="${app.relativeBaseUrl()}ebayListingAdmin/loadProfileProperties?profileId=${profileId}&property=payment-method">
            <span class="title"><g:message code="payment.method"/></span>
        </div>
        <div class="bmui-tab-header" data-tabify-tab-id="postage" data-tabify-url="${app.relativeBaseUrl()}ebayListingAdmin/loadProfileProperties?profileId=${profileId}&property=postage">
            <span class="title"><g:message code="postage"/></span>
        </div>
        <div class="bmui-tab-header" data-tabify-tab-id="returnPolicy" data-tabify-url="${app.relativeBaseUrl()}ebayListingAdmin/loadProfileProperties?profileId=${profileId}&property=return-policy">
            <span class="title"><g:message code="return.policy"/></span>
        </div>
        <div class="bmui-tab-header" data-tabify-tab-id="setting" data-tabify-url="${app.relativeBaseUrl()}ebayListingAdmin/loadProfileProperties?profileId=${profileId}&property=setting">
            <span class="title"><g:message code="settings"/></span>
        </div>
    </div>
    <div class="bmui-tab-body-container">
        <div id="bmui-tab-basic"></div>
        <div id="bmui-tab-pricing"></div>
        <div id="bmui-tab-paymentMethod"></div>
        <div id="bmui-tab-postage"></div>
        <div id="bmui-tab-returnPolicy"></div>
        <div id="bmui-tab-setting"></div>
    </div>
</div>