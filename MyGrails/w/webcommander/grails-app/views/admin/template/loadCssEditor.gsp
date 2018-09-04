<div class="header">
    <span class="item-group title"><g:message code="simple.edit.css"/></span>
    <div class="toolbar toolbar-right">
        <div class="tool-group">
            <span class="toolbar-item switch-menu collapsed"><i></i></span>
        </div>
        <div class="tool-group">
            <span class="toolbar-item save disabled" title="<g:message code="save"/>"><i></i></span>
        </div>
    </div>
</div>
<div class="app-tab-content-container body simple-css-panel multi-column three-column">
    <div class="columns first-column">
        <div class="column-content">
            <div class="media-panel css-panel">
                <h4 class="group-label"><g:message code="medias"/></h4>

                <div class="medias css-entry-list">

                </div>

                <div class="add-new-media add-new-entry">
                    <label><g:message code="min.width"/> </label><input type="text" name="min-width">
                    &nbsp;&nbsp;&nbsp;&nbsp;<label><g:message code="max.width"/> </label><input type="text" name="max-width">
                    <span class="btn button-add-new-media add-new-entry" title="<g:message code="add.new.media"/>"><g:message code="add"/></span>
                </div>
            </div>
        </div>
    </div><div class="columns second-column">
        <div class="column-content">
            <div class="rule-panel css-panel">
                <h4 class="group-label"><g:message code="rules"/></h4>
                <div class="rules css-entry-list">

                </div>

                <div class="add-new-rule add-new-entry">
                    <input type="text" class="input-add-new-rule">
                    <span class="btn button-add-new-rule add-new-entry" title="<g:message code="add.new.rule"/>"><g:message code="add"/></span>
                </div>
            </div>
        </div>
    </div><div class="columns last-column">
        <div class="column-content">
            <div class="css-panel attribute-panel">
                <h4 class="group-label"><g:message code="attributes"/></h4>
                <div class="bmui-tab">
                    <div class="bmui-tab-header-container">
                        <div class="bmui-tab-header" data-tabify-tab-id="list">
                            <span class="title"><g:message code="list"/></span>
                        </div>
                        <div class="bmui-tab-header" data-tabify-tab-id="text">
                            <span class="title"><g:message code="text"/></span>
                        </div>
                    </div>
                    <div class="bmui-tab-body-container">
                        <div id="bmui-tab-list">
                            <div class="attribute-list css-entry-list">
                                <div class="attrs">

                                </div>
                            </div>
                            <div class="add-new-attribute add-new-entry">
                                <input type="text" class="input-add-new-attribute">
                                <span class="btn button-add-new-attribute add-new-entry" title="<g:message code="add.new.attribute"/>"><g:message code="add"/></span>
                            </div>
                        </div>

                        <div id="bmui-tab-text">
                            <div class="attr-panel-text">
                                <textarea class="attr-text"></textarea>
                            </div>
                        </div>
                    </div>
                </div>
             </div>
        </div>
    </div>
</div>