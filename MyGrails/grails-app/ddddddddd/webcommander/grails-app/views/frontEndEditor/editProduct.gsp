<%@ page import="com.webcommander.content.Navigation;com.webcommander.webcommerce.Category; com.webcommander.design.Layout; com.webcommander.constants.DomainConstants" %>
<div class="fee-form-content">
    <form action="${app.relativeBaseUrl()}frontEndEditor/saveProduct" method="post" enctype="multipart/form-data" class="create-edit-form">
        <div class="fee-padding-30 fee-body">
            <div class="fee-row">
                <div class="fee-col fee-col-50 form-row mandatory">
                    <label for="productName"><g:message code="product.name"/><span class="suggestion"><g:message code="suggestion.product.name"/></span></label>
                    <input name="name" id="productName" type="text" class="form-full-width" value="${product.name.encodeAsBMHTML()}" validation="required rangelength[2, 100]" maxlength="100">
                </div>
                <div class="fee-col fee-col-50 form-row mandatory">
                    <label for="productSku"><g:message code="sku"/><span class="suggestion">(Stock Keeping Unit)</span></label>
                    <input name="sku" id="productSku" type="text" class="form-full-width unique" value="${product.sku.encodeAsBMHTML()}" validation="required maxlength[40]" maxlength="40">
                </div>
            </div>
            <div class="fee-row">
                <div class="fee-col fee-col-50 form-row">
                    <label for="productHeading"><g:message code="heading"/><span class="suggestion"><g:message code="insert.an.additional.heading.for.your.product.page"/></span></label>
                    <input name="heading" id="productHeading" type="text" class="form-full-width" value="${product.heading.encodeAsBMHTML()}" validation="maxlength[200]" maxlength="200">
                </div>
                <div class="fee-col fee-col-50 form-row chosen-wrapper">
                    <label for="productCategories"><g:message code="parents"/><span class="suggestion"><g:message code="define.parent.for.product"/></span></label>
                    <ui:hierarchicalSelect name="categories" id="productCategories" class="form-full-width parents-selector special-select-chosen always-bottom" domain="${Category}" custom-attrs="${[multiple: 'true', 'chosen-highlighted': product.parent?.id ?: parentCategory, 'data-placeholder': g.message(code: "select.categories"), 'chosen-hiddenfieldname': "parent"]}" values="${product.parents.id ?: parentCategory}"/>
                </div>
            </div>
            <div class="fee-row">
                <div class="fee-col fee-col-50 form-row fixed-price-specify mandatory">
                    <label for="basePrice"><g:message code="base.price"/><span class="suggestion"><g:message code="suggestion.product.base.price"/></span></label>
                    <input name="basePrice" id="basePrice" restrict="decimal" type="text" class="form-full-width" value="${product.basePrice?.toAdminPrice()}" maxlength="16" validation="required@if{self::visible} price number maxlength[16]">
                </div>
                <div class="fee-col fee-col-50 form-row fixed-price-specify mandatory">
                    <label for="costPrice"><g:message code="cost.price"/><span class="suggestion"><g:message code="suggestion.product.cost.price"/></span></label>
                    <input name="costPrice" id="costPrice" restrict="decimal" type="text" class="form-full-width" value="${product.costPrice?.toAdminPrice()}" maxlength="16" validation="number maxlength[16] price">
                </div>
            </div>
            <div class="form-row thicker-row">
                <label><g:message code="image"/></label>
                <div class="form-image-block">
                    <input type="file" name="images" file-type="image" queue="product-image-queue-${product.id}" multiple="true" style="display: none;">
                    <div id="product-image-queue-${product.id}" class="multiple-image-queue"></div>
                </div>
            </div>
            <div class="form-row">
                <label for="summary"><g:message code="product.summary"/><span class="suggestion"><g:message code="suggestion.product.summary"/></span></label>
                <textarea class="form-full-width" name="summary" id="summary" maxlength="500" validation="maxlength[500]">${product.summary}</textarea>
            </div>
            <div class="form-row">
                <label for="productDescription"><g:message code="product.description"/><span class="suggestion"><g:message code="suggestion.product.detials"/></span></label>
                <textarea class="wceditor no-auto-size form-full-width" toolbar-type="advanced" name="description" id="productDescription" maxlength="65535" validation="maxlength[65535]">${product.description}</textarea>
            </div>
        </div>
        <div class="fee-button-wrapper fee-config-footer">
            <button type="submit" class="submit-button edit-popup-form-submit"><g:message code="${product.id ? "update" : "save"}"/></button>
            <button type="button" class="cancel-button fee-common"><g:message code="cancel"/></button>
        </div>
    </form>
</div>