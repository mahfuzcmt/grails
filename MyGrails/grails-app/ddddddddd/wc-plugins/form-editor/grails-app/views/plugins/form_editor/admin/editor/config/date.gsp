<%@ page import="com.webcommander.util.StringUtil" %>
<div class="body">
    <div type="basic" class="properties config-section">
        <div class="form-row">
            <label><g:message code="html.name"/></label>
            <input type="text" value="" name="field.name" validation="required maxlength[100]" maxlength="100">
        </div>
        <div class="form-row">
            <label><g:message code="label"/></label>
            <input type="text" value="" name="field.label" validation="maxlength[100]" maxlength="100">
        </div>
        <div class="form-row">
            <label><g:message code="html.class.s"/></label>
            <input type="text" value="" name="field.clazz" validation="maxlength[100]" maxlength="100">
        </div>
        <div class="form-row">
            <label><g:message code="placeholder"/></label>
            <input type="text" name="field.placeholder" validation="maxlength[255]" maxlength="255">
        </div>
        <div class="form-row">
            <label><g:message code="hover.text"/></label>
            <input type="text" value="" name="field.title" validation="maxlength[100]" maxlength="100">
        </div>
    </div>
    <div type="validation" class="properties config-section">
        <input type="hidden" name="field.validation" class="validation-field">
        <div class="form-row">
            <input type="checkbox" name="r-validation" class="validation-required single" value="required">
            <label><g:message code="required" /></label>
        </div>
    </div>
    <div type="date-properties" class="date-properties config-section">
        <div class="form-row">
            <label><g:message code="type"/></label>
        </div>
        <div class="form-row">
            <input type="radio" name="field.extra.config.type"  value="date" toggle-target="date-type-config" field-update="true">
            <label><g:message code="date"/> </label>
        </div>
        <div class="form-row">
            <input type="radio" name="field.extra.config.type" value="date_time" toggle-target="date-time-type-config" field-update="true">
            <label><g:message code="date.time"/> </label>
        </div>
        <div class="form-row">
            <input type="radio" name="field.extra.config.type" value="time" toggle-target="time-type-config" field-update="true">
            <label><g:message code="time" /> </label>
        </div>
        <div class="form-row">
            <input type="radio" name="field.extra.config.type" value="day_of_week" field-update="true">
            <label><g:message code="day.of.week"/> </label>
        </div>
        <div class="date-type-config date-time-type-config">
            <div class="form-row">
                <label><g:message code="date.type"/></label>
                <div class="radio-group horizontal">
                    <div class="radio">
                        <input type="radio" name="field.extra.config.date_type" value="calender" field-update="true">
                        <label><g:message code="calender"/></label>
                    </div>
                    <div class="radio">
                        <input type="radio" name="field.extra.config.date_type" value="dropdown" field-update="true" toggle-target="dropdown-date-type-config">
                        <label><g:message code="dropdown"/></label>
                    </div>
                </div>
            </div>
            <div class="dropdown-date-type-config">
                <div class="form-row">
                    <input type="checkbox" name="field.extra.config.show_year" value="true">
                    <label><g:message code="year"/></label>
                </div>
                <div class="form-row">
                    <input type="checkbox" name="field.extra.config.show_month" value="true">
                    <label><g:message code="month"/></label>
                </div>
                <div class="form-row">
                    <input type="checkbox" name="field.extra.config.show_date" value="true">
                    <label><g:message code="date"/></label>
                </div>
            </div>
        </div>
        <div class="form-row date-time-type-config time-type-config">
            <label><g:message code="time.format"/></label>
            <div class="radio-group horizontal">
                <div class="radio">
                    <input type="radio" name="field.extra.config.time_format" value="24">
                    <label><g:message code="24hr"/></label>
                </div>
                <div class="radio">
                    <input type="radio" name="field.extra.config.time_format" value="12">
                    <label><g:message code="12hr"/></label>
                </div>
            </div>
        </div>
        <div class="datefield-between date-type-config date-time-type-config">
            <div class="form-row">
                <label><g:message code="date.range"/></label>
            </div>
            <div class="double-input-row">
                <div class="form-row">
                    <input type="text" class="datefield-from smaller" name="field.extra.config.date_range_from" placeholder="<g:message code="start.date"/>">
                </div><div class="form-row">
                    <input type="text" class="datefield-to smaller" name="field.extra.config.date_range_to" placeholder="<g:message code="end.date"/>">
                </div>
            </div>
        </div>
        <div class="time-selector-between date-time-type-config time-type-config">
            <div class="form-row">
                <label><g:message code="time.range"/></label>
            </div>
            <div class="double-input-row">
                <div class="form-row">
                    <input type="text" class="time-selector-from smaller dropdown-time-picker" data-format="HH:mm" data-template="HH : mm" name="field.extra.config.time_range_from">
                </div><div class="form-row">
                    <input type="text" class="time-selector-to smaller dropdown-time-picker" data-format="HH:mm" data-template="HH : mm" name="field.extra.config.time_range_to">
                </div>
            </div>
        </div>
    </div>
</div>