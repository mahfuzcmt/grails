<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.util.AppUtil" %>
<form action="${app.relativeBaseUrl()}abandonedCartAdmin/loadAppView" class="edit-popup-form create-edit-form">
    <div class="double-input-row">
        <div class="form-row">
            <label><g:message code="customer.name" args="${[(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce") == 'true')?"Customer":"Member"]}"/></label>
            <input type="text" class="large" name="name" value="${params.searchText}"/>
        </div><div class="form-row">
        <label><g:message code="notification.status"/></label>
        <g:select class="large" name="notificationStatus" from='${[ g.message(code: "any"), g.message(code: "pending"), g.message(code: "sent"), g.message(code: "disabled")]}' keys="['', 'pending', 'sent', 'disabled']"/>
    </div>
    </div>
    <div class="double-input-row">
        <div class="form-row chosen-wrapper">
            <label><g:message code="email"/></label>
            <input type="text" class="large" name="email"/>
        </div><div class="form-row chosen-wrapper">
            <label><g:message code="no.of.notification.sent"/></label>
            <input type="text" class="large" name="notificationSentCount"/>
        </div>
    </div>
    <div class="form-row datefield-between">
        <label><g:message code="created.between"/></label>
        <input type="text" class="datefield-from smaller" name="createdFrom"><span class="date-field-separator">-</span><input type="text" class="datefield-to smaller" name="createdTo"/>
    </div>
    <div class="button-line">
        <button type="submit" class="submit-button"><g:message code="search"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>