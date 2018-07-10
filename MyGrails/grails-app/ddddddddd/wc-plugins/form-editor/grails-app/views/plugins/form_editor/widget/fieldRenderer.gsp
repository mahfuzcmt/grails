<%@ page import="com.webcommander.admin.Country; com.webcommander.util.StringUtil; com.webcommander.plugin.form_editor.FormField; com.webcommander.plugin.form_editor.constants.DomainConstants; com.webcommander.plugin.form_editor.util.TemplateHelper as TH; com.webcommander.admin.State" %>
<app:enqueueSiteJs src="plugins/form-editor/js/moment-js/moment.js" scriptId="moment"/>
<app:enqueueSiteJs src="plugins/form-editor/js/combodate-js/combodate.js" scriptId="combodate"/>
<div class="form-row ${field.type} ${TH.text(field.clazz)} ${((field.validation)?.indexOf('required')) >= 0 ? 'mandatory' : ''}" id="fw-${field.uuid}">
    <g:if test="${field.label}">
        <label>${TH.text(field.label)}</label>
    </g:if>
    <g:if test="${field.type == DomainConstants.FORM_FIELD.TEXTBOX}">
        <input type="text" ${TH.validation(field.validation)} ${TH.name('submit.' + field.name)} ${TH.placeholder(field.placeholder)}${TH.title(field.title)} ${TH.value(field.value)} id="ff-${field.uuid}"/>
        <g:if test="${field.validation?.contains("email") && field.configs.show_confirm_email == "true"}">
            <div class="sub-form-row">
                <label><g:message code="confirm.email"/></label>
                <input type="text" class="match-email" validation="eq[ff-${field.uuid}]" />
            </div>
        </g:if>
    </g:if>
    <g:elseif test="${field.type == DomainConstants.FORM_FIELD.TEXTAREA}">
        <textarea ${TH.name('submit.' + field.name)} ${TH.title(field.title)} ${TH.placeholder(field.placeholder)} ${TH.validation(field.validation)} rows="3" cols="2">${TH.text(field.value)}</textarea>
    </g:elseif>
    <g:elseif test="${field.type == DomainConstants.FORM_FIELD.DROPDOWN}">
        <g:if test="${field.configs.option_type == "country"}">
            <ui:countryList optionKey="name" name="${'submit.' + field.name}" title="${field.title}" value="${field.value}" validation="${field.validation}"/>
        </g:if>
        <g:elseif test="${field.configs.option_type == "state"}">
            <ui:domainSelect domain="${State}" name="${'submit.' + field.name}" title="${field.title}" value="${field.value}" validation="${field.validation}" filter="${{eq("country.id", field.configs.state_country?.toLong(0))}}"/>
        </g:elseif>
        <g:else>
            <select ${TH.name('submit.' + field.name)} ${TH.validation(field.validation)} ${TH.title(field.title)}>
                <g:each in="${field.extras}" var="option">
                    <g:if test="${option.type == "option"}">
                        <option ${TH.value(option.value)} ${option.extraValue == "selected" ? "selected='selected'" : ""}>${option.label ? TH.text(option.label) : TH.text(option.value)}</option>
                    </g:if>
                </g:each>
            </select>
        </g:else>
    </g:elseif>
    <g:elseif test="${field.type == DomainConstants.FORM_FIELD.CHECKBOX}">
        <div class="radio-checkbox-row" validation="${field.validation ? "skip@if{this::input:checked} fail" : ""}" message_template="<g:message code="one.option.required"/> ">
            <g:each in="${field.extras}" var="input">
                <g:if test="${input.type == "option"}">
                    <div class="checkbox">
                        <input id="check-box-${input.id}" type="checkbox" ${TH.title(field.title)} ${TH.name('submit.' + field.name)} ${TH.value(input.value)}  ${input.extraValue ? " checked='checked'" : ""}>
                        <label for="check-box-${input.id}">${TH.text(input.label)} </label>
                    </div>
                </g:if>
            </g:each>
        </div>
    </g:elseif>
    <g:elseif test="${field.type == DomainConstants.FORM_FIELD.RADIO}">
        <div class="radio-checkbox-row" validation="${field.validation ? "skip@if{this::input:checked} fail" : ""}" message_template="field.required">
            <g:each in="${field.extras}" var="input">
                <g:if test="${input.type == "option"}">
                    <div class="radio">
                        <input id="radio-box-${input.id}" type="radio" ${TH.name('submit.' + field.name)} ${TH.title(field.title)} ${TH.value(input.value)} ${input.extraValue ? " checked='checked'" : ""}/>
                        <label for="radio-box-${input.id}">${TH.text(input.label)}</label>
                    </div>
                </g:if>
            </g:each>
        </div>
    </g:elseif>
    <g:elseif test="${field.type == DomainConstants.FORM_FIELD.FILE}">
        <g:set var="validation" value="${(field.validation ?: "") + (field.configs.upload_extensions ? " file-extensions[${field.configs.upload_extensions}]" : "") + (field.configs.upload_size ? " file-size[${field.configs.upload_size}, ${field.configs.upload_size_unit}]" : "")}"/>
        <g:if test="${field.configs.upload_style == "drop_box"}">
            <div class="dropzone-wrapper" >
                <div class="dropzone"><span class="dropzone-text">Drop File/Click Here</span></div>
                <input id="${field.uuid}" type="file" ${TH.name('submit.' + field.name)}  ${TH.title(field.title)} validation="${validation}" />
            </div>
        </g:if>
        <g:else>
            <input id="${field.uuid}" type="file" ${TH.name('submit.' + field.name)}  ${TH.title(field.title)} validation="${validation}"/>
        </g:else>
    </g:elseif>
    <g:elseif test="${field.type == DomainConstants.FORM_FIELD.DATE}">
        <g:if test="${(field.configs.type == "date")}">
            <g:if test="${field.configs.date_type == "calender"}">
                <input type="text"  class="date-picker" ${TH.validation(field.validation)} ${TH.name('submit.' + field.name)} ${TH.placeholder(field.placeholder)}
                    ${TH.title(field.title)} ${TH.value(field.value)}
            </g:if>
            <g:elseif test="${field.configs.date_type == "dropdown"}">
                <% String dataFormat="", dataTemplate=""; %>
                <% dataFormat = (field.configs.show_year == "true" ? "YYYY" : "") + (field.configs.show_month == "true" ? (field.configs.show_year == "true" ? "-MM" : "MM") : "") + (field.configs.show_date == "true" ? (field.configs.show_month == "true" ? "-DD" : "DD") : "") %>
                <% dataTemplate = (field.configs.show_date == "true" ? "DD" : "") + (field.configs.show_month == "true" ? "MM" : "") + (field.configs.show_year == "true" ? "YYYY" : "") %>
                <input type="text" class="dropdown-date-picker" ${TH.name('submit.' + field.name)} ${TH.placeholder(field.placeholder)}
                ${TH.title(field.title)} ${TH.value(field.value)} data-format="${dataFormat}" data-template="${dataTemplate}" validation="${field.validation} date-range[${field.configs.date_range_from},${field.configs.date_range_to}]"
            </g:elseif>
            date-range-start="${field.configs.date_range_from}" date-range-end="${field.configs.date_range_to}"/>
        </g:if>
        <g:if test="${(field.configs.type == "date_time")}">
            <g:set var="validation" value="${(field.configs.date_range_from || field.configs.date_range_to) ? ((field.configs.time_range_from || field.configs.time_range_to) ?
                    " date-range[${field.configs.date_range_from},${field.configs.date_range_to}] time-range[${field.configs.time_range_from},${field.configs.time_range_to}]" :
                    " date-range[${field.configs.date_range_from},${field.configs.date_range_to}]") : " time-range[${field.configs.time_range_from},${field.configs.time_range_to}]"}"/>

            <g:if test="${field.configs.date_type == "calender"}">
                <input type="text"  class="date-time-picker" ${TH.name('submit.' + field.name)} ${TH.placeholder(field.placeholder)}
                    ${TH.title(field.title)} ${TH.value(field.value)} time-format="${field.configs.time_format}" validation="${field.validation} ${validation}"
            </g:if>
            <g:elseif test="${field.configs.date_type == "dropdown"}">
                <% dataFormat=""; dataTemplate=""; %>
                <% dataFormat = (field.configs.show_year == "true" ? "YYYY" : "") + (field.configs.show_month == "true" ? (field.configs.show_year == "true" ? "-MM" : "MM") : "") + (field.configs.show_date == "true" ? (field.configs.show_month == "true" ? "-DD" : "DD") : "") %>
                <% dataTemplate = (field.configs.show_date == "true" ? "DD" : "") + (field.configs.show_month == "true" ? "MM" : "") + (field.configs.show_year == "true" ? "YYYY" : "") %>
                <g:if test="${field.configs.time_format == "24"}">
                    <input type="text" class="dropdown-date-time-picker" ${TH.name('submit.' + field.name)} ${TH.placeholder(field.placeholder)}
                    ${TH.title(field.title)} ${TH.value(field.value)} data-format="${dataFormat} HH:mm" data-template="${dataTemplate} HH : mm" time-format="${field.configs.time_format}" validation="${field.validation} ${validation}"
                </g:if>
                <g:elseif test="${field.configs.time_format == "12"}">
                    <input type="text" class="dropdown-date-time-picker" ${TH.name('submit.' + field.name)} ${TH.placeholder(field.placeholder)}
                    ${TH.title(field.title)} ${TH.value(field.value)} data-format="${dataFormat} h:mm:a" data-template="${dataTemplate} hh : mm a" time-format="${field.configs.time_format}" validation="${field.validation} ${validation}"
                </g:elseif>
            </g:elseif>
            date-range-start="${field.configs.date_range_from}" date-range-end="${field.configs.date_range_to}"/>
        </g:if>
        <g:if test="${(field.configs.type == "time")}">
            <g:if test="${field.configs.time_format == "24"}">
                <input type="text" class="dropdown-time-picker" ${TH.name('submit.' + field.name)} ${TH.placeholder(field.placeholder)}
                    ${TH.title(field.title)} ${TH.value(field.value)} data-format="HH:mm" data-template="HH : mm" time-format="${field.configs.time_format}" validation="${field.validation} time-range[${field.configs.time_range_from},${field.configs.time_range_to}]">
            </g:if>
            <g:elseif test="${field.configs.time_format == "12"}">
                <input type="text" class="dropdown-time-picker" ${TH.name('submit.' + field.name)} ${TH.placeholder(field.placeholder)}
                    ${TH.title(field.title)} ${TH.value(field.value)} data-format="h:mm:a" data-template="hh : mm a" time-format="${field.configs.time_format}" validation="${field.validation} time-range[${field.configs.time_range_from},${field.configs.time_range_to}]">
            </g:elseif>
        </g:if>
        <g:if test="${field.configs.type == "day_of_week"}">
            <g:select name="${TH.text('submit.' + field.name)}" title="${TH.text(field.title)}" keys="${DomainConstants.DAYS_OF_WEEK.keySet()}"
                  from="${DomainConstants.DAYS_OF_WEEK.collect {g.message(code: it.value)}}" value="${field.value}"/>
        </g:if>
    </g:elseif>
    <g:elseif test="${field.type == DomainConstants.FORM_FIELD.WEEKDAY}">
        <g:select name="${TH.text('submit.' + field.name)}" title="${TH.text(field.title)}" keys="${DomainConstants.DAYS_OF_WEEK.keySet()}"
                  from="${DomainConstants.DAYS_OF_WEEK.collect {g.message(code: it.value)}}" value="${field.value}"/>
    </g:elseif>
    <g:elseif test="${field.type == DomainConstants.FORM_FIELD.MONTH}">
        <g:select name="${TH.text('submit.' + field.name)}" title="${TH.text(field.title)}" keys="${DomainConstants.MONTH.keySet()}"
                  from="${DomainConstants.MONTH.collect {g.message(code: it.value)}}" value="${field.value}"/>
    </g:elseif>
    <g:elseif test="${field.type == DomainConstants.FORM_FIELD.DATE_OF_MONTH}">
        <g:select name="${TH.text('submit.' + field.name)}" title="${TH.text(field.title)}" keys="${(1..31)}" from="${(1..31)}" value="${field.value}"/>
    </g:elseif>
    <g:elseif test="${field.type == DomainConstants.FORM_FIELD.COUNTRY}">
        <ui:countryList optionKey="name" name="${'submit.' + field.name}" title="${field.title}" value="${field.value}"/>
    </g:elseif>
    <g:elseif test="${field.type == DomainConstants.FORM_GROUP_FIELD.FULL_NAME}">
        <div class="sub-form-row">
            <g:each in="${field.fields}" var="input">
                <span>${TH.text(input.label)}:</span>
                <input type="text" ${TH.name('submit.' + input.name)} ${TH.validation(input.validation)}
                    ${TH.placeholder(input.placeholder)} ${TH.title(input.title)}/>
            </g:each>
        </div>
    </g:elseif>
    <g:elseif test="${field.type == DomainConstants.FORM_GROUP_FIELD.CONFIRM_EMAIL}">
        <div class="sub-form-row">
            <g:set var="formNewEmail" value="${StringUtil.uuid}"/>
            <span>${TH.text(field.extras[0].value)}:</span>
            <input type="text" id="${formNewEmail}" ${TH.name('submit.' + field.name)} ${TH.validation((field.validation?.indexOf('email') >= 0) ? field.validation : 'email ' + TH.text(field.validation))}
                ${TH.placeholder(field.placeholder)} ${TH.title(field.title)} ${TH.value(field.value)}/>
            <span>${TH.text(field.extras[1].value)}:</span>
            <input type="text" class="match-email"  ${TH.title(field.extras.find { it.type == "confirm-title"}?.value)} validation="${field.validation?.indexOf('email')  >= 0 ? field.validation : 'email ' + TH.text(field.validation)} compare[${formNewEmail}, string, eq]" message_params="(${TH.text(field.extras[0].value)} above)"/>
        </div>
    </g:elseif>
    <g:elseif test="${field.type == DomainConstants.FORM_FIELD.EMAIL}">
        <input id="ff-${field.uuid}" type="text" ${TH.validation("email " + (field.validation ?: ""))} ${TH.name('submit.' + field.name)} ${TH.placeholder(field.placeholder)}
            ${TH.title(field.title)} ${TH.value(field.value)}/>
    </g:elseif>
    <g:elseif test="${field.type == DomainConstants.FORM_FIELD.TEXT}">${field.value}</g:elseif>
</div>

<g:if test="${(field.type == DomainConstants.FORM_FIELD.RADIO || field.type == DomainConstants.FORM_FIELD.DROPDOWN) && field.conditions}">
    <%
        fieldConditions[field.uuid] = field.conditions.collect {
            [dependentFieldUUID: it.dependentFieldUUID, action: it.action, targetOption: it.targetOption]
        };
    %>
</g:if>