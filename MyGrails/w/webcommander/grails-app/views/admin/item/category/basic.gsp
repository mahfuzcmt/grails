<%@ page import="com.webcommander.util.StringUtil; com.webcommander.webcommerce.Category; com.webcommander.constants.DomainConstants; com.webcommander.admin.Customer; com.webcommander.admin.CustomerGroup" %>
<form action="${app.relativeBaseUrl()}categoryAdmin/saveBasic" method="post" class="create-edit-form category-basic-form" enctype="multipart/form-data">
    <input type="hidden" name="id" value="${category.id}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="category.information"/></h3>
            <div class="info-content"><g:message code="section.text.category.info"/> </div>
        </div>
        <div class="form-section-container">
            <div class="double-input-row">
                <div class="form-row mandatory">
                    <label><g:message code="category.name"/> <span class="suggestion"><g:message code="suggestion.category.name"/> </span></label>
                    <input type="text" name="name" class="medium unique" value="${category.name.encodeAsBMHTML()}" validation="required maxlength[255]" maxlength="255" composite-unique="parent">
                </div><div class="form-row mandatory">
                    <label><g:message code="sku"/><span class="suggestion"> (Stock Keeping Unit)</span> </label>
                    <input type="text" name="sku" class="medium unique" value="${category.sku.encodeAsBMHTML()}" validation="required">
                </div>
            </div>
            <div class="form-row trash-row" style="display: none;">
                <label><g:message code="what.to.do"/></label>
                <div>
                    <a onclick="return false" class="trash-duplicate-restore fake-link"><g:message code="restore"/></a> <g:message code="restore.and.close.window"/>
                    <br/>
                    <g:message code="or"/>
                    <br/>
                    <input type="checkbox" name="deleteTrashItem.name" class="trash-duplicate-delete single"> &nbsp;<g:message code="delete.and.save"/>
                </div>
            </div>
            <div class="double-input-row">
                <div class="form-row">
                    <label><g:message code="url.identifier"/><span class="suggestion"> e.g. my-category</span> </label>
                    <input type="text" name="url" class="medium unique" value="${category.url.encodeAsBMHTML()}" validation="maxlength[100]" maxlength="100">
                </div><div class="form-row">
                    <label><g:message code="title"/><span class="suggestion"> e.g. Detailed Name of Category</span> </label>
                    <input type="text" name="title" class="medium" value="${category.title.encodeAsBMHTML()}" validation="maxlength[200]" maxlength="200">
                </div>
            </div>
            <div class="form-row">
                <label><g:message code="heading"/><span class="suggestion"> Insert an additional heading for your Category Page</span> </label>
                <input type="text" name="heading" class="medium" value="${category.heading.encodeAsBMHTML()}" validation="maxlength[200]" maxlength="200">
            </div>

            <plugin:hookTag hookPoint="filterProfileSelectInCategoryBasic"/>

            <div class="double-input-row">
                <div class="form-row chosen-wrapper">
                    <label><g:message code="availability"/><span class="suggestion"> e.g. Available</span> </label>
                    <g:select name="isAvailable" class="medium" toggle-target="display-availability" from="[g.message(code: 'available'), g.message(code: 'not.available')]" keys="${[true, false]}" value="${category.isAvailable}"/>
                </div><div class="form-row chosen-wrapper">
                    <label><g:message code="parent.category"/><span class="suggestion"> Define a Parent Category for this Category</span> </label>
                    <ui:hierarchicalSelect name="parent" class="medium category-selector" prepend="${['': g.message(code: "none")]}" value="${category.parent?.id ?: parentCategory}" domain="${Category}" filter="${{ne("id", category.id?: 0.toLong())}}"/>
                </div>
            </div>
            <div class="display-availability-true">
                <div class="double-input-row">
                    <div class="form-row">
                        <input type="checkbox" class="single" name="isAvailableOnDateRange" toggle-target="datefield-between" ${category.isAvailableOnDateRange ? "checked='checked'" : ""}>
                        <span><g:message code="available.on.daterange"/></span>
                    </div><div class="form-row">
                        <input type="radio" name="availableFor"
                               value="${DomainConstants.CATEGORY_AVAILABLE_FOR.EVERYONE}" ${category.availableFor == DomainConstants.CATEGORY_AVAILABLE_FOR.EVERYONE ? "checked='checked'" : ""}>
                        <span class="inline-label"><g:message code="everyone"/></span>
                        <input type="radio" name="availableFor"
                               value="${DomainConstants.CATEGORY_AVAILABLE_FOR.CUSTOMER}" ${category.availableFor == DomainConstants.CATEGORY_AVAILABLE_FOR.CUSTOMER ? "checked='checked'" : ""}>
                        <span class="inline-label"><g:message code="customers"/></span>
                        <input type="radio" toggle-target="choose-customer" toggle-anim="slide" name="availableFor" value="${DomainConstants.CATEGORY_AVAILABLE_FOR.SELECTED}" ${category.availableFor == DomainConstants.CATEGORY_AVAILABLE_FOR.SELECTED ? "checked='checked'" : ""}>
                        <span class="inline-label"><g:message code="selected.customer"/></span>
                        <span class="select-customer">
                            <span class="tool-icon choose choose-customer"></span>
                        </span>
                    </div>
                </div>
                <div class="form-row datefield-between">
                    <g:set var="catdateid" value="${StringUtil.uuid}"/>
                    <input type="text" id="${catdateid}" class="datefield-from smaller" name="availableFromDate" validate-on="change"
                           value="${category.availableFromDate?.toDatePickerFormat(false, session.timezone)}"> &nbsp; &nbsp; - &nbsp;<input
                        type="text" class="datefield-to smaller" name="availableToDate" value="${category.availableToDate?.toDatePickerFormat(false, session.timezone)}" validate-on="change"
                        validation="skip@if{self::hidden} either_required[${catdateid}, <g:message code="from"/>, <g:message code="to"/>]" depends="#${catdateid}">
                </div>
            </div>
            <div class="form-row thicker-row">
                <label><g:message code="image"/></label>
                <div class="form-image-block">
                    <input type="file" name="image" file-type="image" remove-option-name="remove-image" ${category.image ? "" : "style='display: none'"} ${category.image ?
                            'remove-support="true"' : 'reset-support="true"'} size-limit="2097152" previewer="category-image-preview-${category.id}">
                    <div class="preview-image">
                        <img id="category-image-preview-${category.id}" src="${appResource.getCategoryImageURL(category: category, "150")}">
                    </div>
                </div>
            </div>
            <div class="form-row">
                <label><g:message code="summary"/><span class="suggestion"><g:message code="suggestion.category.edit.summary"/> </span> </label>
                <textarea class="xx-larger" name="summary" validation="maxlength[500]" maxlength="500">${category.summary}</textarea>
            </div>
            <div class="form-row">
                <label><g:message code="description"/><span class="suggestion"><g:message code="suggestion.category.edit.description"/></span> </label>
                <textarea class="wceditor no-auto-size xx-larger" name="description" toolbar-type="advanced" maxlength="2000">${category.description}</textarea>
            </div>
            <div class="${params.target == "create" ? "button-line" : "form-row"}">
                <g:if test="${params.target == "create"}">
                    <label>&nbsp</label>
                </g:if>
                <button type="submit" class="submit-button"><g:message code="${category.id ? "update" : "save"}"/></button>
                <g:if test="${params.target == "create"}">
                    <button type="button" class="cancel-button"><g:message code="cancel"/></button>
                </g:if>
            </div>
        </div>
    </div>
</form>