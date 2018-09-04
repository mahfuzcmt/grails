<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.util.AppUtil" %>
<form class="create-edit-form" action="${app.relativeBaseUrl()}customerGroup/save" method="post">
    <input name="id" type="hidden" value="${group.id}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="customer.group" args="${[(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce") == 'true')?"Customer":"Member"]}"/></h3>
            <div class="info-content"><g:message code="section.text.create.customer.group" args="${[(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce") == 'true')?"Customer":"Member"]}"/></div>
        </div>
        <div class="form-section-container">
            <div class="double-input-row">
                <div class="mandatory form-row">
                    <label><g:message code="name"/><span class="suggestion">e.g. Group 1</span></label>
                    <input type="text" class="medium unique" name="name" validation="required rangelength[4,50]" value="${group.name.encodeAsBMHTML()}" maxlength="40">
                </div><div class="form-row">
                    <label><g:message code="description"/><span class="suggestion">e.g. Australian ${(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce") == 'true')?"Customer":"Member"} Group</span></label>
                    <input type="text" class="medium" validation="maxlength[255]" value="${group.description.encodeAsBMHTML()}" name="description" maxlength="255">
                </div>
            </div>
            <div class="double-input-row chosen-wrapper">
                <div class="form-row">
                    <label><g:message code="status"/><span class="suggestion">e.g. Active</span></label>
                    <g:select class="medium" from="['Active', 'Inactive']" keys="['A', 'I']" name="status" value="${group.status}"></g:select>
                </div>
                <div class="form-row chosen-wrapper">
                    <label><g:message code="tax.default.code"/><span class="suggestion"></span></label>
                    %{--<ui:domainSelect name="defaultTaxCode" class="medium tax-profile-selector" domain="${com.webcommander.webcommerce.TaxCode}" text="label" key="name" value="${group?.defaultTaxCode}" prepend="${['': g.message(code: "select.")]}" />--}%
                    <g:select name="defaultTaxCode" class="medium tax-profile-selector" from="${codes}" optionValue="label" optionKey="name" value="${group?.defaultTaxCode}" noSelection="['': g.message(code: 'none')]"/>
                </div>
            </div>
            <g:include view="/admin/customer/customerSelection.gsp" model="[customers : group.customers]"/>
            <div class="form-row">
                <button type="submit" class="submit-button edit-popup-form-submit"><g:message code="${ group.id ? "update" : "save"}"/></button>
                <button type="button" class="cancel-button"><g:message code="cancel"/></button>
            </div>
        </div>
    </div>
</form>