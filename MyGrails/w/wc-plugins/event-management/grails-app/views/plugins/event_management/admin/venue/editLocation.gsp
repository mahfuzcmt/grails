<%@ page import="com.webcommander.admin.Operator" %>
<form action="${app.relativeBaseUrl()}eventAdmin/saveVenueLocation" method="post" class="create-edit-form" enctype="multipart/form-data">
    <input type="hidden" name="id" value="${location?.id}">
    <input type="hidden" name="venueId" value="${venueId}">
    <div class="bmui-tab">
        <div class="bmui-tab-header-container top-side-header">
            <div class="bmui-tab-header" data-tabify-tab-id="general">
                <span class="title"><g:message code="general"/></span>
            </div>

            <div class="bmui-tab-header" data-tabify-tab-id="image">
                <span class="title"><g:message code="image"/></span>
            </div>
        </div>
        <div class="bmui-tab-body-container">
            <div id="bmui-tab-general">
                <div class="form-section">
                    <div class="form-section-info">
                        <h3><g:message code="location.info"/> </h3>
                        <div class="info-content"><g:message code="form.section.text.event.location"/></div>
                    </div>
                    <div class="form-section-container">
                        <div class="form-row mandatory">
                            <label><g:message code="name"/></label>
                            <input type="text" name="name" class="large unique" value="${location?.name.encodeAsBMHTML()}" validation="required rangelength[2, 100]"
                                   maxlength="100" unique-action="isUniqueVenueLocation" composite-unique="venue.id">
                            <input type="hidden" name="venue.id" value="${venueId}">
                        </div>
                        <div class="form-row">
                            <label><g:message code="organiser"/></label>
                            <ui:domainSelect class="large organiser-selector" name="organiser" domain="${Operator}" text="fullName" value="${location?.organiser?.id}"/>
                        </div>
                        <div class="form-row">
                            <label><g:message code="description"/></label>
                            <textarea class="wceditor no-auto-size xx-larger" style="height: 240px" data-type="advanced" name="description">${location?.description}</textarea>
                        </div>
                    </div>
                </div>
            </div>
            <div id="bmui-tab-image">
                <div class="form-section">
                    <div class="form-section-info">
                        <h3><g:message code="location.image"/> </h3>
                        <div class="info-content"><g:message code="form.section.text.event.location.image"/></div>
                    </div>
                    <div class="form-section-container">
                        <input type="file" name="images" file-type="image" queue="location-image-queue" multiple="true" >
                        <div id="location-image-queue" class="multiple-image-queue">
                        </div>
                        <hr>
                        <div class="location-image-container">
                            <div class="left-scroller scroll-navigator" style="display: none"></div><div class="right-scroller scroll-navigator" style="display: none"></div>
                            <div class="location-image-wrapper one-line-scroll-content">
                                <g:each in="${location?.images}" var="image">
                                    <div image-id="${image.id}" image-name="${image.name}" class="image-thumb">
                                        <span class="tool-icon remove"></span>
                                        <input type="hidden" name="imageId" value="${image.id}">
                                        <div class="image-container">
                                            <img src="${app.customResourceBaseUrl()}resources/venue-location/location-${location?.id}/100-${image.name}">
                                        </div>
                                    </div>
                                </g:each>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="form-section">
                <div class="form-row wcui-horizontal-tab-button">
                    <button type="submit" class="submit-button edit-popup-form-submit"><g:message code="${location?.id ? "update" : "save"}"/></button>
                    <button type="button" class="cancel-button"><g:message code="cancel"/></button>
                </div>
            </div>
        </div>
    </div>
</form>