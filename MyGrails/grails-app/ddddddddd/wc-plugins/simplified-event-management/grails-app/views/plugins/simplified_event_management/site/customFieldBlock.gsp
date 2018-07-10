<%@ page import="com.webcommander.plugin.simplified_event_management.constants.SimplifiedEventConstants" %>
<form class="order-custom-fields-form" action="${app.relativeBaseUrl()}simplifiedEvent/holdFields" method="post" submit-type="always">
    <input type="hidden" name="eventId" value="${eventId}">
    <g:each in="${1..quantity}" status="i" var="q">
        <div class="fields-setting">
            <h3>Ticket No: ${i+1}</h3>
            <g:each in="${fields}" var="field">
                <div class="form-row ${(field.validation?.indexOf('required')) >= 0 ? 'mandatory' : ''} ${field.clazz.encodeAsBMHTML()}">
                    <label>${field.label.encodeAsBMHTML()}:</label>
                    <g:if test="${field.type == SimplifiedEventConstants.EVENT_CHECKOUT_FIELD_TYPE.TEXT}">
                        <input type="text" name="custom.ticket${i+1}.${field.name.encodeAsBMHTML()}" value="${field.value.encodeAsBMHTML()}" placeholder="${field.placeholder.encodeAsBMHTML()}" ${field.validation ?
                                'validation="' + field.validation.encodeAsBMHTML() + '"' : ''} title="${field.title.encodeAsBMHTML()}">
                    </g:if>
                    <g:elseif test="${field.type == SimplifiedEventConstants.EVENT_CHECKOUT_FIELD_TYPE.LONG_TEXT}">
                        <textarea name="custom.ticket${i+1}.${field.name.encodeAsBMHTML()}" placeholder="${field.placeholder.encodeAsBMHTML()}" ${field.validation ?
                                'validation="' + field.validation.encodeAsBMHTML() + '"' : ''} title="${field.title.encodeAsBMHTML()}">${field.value.encodeAsBMHTML()}</textarea>
                    </g:elseif>
                    <g:elseif test="${field.type == SimplifiedEventConstants.EVENT_CHECKOUT_FIELD_TYPE.SINGLE_SELECT_RADIO || field.type == SimplifiedEventConstants.EVENT_CHECKOUT_FIELD_TYPE.MULTISELECT_CHECKBOX}">
                        <g:set var="type" value="${field.type == SimplifiedEventConstants.EVENT_CHECKOUT_FIELD_TYPE.SINGLE_SELECT_RADIO ? 'radio' : 'checkbox'}"/>
                        <span class="radio-group" title="${field.title.encodeAsBMHTML()}">
                            <g:each in="${field.options}" var="option">
                                <g:if test="${option.indexOf("=") > -1}">
                                    <g:set var="options" value="${option.split('=')}"/>
                                    <g:set var="key" value="${options[0]}"/>
                                    <g:set var="value" value="${options[1]}"/>
                                </g:if>
                                <g:else>
                                    <g:set var="key" value="${option}"/>
                                    <g:set var="value" value="${option}"/>
                                </g:else>
                                <input type="${type}" name="custom.ticket${i+1}.${field.name.encodeAsBMHTML()}" value="${key.encodeAsBMHTML()}" ${value == field.value ? 'checked' : ''}> &nbsp; &nbsp; ${value.encodeAsBMHTML()}
                                &nbsp; &nbsp; &nbsp; &nbsp;
                            </g:each>
                        </span>
                    </g:elseif>
                    <g:elseif test="${field.type == SimplifiedEventConstants.EVENT_CHECKOUT_FIELD_TYPE.SINGLE_SELECT_DROPDOWN || field.type ==
                            SimplifiedEventConstants.EVENT_CHECKOUT_FIELD_TYPE.MULTISELECT_LISTBOX}">
                        <select title="${field.title.encodeAsBMHTML()}" name="custom.ticket${i+1}.${field.name.encodeAsBMHTML()}" ${field.type == SimplifiedEventConstants.EVENT_CHECKOUT_FIELD_TYPE.MULTISELECT_LISTBOX ?
                                'multiple="multiple" size="' + field.options.size() + '"' : ''}>
                            <g:set var="selecteds" value="${field.value?.split(',') ?: []}"/>
                            <g:each in="${field.options}" var="option">
                                <g:if test="${option.indexOf("=") > -1}">
                                    <g:set var="options" value="${option.split('=')}"/>
                                    <g:set var="key" value="${options[0]}"/>
                                    <g:set var="value" value="${options[1]}"/>
                                </g:if>
                                <g:else>
                                    <g:set var="key" value="${option}"/>
                                    <g:set var="value" value="${option}"/>
                                </g:else>
                                <option value="${key.encodeAsBMHTML()}" ${selecteds.contains(value) ? 'selected' : ''}>${value.encodeAsBMHTML()}</option>
                            </g:each>
                        </select>
                    </g:elseif>
                </div>
            </g:each>
        </div>
    </g:each>
    <input type="button" value="${g.message(code: 'continue')}" class="button step-continue-button">
</form>