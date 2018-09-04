<div class="form-section custom-information-section">
    <div class="form-section-info">
        <h3><g:message code="custom.information"/></h3>
        <div class="info-content"><g:message code="section.text.setting.product.page.custom.information"/></div>
    </div>
    <div class="form-section-container">
        <table class="custom-information-group">
            <colgroup>
                <col>
                <col>
            </colgroup>
            <tr class="first-row multi-conditions">
                <th>
                    <div class="form-row">
                        <label><g:message code="custom.information.title"/><span class="suggestion">e.g. Related Product</span></label>
                        <input type="text" name="custom.information.title" class="custom-information-title" validation/>
                    </div>
                </th>
                <th>
                    <span class="tool-icon add add-row"></span>
                </th>
            </tr>
            <g:each in="${informations}" var="information">
                <tr information-id="${information.id}">
                    <td class="title editable">${information.title.encodeAsBMHTML()}</td>
                    <td><span class="tool-icon remove"></span></td>
                </tr>
            </g:each>
        </table>
    </div>
</div>