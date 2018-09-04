<div class="custom-project-details">
    <input type="hidden" name="detailsId" value="${details.id}">
    <div class="header">
        <div class="tool-bar right">
            <span class="toolbar-btn add-file"><g:message code="add.file"/></span>
        </div>
    </div>
    <div class="body">
        <div class="details">${details.details}</div>
    </div>
    <div class="footer">
        <div class="files">
            <g:each in="${details.files}" var="file">
                <span class="file ${file.ext}">${file.name}.${file.ext}</span>
            </g:each>
        </div>
    </div>
</div>