<%@ page import="com.webcommander.plugin.event_management.EquipmentType" %>
<form action="${app.relativeBaseUrl()}eventAdmin/loadEquipmentView" method="post" class="edit-popup-form">
    <div class="form-row">
        <label><g:message code="name"/></label>
        <input type="text" name="name" class="large" value="${params.searchText ?: ""}"/>
    </div>
    <div class="form-row">
        <label><g:message code="equipment.type"/></label>
        <ui:domainSelect domain="${EquipmentType}" class="large" name="type" text="name" prepend="${["":g.message(code: "any.type")]}"/>
    </div>
    <div class="button-line">
        <button type="submit" class="submit-button"><g:message code="search"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>