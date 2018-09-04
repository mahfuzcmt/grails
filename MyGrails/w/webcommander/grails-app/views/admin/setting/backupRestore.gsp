<div class="create-edit-form">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="backup"/></h3>
            <div class="info-content"><g:message code="section.text.backup.info"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row">
                <button class="submit-button backup" type="submit"><g:message code="backup.now"/></button>
            </div>
        </div>
    </div>
</div>
<div class="section-separator"></div>
<div class="create-edit-form">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="restore"/></h3>
            <div class="info-content"><g:message code="section.text.restore.info"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row chosen-wrapper">
                <g:set var="restorePoint" value="${backupList ?: []}"/>
                <g:set var="text" value="${restorePoint.collect {Date.parse("E MMM dd H:m:s z yyyy", it.replaceAll("_", " ").replaceAll("-", ":")).toAdminFormat(true, false, session.timezone)}}"/>
                <label><g:message code="restore.point"/><span class="suggestion"><g:message code="suggestion.setting.backup.restore.point"/></span></label>
                <g:select class="medium" name="restorePoint"  from="${text}" keys="${restorePoint}" noSelection="${['': g.message(code: 'select.restore.points')]}"/>
            </div>
            <div class="form-row">
                <label>&nbsp;</label>
                <button class="submit-button restore" type="submit"><g:message code="restore"/></button>
            </div>
        </div>
    </div>
</div>