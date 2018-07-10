<%@ page import="com.webcommander.constants.NamedConstants; com.webcommander.constants.DomainConstants" %>
<g:form class="edit-popup-form create-edit-form" controller="widget" action="saveYoutubeWidget">
    <input type="hidden" name="mediaUrl" class="media-url" value="${widget.content}">
    <span class="configure-btn" title="<g:message code="configuration"/>"><i></i></span>
    <div class="form-section">
        <div class="form-section-info">
            <div class="form-section-info">
                <h3><g:message code="youtube.selection.info"/></h3>
                <div class="info-content"><g:message code="section.text.youtube.selection"/></div>
            </div>
        </div>
        <div class="form-section-container">
            <div class="widget-config-panel youtube-widget-config">
                <div class="form-row">
                    <label><g:message code="title" /></label>
                    <input type="text" name="title" class="medium" maxlength="255" validation= " maxlength[255]" value="${widget.title}">
                </div>
            </div>
            <div class="form-row video-source">
                <label><g:message code="video.source"/> </label>
                <input type="text" class="medium youtube-source-url">
                <button class="small search" type="button"><g:message code="search"/></button>
            </div>
            <div class="form-row youtube-item-container">
                <div class="navigation-wrapper">
                    <span class="navigation left disabled"><g:message code="prev" /></span>
                    <span class="navigation right disabled"><g:message code="next"/> </span>
                </div>
                <div class="resultViewer">

                </div>
            </div>
            <div class="double-input-row">
                <div class="form-row">
                    <label><g:message code="width" /></label>
                    <input type="text" name="width" class="medium" validation="number maxlength[3]" value="${config.width}"/>
                </div><div class="form-row">
                    <label><g:message code="height" /></label>
                    <input type="text" name="height" class="medium" validation="number maxlength[3]" value="${config.height}" />
                </div>
            </div>
            <div class="double-input-row">
                <div class="form-row">
                    <label><g:message code="auto.play"/></label>
                    <input type="checkbox" class="single" name="autoPlay" value="1" uncheck-value="0" ${config.autoPlay == "1" ? "checked" : ""}>
                </div><div class="form-row">
                    <label><g:message code="show.suggested.videos.finishes"/></label>
                    <input type="checkbox" class="single" name="showSuggesion" value="1" uncheck-value="0" ${config.showSuggesion == "1" ? "checked" : ""}>
                </div>
            </div>
            <div class="double-input-row">
                <div class="form-row">
                    <label><g:message code="show.player.controls"/></label>
                    <input type="checkbox" class="single" name="showControls" value="1" uncheck-value="0" ${config.showControls == "1" ? "checked" : ""}>
                </div><div class="form-row">
                    <label><g:message code="show.video.title.player.actions"/></label>
                    <input type="checkbox" class="single" name="showInfo" value="1" uncheck-value="0" ${config.showInfo == "1" ? "checked" : ""}>
                </div>
            </div>
             <div class="form-row">
                 <label><g:message code="allow.full.screen"/> </label>
                 <input type="radio" name="allowFullScreen" class="radio" value="yes" ${config.allowFullScreen == "yes" ? "checked" : ""}/> <g:message code="yes"/>
                 <input type="radio" name="allowFullScreen" class="radio" value="no"  ${config.allowFullScreen == "no" ? "checked" : ""}/> <g:message code="no"/>
             </div>
            <div class="button-line">
                <button type="submit" class="submit-button"><g:message code="update"/></button>
                <button type="button" class="cancel-button"><g:message code="cancel"/></button>
            </div>
        </div>
    </div>
</g:form>

