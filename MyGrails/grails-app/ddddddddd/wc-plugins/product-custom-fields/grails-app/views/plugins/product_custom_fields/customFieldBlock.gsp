<%@ page import="com.webcommander.plugin.product_custom_fields.DomainConstants" %>
<form class="custom-field-container">
    <input type="hidden" name="hasCustomField" value="true">
    <g:if test="${title}">
        <h4 class="title">${title.encodeAsBMHTML()}</h4>
    </g:if>
    <g:each in="${fields}" var="field">
        <g:set var="validation" value="${field.validation ? field.validation.encodeAsBMHTML() : ""}"/>
        <div class="form-row ${(validation.indexOf('required')) >= 0 ? 'mandatory' : ''} ${field.clazz.encodeAsBMHTML()}">
            <label>${field.label.encodeAsBMHTML()}:</label>
            <g:if test="${field.type == DomainConstants.PRODUCT_CHECKOUT_FIELD_TYPE.TEXT}">
                <input type="text" name="custom.${field.name.encodeAsBMHTML()}" value="${field.value.encodeAsBMHTML()}" placeholder="${field.placeholder.encodeAsBMHTML()}"
                    ${validation ? 'validation="' + validation + '"' : ''} title="${field.title.encodeAsBMHTML()}">
            </g:if>
            <g:if test="${field.type == DomainConstants.PRODUCT_CHECKOUT_FIELD_TYPE.LONG_TEXT}">
                <textarea name="custom.${field.name.encodeAsBMHTML()}" placeholder="${field.placeholder.encodeAsBMHTML()}"
                    ${validation ? 'validation="' + validation + '"' : ''} title="${field.title.encodeAsBMHTML()}">${field.value.encodeAsBMHTML()}</textarea>
            </g:if>
            <g:if test="${field.type == DomainConstants.PRODUCT_CHECKOUT_FIELD_TYPE.SINGLE_SELECT_RADIO || field.type == DomainConstants.PRODUCT_CHECKOUT_FIELD_TYPE.MULTISELECT_CHECKBOX}">
                <g:set var="type" value="${field.type == DomainConstants.PRODUCT_CHECKOUT_FIELD_TYPE.SINGLE_SELECT_RADIO ? 'radio' : 'checkbox'}"/>
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
                        <input type="${type}" name="custom.${field.name.encodeAsBMHTML()}" value="${key.encodeAsBMHTML()}" ${value == field.value ? 'checked' : ''}> &nbsp; &nbsp; ${value.encodeAsBMHTML()}
                        &nbsp; &nbsp; &nbsp; &nbsp;
                    </g:each>
                </span>
            </g:if>
            <g:if test="${field.type == DomainConstants.PRODUCT_CHECKOUT_FIELD_TYPE.SINGLE_SELECT_DROPDOWN || field.type ==
                    DomainConstants.PRODUCT_CHECKOUT_FIELD_TYPE.MULTISELECT_LISTBOX}">
                <select title="${field.title.encodeAsBMHTML()}" name="custom.${field.name.encodeAsBMHTML()}" ${field.type == DomainConstants.PRODUCT_CHECKOUT_FIELD_TYPE.MULTISELECT_LISTBOX ?
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
            </g:if>
        </div>
    </g:each>
</form>