<form action="${app.relativeBaseUrl()}categoryAdmin/saveMetatags" method="post" class="create-edit-form meta-tag-form">
    <input type="hidden" name="id" value="${category.id}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="meta.tag"/></h3>
            <div class="info-content"><g:message code="section.text.category.meta.tag"/> </div>
        </div>
        <div class="form-section-container">
            <g:include view="admin/metatag/metaTagEditor.gsp" model="[metaTags: category.metaTags]"/>
            <div class="form-row">
                <label>&nbsp;</label>
                <button type="submit" class="submit-button meta-tag-form-submit"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>