<%@ page import="com.webcommander.constants.*;" %>
<div class="triple-input-row">
    <div class="form-row">
        <label class="large"><g:message code="category.work.sheet"/><span class="suggestion">Example</span></label>
        <select name="categoryWorkSheet" class="category-work-sheet large">
            <option value=""><g:message code="not.import"/></option>
            <g:each in="${sheetNames}" var="sheet">
            <option value="${sheet}" ${sheet == categorySheet ? "selected='selected'" : ""}>${sheet}</option>
            </g:each>
        </select>
    </div><div class="form-row">
        <label class="large"><g:message code="overwrite.existing.category"/><span class="suggestion">Example</span></label>
        <select name="categoryOverwrite" class="large required">
            <option value="1">Yes</option>
            <option value="0">No</option>
        </select>
    </div><div class="form-row">
        <label class="large"><g:message code="category.match.by"/><span class="suggestion">Example</span></label>
        <select name="categoryMatchBy" class="large">
            <option value="name"><g:message code="name" /></option>
            <option value="sku"><g:message code="sku" /></option>
        </select>
    </div>
</div>
<div class="triple-input-row">
    <div class="form-row">
        <label class="large"><g:message code="parent.match.by"/><span class="suggestion">Example</span></label>
        <select name="categoryParentMatchBy" class="large">
            <option value="name"><g:message code="name" /></option>
            <option value="sku"><g:message code="sku" /></option>
        </select>
    </div><div class="form-row">
        <label class="large"><g:message code="image.match.by"/><span class="suggestion">Example</span></label>
        <select name="categoryImageMatchBy" class="large" toggle-target="category-image-mapping-field">
            <option value=""><g:message code="map.by.column"/></option>
            <option value="name"><g:message code="name" /></option>
            <option value="sku"><g:message code="sku" /></option>
        </select>
    </div><div class="form-row">
        <label class="large"><g:message code="image.path"/><span class="suggestion">Example</span></label>
        <input type="text" class="large required" name="categoryImagePath" value="" />
    </div>
</div>
<g:include view="/admin/item/import/categoryMappingFields.gsp" model="[columnNames: categoryColumns, categoryFields: categoryFields, categorySheet: categorySheet, categorySheetRows: sheetRows[categorySheet.toString()]]" />