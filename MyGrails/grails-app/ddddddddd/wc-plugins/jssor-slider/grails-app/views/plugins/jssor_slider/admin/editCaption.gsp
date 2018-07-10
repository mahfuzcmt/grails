<%@ page import="com.webcommander.plugin.jssor_slider.constant.DomainConstant" %>
<g:set var="captionType" value="${DomainConstant.CAPTION_TYPE}"/>
<g:set var="transition" value="${DomainConstant.CAPTION_TRANSITIONS}"/>
<g:form controller="jssorSlider" action="updateCaption" class="create-edit-form">
    <input type="hidden" name="id" value="${caption.id}">
    <input type="hidden" name="imageId" value="${params.imageId}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="caption.information"/></h3>
            <div class="info-content"><g:message code="caption.details.info"/></div>
        </div>
        <div class="form-section-container">
            <div class="double-input-row">
                <div class="form-row chosen-wrapper">
                    <label><g:message code="type"/><span class="suggestion">e.g. Button, Description</span></label>
                    <g:select name="type" from="${captionType.collect {g.message(code: it.value)}}" keys="${captionType.values()}" value="${caption.type}" toggle-target="caption-type"/>
                </div><div class="form-row caption-type-button">
                    <label><g:message code="url"/><span class="suggestion">e.g. ${app.baseUrl()}</span></label>
                    <input type="text" name="url" value="${caption.url}">
                </div>
            </div>
            <div class="form-row">
                <label><g:message code="caption"/><span class="suggestion">e.g. Caption content or html DOM</span></label>
                <textarea name="text" maxlength="65000" validation="maxlength[65000]">${caption.text}</textarea>
            </div>
            <div class="double-input-row">
                <div class="form-row chosen-wrapper">
                    <label><g:message code="animation"/><span class="suggestion">e.g. Move Left</span></label>
                    <g:include view="/plugins/jssor_slider/admin/effectChosen.gsp" model="[name: 'animation', caption: true, value: caption.animation]"/>
                </div><div class="form-row mandatory">
                    <label><g:message code="animation.duration"/><span class="suggestion">e.g. 1000 (ms)</span></label>
                    <input type="text" name="duration" maxlength="10" value="${caption.duration}" validation="required digits min[0]" restrict="numeric">
                </div>
            </div>
            <div class="form-row mandatory">
                <label><g:message code="delay"/><span class="suggestion">e.g. 500 (ms)</span></label>
                <input type="text" name="delay" maxlength="10" value="${caption.delay}" validation="required digits min[0]" restrict="numeric">
            </div>
            <div class="form-row">
                <button type="submit" class="submit-button">Save</button>
                <button type="button" class="cancel-button">Cancel</button>
            </div>
        </div>
    </div>
</g:form>