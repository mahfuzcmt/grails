<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.constants.NamedConstants" %>
<div class="toolbar-share">
    <span class="header-title"><g:message code="web.commerce"/> > <g:message code="product"/> > ${product.name.encodeAsBMHTML()} > <g:message code="variation"/> > ${details.name ?: product.name.encodeAsBMHTML() + "_" + details.id} > <g:message code="advance"/> </span>
</div>
<form action="${app.relativeBaseUrl()}enterpriseVariation/saveProperties" method="post" class="create-edit-form enterprise-product-advance-form">
    <input type="hidden" name="id" value="${details.id}">
    <input type="hidden" name="type" value="advanced">
    <input type="hidden" name="variationId" value="${variationId}">
    <plugin:hookTag hookPoint="productAdvancedInformation" attrs="${[entityType: "variation"]}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="advance.information"/></h3>
            <div class="info-content"><g:message code="section.text.product.advance"/></div>
        </div>
        <div class="form-section-container">
        <plugin:hookTag hookPoint="advanceNumberBlock" attrs="[product: product]">
            <div class="double-input-row">
                <div class="form-row with-check-box">
                    <label><g:message code="global.trade.item.number"/></label>
                    <input type="text" name="advanced.globalTradeItemNumber" class="medium" ${detailsMap.containsKey("globalTradeItemNumber") ? '' : 'disabled'} value="${detailsMap.globalTradeItemNumber ?: product.globalTradeItemNumber.encodeAsBMHTML()}">
                    <input type="checkbox" class="multiple active-check" value="true" ${detailsMap.containsKey("globalTradeItemNumber") ? 'checked' : ''}>
                </div>
            </div>
        </plugin:hookTag>
            <div class="form-row chosen-wrapper with-check-box">
                <label><g:message code="product.condition"/></label>
                <ui:namedSelect name="advanced.productCondition" key="${NamedConstants.PRODUCT_CONDITION}" class="medium" disabled="${detailsMap.containsKey("productCondition") ? 'false' : 'true'}" value="${detailsMap.productCondition ?: product.productCondition}"/>
                <input type="checkbox" class="multiple active-check" value="true" ${detailsMap.containsKey("productCondition") ? 'checked' : ''}>
            </div>
            <plugin:hookTag hookPoint="enterpriseProductAdvancedEditTab" attrs="${[productId: product.id, detailsMap: detailsMap]}"/>
        </div>
    </div>
    <div class="section-separator"></div>

    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="webtool"/> </h3>
            <div class="info-content"><g:message code="section.text.product.webtool"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row">
                <g:set var="disable" value="${detailsMap.disableGooglePageTracking.toBoolean() ?: product.disableGooglePageTracking}"/>
                <input type="checkbox" class="single" name="advanced.disableGooglePageTracking" value="true" uncheck-value="false" ${disable ? "checked=checked" : ""}/>
                <span class="inline-lable"><g:message code="disable.tracking"/></span>
            </div>
        </div>
    </div>

    <div class="section-separator"></div>

    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="meta.tag"/></h3>
            <div class="info-content"><g:message code="section.text.product.meta.tag"/></div>
        </div>
        <input type="checkbox" name="enabeMetatag" class="multiple active-check" disable-also="metatag" value="true" ${details.metaTags ? 'checked' : ''}>
        <div class="form-section-container meta-tag-container">
            <div class="variation-metatag metatag ${details.metaTags ? 'enable' : ''}">
                <div class="overlay-panel ${details.metaTags ? '' : 'disabled'}"></div>
                <g:include view="/admin/metatag/metaTagEditor.gsp" model="${[metaTags: details.metaTags ?: product.metaTags]}"/>
            </div>
        </div>
    </div>
    </plugin:hookTag>
    <div class="form-section-container">
        <div class="form-row">
            <label>&nbsp;</label>
            <button type="submit" class="submit-button product-advance-form-submit"><g:message code="update"/></button>
        </div>
    </div>
</form>