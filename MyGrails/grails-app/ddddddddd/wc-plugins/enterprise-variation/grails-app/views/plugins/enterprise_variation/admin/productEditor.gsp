<%@ page import="com.webcommander.constants.DomainConstants" %>
<div class="header multi-tab-shared-header">
    <span class="header-title"></span>
    <div class="toolbar toolbar-right">
        <span class="tool-group toolbar-btn save save-all"><g:message code="save.all"/></span>
        <span class="tool-group toolbar-btn cancel"><g:message code="cancel"/></span>
    </div>
</div>
<g:set var="vId" value="${variation.id}"/>
<div class="bmui-tab left-side-header">
    <div class="bmui-tab-header-container">
        <div class="bmui-tab-header" data-tabify-tab-id="basic" data-tabify-url="${app.relativeBaseUrl()}enterpriseVariation/loadProductProperties?vId=${vId}&property=basic">
            <span class="title"><g:message code="basic"/></span>
        </div>
        <div class="bmui-tab-header" data-tabify-tab-id="priceStock" data-tabify-url="${app.relativeBaseUrl()}enterpriseVariation/loadProductProperties?vId=${vId}&property=priceStock">
            <span class="title"><g:message code="price.stock"/></span>
        </div>
        <div class="bmui-tab-header" data-tabify-tab-id="imageVideo" data-tabify-url="${app.relativeBaseUrl()}enterpriseVariation/loadProductProperties?vId=${vId}&property=imageVideo">
            <span class="title"><g:message code="images.and.video"/></span>
        </div>
        <div class="bmui-tab-header" data-tabify-tab-id="advanced" data-tabify-url="${app.relativeBaseUrl()}enterpriseVariation/loadProductProperties?vId=${vId}&property=advanced">
            <span class="title"><g:message code="advanced"/></span>
        </div>
        <g:if test="${product.productType == DomainConstants.PRODUCT_TYPE.DOWNLOADABLE}">
            <div class="bmui-tab-header" data-tabify-tab-id="productFile" data-tabify-url="${app.relativeBaseUrl()}enterpriseVariation/loadProductProperties?vId=${vId}&property=productFile">
                <span class="title"><g:message code="product.file"/></span>
            </div>
        </g:if>
        <plugin:hookTag hookPoint="enterpriseProductEditorTabHeader"/>
    </div>
    <div class="bmui-tab-body-container">
        <div id="bmui-tab-basic">
        </div>
        <div id="bmui-tab-priceStock">
        </div>
        <div id="bmui-tab-imageVideo">
        </div>
        <div id="bmui-tab-advanced">
        </div>
        <plugin:hookTag hookPoint="enterpriseProductEditorTabBody"/>
    </div>
</div>