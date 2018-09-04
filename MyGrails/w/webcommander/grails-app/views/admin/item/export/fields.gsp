<g:each in="${fields}" var="field">
    <g:set var="isMandatory" value="${mandatoryFields.contains(field.key)}"/>
    <div class="double-input-row">
        <div class="form-row">
            <span class="label">
                <g:if test="${isMandatory}">
                    <input  type="hidden" value="true" name="${prefix + "." + field.key}">
                </g:if>
                <input type="checkbox" class="single" value="true" uncheck-value="false" checked name="${prefix + "." + field.key}" ${isMandatory ? "disabled" : ""}>
                <span><g:message code="${field.value}"/></span>
            </span>
        </div><div class="form-row">
            <input name="${prefix + ".label." + field.key}" type="text" class="medium">
        </div>
    </div>
</g:each>