<%@ page import="com.webcommander.constants.Galleries; com.webcommander.design.WidgetService; com.webcommander.constants.NamedConstants; com.webcommander.content.Album" %>
<g:form class="create-edit-form gallery-widget" controller="widget" action="saveGalleryWidget">
    <input type='hidden' name='gallery' value='${config.gallery}'>
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="gallery.selection.info"/></h3>
            <div class="info-content"><g:message code="section.text.gallery.selection"/></div>
        </div>
        <div class="form-section-container">
            <div class="gallery-widget-first-view" step="1">
                <div class="form-row">
                    <label><g:message code="title"/></label>
                    <input type="text" class="medium" name="title" value="${widget.title}">
                </div>
                <div class="form-row">
                    <label><g:message code="type"/></label>
                </div>
                <g:each in="${NamedConstants.GALLERY_CONTENT_TYPES}" var="it">
                    <div class="form-row">
                        <input type="radio" name="galleryContentType" value="${it.key}" ${config.galleryContentType == it.key ? "checked" : ""}>
                        <span><g:message code="${it.value}"/></span>
                    </div>
                </g:each>

                <div class="form-row">
                    <button type="button" class="cancel-button"><g:message code="cancel"/></button>
                    <button type="button" class="submit-button gallery-widget-first-view-submit"><g:message code="next"/></button>
                </div>
            </div>
            <div class="gallery-widget-second-view" step="2">

                <div class="form-row">
                    <button type="button" class="cancel-button"><g:message code="cancel"/></button>
                    <button type="button" class="previous-button" previous="1"><g:message code="previous"/></button>
                    <button type="button" class="submit-button gallery-widget-first-view-submit"><g:message code="next"/></button>
                </div>
            </div>
            <div class="gallery-widget-third-view" step="3">
                <div class="gallery-types-container form-row thicker-row">

                </div>
                <div class="form-row btn-row">
                    <button type="button" class="cancel-button"><g:message code="cancel"/></button>
                    <button type="button" class="previous-button" previous="2"><g:message code="previous"/></button>
                    <button type="button" class="submit-button gallery-widget-first-view-submit"><g:message code="next"/></button>
                </div>
            </div>
            <div class="gallery-widget-last-view" step="4">

            </div>
        </div>
    </div>
</g:form>