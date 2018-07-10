<%@ page import="com.webcommander.constants.DomainConstants as DC; com.webcommander.constants.NamedConstants as NC;" %>
<form action="${app.relativeBaseUrl()}setting/saveConfigurations" method="post" onsubmit="return false" class="create-edit-form">
    <input type="hidden" name="type" value="${type}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="front.end.pages"/></h3>
            <div class="info-content"><g:message code="section.text.front.end.pages.info"/></div>
        </div>
        <div class="form-section-container">
            <plugin:hookTag hookPoint="frontendPagesSetting" attrs="${[config: config]}">
                <div class="form-row">
                    <label><g:message code="header.code"/><span class="suggestion">e.g. enter html code here</span></label>
                    <textarea name="${type}.header_code" maxlength="5000" validation="maxlength[5000]">${config.header_code}</textarea>
                </div>
                <div class="form-row">
                    <label><g:message code="body.beginning.code"/><span class="suggestion">e.g. enter html code here</span></label>
                    <textarea name="${type}.body_beginning_code" maxlength="5000" validation="maxlength[5000]">${config.body_beginning_code}</textarea>
                </div>
                <div class="form-row">
                    <label><g:message code="body.ending.code"/><span class="suggestion">e.g. enter html code here</span></label>
                    <textarea name="${type}.body_ending_code" maxlength="5000" validation="maxlength[5000]">${config.body_ending_code}</textarea>
                </div>
            </plugin:hookTag>
            <div class="form-row">
                <button class="submit-button" type="submit"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>