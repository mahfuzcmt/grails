<%@ page import="org.apache.commons.io.FilenameUtils" %>
<form action="${app.relativeBaseUrl()}eventAdmin/saveSession" method="post" class="create-edit-form downloadable-spec-form" enctype="multipart/form-data">
    <input type="hidden" name="id" value="${eventSession.id}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="event.session.info"/></h3>
            <div class="info-content"><g:message code="form.section.text.event.session.info"/></div>
        </div>
        <div class="form-section-container">

            <div class="form-row mandatory">
                <label><g:message code="name"/></label>
                <input type="text" class="large unique" unique-action="isUniqueEventSession" composite-unique="event.id" name="name"
                       value="${eventSession.name.encodeAsBMHTML()}" validation="required rangelength[2,100]">
                <input type="hidden" name="event.id" value="${event.id}">
            </div>
            <div class="double-input-row">
                <div class="form-row mandatory">
                    <label><g:message code="start.time"/></label>
                    <g:set var="startTime" value="${UUID.randomUUID().toString()}"/>
                    <input type="text" id="${startTime}" class="timefield large" name="startTime" validation="required compareSessionWithEvent[${startTime}, start, ${event.startTime?.toAdminFormat(true, false, session.timezone)}]"
                           value="${eventSession.startTime?.toDatePickerFormat(true, session.timezone)}"/>
                </div><div class="form-row mandatory">
                    <label><g:message code="end.time"/></label>
                    <input type="text" class="timefield large" name="endTime" value="${eventSession.endTime?.toDatePickerFormat(true, session.timezone)}"
                           validation="required compareSessionWithEvent[${endTime}, end, ${event.endTime?.toAdminFormat(true, false, session.timezone)}] compare[${startTime}, date, gt]" depends="#${startTime}"/>
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
                    <g:if test="${eventSession.file}">
                        <span class="file ${eventSession.file.substring(eventSession.file.lastIndexOf(".") + 1, eventSession.file.length())}">
                            <span class="tree-icon"></span>
                        </span>
                        <span class="name">${eventSession.file}</span>
                        <span class="tool-icon remove" file-name="${eventSession.file}"></span>
                    </g:if>
                </div>
            </div>
            <div class="form-row">
                <label><g:message code="description"/></label>
                <textarea class="wceditor no-auto-size xx-large" name="description">${eventSession.description}</textarea>
            </div>
            <div class="form-row">
                <button type="submit" class="submit-button"><g:message code="${eventSession.id ? 'update' : 'save'}"/></button>
                <button type="button" class="cancel-button"><g:message code="cancel"/></button>
            </div>
        </div>
    </div>

</form>