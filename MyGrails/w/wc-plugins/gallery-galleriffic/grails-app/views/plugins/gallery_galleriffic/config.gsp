<div class="gallery-config-view">
    <div class="form-row">
        <label><g:message code="layout"/></label>
        <g:select name="layout" keys="${["alternate", "rollover"]}" from="${[g.message(code: "alternate"), g.message(code: "rollover")]}" value="${widgetConfig['layout']}" toggle-target="layout-depends"/>
    </div>
    <div class="double-input-row">
        <div class="form-row mandatory">
            <label><g:message code="max.no.of.thumb"/></label>
            <input type="text" name="numThumbs" value="${widgetConfig['numThumbs'] ?: 20}" validation="required digits gt[0]" restrict="numeric">
        </div><div class="form-row">
            <label><g:message code="auto.play" /></label>
            <input type="checkbox" name="autoStart" class="single" value="true" uncheck-value="false" ${widgetConfig['autoStart'] == "true" ? "checked" : ""}>
        </div>
    </div>
    <g:set var="position" value="${['left', 'right']}"/>
    <div class="form-row layout-depends-rollover">
        <label><g:message code="thumb.position"/></label>
        <g:select name="thumbPositionRollover" keys="${position}" from="${position.collect { g.message(code: it)}}" value="${widgetConfig['thumbPositionRollover'] ?: "left"}"/>
    </div>
    <g:set var="position" value="${['top', 'bottom']}"/>
    <div class="form-row layout-depends-alternate">
        <label><g:message code="thumb.position"/></label>
        <g:select name="thumbPositionAlternate" keys="${position}" from="${position.collect { g.message(code: it)}}" value="${widgetConfig['thumbPositionAlternate'] ?: "top"}"/>
    </div>
    <div class="double-input-row">
        <div class="form-row">
            <label><g:message code="show.caption"/></label>
            <input type="checkbox" class="single" name="showCaption" value="true" uncheck-value="false" ${widgetConfig["showCaption"] == "false" ? "" : "checked"} toggle-target="thumb-depends">
        </div><div class="form-row mandatory">
            <label><g:message code="thumb.offset"/><span class="suggestion">e.g. 180 (px)</span></label>
            <input type="text" name="thumbOffset" value="${widgetConfig['thumbOffset'] ?: 180}" validation="required digits gt[0]" restrict="numeric">
        </div>
    </div>
    <div class="thumb-depends">
        <div class="form-row layout-depends-rollover">
            <label><g:message code="caption.position"/></label>
            <g:select name="captionPositionRollover" keys="${position}" from="${position.collect { g.message(code: it)}}" value="${widgetConfig['captionPositionRollover'] ?: "right"}"/>
        </div>
        <g:set var="position" value="${['top', 'bottom', 'left', 'right']}"/>
        <div class="form-row layout-depends-alternate">
            <label><g:message code="caption.position"/></label>
            <g:select name="captionPositionAlternate" keys="${position}" from="${position.collect { g.message(code: it)}}" value="${widgetConfig['captionPositionAlternate'] ?: "right"}"/>
        </div>
    </div>

    <div class="button-line">
        <button type="button" class="previous"><g:message code="previous"/></button>
        <button type="submit" class="edit-popup-form-submit submit-button apply"><g:message code="update"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</div>
