<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.constants.NamedConstants" %>
<form action="${app.relativeBaseUrl()}productAdmin/saveAdvanced" method="post" class="create-edit-form product-advance-form">
    <input type="hidden" name="id" value="${product.id}">
    <plugin:hookTag hookPoint="productAdvancedInformation" attrs="${[entityType: "product"]}">
        <div class="form-section">
            <div class="form-section-info">
                <h3><g:message code="advance.information"/></h3>
                <div class="info-content"><g:message code="section.text.product.advance"/></div>
            </div>
            <div class="form-section-container">
             <plugin:hookTag hookPoint="advanceNumberBlock" attrs="[product: product]">
                 <div class="double-input-row">
                     <div class="form-row">
                         <label><g:message code="global.trade.item.number"/></label>
                         <input type="text" name="globalTradeItemNumber" class="medium" value="${product.globalTradeItemNumber.encodeAsBMHTML()}">
                     </div>
                 </div>
             </plugin:hookTag>

                <g:if test="${product.productType != DomainConstants.PRODUCT_TYPE.DOWNLOADABLE}">
                    <div class="form-row chosen-wrapper">
                        <label><g:message code="product.condition"/></label>
                        <ui:namedSelect key="${NamedConstants.PRODUCT_CONDITION}" class="medium" value="${product.productCondition}" name="condition"/>
                    </div>
                </g:if>
                <plugin:hookTag hookPoint="productAdvancedEditTab" attrs="${[productId: product.id]}"/>
            </div>
        </div>
        <div class="section-separator"></div>

        <div class="form-section">
            <div class="form-section-info">
                <h3><g:message code="webtool"/> </h3>
                <div class="info-content"><g:message code="section.text.product.webtool"/> </div>
            </div>
            <div class="form-section-container">
                <div class="form-row">
                    <input type="checkbox" class="single" name="disableTracking" value="true" uncheck-value="false" ${product.disableGooglePageTracking ? "checked=checked" : ""}/>
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
            <div class="form-section-container">
                <g:include view="/admin/metatag/metaTagEditor.gsp" model="${[metaTags: product.metaTags]}"/>
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