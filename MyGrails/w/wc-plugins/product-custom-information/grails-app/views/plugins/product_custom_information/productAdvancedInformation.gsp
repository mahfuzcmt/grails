<div class="section-separator"></div>
<div class="form-section">
    <div class="form-section-info">
        <h3><g:message code="custom.information"/></h3>
        <div class="info-content"><g:message code="section.text.setting.product.page.custom.information"/></div>
    </div>

    <div class="form-section-container">
        <g:each in="${informations}" var="information" status="i">
            <div class="form-row with-check-box" information-id="${information.id}">
            <label>${information.title.encodeAsBMHTML()}<span class="suggestion">Put a detailed description of your product. You can assert a link, table, image, video or other cool stuff in here</span> </label>
                <div class="rteditor-wrap">
                    <g:if test="${entityType == "variation"}">
                        <div class="overlay-panel auto-change customInformation-${information.id} ${values[i] != null ? (values[i].entityType != "product" ? '' : 'disabled') : 'disabled'}"></div>
                        <g:if test="${values[i] != null}">
                            <textarea class="wceditor no-auto-size form-full-width" toolbar-type="advanced" name="customInformation.${information.id}" ${values[i].entityType != "product" ? '' : 'disabled'} maxlength="65535" validation="maxlength[65535]">${values[i].value}</textarea>
                        </g:if>
                        <g:else>
                            <textarea class="wceditor no-auto-size form-full-width" toolbar-type="advanced" name="customInformation.${information.id}" ${values[i] ? '' : 'disabled'} maxlength="65535" validation="maxlength[65535]"></textarea>
                        </g:else></div>
                        <input type="checkbox" class="multiple active-check" disable-also="customInformation-${information.id}"  value="true" ${values[i] != null ? (values[i].entityType != "product" ? 'checked' : '') : ''}>
                    </g:if>
                    <g:else>
                        <g:if test="${values[i] != null}">
                            <textarea class="wceditor" name="customInformation.${information.id}">${values[i].value}</textarea>
                        </g:if>
                        <g:else>
                            <textarea class="wceditor" name="customInformation.${information.id}"></textarea>
                        </g:else></div>
                    </g:else>
            </div>
        </g:each>
    </div>
</div>