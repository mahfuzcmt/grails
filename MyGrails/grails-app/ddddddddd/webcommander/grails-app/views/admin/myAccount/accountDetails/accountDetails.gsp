<div class="toolbar-share"></div>
<div class="app-tab-content-container">
    %{--</license:active>--}%
    <div class="info-section">
        <div class="info-section-info">
            <h3><g:message code="account.details"/></h3>
            <div class="info-content"><g:message code="section.text.my.account.info"/></div>
        </div>
        <div class="info-section-container account-info section" section="info">
            <g:include controller="myAccount" action="accountInfo"/>
        </div>
    </div>
    <div class="section-separator"></div>
    <div class="info-section">
        <div class="info-section-info">
            <h3><g:message code="billing.address"/></h3>
            <div class="info-content"><g:message code="section.text.my.account.billing.address"/></div>
        </div>
        <div class="info-section-container section billing-address" section="billing-address">
            %{--TODO: --}%
            Need API
        </div>
    </div>
    <div class="section-separator"></div>
    <div class="info-section">
        <div class="info-section-info">
            <h3><g:message code="website.details"/></h3>
            <div class="info-content"><g:message code="section.text.my.account.website.details"/></div>
        </div>
        <div class="info-section-container section website-details" section="website-details">
            <g:include controller="myAccount" action="websiteDetails"/>
        </div>
    </div>
    %{--</license:active>--}%
    <license:inactive>
        <div class="error-message">Provisioning is not activated</div>
    </license:inactive>
</div>

