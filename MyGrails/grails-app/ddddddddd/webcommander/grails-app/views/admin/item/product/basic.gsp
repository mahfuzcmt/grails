<%@  page import="com.webcommander.manager.HookManager; com.webcommander.constants.NamedConstants; com.webcommander.util.StringUtil; com.webcommander.webcommerce.Category; com.webcommander.constants.DomainConstants" %>
<form action="${app.relativeBaseUrl()}productAdmin/saveBasicProperties" method="post" class="create-edit-form product-basic-create-edit">
    <input type="hidden" name="id" value="${product.id}">
    <g:set var="isCombined" value="${product.id ? product.isCombined : params.isCombined.toBoolean()}"/>
    <input type="hidden" name="isCombined" value="${isCombined}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="product.info"/></h3>
            <div class="info-content"><g:message code="section.text.product.info"/></div>
        </div>
        <div class="form-section-container">
            <div class="double-input-row">
                <div class="form-row mandatory">
                    <label><g:message code="product.name"/><span class="suggestion"><g:message code="suggestion.product.name"/> </span></label>
                    <input name="name" type="text" class="form-full-width" value="${product.name.encodeAsBMHTML()}" validation="required rangelength[2, 100]" maxlength="100">
                </div><div class="form-row mandatory">
                    <label><g:message code="sku"/><span class="suggestion">  (Stock Keeping Unit)</span></label>
                    <input name="sku" type="text" class="form-full-width unique" value="${product.sku.encodeAsBMHTML()}" validation="required maxlength[40]" maxlength="40">
                </div>
            </div>
            <div class="double-input-row">
                <div class="form-row">
                    <label><g:message code="title"/>  <span class="suggestion"><g:message code="suggestion.product.title"/> </span></label>
                    <input name="title" type="text" class="form-full-width" value="${product.title.encodeAsBMHTML()}" validation="maxlength[200]" maxlength="200">
                </div><div class="form-row half">
                    <label><g:message code="url.identifier"/><span class="suggestion">  e.g. product-name</span></label>
                    <input name="url" type="text" class="form-full-width unique" value="${product.url.encodeAsBMHTML()}" validation="maxlength[100] url_folder" maxlength="100">
                </div>
            </div>
            <div class="double-input-row">
                <div class="form-row half">
                    <label><g:message code="heading"/><span class="suggestion"> <g:message code="insert.an.additional.heading.for.your.product.page"/></span></label>
                    <input name="heading" type="text" class="form-full-width" value="${product.heading.encodeAsBMHTML()}" validation="maxlength[200]" maxlength="200">
                </div><div class="form-row half chosen-wrapper">
                    <label><g:message code="parents"/><span class="suggestion"><g:message code="define.parent.for.product"/></span></label>
                    <ui:hierarchicalSelect name="categories" class="form-full-width parents-selector special-select-chosen" domain="${Category}" custom-attrs="${[multiple: 'true', 'chosen-highlighted': product.parent?.id ?: parentCategory, 'data-placeholder': g.message(code: "select.categories"), 'chosen-hiddenfieldname': "parent"]}" values="${product.parents.id ?: parentCategory}"/>
                </div>
            </div>
            <div class="form-row trash-row" style="display: none;">
                <label><g:message code="what.to.do"/></label>
                <div>
                    <a onclick="return false" class="trash-duplicate-restore fake-link"><g:message code="restore"/></a> <g:message code="restore.and.close.window"/>
                    <br/>
                    <g:message code="or"/>
                    <br/>
                    <input type="checkbox" name="deleteTrashItem.sku" class="trash-duplicate-delete single"> &nbsp;<g:message code="delete.and.save"/>
                </div>
            </div>
            <g:if test="${!product.id}">
                <div class="form-row chosen-wrapper">
                    <label><g:message code="product.type"/></label>
                    <ui:namedSelect key="${NamedConstants.PRODUCT_TYPE}" name="productType" class="form-full-width" toggle-target="hide-if-type"/>
                </div>
            </g:if>
            <div class="form-row chosen-wrapper">
                <label><g:message code="availability"/></label>
                <g:select name="isAvailable" class="form-full-width" toggle-target="display-availability" from="[g.message(code: 'available'), g.message(code: 'not.available')]" keys="${[true, false]}" value="${product.isAvailable}"/>
            </div>
            <div class="display-availability-true">
                <div class="double-input-row">
                    <div class="form-row half">
                        <input type="checkbox" class="single" name="isAvailableOnDateRange" toggle-target="datefield-between" ${product.isAvailableOnDateRange ? "checked='checked'" : ""}>
                        <span><g:message code="available.on.daterange"/></span>
                    </div><div class="form-row half">
                        <label><g:message code="visibility"/><span class="suggestion"> Who can see this product?</span></label>
                        <span class="radio-wrapper">
                            <input type="radio" name="availableFor" value="${DomainConstants.PRODUCT_AVAILABLE_FOR.EVERYONE}" ${product.availableFor == DomainConstants.PRODUCT_AVAILABLE_FOR.EVERYONE ? "checked='checked'" : ""}>
                            <span class="value"><g:message code="everyone"/></span>
                        </span>
                        <span class="radio-wrapper">
                            <input type="radio" name="availableFor" value="${DomainConstants.PRODUCT_AVAILABLE_FOR.CUSTOMER}" ${product.availableFor == DomainConstants.PRODUCT_AVAILABLE_FOR.CUSTOMER ? "checked='checked'" : ""}>
                            <span class="value"><g:message code="customers"/></span>
                        </span>
                        <span class="radio-wrapper">
                            <input type="radio" toggle-target="select-customer" toggle-anim="slide" name="availableFor" value="${DomainConstants.PRODUCT_AVAILABLE_FOR.SELECTED}" ${product.availableFor == DomainConstants.PRODUCT_AVAILABLE_FOR.SELECTED ? "checked='checked'" : ""}>
                            <span class="selected-customer">
                                <span class="value"><g:message code="selected.customer"/></span>
                                <span class="select-customer">
                                    <span class="tool-icon choose choose-customer"></span>
                                </span>
                            </span>
                            <g:each in="${product.availableToCustomers}" var="customer">
                                <input type="hidden" name="customer" value="${customer.id}">
                            </g:each>
                            <g:each in="${product.availableToCustomerGroups}" var="customerGroup">
                                <input type="hidden" name="customerGroup" value="${customerGroup.id}">
                            </g:each>
                        </span>

                    </div>
                </div>
                <div class="form-row datefield-between">
                    <g:set var="prodateid" value="${StringUtil.uuid}"/>
                    <input type="text" id="${prodateid}" class="datefield-from smaller" name="availableFromDate" validate-on="change" value="${product.availableFromDate?.toDatePickerFormat(false, session.timezone)}"><span class="date-field-separator">-</span><input type="text" class="datefield-to smaller" validate-on="change" name="availableToDate" value="${product.availableToDate?.toDatePickerFormat(false, session.timezone)}" validation="skip@if{self::hidden} either_required[${prodateid}, <g:message code="from"/>, <g:message code="to"/>]" depends="#${prodateid}">
                </div>
            </div>
            <g:if test="${isCombined}">
                <div class="form-row">
                    <input type="checkbox" class="single" name="isCombinationPriceFixed" value="true" ${product.isCombinationPriceFixed ? "checked" : ""} toggle-target="fixed-price-specify">
                    <span><g:message code="specify.fixed.price"/></span>
                </div>
                <div class="hide-if-type-downloadable" do-reverse-toggle>
                    <div class="form-row fixed-price-specify" do-reverse-toggle>
                        <label>&nbsp;</label>
                        <input type="checkbox" class="single" name="isCombinationQuantityFlexible" value="true" ${product.isCombinationQuantityFlexible ? "checked" : ""}>
                        <span><g:message code="customer.may.specify.quantity"/></span>
                    </div>
                </div>
            </g:if>
            <div class="double-input-row">
                <div class="form-row mandatory fixed-price-specify half">
                    <label><g:message code="base.price"/><span class="suggestion"><g:message code="suggestion.product.base.price"/></span></label>
                    <input name="basePrice" restrict="decimal" type="text" class="form-full-width" value="${product.basePrice?.toAdminPrice()}" maxlength="16" validation="required@if{self::visible} price number maxlength[16]">
                </div><div class="form-row fixed-price-specify half">
                    <label><g:message code="cost.price"/><span class="suggestion"><g:message code="suggestion.product.cost.price"/></span></label>
                    <input name="costPrice" restrict="decimal" type="text" class="form-full-width" value="${product.costPrice?.toAdminPrice()}" maxlength="16" validation="number maxlength[16] price">
                </div>
            </div>
            <div class="form-row chosen-wrapper">
                <label><g:message code="administrative.status"/><span class="suggestion">e.g. Active</span></label>
                <g:select name="active" from="${['active', 'inactive'].collect {g.message(code: it)}}" keys="${['true', 'false']}" class="medium" value="${product.isActive}"/>
            </div>
            <div class="form-row">
                <label><g:message code="product.summary"/><span class="suggestion"><g:message code="suggestion.product.summary"/></span></label>
                <textarea class="form-full-width" name="summary" maxlength="500" validation="maxlength[500]">${product.summary}</textarea>
            </div>
            <div class="form-row">
                <label><g:message code="product.description"/><span class="suggestion"> <g:message code="suggestion.product.detials"/></span></label>
                <textarea class="wceditor no-auto-size form-full-width" toolbar-type="advanced" name="description" maxlength="65535" validation="maxlength[65535]">${product.description}</textarea>
            </div>
            <div class="form-row">
                <label>&nbsp</label>
                <button type="submit" class="submit-button"><g:message code="${product.id ? "update" : "save"}"/></button>
                <g:if test="${params.target == "create"}">
                    <button type="button" class="cancel-button"><g:message code="cancel"/></button>
                </g:if>
            </div>
        </div>
    </div>
</form>