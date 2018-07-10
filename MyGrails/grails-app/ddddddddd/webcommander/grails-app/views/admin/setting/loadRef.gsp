<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.constants.NamedConstants" %>
<div class="form-row ref-selector-row term-condition-field">
    <label>
        <g:message code="${NamedConstants.TERMS_AND_CONDITION_TYPE[type]}"/>
        <g:if test="${type == DomainConstants.TERMS_AND_CONDITION_TYPE.SPECIFIC_TEXT}">
            <span class="suggestion">e.g. Provide a Specific Text</span>
        </g:if>
    </label>
    <g:if test="${type == DomainConstants.TERMS_AND_CONDITION_TYPE.EXTERNAL_LINK}">
        <input type="text" class="large" validation="required url" name="checkout_page.terms_and_condition_ref" value="${ref}">
    </g:if>
    <g:elseif test="${type == DomainConstants.TERMS_AND_CONDITION_TYPE.SPECIFIC_TEXT}">
        <textarea class="xx-larger" validation="skip@if{self::hidden} required" name="checkout_page.terms_and_condition_ref" >${ref}</textarea>
    </g:elseif>
    <g:elseif test="${type == DomainConstants.TERMS_AND_CONDITION_TYPE.PAGE}">
        <g:if test="${items.size() > 0}">
            <g:select from="${items}" optionKey="id" optionValue="name" validation="required" name="checkout_page.terms_and_condition_ref" value="${ref}" class="large"></g:select>
        </g:if>
        <g:else>
            <span validation="fail"><g:message code="no.page.found"/></span>
        </g:else>
    </g:elseif>
</div>