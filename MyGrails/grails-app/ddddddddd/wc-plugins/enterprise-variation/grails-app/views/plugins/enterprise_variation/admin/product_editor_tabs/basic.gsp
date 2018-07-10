<div class="toolbar-share">
    <span class="header-title"><g:message code="web.commerce"/> > <g:message code="product"/> > ${variation.product.name.encodeAsBMHTML()} > <g:message code="variation"/> > ${details.name ?: product.name.encodeAsBMHTML() + "_" + details.id} > <g:message code="basic"/> </span>
</div>
<form action="${app.relativeBaseUrl()}enterpriseVariation/saveProperties" method="post" class="create-edit-form product-basic-create-edit">
    <input type="hidden" name="id" value="${details.id}">
    <input type="hidden" name="type" value="basic">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="product.info"/></h3>
            <div class="info-content"><g:message code="section.text.product.info"/></div>
        </div>
        <div class="form-section-container">
            <div class="double-input-row">
                <div class="form-row mandatory">
                    <label><g:message code="product.name"/><span class="suggestion"><g:message code="suggestion.product.name"/> </span></label>
                    <input name="name" type="text" class="form-full-width" value="${details.name ?: product.name.encodeAsBMHTML() + "_" + details.id}" validation="required rangelength[2, 100]" maxlength="100">
                </div><div class="form-row mandatory">
                    <label><g:message code="sku"/><span class="suggestion">  (Stock Keeping Unit)</span></label>
                    <input name="sku" type="text" class="form-full-width" value="${details.sku ?: product.sku + "-" + details.id}" validation="required maxlength[50]" maxlength="50">
                </div>
            </div>
            <div class="double-input-row">
                <div class="form-row with-check-box">
                    <label><g:message code="title"/>  <span class="suggestion"><g:message code="suggestion.product.title"/> </span></label>
                    <input name="basic.title" type="text" class="form-full-width" ${detailsMap.containsKey("title") ? '' : 'disabled'} value="${detailsMap.title ?: product.title.encodeAsBMHTML()}" validation="maxlength[200]" maxlength="200">
                    <input type="checkbox" class="multiple active-check" value="true" ${detailsMap.containsKey("title") ? 'checked' : ''}>
                </div><div class="form-row">
                    <label><g:message code="url.identifier"/><span class="suggestion">  e.g. product-name</span></label>
                    <input name="url" type="text" class="form-full-width" value="${details.url ?: product.url + "-" + details.id}" validation="required url_folder maxlength[100]" maxlength="100">
                </div>
            </div>
            <div class="double-input-row">
                <div class="form-row with-check-box">
                    <label><g:message code="heading"/><span class="suggestion"><g:message code="insert.an.additional.heading.for.your.product.page"/></span></label>
                    <input name="basic.heading" type="text" class="form-full-width" ${detailsMap.containsKey("heading") ? '' : 'disabled'} value="${detailsMap.heading ?: product.heading.encodeAsBMHTML()}" validation="maxlength[200]" maxlength="200">
                    <input type="checkbox" class="multiple active-check" value="true" ${detailsMap.containsKey("heading") ? 'checked' : ''}>
                </div><div class="form-row chosen-wrapper with-check-box">
                    <label><g:message code="availability"/></label>
                    <g:select name="basic.isAvailable" class="form-full-width" toggle-target="display-availability" disabled="${detailsMap.containsKey("availability") ? 'false' : 'true'}" from="[g.message(code: 'available'), g.message(code: 'not.available')]" keys="${[true, false]}" value="${detailsMap.isAvailable ?: product.isAvailable}"/>
                </div>
            </div>
            <div class="double-input-row">
                <div class="form-row mandatory fixed-price-specify with-check-box">
                    <label><g:message code="base.price"/><span class="suggestion"><g:message code="suggestion.product.base.price"/></span></label>
                    <input name="basic.basePrice" restrict="decimal" type="text" class="form-full-width" ${detailsMap.containsKey("basePrice") ? '' : 'disabled'} value="${detailsMap.basePrice ? detailsMap.basePrice.toAdminPrice() : product.basePrice?.toAdminPrice()}" maxlength="16" validation="required@if{self::visible} number maxlength[16] price">
                    <input type="checkbox" class="multiple active-check" value="true" ${detailsMap.containsKey("basePrice") ? 'checked' : ''}>
                </div><div class="form-row fixed-price-specify with-check-box">
                    <label><g:message code="cost.price"/><span class="suggestion"><g:message code="suggestion.product.cost.price"/></span></label>
                    <input name="basic.costPrice" restrict="decimal" type="text" ${detailsMap.containsKey("costPrice") ? '' : 'disabled'} class="form-full-width" value="${detailsMap.costPrice ? detailsMap.costPrice.toAdminPrice() : product.costPrice?.toAdminPrice()}" maxlength="16" validation="number maxlength[16] price">
                    <input type="checkbox" class="multiple active-check" value="true" ${detailsMap.containsKey("costPrice") ? 'checked' : ''}>
                </div>
            </div>
            <div class="form-row chosen-wrapper with-check-box">
                <label><g:message code="administrative.status"/><span class="suggestion">e.g. Active</span></label>
                <g:select name="basic.active" from="${['active', 'inactive'].collect {g.message(code: it)}}" disabled="${detailsMap.containsKey("active") ? 'false' : 'true'}" keys="${['true', 'false']}" class="medium" value="${detailsMap.active ?: product.isActive}"/>
            </div>
            <div class="form-row with-check-box">
                <label><g:message code="product.summary"/><span class="suggestion"><g:message code="suggestion.product.summary"/></span></label>
                <textarea class="form-full-width" name="basic.summary" ${detailsMap.containsKey("summary") ? '' : 'disabled'} maxlength="500" validation="maxlength[500]">${detailsMap.summary ?: product.summary}</textarea>
                <input type="checkbox" class="multiple active-check" value="true" ${detailsMap.containsKey("summary") ? 'checked' : ''}>
            </div>
            <div class="form-row with-check-box">
                <label><g:message code="product.description"/><span class="suggestion"> <g:message code="suggestion.product.detials"/></span></label>
                <div class="rteditor-wrap">
                    <div class="overlay-panel description ${detailsMap.containsKey("description") ? '' : 'disabled'}"></div>
                    <textarea class="wceditor no-auto-size form-full-width" toolbar-type="advanced" name="basic.description" ${detailsMap.containsKey("description") ? '' : 'disabled'} maxlength="65535" validation="maxlength[65535]">${detailsMap.description ?: product.description}</textarea>
                </div>
                <input type="checkbox" class="multiple active-check" disable-also="description"  value="true" ${detailsMap.containsKey("description") ? 'checked' : ''}>
            </div>
            <div class="form-row">
                <button type="submit" class="submit-button"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>