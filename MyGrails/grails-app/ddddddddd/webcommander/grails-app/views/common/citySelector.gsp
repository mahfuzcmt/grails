<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.util.AppUtil" %>
<g:set var="validation_attr" value="${validation ? 'validation="' + validation + '"' : ''}"/>
<g:set var="auto_suggest_address" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, 'auto_suggest_address')}"></g:set>
<g:if test="${cityExists && auto_suggest_address.toBoolean()}">
    <select name="${fieldName}" ${cities.size() > 0 ?: 'disabled="disabled"'} ${validation_attr} class="${params.section == "billing" ? "et_billing_edit_city" : "et_shipping_edit_city"}" et-category="dropdown">
        <g:if test="${cities.size() == 0}">
            <option value=""><g:message code="enter.valid.post.code"/></option>
        </g:if>
        <g:each in="${cities}" var="city">
            <option value="${city.name}" state="${city.state.id}" ${city.name == selectedCity ? 'selected="selected"' : ''}>${city.name}</option>
        </g:each>
    </select>
</g:if>
<g:else>
    <input name="${fieldName}" class="${params.section == "billing" ? "et_billing_edit_city" : "et_shipping_edit_city"}" et-category="textbox" type="text" ${validation_attr} value="${selectedCity}">
</g:else>