<%@ page import="com.webcommander.constants.DomainConstants"%>
<div class="bmui-tab left-side-header create-edit-panel">
    <div class="bmui-tab-header-container">
        <div class="bmui-tab-header" data-tabify-tab-id="basic" data-tabify-url="${app.relativeBaseUrl()}productAdmin/loadProductProperties?id=${productId}&property=basic">
            <span class="title"><g:message code="basic"/></span>
        </div>
        <div class="bmui-tab-header" data-tabify-tab-id="price-quantity" data-tabify-url="${app.relativeBaseUrl()}productAdmin/loadProductProperties?id=${productId}&property=price-stock">
            <span class="title"><g:message code="price.stock"/></span>
        </div>
        <div class="bmui-tab-header" data-tabify-tab-id="image-video" data-tabify-url="${app.relativeBaseUrl()}productAdmin/loadProductProperties?id=${productId}&property=image-video">
            <span class="title"><g:message code="images.and.video"/></span>
        </div>
        <div class="bmui-tab-header" data-tabify-tab-id="advanced" data-tabify-url="${app.relativeBaseUrl()}productAdmin/loadProductProperties?id=${productId}&property=advanced">
            <span class="title"><g:message code="advanced"/></span>
        </div>
        <div class="bmui-tab-header" data-tabify-tab-id="relatedProducts" data-tabify-url="${app.relativeBaseUrl()}productAdmin/loadProductProperties?id=${productId}&property=relatedProducts&exclude=${productId}">
            <span class="title"><g:message code="related.products"/></span>
        </div>
        <g:if test="${product.productType == DomainConstants.PRODUCT_TYPE.DOWNLOADABLE}">
            <div class="bmui-tab-header" data-tabify-tab-id="productFile" data-tabify-url="${app.relativeBaseUrl()}productAdmin/loadProductProperties?id=${productId}&property=productFile">
                <span class="title"><g:message code="product.file"/></span>
            </div>
        </g:if>
        <g:if test="${product.isCombined}">
            <div class="bmui-tab-header" data-tabify-tab-id="includedProducts" data-tabify-url="${app.relativeBaseUrl()}productAdmin/loadProductProperties?id=${productId}&property=includedProducts">
                <span class="title"><g:message code="included.products"/></span>
            </div>
        </g:if>
        <plugin:hookTag hookPoint="productEditorTabHeader" attrs="[product: product]"/>
    </div>
    <div class="bmui-tab-body-container product-editor-body">
        <div id="bmui-tab-basic">
        </div>
        <div id="bmui-tab-price-quantity">
        </div>
        <div id="bmui-tab-customProperties">
        </div>
        <div id="bmui-tab-image-video">
        </div>
        <div id="bmui-tab-advanced">
        </div>
        <div id="bmui-tab-relatedProducts">
        </div>
        <g:if test="${product.productType == DomainConstants.PRODUCT_TYPE.DOWNLOADABLE}">
            <div id="bmui-tab-productFile">
            </div>
        </g:if>
        <g:if test="${product.isCombined}">
            <div id="bmui-tab-includedProducts">
            </div>
        </g:if>
        <plugin:hookTag hookPoint="productEditorTabBody" attrs="[product: product]"/>
    </div>
</div>