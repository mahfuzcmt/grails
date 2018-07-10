<g:form class="create-edit-form" controller="popupAdmin" action="save">
    <input type="hidden" name="id" value="${popup.id}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="general.info"/></h3>
            <div class="info-content"><g:message code="section.text.general.info"/></div>
        </div>
        <div class="form-section-container">
            <plugin:hookTag hookPoint="popupCreateForm">
                <div class="double-input-row">
                    <div class="form-row">
                        <label><g:message code="name"/></label>
                        <input type="text" name="name" value="${popup.name.encodeAsBMHTML()}" maxlength="100" validation="required maxlength[100]">
                    </div><div class="form-row">
                        <label><g:message code="popup.identifier"/></label>
                        <input type="text" class="unique" name="identifier" value="${popup.identifier}" maxlength="100" validation="required maxlength[100] alphanumeric">
                    </div>
                </div>
                <div class="form-row">
                    <label><g:message code="content.type" /></label>
                    <ui:namedSelect name="contentType" key="${contentTypes}" value="${popup.contentType}" toggle-target="content-type"/>
                </div>
                <div class="form-row content-type-html">
                    <label><g:message code="content"/></label>
                    <textarea class="wceditor" name="content" validation="skip@if{self::hidden} required maxlength[1000]">${popup.content?.encodeAsBMHTML()}</textarea>
                </div>
            </plugin:hookTag>
            <div class="form-row">
                <button type="submit" class="submit-button"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</g:form>