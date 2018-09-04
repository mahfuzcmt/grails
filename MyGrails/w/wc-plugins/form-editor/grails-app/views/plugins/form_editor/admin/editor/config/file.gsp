<div class="body leftbar-accordion">
    <div class="label-bar properties-header">
        <label><g:message code="properties.for.file"/></label>
        <span class="tool-icon toggle-icon"></span>
    </div>
    <div class="accordion-item properties">
        <div type="basic" class="config-section">
            <div class="form-row">
                <label><g:message code="html.name"/></label>
                <input type="text" value="" name="field.name" validation="required maxlength[100]" maxlength="100">
            </div>
            <div class="form-row">
                <label><g:message code="label"/></label>
                <input type="text" value="" name="field.label" validation="maxlength[100]" maxlength="100">
            </div>
            <div class="form-row">
                <label><g:message code="html.class.s"/></label>
                <input type="text" value="" name="field.clazz" validation="maxlength[100]" maxlength="100">
            </div>
            <div class="form-row">
                <label><g:message code="hover.text"/></label>
                <input type="text" value="" name="field.title" validation="maxlength[100]" maxlength="100">
            </div>
        </div>
        <div type="validation" class="config-section">
            <input type="hidden" name="field.validation" class="validation-field">
            <div class="form-row">
                <input type="checkbox" name="r-validation" class="validation-required single" value="required">
                <label><g:message code="required" /></label>
            </div>
        </div>
        <div type="file-style" class="config-section">
            <div class="form-row">
                <label><g:message code="upload.style"/> </label>
                <div class="radio-group horizontal">
                    <div class="radio">
                        <input type="radio" name="field.extra.config.upload_style" value="drop_box" field-update="true">
                        <label><g:message code="drop.box"/> </label>
                    </div>
                    <div class="radio">
                        <input type="radio" name="field.extra.config.upload_style" value="upload" field-update="true">
                        <label><g:message code="upload"/> </label>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div class="label-bar validation-header">
        <label><g:message code="validation"/></label>
        <span class="tool-icon toggle-icon"></span>
    </div>
    <div type="file-prop" class="config-section accordion-item validation">
        <div class="form-row">
            <label><g:message code="size"/></label>
        </div>
        <div class="double-input-row">
            <div class="form-row">
                <input type="text" restrict="numeric" maxlength="6" name="field.extra.config.upload_size" validation="gt[0]">
            </div><div class="form-row">
                <g:select class="sub-field" name="field.extra.config.upload_size_unit" from="${["MB", "KB"]}" keys="${["mb", "kb"]}"/>
            </div>
        </div>
        <div class="form-row format-row">
            <label><g:message code="upload.format"/></label>
            <input class="extensions" type="hidden" name="field.extra.config.upload_extensions">
            <div class="check-box-group">
                <g:set var="formats" value="${["doc", "txt", "js", "class", "xls", "mpeg", "jpg", "csv", "docx", "wav", "java", "psd", "xlsx", "mp3", "jar"]}"/>
                <g:each in="${formats}" var="format">
                    <div class="check-box">
                        <input type="checkbox" class="format-${format}" name="format" value="${format}">
                        <label>.${format}</label>
                    </div>
                </g:each>
            </div>
        </div>
    </div>
</div>