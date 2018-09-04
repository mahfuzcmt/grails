<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.constants.NamedConstants" %>
<form id="frmProductSetting" class="create-edit-form" action="${app.relativeBaseUrl()}setting/saveConfigurations" method="POST"
      xmlns="http://www.w3.org/1999/html">
    <input type="hidden" name="type" value="product">
    <plugin:hookTag hookPoint="productPageSettings">
        <div class="form-section">
            <div class="form-section-info">
                <h3><g:message code="product.page"/></h3>
                <div class="info-content"><g:message code="section.text.setting.product.page"/></div>
            </div>
            <div class="form-section-container">
                <plugin:hookTag hookPoint="productPageConfigurationForm"/>
                <div class="double-input-row">
                    <div class="form-row">
                        <input type="checkbox" class="single" name="product.add_to_cart" value="true" uncheck-value="false" ${productSettings.add_to_cart.toBoolean() ? 'checked' : ''}>
                        <span><g:message code="add.to.cart"/></span>
                    </div><div class="form-row">
                    <input type="checkbox" class="single" name="product.enable_like" value="true" uncheck-value="false" toggle-target="show-config-for-like" ${productSettings.enable_like.toBoolean()?'checked':''}>
                    <span><g:message code="enable.social.media.like"/></span>
                </div>
                </div>
                <div class="show-config-for-like">
                    <g:each var="profile" status="i" in="${[[name:'facebook', value: ''], [name: 'twitter', value: ''], [name: 'googleplus', value: ''], [name: 'pinterest', value: '']]}">
                        <g:set var="profileName" value="like_${profile.name}"/>
                        <div class="form-row like-${profile.name}">
                            <g:if test="${productSettings[profileName].toBoolean()}">
                                <%
                                    profile.value = "checked"
                                %>
                            </g:if>
                            <input type="checkbox" class="multiple" name="product.like_${profile.name}" value="true" uncheck-value="false" ${profile.value == 'checked' ? 'checked="checked"' : ''}>
                            <img src="${app.systemResourceBaseUrl()}images/social-media-icons/${profile.name}-like.png">
                        </div>
                    </g:each>
                    <div class="form-row">
                        <input type="checkbox" class="multiple" name="product.tell_friend" value="true" uncheck-value="false" ${productSettings.tell_friend.toBoolean() ? 'checked' : ''}>
                        <span><g:message code="tell.friend"/></span>
                    </div>
                </div>
                <div class="double-input-row">
                    <div class="form-row">
                        <input type="checkbox" class="single" name="product.image_zoom" value="true" uncheck-value="false" ${productSettings.image_zoom.toBoolean() ? 'checked' : ''} toggle-target="image-zoom">
                        <span> <g:message code="image.zoom"/></span>
                    </div><div class="form-row">
                        <input type="checkbox" class="single" name="product.show_description" value="true" uncheck-value="false" ${productSettings.show_description.toBoolean() ? 'checked' : ''}>
                        <span><g:message code="show.description"/></span>
                    </div>
                </div>
                <div class="form-row image-zoom chosen-wrapper">
                    <label><g:message code="zoom.type"/><span class="suggestion"><g:message code="suggestion.setting.product.zoom.type"/></span></label>
                    <ui:namedSelect name="product.zoom_type" key="${NamedConstants.ZOOM_TYPE}" value="${productSettings.zoom_type}" class="large"/>
                </div>
                <div class="form-row">
                    <label><g:message code="label.for.call.for.price"/></label>
                    <input type="text" name="product.label_for_call_for_price" value="${productSettings.label_for_call_for_price}" validation="required">
                </div>
                <div class="form-row">
                    <label><g:message code="label.for.expect.to.pay"/></label>
                    <input type="text" name="product.label_for_expect_to_pay" value="${productSettings.label_for_expect_to_pay}" >
                </div>
                <div class="form-row">
                    <label><g:message code="label.for.base.price"/></label>
                    <input type="text" name="product.label_for_base_price" value="${productSettings.label_for_base_price}" >
                </div>
                <div class="double-input-row">
                    <div class="form-row">
                        <label><g:message code="strike.through.previous.price"/></label>
                        <input type="checkbox" class="single" name="product.strike_through_previous_price" ${productSettings.strike_through_previous_price == "true" ? "checked" : ""} value="true" uncheck-value="false">
                    </div><div class="form-row">
                    <label><g:message code="show.tax.with.expect.to.pay.price"/></label>
                    <input type="checkbox" class="single" name="product.expect_to_pay_price_with_tax" ${productSettings.expect_to_pay_price_with_tax == "true" ? "checked" : ""} value="true" uncheck-value="false">
                </div>
                </div>
            </div>
        </div>
        <div class="form-section">
            <div class="form-section-info">
                <h3><g:message code="related.products"/></h3>
                <div class="info-content"><g:message code="section.text.setting.search.related.product"/></div>
            </div>
            <div class="form-section-container">
                <g:set var="relatedProduct" value="${DomainConstants.SITE_CONFIG_TYPES.RELATED_PRODUCT}"/>
                <input type="hidden" name="type" value="${relatedProduct}">
                <div class="form-row">
                    <label><g:message code="title"/><span class="suggestion">e.g. Related Product</span></label>
                    <input type="text" name="${relatedProduct}.title" value="${relatedProductSettings.title}">
                </div>
                <div class="form-row chosen-wrapper">
                    <label><g:message code="display.type"/><span class="suggestion">e. g. List</span></label>
                    <ui:namedSelect class="medium display-type" toggle-target="pagination-props"  name="${relatedProduct}.display-type" key="${NamedConstants.PRODUCT_WIDGET_VIEW_MESSAGE}" value="${relatedProductSettings["display-type"]}"/>
                </div>
                <div class="form-row mandatory">
                    <label><g:message code="number.of.products"/></label>
                    <input type="text" class="medium" name="${relatedProduct}.item-per-page" value="${relatedProductSettings["item-per-page"] ?: 10}" validation="skip@if{self::hidden} required gt[0] number maxlength[6]" maxlength="6" restrict="decimal"/>
                </div>
                <div class="form-row">
                    <input type="checkbox" class="single" name="${relatedProduct}.price" value="true" uncheck-value="false" ${relatedProductSettings["price"] == "true" ? "checked" : ""} toggle-target="price-toggle">
                    <span><g:message code="price"/></span>
                </div>
                <div class="form-row">
                    <label><g:message code="label.for.call.for.price"/></label>
                    <input type="text" name="${relatedProduct}.label_for_call_for_price" value="${relatedProductSettings.label_for_call_for_price}" validation="required">
                </div>
                <div class="price-toggle">
                    <div class="form-row">
                        <label><g:message code="label.for.price" /></label>
                        <input type="text" name="${relatedProduct}.label_for_price" value="${relatedProductSettings.label_for_price}" >
                    </div>
                    <div class="double-input-row">
                        <div class="form-row">
                            <input type="checkbox" class="single" name="${relatedProduct}.expect_to_pay_price" value="true" uncheck-value="false" ${relatedProductSettings.expect_to_pay_price == "true" ? "checked='checked'" : ""} toggle-target="label-for-expect">
                            <span><g:message code="show.expect.to.pay.price" /></span>
                        </div><div class="form-row label-for-expect">
                        <input type="checkbox" class="single" name="${relatedProduct}.expect_to_pay_price_with_tax" ${relatedProductSettings.expect_to_pay_price_with_tax == "true" ? "checked" : ""} value="true" uncheck-value="false">
                        <span><g:message code="show.tax.with.expect.to.pay.price"/></span>
                    </div>
                    </div>
                    <div class="form-row label-for-expect">
                        <label><g:message code="label.for.expect.to.pay" /></label>
                        <input type="text" name="${relatedProduct}.label_for_expect_to_pay" value="${relatedProductSettings.label_for_expect_to_pay}">
                    </div>
                </div>
                <div class="form-row">
                    <input type="checkbox" class="single" name="${relatedProduct}.description" value="true" uncheck-value="false" ${relatedProductSettings["description"] == "true" ? "checked" : ""}>
                    <span><g:message code="short.description"/></span>
                </div>
                <div class="form-row">
                    <input type="checkbox" class="single" name="${relatedProduct}.add_to_cart" value="true" uncheck-value="false" ${relatedProductSettings["add_to_cart"] == "true" ? "checked" : ""}>
                    <span><g:message code="add.to.cart"/></span>
                </div>
                <div class="form-row pagination-props-image pagination-props-list">
                    <input type="checkbox" class="single" name="${relatedProduct}.show_view_switcher" value="true" uncheck-value="false" ${relatedProductSettings["show_view_switcher"] == "true" ? "checked" : ""} >
                    <span><g:message code="show.view.switching.buttons"/></span>
                </div>
            </div>
        </div>
        <div class="form-section">
            <div class="form-section-info">
                <h3><g:message code="product.properties"/></h3>
                <div class="info-content"><g:message code="section.text.setting.product.properties"/></div>
            </div>
            <div class="form-section-container">
                <g:set var="productPro" value="${DomainConstants.SITE_CONFIG_TYPES.PRODUCT_PROPERTIES}"/>
                <input type="hidden" name="type" value="${productPro}">
                <div class="double-input-row">
                    <div class="form-row">
                        <input type="checkbox" name="${productPro}.show_height" value="true" uncheck-value="false" class="single" ${productPropertiesSettings.show_height == "true" ? "checked" : ""}>
                        <span><g:message code="show.height"/></span>
                    </div><div class="form-row">
                        <input type="checkbox" name="${productPro}.show_width" value="true" uncheck-value="false" class="single" ${productPropertiesSettings.show_width == "true" ? "checked" : ""}>
                        <span><g:message code="show.width"/></span>
                    </div>
                </div>
                <div class="double-input-row">
                    <div class="form-row">
                        <input type="checkbox" name="${productPro}.show_length" value="true" uncheck-value="false" class="single" ${productPropertiesSettings.show_length == "true" ? "checked" : ""}>
                        <span><g:message code="show.length"/></span>
                    </div><div class="form-row">
                        <input type="checkbox" name="${productPro}.show_weight" value="true" uncheck-value="false" class="single" ${productPropertiesSettings.show_weight == "true" ? "checked" : ""}>
                        <span><g:message code="show.weight"/></span>
                    </div>
                </div>
            </div>
        </div>
    </plugin:hookTag>
    <div class="form-section">
        <div class="form-section-container">
            <div class="form-row">
                <button class="submit-button" type="submit"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>
