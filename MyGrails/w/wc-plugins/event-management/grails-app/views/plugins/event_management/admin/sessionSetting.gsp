<form id="frmSessionSetting" class="create-edit-form" action="${app.relativeBaseUrl()}setting/saveConfigurations" onsubmit="return false" method="POST">
    <input type="hidden" name="type" value="event_session_setting">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="event.session.setting"/></h3>
            <div class="info-content"><g:message code="section.text.setting.event.session"/></div>
        </div>
        <div class="form-section-container">
            <div class="double-input-row">
                <div class="form-row">
                    <input type="checkbox" class="single" name="event_session_setting.enable_session_for_event" value="true" uncheck-value="false" ${config.enable_session_for_event == "true" ? "checked" : ""}>
                    <span><g:message code="enable.session.for.event"/></span>
                </div><div class="form-row">
                    <input type="checkbox" class="single" name="event_session_setting.force_session_selection_during_booking" value="true" uncheck-value="false" ${config.force_session_selection_during_booking == "true" ? "checked" : ""}>
                    <span><g:message code="force.session.selection.during.booking"/></span>
                </div>
            </div>
            <div class="double-input-row">
                <div class="form-row">
                    <input type="checkbox" class="single" name="event_session_setting.include_session_topic_in_event_display" value="true" uncheck-value="false" ${config.include_session_topic_in_event_display == "true" ? "checked" : ""}>
                    <span><g:message code="include.session.topic.in.event.display"/></span>
                </div><div class="form-row">
                    <input type="checkbox" class="single" name="event_session_setting.supply_personalized_program_with_booking" value="true" uncheck-value="false" ${config.supply_personalized_program_with_booking == "true" ? "checked" : ""}>
                    <span><g:message code="supply.personalized.program.with.booking"/></span>
                </div>
            </div>
            <div class="form-row">
                <button class="submit-button" type="submit"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>