<%@ page import="com.webcommander.manager.PathManager; com.webcommander.constants.NamedConstants; com.webcommander.util.AppUtil" %>
<g:each in="${fields.sort {a, b -> a.id <=> b.id}}" var="field">
    <div class="form-row ${field.validation?.contains("required") ? 'mandatory' : ''} ${(field.htmlType == NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.SELECT) ? 'chosen-wrapper' : ''}  ${field.clazz}">
        <label><app:message code="${field.label}"/></label>
        <g:if test="${field.htmlType == NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.TEXT}">
            <input type="text" value="${field.value}"
        </g:if>
        <g:if test="${field.htmlType == NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.PASSWORD}">
            <input type="password" value="${field.value}"
        </g:if>
        <g:if test="${field.htmlType == NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.CHECK_BOX}">
            <input type="checkbox" class="single" value="true" ${field.value == "true" ? "checked" : ""}
        </g:if>
        <g:if test="${field.htmlType == NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.FILE}">
            <g:if test="${field.extraAttrs == "file-type='image'" && new File(PathManager.getResourceRoot("payment-gateway/CRD/custom_logo.png")).exists()}">
                <div class="custom-logo"><img src="${app.customResourceBaseUrl()}resources/payment-gateway/CRD/custom_logo.png?_cc=${new Date().time}"></div>
            </g:if>
            <g:if test="${new File(PathManager.getCustomRestrictedResourceRoot("certificates/payway.cert")).exists() && field.fieldFor == "PAYWAY" }">
                <span class="image-thumb">payway.cert</span>
            </g:if>
            <input type="file" value="true"
        </g:if>
        <g:if test="${field.htmlType == NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.SELECT}">
            <select
        </g:if>
        <g:if test="${field.htmlType == NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.MULTI_CHECK_BOX}">
            <%
                List values = field.value?.split(",") ?: []
                field.optionLabel.collect { app.message(code: it) }.eachWithIndex { v, i ->
                    def value = field.optionValue[i]
                    out << "<span class='input-label-combined'>"
                    out << "<input type='checkbox' name='metafield.${field.fieldFor}.${field.name}' value='${value}' ${value in values ? 'checked' : ''}>"
                    out << "<label class = 'value'>${v}</label>"
                }
            %>
        </g:if>
        <g:else>
            name="metafield.${field.fieldFor}.${field.name}" class="large" ${field.validation ? "validation=\"skip@if{self::hidden} skip@if{global:#payment-gateway-config-gateway-enabled:not(:checked)} " + field.validation + "\"" : ""} ${field.extraAttrs} ${field.fieldId ? "id='" + field.fieldId + "'" : ""} depends="#payment-gateway-config-gateway-enabled">
        </g:else>
        <g:if test="${field.htmlType == NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.SELECT}">
            <%
                field.optionLabel.collect { app.message(code: it) }.eachWithIndex { v, i ->
                    def value = field.optionValue[i]
                    String selected = value == field.value ? "selected" : "";
                    out << "<option value='$value' $selected>$v</option>"
                }
            %>
            </select>
        </g:if>
    </div>
</g:each>