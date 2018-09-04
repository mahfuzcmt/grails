<form class="ask-question-setting-form create-edit-form" action="${app.relativeBaseUrl()}setting/saveConfigurations" method="post" id="askQuestionSettingForm">
    <input type="hidden" name="type" value="ask_question">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="ask.question"/></h3>
            <div class="info-content"><g:message code="section.text.ask.question.setting.info"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row thicker-row chosen-wrapper">
                <label>&nbsp;</label>
                <input type="checkbox" class="single" name="ask_question.ask_question" value="on" uncheck-value="off" ${config.ask_question == 'on' ? 'checked="checked"' : ''} />
                <g:message code="ask.question"/>
            </div>
            <div class="form-row">
                <button type="submit" class="submit-button ask-question-setting-form-submit"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>