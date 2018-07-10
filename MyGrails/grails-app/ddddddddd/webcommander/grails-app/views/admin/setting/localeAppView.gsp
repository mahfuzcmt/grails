<form id="frmLocaleSettings" class="create-edit-form" action="${app.relativeBaseUrl()}setting/saveConfigurations" method="POST">
    <g:set var="dateForm" value="['1/21/2001', '21/1/2001', 'Mon, 1 Jan 2001', 'Mon, 1 January 2001', 'Monday, January 01, 2001', 'January 01, 2001', '1/21/01', '2001-01-21', '1-Jan-01', '1.21.2001', '21.1.2001', 'Jan 1, 2011', 'Jan-01']"></g:set>
    <g:set var="dateKeys" value="['M/d/yyyy', 'd/M/yyyy','EEE, d MMM yyyy', 'EEE, d MMMM yyyy', 'EEEE, MMMM dd, yyyy', 'MMMM dd, yyyy', 'M/d/yy', 'yyyy-MM-dd', 'd-MMM-yy', 'M.d.yyyy', 'd.M.yyyy', 'MMM d, yyyy', 'MMM-yy']"></g:set>
    <g:set var="timeForm" value="['9:4 AM', '9:4:5 AM', '09:04 AM', '09:04:05 AM', '23:4', '23:4:5', '23:04', '23:04:05']"></g:set>
    <g:set var="timeKeys" value="['h:m a', 'h:m:s a', 'hh:mm a', 'hh:mm:ss a', 'H:m', 'H:m:s', 'HH:mm', 'HH:mm:ss']"></g:set>

    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="email"/></h3>
            <div class="info-content"><g:message code="section.text.setting.local.email"/></div>
        </div>
        <div class="form-section-container">

            <input type="hidden" name="type" value="email">

            <div class="double-input-row">
                <div class="form-row chosen-wrapper">
                    <label><g:message code="date.format"/><span class="suggestion"><g:message code="suggestion.setting.date.formate"/></span></label>
                    <g:select class="medium" name="email.date_format" from="${dateForm}" keys="${dateKeys}" value="${email.date_format}"/>
                </div><div class="form-row chosen-wrapper">
                    <label><g:message code="time.format"/><span class="suggestion"><g:message code="suggestion.setting.time.format"/></span></label>
                    <g:select class="medium" name="email.time_format" from="${timeForm}" keys="${timeKeys}" value="${email.time_format}"/>
                </div>
            </div>
            <div class="form-row chosen-wrapper">
                <label><g:message code="time.zone"/><span class="suggestion"><g:message code="suggestion.setting.time.zone"/></span></label>
                <g:select class="medium" name="email.time_zone" keys="${timeZone.keySet()}" from="${timeZone.values()}" value="${email.time_zone}"/>
            </div>
        </div>
    </div>
    <div class="section-separator"></div>
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="site"/></h3>
            <div class="info-content"><g:message code="section.text.setting.local.site"/></div>
        </div>
        <div class="form-section-container">

            <input type="hidden" name="type" value="locale">

            <div class="double-input-row">
                <div class="form-row chosen-wrapper">
                    <label><g:message code="date.format"/><span class="suggestion"><g:message code="suggestion.setting.date.formate"/></span></label>
                    <g:select class="medium" name="locale.site_date_format" from="${dateForm}" keys="${dateKeys}" value="${locale.site_date_format}"/>
                </div><div class="form-row chosen-wrapper">
                    <label><g:message code="time.format"/><span class="suggestion"><g:message code="suggestion.setting.time.format"/></span></label>
                    <g:select class="medium" name="locale.site_time_format" from="${timeForm}" keys="${timeKeys}" value="${locale.site_time_format}"/>
                </div>
            </div>
        </div>
    </div>
    <div class="section-separator"></div>
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="admin"/></h3>
            <div class="info-content"><g:message code="section.text.setting.local.admin"/></div>
        </div>
        <div class="form-section-container">
            <div class="double-input-row">
                <div class="form-row chosen-wrapper">
                    <label><g:message code="date.format"/><span class="suggestion"><g:message code="suggestion.setting.date.formate"/></span></label>
                    <g:select class="medium" name="locale.admin_date_format" from="${dateForm}" keys="${dateKeys}" value="${locale.admin_date_format}"/>
                </div><div class="form-row chosen-wrapper">
                    <label><g:message code="time.format"/><span class="suggestion"><g:message code="suggestion.setting.time.format"/></span></label>
                    <g:select class="medium" name="locale.admin_time_format" from="${timeForm}" keys="${timeKeys}" value="${locale.admin_time_format}"/>
                </div>
            </div>
            <div class="form-row">
                <button class="submit-button" type="submit"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>
