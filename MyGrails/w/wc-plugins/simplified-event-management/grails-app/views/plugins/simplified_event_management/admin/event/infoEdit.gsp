<%@ page import="org.apache.commons.io.FilenameUtils; com.webcommander.webcommerce.TaxProfile" %>
<form action="${app.relativeBaseUrl()}simplifiedEventAdmin/saveEvent" method="post" class="create-edit-form downloadable-spec-form" enctype="multipart/form-data">
    <input type="hidden" name="id" value="${event.id}">
    <div class="bmui-tab">
        <div class="bmui-tab-header-container top-side-header">
            <div class="bmui-tab-header" data-tabify-tab-id="general">
                <span class="title"><g:message code="general"/></span>
            </div>

            <div class="bmui-tab-header" data-tabify-tab-id="image">
                <span class="title"><g:message code="image"/></span>
            </div>

            <div class="bmui-tab-header" data-tabify-tab-id="metatag">
                <span class="title"><g:message code="meta.tag"/></span>
            </div>
        </div>
        <div class="bmui-tab-body-container">
            <div id="bmui-tab-general">
                <div class="form-section">
                    <div class="form-section-info">
                        <h3><g:message code="event.info"/></h3>
                        <div class="info-content"><g:message code="form.section.text.event.info"/></div>
                    </div>
                    <div class="form-section-container">
                        <div class="double-input-row">
                            <div class="form-row mandatory">
                                <label><g:message code="event.name"/><span class="suggestion"><g:message code="suggestion.event.name"/></span></label>
                                <input type="text" name="name" class="large" value="${event.name}" validation="required rangelength[2, 100]" maxlength="100">
                            </div><div class="form-row chosen-wrapper">
                                <label><g:message code="title"/><span class="suggestion"><g:message code="suggestion.event.title"/></span></label>
                                <input type="text" name="title" class="large" value="${event.title.encodeAsBMHTML()}" validation="maxlength[200]" maxlength="200">
                            </div>
                        </div>
                        <div class="double-input-row">
                            <div class="form-row">
                                <label><g:message code="heading"/></label>
                                <input type="text" name="heading" class="large" value="${event.heading.encodeAsBMHTML()}" validation="maxlength[200]" maxlength="200">
                            </div><div class="form-row">
                                <label>&nbsp;</label>
                                <input toggle-target="show-purchase-option" class="single" type="checkbox" name="isPublic" value="true" uncheck-value="false" ${event.isPublic ? "checked='checked'" : ""}>
                                <g:message code="public"/>
                            </div>
                        </div>
                        <div class="form-row">
                            <label><g:message code="personalized.program"/></label>
                            <div class="thicker-row">
                                <input name="file" type="file" size-limit="9097152" class="large" text-helper="no">
                            </div>
                        </div>
                        <div class="form-row">
                            <label></label>
                            <div class="personalized-file-block file-selection-queue">
                                <g:if test="${event.file}">
                                    <span class="file ${event.file.substring(event.file.lastIndexOf(".") + 1, event.file.length())}">
                                        <span class="tree-icon"></span>
                                    </span>
                                    <span class="name">${event.file}</span>
                                    <span class="tool-icon remove" file-name="${event.file}"></span>
                                </g:if>
                            </div>
                        </div>
                        <div class="double-input-row">
                            <div class="form-row mandatory">
                                <label><g:message code="start.time"/><span class="suggestion"><g:message code="suggestion.event.start.time"/></span></label>
                                <g:set var="startTime" value="${UUID.randomUUID().toString()}"/>
                                <input type="text" id="${startTime}" class="timefield large" name="startTime" validation="required" value="${event.startTime?.toDatePickerFormat(true, session.timezone)}"/>
                            </div><div class="form-row mandatory">
                                <label><g:message code="end.time"/><span class="suggestion"><g:message code="suggestion.event.end.time"/></span></label>
                                <input type="text" class="timefield large" name="endTime" value="${event.endTime?.toDatePickerFormat(true, session.timezone)}"
                                       validation="required compare[${startTime}, date, gt]" depends="#${startTime}">
                            </div>
                        </div>
                        <div class="double-input-row">
                            <div class="form-row mandatory">
                                <label><g:message code="maximum.ticket.number"/></label>
                                <g:set var="maxTicket" value="${UUID.randomUUID().toString()}"/>
                                <input name="maxTicket" restrict="numeric" id="${maxTicket}" type="text" class="medium" value="${event.maxTicket}" validation="required">
                            </div><div class="form-row">
                                <label><g:message code="maximum.ticket.per.person"/></label>
                                <input name="maxTicketPerPerson" restrict="numeric" type="text" class="medium" value="${event.maxTicketPerPerson}" validation="compare[${maxTicket}, number, lt]">
                            </div>
                        </div>
                        <div class="double-input-row">
                            <div class="form-row mandatory">
                                <label><g:message code="ticket.price"/></label>
                                <input type="text" class="large" name="ticketPrice" value="${event.ticketPrice?.toPrice() ?: ""}" validation="required number max[99999999]" restrict="decimal">
                            </div><div class="form-row">
                                <g:set var="googleMap" value="${UUID.randomUUID().toString()}"/>
                                <label><g:message code="show.google.map"/></label>
                                <input toggle-target="show-latitude-longitude" class="single" type="checkbox" id="${googleMap}" name="showGoogleMap" value="true" uncheck-value="false" ${event.showGoogleMap ? "checked" : ""}>
                            </div>
                        </div>
                        <div class="double-input-row show-latitude-longitude">
                            <div class="form-row mandatory">
                                <label><g:message code="latitude"/></label>
                                <input type="text" class="large" value="${event?.latitude}" name="latitude" validation="required@if{global:#${googleMap}:checked} number max[99999999]">
                            </div><div class="form-row mandatory">
                                <label><g:message code="longitude"/></label>
                                <input type="text" class="large" value="${event?.longitude}" name="longitude" validation="required@if{global:#${googleMap}:checked} number max[99999999]">
                            </div>
                        </div>
                        <div class="form-row">
                            <label><g:message code="tax.profile"/></label>
                            <ui:domainSelect name="taxProfile" class="medium" domain="${TaxProfile}" prepend="${['': g.message(code: "none")]}" value="${event.taxProfile?.id}"/>
                        </div>
                        <div class="form-row mandatory">
                            <label><g:message code="address"/><span class="suggestion"><g:message code="suggestion.address"/></span></label>
                            <textarea class="large" type="text" name="address">${event?.address}</textarea>
                        </div>
                        <div class="form-row">
                            <label><g:message code="event.summary"/><span class="suggestion"><g:message code="suggestion.event.summary"/></span></label>
                            <textarea class="xx-larger" name="summary" validation="maxlength[500]" maxlength="500">${event.summary}</textarea>
                        </div>
                        <div class="form-row tinymce-container">
                            <label><g:message code="event.description"/><span class="suggestion"><g:message code="suggestion.event.description"/></span></label>
                            <textarea class="wceditor no-auto-size xx-larger" style="height: 240px" toolbar-type="advanced" name="description" >${event.description}</textarea>
                        </div>
                    </div>
                </div>
            </div>
            <div id="bmui-tab-image">
                <div class="form-section">
                    <div class="form-section-info">
                        <h3><g:message code="Image"/></h3>
                        <div class="info-content"><g:message code="form.section.text.event.image"/></div>
                    </div>
                    <div class="form-section-container">
                        <input type="file" name="images" file-type="image" queue="event-image-queue" multiple="true" >
                        <div id="event-image-queue" class="multiple-image-queue">
                        </div>
                        <div class="event-image-container">
                            <div class="left-scroller scroll-navigator" style="display: none"></div><div class="right-scroller scroll-navigator" style="display: none"></div>
                            <div class="event-image-wrapper one-line-scroll-content">
                                <g:each in="${event?.images}" var="image">
                                    <div image-id="${image.id}" image-name="${image.name}" class="image-thumb">
                                        <span class="tool-icon remove"></span>
                                        <input type="hidden" name="imageId" value="${image.id}">
                                        <div class="image-container">
                                            <img src="${app.customResourceBaseUrl()}resources/simplified-event/event-${event?.id}/images/100-${image.name}">
                                        </div>
                                    </div>
                                </g:each>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div id="bmui-tab-metatag">
                <div class="form-section">
                    <div class="form-section-info">
                        <h3><g:message code="meta.tag"/></h3>
                        <div class="info-content"><g:message code="form.section.text.event.meta.tag"/></div>
                    </div>
                    <div class="form-section-container">
                        <g:include view="/admin/metatag/metaTagEditor.gsp" model="[metaTags: event.metaTags?: [:]]"/>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div class="form-row wcui-horizontal-tab-button">
        <button type="submit" class="submit-button edit-popup-form-submit"><g:message code="${event.id ? "update" : "save"}"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>