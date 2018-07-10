<%@ page import="com.webcommander.constants.NamedConstants" %>
<form action="${app.relativeBaseUrl()}setting/saveTemplate" method="post" class="create-edit-form edit-popup-form">
    <input type="hidden" name="templateId" value="${template.id}">
     <div class="double-input-row">
         <div class="form-row chosen-wrapper">
             <label><g:message code="type"/></label>
             <ui:namedSelect name="contentType" class="medium" value="${template.contentType}" key="${NamedConstants.EMAIL_CONTENT_TYPE}"/>
         </div><div class="form-row">
             <label><g:message code="subject"/><span class="suggestion"><g:message code="enter.appropriate.subject"/></span></label>
             <input type="text" class="medium" name="subject" value="${template.subject}">
         </div>
     </div>
    <g:if test="${template.isActiveReadonly}">
        <input type="hidden" name="active" value="true">
        <div class="form-row">
            <input type="checkbox" class="single" value="true" checked disabled>
            <span><g:message code="active"/></span>
        </div>
    </g:if>
    <g:else><div class="form-row">
            <input type="checkbox" class="single" name="active" value="true" uncheck-value="false" ${template.active ? 'checked' : ''}>
            <span><g:message code="active"/></span>
        </div>
    </g:else>
    <g:if test="${template.isCcToAdminReadonly}">
        <input type="hidden" name="ccToAdmin" value="${template.ccToAdmin}" >
        <div class="form-row">
            <input type="checkbox" class="single" value="true" ${template.ccToAdmin ? 'checked' : ''} disabled>
            <span><g:message code="cc.to.admin"/></span>
        </div>
    </g:if>
    <g:else><div class="form-row">
            <input type="checkbox" class="single" name="ccToAdmin" value="true" uncheck-value="false" ${template.ccToAdmin ? 'checked' : ''}>
            <span><g:message code="cc.to.admin"/></span>
        </div>
    </g:else>
<div class="bmui-tab">
        <div class="bmui-tab-header-container top-side-header">
            <div class="bmui-tab-header" data-tabify-tab-id="text">
                <span class="title"><g:message code="text"/></span>
            </div>

            <div class="bmui-tab-header" data-tabify-tab-id="html">
                <span class="title"><g:message code="html"/></span>
            </div>
            <div class="toolbar toolbar-right">
                <div class="tool-group">
                    <span class="toolbar-item reset reset-default" title="<g:message code="reset"/>"><i></i></span>
                </div>
            </div>
        </div>
        <div class="bmui-tab-body-container">
            <div id="bmui-tab-text">
                <textarea class="xx-larger data-txt" name="txt" style="height: 400px;">${text}</textarea>
            </div>
            <div id="bmui-tab-html">
                <textarea class="code-mirror-editor no-auto-size xx-larger data-html" name="html" toolbar-type="advanced" style="height: 400px;">${html}</textarea>
            </div>
        </div>
    </div>
    <div class="accordion-panel" accordion-all_close="true">
        <div class="label-bar collapsed"><a class="toggle-icon"></a>
            <g:message code="macros"/>
        </div>
        <%

        %>
        <div class="accordion-item collapsed">
            <util:mailTemplateMacroRenderer macros="${macros}"/>
        </div>
    </div>
    <div class="button-line">
        <button type="submit" class="submit-button edit-popup-form-submit"><g:message code="update"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>
