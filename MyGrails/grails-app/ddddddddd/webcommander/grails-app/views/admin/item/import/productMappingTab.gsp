<%@ page import="com.webcommander.constants.*;" %>
<div class="triple-input-row">
    <div class="form-row">
        <label class="large"><g:message code="product.work.sheet"/><span class="suggestion">Example</span></label>
        <select name="productWorkSheet" class="product-work-sheet large">
            <option value=""><g:message code="not.import"/></option>
            <g:each in="${sheetNames}" var="sheet">
                <option value="${sheet}" ${sheet == productSheet ? "selected='selected'" : ""}>${sheet}</option>
            </g:each>
        </select>
    </div><div class="form-row">
        <label class="large"><g:message code="overwrite.existing.product"/><span class="suggestion">Example</span></label>
        <select name="productOverwrite" class="large required">
            <option value="1">Yes</option>
            <option value="0">No</option>
        </select>
    </div><div class="form-row">
        <label class="large"><g:message code="product.match.by"/><span class="suggestion">Example</span></label>
        <select name="productMatchBy" class="large">
            <option value="name"><g:message code="name" /></option>
            <option value="sku"><g:message code="sku" /></option>
        </select>
    </div>
</div>
<div class="triple-input-row">
    <div class="form-row">
        <label class="large"><g:message code="category.match.by"/><span class="suggestion">Example</span></label>
        <select name="productParentMatchBy" class="large">
            <option value="name"><g:message code="name" /></option>
            <option value="sku"><g:message code="sku" /></option>
        </select>
    </div><div class="form-row">
        <label class="large"><g:message code="image.path"/><span class="suggestion">Example</span></label>
        <input type="text" class="large" name="productImagePath" value="">
    </div><div class="form-row">
        <label class="large"><g:message code="video.path"/><span class="suggestion">Example</span></label>
        <input type="text" class="large" name="productVideoPath" value="">
    </div>
</div>
<g:include view="/admin/item/import/productMappingFields.gsp" model="[columnNames: productColumns, productSheet: productSheet, productSheetRows: sheetRows[productSheet.toString()]]" />