<form id="responsiveSettingsForm" class="create-edit-form" action="${app.relativeBaseUrl()}setting/saveConfigurations" method="post">
    <input type="hidden" name="type" value="responsive">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="responsive"/></h3>
            <div class="info-content"><g:message code="section.text.responsive.info"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row">
                <label><g:message code="enable.responsiveness.page"/></label>
                <input name="responsive.is_responsive" class="single" type="checkbox" value="true" uncheck-value="false" ${config.is_responsive == "true" ? "checked" : ''}>
                <span class="note"><g:message code="changing.value.not.affect.open.editor"/></span>
            </div>
            <div class="form-row">
                <button class="submit-button" type="submit"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>
<div class="section-separator"></div>
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="resolutions"/></h3>
            <div class="info-content"><g:message code="section.text.resolution.info"/></div>
        </div>
        <div class="form-section-container">
            <div class="resolutions-container">
                <table class="resolutions">
                    <colgroup>
                        <col>
                        <col>
                        <col style="width: 80px">
                    </colgroup>
                    <thead>
                    <tr>
                        <th><g:message code="min.width"/></th>
                        <th><g:message code="max.width"/></th>
                        <th><g:message code="actions"/> </th>
                    </tr>
                    </thead>
                    <tbody>
                    <g:each in="${resolutions}" var="resolution">
                        <tr resolution-id="${resolution.id}">
                            <td class="editable min-width" restrict="numeric">${resolution.min}</td>
                            <td class="editable max-width" restrict="numeric">${resolution.max}</td>
                            <td><span class="tool-icon remove"></span></td>
                        </tr>
                    </g:each>
                    <tr class="last-row">
                        <td><input type="text" class="min-width td-full-width" placeholder="<g:message code="min.width"/>"></td>
                        <td><input type="text" class="max-width td-full-width" placeholder="<g:message code="max.width"/>"></td>
                        <td><span class="tool-icon add add-row"></span></td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>