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
                <span class="toolbar-item send-mail" title="<g:message code="send.test.mail"/>"><i></i></span>
                <span class="toolbar-item edit-mail" title="<g:message code="edit.mail"/>"><i></i></span>
            </div>
        </div>
    </div>
    <div class="form-row">
       <g:message code="subject"/> ${template.subject}
    </div>
    <div class="form-row">
        <label><g:message code="email.body"/></label>
    </div>
    <div class="bmui-tab-body-container">
        <div id="bmui-tab-text">
            ${text.encodeAsBMHTML()}
        </div>
        <div id="bmui-tab-html">
            ${html}
        </div>
    </div>
</div>