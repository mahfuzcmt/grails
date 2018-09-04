<%@ page import="com.webcommander.plugin.anything_slider.AnythingSliderConstants" %>
<div class="gallery-config-view create-edit-form">
    <div class="double-input-row">
        <div class="form-row mandatory">
            <label><g:message code="min.height"/></label>
            <input type="text" name="height" value="${widgetConfig['height']?: '300'}" validation="digits required min[300]" class="small" restrict="numeric">
            <span class="note">(px)</span>
        </div><div class="form-row">
            <label><g:message code="theme"/></label>
            <g:select name="theme" from="${AnythingSliderConstants.THEME.values()}" value="${widgetConfig['theme']}" keys="${AnythingSliderConstants.THEME.keySet()}" class="small"/>
        </div>
    </div>
    <div class="double-input-row">
        <div class="form-row mandatory">
            <label><g:message code="starting.slide.number"/></label>
            <input type="text" name="start_panel" value="${widgetConfig['start_panel'] ?: "1"}" validation="digits required" restrict="numeric" class="small">
        </div><div class="form-row">
            <label><g:message code="mode"/></label>
            <g:select name="mode" from="${AnythingSliderConstants.MODE.values()}" value="${widgetConfig['mode']}" keys="${AnythingSliderConstants.MODE.keySet()}" toggle-target="transition-mode" class="small"/>
        </div>
    </div>
    <div class="double-input-row transition-mode-h">
        <div class="form-row">
            <label><g:message code="show.multiple.slide"/></label>
            <input type="checkbox" class="single" name="show_multiple" value="true" ${widgetConfig['show_multiple'] == "true" ? "checked" : ""} uncheck-value="false" toggle-target="show-multiple">
        </div><div class="form-row mandatory show-multiple">
            <label><g:message code="slide.at.once"/></label>
            <input type="text" name="slide_at_once" value="${widgetConfig['slide_at_once'] ?: "5"}" validation="digits required@if{self::visible} gt[1]" restrict="numeric" class="small">
        </div>
    </div>
    <div class="double-input-row">
        <div class="form-row">
            <label><g:message code="transition.effect"/></label>
            <g:select name="transition_effect" from="${AnythingSliderConstants.EASING.values()}" value="${widgetConfig['transition_effect']}" keys="${AnythingSliderConstants.EASING.keySet()}" class="small"/>
        </div><div class="form-row">
            <label><g:message code="animation.time"/></label>
            <input type="text" name="animation_time" value="${widgetConfig['animation_time']?: '600'}" validation="digits min[500]" class="small" restrict="numeric">
            <span class="note">(ms)</span>
        </div>
    </div>
    <div class="double-input-row">
        <div class="form-row">
            <label><g:message code="hyperlink.images"/></label>
            <input type="checkbox" class="single" name="hyperlink_images" value="true" ${widgetConfig['hyperlink_images'] == "true" ? "checked" : ""} uncheck-value="false">
        </div><div class="form-row">
            <label><g:message code="build.arrows"/></label>
            <input type="checkbox" class="single" name="build_arrows" value="true" ${widgetConfig['build_arrows'] == "true" ? "checked" : ""} uncheck-value="false" toggle-target="build-arrow">
        </div>
    </div>

    <div class="double-input-row">
        <div class="form-row">
            <label><g:message code="build.navigation"/></label>
            <input type="checkbox" class="single" name="build_navigation" value="true" ${widgetConfig['build_navigation'] == "true" ? "checked" : ""} uncheck-value="false" toggle-target="build-navigation">
        </div><div class="form-row mandatory build-navigation">
            <label><g:message code="navigation.item"/></label>
            <input type="text" name="navigation_size" value="${widgetConfig['navigation_size'] ?: "0"}" validation="required@if{self::visible} digits range[0,20]" restrict="numeric" class="small">
        </div>
    </div>
    <div class="double-input-row">
        <div class="form-row">
            <label><g:message code="build.start.stop"/></label>
            <input type="checkbox" class="single" name="build_start_stop" value="true" ${widgetConfig['build_start_stop'] == "true" ? "checked" : ""} uncheck-value="false" toggle-target="start-stop">
        </div><div class="form-row">
            <label><g:message code="enable.keyboard"/></label>
            <input type="checkbox" class="single" name="enable_keyboard" value="true" ${widgetConfig['enable_keyboard'] == "true" ? "checked" : ""} uncheck-value="false">
        </div>
    </div>
    <div class="double-input-row">
        <div class="form-row start-stop">
            <label><g:message code="start.text"/></label>
            <input type="text" name="start_text" value="${widgetConfig['start_text'] ?: g.message(code: "start")}" validation="maxlength[50]" maxlength="50" class="small">
        </div><div class="form-row start-stop">
            <label><g:message code="stop.text"/></label>
            <input type="text" name="stop_text" value="${widgetConfig['stop_text'] ?: g.message(code: "stop")}" validation="maxlength[50]" maxlength="50" class="small">
        </div>
    </div>
    <div class="double-input-row build-arrow">
        <div class="form-row">
            <label><g:message code="auto.hide.toggle.arrows"/></label>
            <input type="checkbox" class="single" name="toggle_arrows" value="true" ${widgetConfig['toggle_arrows'] == "true" ? "checked" : ""} uncheck-value="false">
        </div><div class="form-row">
            <label><g:message code="auto.hide.toggle.controls"/></label>
            <input type="checkbox" class="single" name="toggle_controls" value="true" ${widgetConfig['toggle_controls'] == "true" ? "checked" : ""} uncheck-value="false">
        </div>
    </div>
    <div class="double-input-row">
        <div class="form-row">
            <label><g:message code="auto.play"/></label>
            <input type="checkbox" class="single" name="auto_play" value="true" ${widgetConfig['auto_play'] == "false" ? "" : "checked"} uncheck-value="false" toggle-target="auto-play">
        </div><div class="form-row">
            <label><g:message code="infinite.slides"/></label>
            <input type="checkbox" class="single" name="infinite_slides" value="true" ${widgetConfig['infinite_slides'] == "false" ? "" : "checked"} uncheck-value="false">
        </div>
    </div>
    <div class="form-row mandatory auto-play">
        <label><g:message code="delay"/></label>
        <input type="text" name="delay" value="${widgetConfig['delay']?:'3000'}" validation="digits required@if{self::visible} min[1000]" class="small" restrict="numeric">
        <span class="note">(ms)</span>
    </div>
    <div class="double-input-row">
        <div class="form-row">
            <label><g:message code="pause.on.hover"/></label>
            <input type="checkbox" class="single" name="pause_on_hover" value="true" ${widgetConfig['pause_on_hover'] == "true" ? "checked" : ""} uncheck-value="false">
        </div><div class="form-row">
            <label><g:message code="play.right.to.left"/></label>
            <input type="checkbox" class="single" name="play_rtl" value="true" ${widgetConfig['play_rtl'] == "true" ? "checked" : ""} uncheck-value="false">
        </div>
    </div>
    <div class="double-input-row">
        <div class="form-row">
            <label><g:message code="alternate.text"/></label>
            <input type="checkbox" class="single" name="show_caption" value="true" ${widgetConfig['show_caption'] == "true" ? "checked" : ""} uncheck-value="false">
        </div><div class="form-row build-navigation">
            <label><g:message code="show.thumbnails"/></label>
            <input type="checkbox" class="single" name="show_thumbnails" value="true"  ${widgetConfig['show_thumbnails'] == "true" ? "checked" : ""} uncheck-value="false">
        </div>
    </div>
    <div class="button-line">
        <button type="button" class="previous"><g:message code="previous"/></button>
        <button type="submit" class="edit-popup-form-submit submit-button apply"><g:message code="update"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</div>
