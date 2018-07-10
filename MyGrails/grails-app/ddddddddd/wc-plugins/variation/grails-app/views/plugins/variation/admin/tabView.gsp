<variation:allowed>
    <div class="header multi-tab-shared-header">
        <span class="header-title"></span>
            <div class="toolbar toolbar-right">
                <div class="tool-group">
                    <label><g:message code="type"/>:</label>
                    <g:select name="vaiationTypeSelector" from="${[g.message(code: "none"), g.message(code: "disposable")]}" keys="${["all", "disposable"]}" class="small"/>
                </div>
            </div>
    </div>

    <div class="bmui-tab left-side-header">
        <div class="bmui-tab-header-container">
            <div class="bmui-tab-header" data-tabify-tab-id="type" data-tabify-url="${app.relativeBaseUrl()}variationAdmin/loadVariationTypes">
                <span class="title"><g:message code="variation.type"/></span>
            </div>
            <div class="bmui-tab-header" data-tabify-tab-id="option" data-tabify-url="${app.relativeBaseUrl()}variationAdmin/loadVariationOptions">
                <span class="title"><g:message code="variation.value"/></span>
            </div>
        </div>
        <div class="bmui-tab-body-container"></div>
    </div>
</variation:allowed>