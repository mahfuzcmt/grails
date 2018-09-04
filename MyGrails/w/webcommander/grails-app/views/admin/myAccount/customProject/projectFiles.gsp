<%@ page import="com.webcommander.converter.json.JSON" %>
<div class="custom-project-files">
    <input type="hidden" name="projectId" value="${params.id}">
    <div class="header">
        <span class="heading-title"><g:message code="files"/> </span>
        <div class="tool-bar right">
            <span class="toolbar-btn add-file"><g:message code="add.file"/></span>
        </div>
    </div>
    <div class="body">
        <g:each in="${files}" var="projectFile">
            <div class="project-file">
                <div class="top">
                    <span class="owner-name">${projectFile.addedBy}</span>
                    <span class="date">${JSON.dateFormatter.parse(projectFile.created).toAdminFormat(true, false, session.timezone)}</span>
                </div>
                <div class="description">${projectFile.description}</div>
                <div class="bottom files">
                    <g:each in="${projectFile.files}" var="file">
                        <span class="file ${file.ext}">${file.fullName}</span>
                    </g:each>
                </div>
            </div>
        </g:each>
    </div>
</div>