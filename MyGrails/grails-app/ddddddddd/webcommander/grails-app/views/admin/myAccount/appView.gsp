<div class="header multi-tab-shared-header">
    <span class="header-title"></span>
</div>
<div class="bmui-tab left-side-header">
    <div class="bmui-tab-header-container">
        <div class="bmui-tab-header" data-tabify-tab-id="manage" data-tabify-url="${app.relativeBaseUrl()}myAccount/loadManage">
            <span class="title"><g:message code="manage"/></span>
        </div>
        <div class="bmui-tab-header" data-tabify-tab-id="accountDetails" data-tabify-url="${app.relativeBaseUrl()}myAccount/accountDetails">
            <span class="title"><g:message code="account.details"/></span>
        </div>
        <div class="bmui-tab-header" data-tabify-tab-id="paymentDetails" data-tabify-url="${app.relativeBaseUrl()}myAccount/loadPaymentDetails">
            <span class="title"><g:message code="payment.details"/></span>
        </div>
        <div class="bmui-tab-header" data-tabify-tab-id="purchaseHistory" data-tabify-url="${app.relativeBaseUrl()}myAccount/loadPurchaseHistory">
            <span class="title"><g:message code="purchased.history"/></span>
        </div>
        <div class="bmui-tab-header" data-tabify-tab-id="invoice" data-tabify-url="${app.relativeBaseUrl()}myAccount/loadInvoice">
            <span class="title"><g:message code="invoice"/></span>
        </div>
        <div class="bmui-tab-header" data-tabify-tab-id="subscription" data-tabify-url="${app.relativeBaseUrl()}myAccount/loadSubscription">
            <span class="title"><g:message code="subscription"/></span>
        </div>
        <div class="bmui-tab-header" data-tabify-tab-id="customerSupport" data-tabify-url="${app.relativeBaseUrl()}myAccount/loadCustomerSupport">
            <span class="title"><g:message code="customer.support"/></span>
        </div>
        <div class="bmui-tab-header" data-tabify-tab-id="customProject" data-tabify-url="${app.relativeBaseUrl()}myAccount/loadCustomProject">
            <span class="title"><g:message code="custom.project"/></span>
        </div>
    </div><div class="bmui-tab-body-container">
        <div id="bmui-tab-accountDetails"></div>
        <div id="bmui-tab-paymentDetails"></div>
        <div id="bmui-tab-purchaseHistory"></div>
        <div id="bmui-tab-invoice"></div>
        <div id="bmui-tab-subscription" class="subscription-tab"></div>
        <div id="bmui-tab-customerSupport" class="customerSupport-tab"></div>
        <div id="bmui-tab-customProject" class="customProject-tab"></div>
    </div>
</div>
