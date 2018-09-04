<form action="${app.relativeBaseUrl()}dashboard/payment" class="create-edit-form">
    <div class="form-section billing-information-section">
        <div class="form-section-info">
            <h3><g:message code="billing.information"/></h3>
            <div class="info-content"><g:message code="section.text.billing.info"/></div>
        </div>
        <div class="form-section-container second-container">
            <div class="form-row mandatory">
                <label><g:message code="credit.card.number"/></label>
                <input type="text" name="cardNumber" validation="required cardnumber" autocomplete="off"/>
            </div>
            <div class="double-input-row">
                <div class="form-row mandatory">
                    <label><g:message code="expiration.date"/></label>
                    <div class="twice-input-row">
                        <input name="cardExpiryMonth" type="text" validation="required digits match[^\d{2}$]" message_template_2="Sample value 08" autocomplete="off" placeholder="<g:message code="mm"/>"/><span>-</span><input name="cardExpiryYear" type="text" validation="required digits match[^\d{2}$]" message_template_2="Sample value 12" autocomplete="off" placeholder="<g:message code="yy"/>"/>
                    </div>
                </div><div class="form-row mandatory">
                    <label><g:message code="cvv"/><span class="link suggestion"><g:message code="what.is.this"/></span></label>
                    <input type="text" name="cvv" validation="required cvv" autocomplete="off"/>
                </div>
            </div>
            <span class="billing-title"><g:message code="change.your.billing.address"/></span>
            <div class="double-input-row">
                <div class="form-row mandatory">
                    <label><g:message code="first.name"/></label>
                    <input type="text" name="billing.firstName" value="" validation="required maxlength[200]" maxlength="200"/>
                </div><div class="form-row">
                    <label><g:message code="last.name.surname"/></label>
                    <input type="text" name="billing.lastName" value="" validation="maxlength[200]" maxlength="200"/>
                </div>
            </div>
            <div class="form-row mandatory">
                <label><g:message code="street.address"/></label>
                <input type="text" name="billing.addressLine1" value="${storeDetail? storeDetail.address.addressLine1.encodeAsBMHTML() : ''}" validation="required maxlength[450]" maxlength="450"/>
            </div>
            <div class="double-input-row">
                <div class="form-row">
                    <label><g:message code="city"/></label>
                    <input type="text" name="billing.city" value="${storeDetail? storeDetail.address.city.encodeAsBMHTML() : ''}" validation="maxlength[50]" maxlength="50"/>
                </div><div class="form-row">
                    <label><g:message code="postal/zip.code"/></label>
                    <input type="text" name="billing.postCode" value="${storeDetail? storeDetail.address.postCode : ''}" validation="maxlength[50]"/>
                </div>
            </div>
            <div class="form-row country-selector-row chosen-wrapper">
                <label><g:message code="country"/><span class="suggestion"><g:message code="suggestion.setting.country"/></span></label>
                <ui:countryList id="countryId" name="billing.country" class="large" value="${storeDetail? storeDetail.address.country.id : ''}"/>
            </div>
            <g:if test="${storeDetail}">
                <g:include view="/admin/customer/stateFormFieldView.gsp" model="${[states : states, stateId: storeDetail.address.state?.id]}" params="[inputClass: 'medium', stateName: 'address.state']"/>
            </g:if>
        </div>
        <div class="third-container">
            <span class="billing-title"><g:message code="your.current.billing.address"/></span>
            <div class="address-details">
                %{--<span class="name">${billingAddress.firstName.encodeAsBMHTML() + " " + (billingAddress.lastName ?: '')}</span>
                <span class="address-line">${billingAddress.addressLine1.encodeAsBMHTML() + (billingAddress.addressLine2 ? (", " + billingAddress.addressLine2.encodeAsBMHTML()) : "")}</span>
                <span class="city">${billingAddress.city? billingAddress.city.encodeAsBMHTML() : ""}</span>
                <span class="state">${billingAddress.state?.code? ", " + billingAddress.state.code.encodeAsBMHTML() : ""}</span>
                <span class="post-code">${billingAddress.postCode? ", " + billingAddress.postCode : ""}</span>
                <span class="country">${billingAddress.country?.name.encodeAsBMHTML()}</span>--}%
            </div>
        </div>
    </div>

    <div class="form-section billing-cycle-section">
        <div class="form-section-info">
            <h3><g:message code="billing.cycle"/></h3>
            <div class="info-content"><g:message code="section.text.billing.cycle.info"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row">
                <input type="radio" name="billingCycle" selected value="once-month"/>
                <span><g:message code="bill.me.once.month"/></span><br>
                <input type="radio" name="billingCycle" value="once-year"/>
                <span><g:message code="bill.me.once.year"/></span><br>
                <input type="radio" name="billingCycle" value="two-year"/>
                <span><g:message code="bill.me.two.year"/></span><br>
                <input type="radio" name="billingCycle" value="three-year"/>
                <span><g:message code="bill.me.three.year"/></span>
            </div>
            <div class="form-row">
                <span><g:message code="billing.cycle.charged.message"/></span>
            </div>
            <div class="form-row">
                <button type="submit" class="submit-button"><g:message code="confirm.changes"/></button>
                <button class="cancel-button" type="button"><g:message code="cancel"/></button>
            </div>
        </div>
    </div>
</form>