<form class="edit-popup-form" method="post">
    <div class="bmui-tab">
        <div class="bmui-tab-header-container top-side-header">
            <div class="bmui-tab-header" data-tabify-tab-id="billing">
                <span class="title"><g:message code="billing.address"/></span>
            </div>

            <div class="bmui-tab-header" data-tabify-tab-id="shipping">
                <span class="title"><g:message code="shipping.address"/></span>
            </div>
        </div>
        <div class="bmui-tab-body-container">
            <div id="bmui-tab-billing">
                <g:include controller="order" action="loadAddressEditor" params="${[section: "billing"]}"/>
            </div>

            <div id="bmui-tab-shipping">
                <g:include controller="order" action="loadAddressEditor" params="${[section: "shipping"]}"/>
            </div>
        </div>
    </div>

    <div class="button-line">
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
        &nbsp; &nbsp;
        <button type="button" class="edit-popup-form-submit submit-button update"><g:message code="update"/></button>
    </div>
</form>