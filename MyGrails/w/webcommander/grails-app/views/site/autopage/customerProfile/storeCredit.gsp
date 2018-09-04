<%@ page import="com.webcommander.util.AppUtil" %>
<span class="store-credit-info info"><g:message code="your.current.store.credit.amount" args="${[AppUtil.baseCurrency.symbol, storeCredit]}"/></span>
<g:form class="store-credit-request-form" controller="customer" action="sendStoreCreditRequest">
    <div class="form-row mandatory">
        <label><g:message code="message"/>:</label>
        <textarea name="msg" class="msg" validation="required maxlength[65553]" maxlength="65553"
                  placeholder="${g.message([code: "add.message.to.request.store.credit"])}"></textarea>
    </div>
    <div class="form-row">
        <button type="submit" class="submit-button"><g:message code="send"/></button>
    </div>
</g:form>
