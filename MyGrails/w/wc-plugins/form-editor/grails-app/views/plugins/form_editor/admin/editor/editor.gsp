<%@ page import="com.webcommander.util.StringUtil" %>
<div class="form-editor-panel">
    <input type="hidden" name="id" value="${form.id}">
    <div class="editor-work-area">
        <div class="field-tools left-bar">
            <g:include view="/plugins/form_editor/admin/editor/setupConfig.gsp"/>
            <div class="field-config">
                <span class="tool-group toolbar-btn back"><i></i><g:message code="back.general.settings"/></span>
                <div class="body">

                </div>
            </div>
            <g:include view="/plugins/form_editor/admin/editor/elementTemplate.gsp"/>
        </div>
        <div class="app-tab-content-container">
            <div class="field-drop-zone-wrapper">
                <g:each in="${form.fields}" var="field">
                    <div class="field-drop-zone">
                        <wcform:renderRow field="${field}"/>
                    </div>
                </g:each>
                <div class="field-drop-zone empty-zone"></div>
                <div class="blank-form-msg${form.fields.size() == 0 ? "" : " display-none"}">
                    <span class="icon-block"></span>
                    <p><g:message code="form.currently.empty"/></p>
                    <p><g:message code="please.click.sign.add.elm" encodeAs="raw" args="${['<span class="too-icon add add-elm"> + </span>']}"/></p>
                </div>
            </div>
        </div>
    </div>
    <div class="edit-popup-form display-none">
        <div class="field-thumbs">
            <div field-type="textBox" title="Text Box" class="field-thumb selected textBox"><span class="icon"></span><span class="label"><g:message code="single.line"/> </span></div>
            <div field-type="textArea"  title="Text Area" class="field-thumb textArea"><span class="icon"></span><span class="label"><g:message code="paragraph"/></span></div>
            <div field-type="dropDown" title="Drop Down" class="field-thumb dropDown radioBox checkBox"><span class="icon"></span><span class="label"><g:message code="single.multi.select"/></span></div>
            <div field-type="date" title="Date" class="field-thumb date"><span class="icon"></span><span class="label"><g:message code="date.and.time"/> </span></div>
            <div field-type="file" title="File" class="field-thumb file"><span class="icon"></span><span class="label"><g:message code="file"/></span></div>
            <div field-type="label" title="Label" class="field-thumb label"><span class="icon"></span><span class="label"><g:message code="label"/></span></div>
            <div field-type="spacer" title="Spacer" class="field-thumb spacer"><span class="icon"></span><span class="label"><g:message code="spacer"/></span></div>
            <div field-type="text" title="Plain Text" class="field-thumb text"><span class="icon"></span><span class="label"><g:message code="plain.text"/></span></div>
        </div>
        <div class="button-line">
            <button type="submit" class="submit-button edit-popup-form-submit"><g:message code="add"/> </button>
            <button type="button" class="cancel-button"><g:message code="cancel"/></button>
        </div>
    </div>
</div>