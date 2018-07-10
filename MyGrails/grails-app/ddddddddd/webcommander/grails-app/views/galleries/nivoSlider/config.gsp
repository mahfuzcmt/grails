<div class="gallery-config-view">
    <%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.constants.NamedConstants" %>
    <g:set var="yesno_keys" value="${['true', 'false']}"/>
    <g:set var="yesno_from" value="${[g.message(code: 'yes'), g.message(code: 'no')]}"/>
    <div class="double-input-row">
        <div class="form-row">
            <label><g:message code="width"/></label>
            <input type="text" name="width" value="${widgetConfig['width']}" validation="digits" restrict="numeric" class="small">
            <span class="note">(px)</span>
        </div><div class="form-row">
            <label><g:message code="height"/></label>
            <input type="text" name="height" value="${widgetConfig['height']}" validation="digits" restrict="numeric" class="small">
            <span class="note">(px)</span>
        </div>
    </div>
    <div class="form-row chosen-wrapper">
        <label><g:message code="direction.navigation"/></label>
        <g:select name="directionNav" toggle-target="show-navigation-labels" keys="${yesno_keys}" from="${yesno_from}" value="${widgetConfig['directionNav'] ?: 'false'}" class="small"/>
    </div>
    <div class="double-input-row show-navigation-labels-true">
        <div class="form-row">
            <label><g:message code="label.previous"/></label>
            <input type="text" name="prevText" value="${widgetConfig['prevText'] ?: 's:prev'}" validation="required@if{self::visible}" class="small">
        </div><div class="form-row">
            <label><g:message code="label.next"/></label>
            <input type="text" name="nextText" value="${widgetConfig['nextText'] ?: 's:next'}" validation="required@if{self::visible}" class="small">
        </div>
    </div>
    <div class="form-row manual-advance-row chosen-wrapper">
        <label><g:message code="manual.advance"/></label>
        <g:select name="manualAdvance" keys="${yesno_keys}" from="${yesno_from}" value="${widgetConfig['manualAdvance'] ?: 'false'}" class="small"/>
    </div>
    <div class="form-row chosen-wrapper">
        <label><g:message code="control.navigation"/></label>
        <g:select class="small"  name="controlNav" from="${yesno_from}" keys="${yesno_keys}" value="${widgetConfig['controlNav'] ?: 'false'}" toggle-target="control-as-thumb"/>
    </div>
    <div class="form-row control-as-thumb-true chosen-wrapper">
        <label><g:message code="control.as.thumbs"/></label>
        <g:select name="controlNavThumbs" keys="${yesno_keys}" from="${yesno_from}" value="${widgetConfig['controlNavThumbs'] ?: 'false'}" class="small"/>
    </div>
    <div class="form-row chosen-wrapper">
        <label><g:message code="pause.on.hover"/></label>
        <g:select name="pauseOnHover" keys="${yesno_keys}" from="${yesno_from}" value="${widgetConfig['pauseOnHover'] ?: 'true'}" class="small"/>
    </div>
    <div class="double-input-row">
        <div class="form-row mandatory">
            <label><g:message code="slide.transition.speed"/></label>
            <input type="text" name="animSpeed" value="${widgetConfig['animSpeed'] ?: 1000}" validation="required digits" class="small">
            <span class="note">(ms)</span>
        </div><div class="form-row mandatory">
            <label><g:message code="slide.show.time"/></label>
            <input type="text" name="slideTime" value="${widgetConfig['slideTime'] ?: 4000}" validation="required digits" class="small">
            <span class="note">(ms)</span>
        </div>
    </div>
    <div class="double-input-row">
        <div class="form-row">
            <label><g:message code="effect"/></label>
            <ui:namedSelect class="small" name="effect" key="${NamedConstants.NIVO_SLIDER_EFFECTS}" value="${widgetConfig['effect'] ?: 'random'}"/>
        </div><div class="form-row">
            <label><g:message code="hyperlink.images"/></label>
            <g:select name="customLink" keys="${yesno_keys}" from="${yesno_from}" value="${widgetConfig['customLink'] ?: 'true'}" class="small"/>
        </div>
    </div>

    <div class="button-line">
        <button type="button" class="previous"><g:message code="previous"/></button>
        <button type="submit" class="edit-popup-form-submit submit-button apply"><g:message code="update"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</div>
