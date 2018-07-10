<g:form class="create-edit-form filter-group-form" controller="filterGroup" action="saveFilterGroup">
    <input type="hidden" name="id" value="${filterGroup.id}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="filter.group.info"/></h3>
            <div class="info-content"><g:message code="section.text.filter.group.info"/></div>
        </div>
        <div class="form-section-container">

            <div class="form-row">
                <label><g:message code="active"/></label>
                <input type="checkbox" class="single" name="isActive" ${filterGroup.isActive ? "checked" : ""} value="true">
            </div>

            <div class="form-row mandatory">
                <label><g:message code="name"/><span class="suggestion"> e.g. Name of Filter Group</span> </label>
                <g:textField name="name" value="${filterGroup.name}" maxlength="250"  validation="required maxlength[250]" class="large unique"/>
            </div>

            <div class="form-row tinymce-container">
                <label><g:message code="description"/><span class="suggestion"> Put a detailed description of your filter group. You can insert a link, table, image, video or other cool stuff in here.</span> </label>
                <textarea class="wceditor no-auto-size xx-larger" toolbar-type="advanced" name="description">${filterGroup.description}</textarea>
            </div>

            <div class="form-row">
                <button type="submit" class="submit-button"><g:message code="save"/></button>
                <button type="button" class="cancel-button"><g:message code="cancel"/></button>
            </div>

        </div>
    </div>
</g:form>
