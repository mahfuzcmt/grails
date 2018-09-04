<div id="form-fields-template-container" style="display: none;">
    <div class="form-field-template textBox-template">
        <input readonly="readonly" class="large" type="text">
    </div>
    <div class="form-field-template date-template">
        <div class="sub-template default">
            <span class="Zebra_DatePicker_Icon_Wrapper" style="display: inline-block; position: relative; float: none; top: auto; right: auto; bottom: auto; left: auto;">
                <input type="text" class="datefield-from smaller" name="field.extra.config.date_range_from" readonly="readonly" style="position: relative; top: auto; right: auto; bottom: auto; left: auto;">
                <button type="button" class="Zebra_DatePicker_Icon Zebra_DatePicker_Icon_Inside" style=""></button>
            </span>
        </div>
        <div class="sub-template combodate hidden date_time-dropdown">
            <select class="day raw">
                <option>Day</option></select>
            <select class="month raw">
                <option>Month</option>
            </select>
            <select class="year raw">
                <option>Year</option>
            </select>&nbsp;
            <select class="hour raw">
                <option>Hour</option>
            </select>
            &nbsp;:&nbsp;
            <select class="minute raw">
                <option>Minute</option>
            </select>
        </div>
        <div class="sub-template combodate hidden date-dropdown">
            <select class="day raw" >
                <option>Day</option></select>
            <select class="month raw">
                <option>Month</option>
            </select>
            <select class="year raw">
                <option>Year</option>
            </select>
        </div>
        <div class="sub-template combodate hidden time">
            <select class="hour raw">
                <option>Hour</option>
            </select>
            &nbsp;:&nbsp;
            <select class="minute raw">
                <option>Minute</option>
            </select>
        </div>
        <div class="sub-template hidden day_of_week">
            <select class="large"></select>
        </div>
        <div class="value-cache">
            <input type="hidden" prop-id="1" name="extra.config.1.label" value="type">
            <input type="hidden" prop-id="1" name="extra.config.1.value" value="date">
            <input type="hidden" prop-id="2" name="extra.config.2.label" value="date_type">
            <input type="hidden" prop-id="2" name="extra.config.2.value" value="calender">
            <input type="hidden" prop-id="3" name="extra.config.3.label" value="show_year">
            <input type="hidden" prop-id="3" name="extra.config.3.value" value="true">
            <input type="hidden" prop-id="4" name="extra.config.4.label" value="show_month">
            <input type="hidden" prop-id="4" name="extra.config.4.value" value="true">
            <input type="hidden" prop-id="5" name="extra.config.5.label" value="show_date">
            <input type="hidden" prop-id="5" name="extra.config.5.value" value="true">
            <input type="hidden" prop-id="6" name="extra.config.6.label" value="time_format">
            <input type="hidden" prop-id="6" name="extra.config.6.value" value="24">
        </div>
    </div>
    <div class="form-field-template textArea-template">
        <textarea readonly="readonly" class="large" rows="4"></textarea>
    </div>
    <div class="form-field-template dropDown-template">
        <select class="large"></select>
        <input type="hidden" class="selected-option">
        <div class="value-cache">
            <input type="hidden" prop-id="1" name="extra.option.1.label" value="Option 1">
            <input type="hidden" prop-id="1"  name="extra.option.1.value" value="Option 1">
            <input type="hidden" prop-id="2" name="extra.option.2.label" value="Option 2">
            <input type="hidden" prop-id="2" name="extra.option.2.value" value="Option 2">
            <input type="hidden" prop-id="3" name="extra.config.2.label" value="option_type">
            <input type="hidden" prop-id="3" name="extra.config.2.value" value="none">
        </div>
    </div>
    <div class="form-field-template country-template">
        <select class="large"></select>
    </div>
    <div class="form-field-template weekday-template">
        <select class="large"></select>
    </div>
    <div class="form-field-template month-template">
        <select class="large"></select>
    </div>
    <div class="form-field-template dateOfMonth-template">
        <select class="large"></select>
    </div>
    <div class="form-field-template label-template"></div>
    <div class="form-field-template spacer-template label-less-group-template"></div>
    <div class="form-field-template radioButton-template"></div>
    <div class="form-field-template checkBox-template group-template"></div>
    <div class="form-field-template fullName-template label-less-group-template">
        <span class="form-sub-field first-name">
            <label><g:message code="first.name"/> </label>
            <input type="text" readonly="readonly">
        </span>
        <span class="form-sub-field last-name">
            <label><g:message code="last.name.surname"/> </label>
            <input type="text" readonly="readonly">
        </span>
    </div>
    <div class="form-field-template email-template">
        <input readonly="readonly" class="large" type="text">
    </div>
    <div class="form-field-template file-template">
        <input readonly="readonly" class="large" type="file" text-helper="no">
        <div class="value-cache">
            <input type="hidden" prop-id="1" name="extra.config.1.label" value="upload_style">
            <input type="hidden" prop-id="1" name="extra.config.1.value" value="drop_box">
            <input type="hidden" prop-id="3" name="extra.config.2.label" value="upload_size_unit">
            <input type="hidden" prop-id="3" name="extra.config.2.value" value="mb">
        </div>
    </div>
    <div class="form-field-template text-template label-less-group-template">
        <p>Lorem ipsum dolor sit amet, consectetur adipisicing elit. Adipisci deleniti dolorem doloremque illo odio sapiente.</p>
    </div>
    <div class="form-field-template confirmMail-template label-less-group-template">
        <span class="form-sub-field email">
            <label><g:message code="email"/> </label>
            <input type="text" readonly="readonly">
        </span>
        <span class="form-sub-field confirm-email">
            <label><g:message code="confirm.email"/> </label>
            <input type="text" readonly="readonly">
        </span>
        <span class="value-cache">
            <input type="hidden" name="extra.email-label" value="<g:message code="email"/>">
            <input type="hidden" name="extra.confirm-label" value="<g:message code="confirm.email"/>">
            <input type="hidden" name="extra.confirm-title">
        </span>
    </div>
</div>