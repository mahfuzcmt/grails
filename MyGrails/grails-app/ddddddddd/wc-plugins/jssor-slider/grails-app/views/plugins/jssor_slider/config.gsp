<%@ page import="com.webcommander.plugin.jssor_slider.constant.DomainConstant" %>
<div class="gallery-config-view create-edit-form">
    <div class="form-row">
        <label class="label-inline"><g:message code="auto.play"/></label>
        <input type="checkbox" class="single" name="auto_play" value="true" ${widgetConfig['auto_play'] == "true" || !widgetConfig.containsKey("auto_play") ? "checked" : ""} uncheck-value="false">

        <label class="label-inline"><g:message code="hyperlink.images"/></label>
        <input type="checkbox" class="single" name="hyperlink_images" value="true" ${widgetConfig['hyperlink_images'] == "true" ? "checked" : ""} uncheck-value="false">

        <label class="label-inline"><g:message code="pause.on.hover"/></label>
        <input type="checkbox" class="single" name="pause_on_hover" value="3" ${widgetConfig['pause_on_hover'] == "3" ? "checked" : ""} uncheck-value="0">

        <label class="label-inline"><g:message code="scale.slider"/></label>
        <input type="checkbox" class="single" name="scale_slider" value="true" ${widgetConfig['scale_slider'] == "true" ? "checked" : ""}>

    </div>
    <div class="form-row">
        <label><g:message code="slide.transition.effect"/></label>
        <g:include view="/plugins/jssor_slider/admin/effectChosen.gsp" model="[name: 'sliding_effect', clazz: 'small', value: widgetConfig['sliding_effect']]"/>
    </div>

    <g:set var="navigation" value="${DomainConstant.NAVIGATION_VISIBILITY}"/>
    <div class="double-input-row">
        <div class="form-row">
            <label><g:message code="arrow.visibility"/></label>
            <g:select name="arrow_chance_to_show" from="${navigation.collect {g.message(code: it.key)}}" value="${widgetConfig['arrow_chance_to_show']}" keys="${navigation.values()}" class="small"/>
        </div><div class="form-row mandatory">
            <label><g:message code="slide.duration"/></label>
            <input type="text" name="slide_duration" value="${widgetConfig['slide_duration']?: '800'}" validation="digits required" class="small" restrict="numeric">
            <span class="note">(ms)</span>
        </div>
    </div>
    <div class="double-input-row">
        <div class="form-row">
            <label><g:message code="bullet.visibility"/></label>
            <g:select name="bullet_chance_to_show" from="${navigation.collect {g.message(code: it.key)}}" value="${widgetConfig['bullet_chance_to_show']}" keys="${navigation.values()}" class="small"/>
        </div><div class="form-row mandatory">
            <label><g:message code="auto.play.interval"/></label>
            <input type="text" name="auto_play_interval" value="${widgetConfig['auto_play_interval']?: '1000'}" validation="digits required" class="small" restrict="numeric">
            <span class="note">(ms)</span>
        </div>
    </div>

    <div class="form-row">
        <button type="button" class="previous"><g:message code="previous"/></button>
        <button type="submit" class="edit-popup-form-submit submit-button apply"><g:message code="update"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</div>
