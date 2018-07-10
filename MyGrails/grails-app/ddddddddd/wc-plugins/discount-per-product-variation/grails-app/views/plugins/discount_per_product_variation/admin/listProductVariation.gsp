<div class="multi-check-container">
    <div class="multi-check-body">
        <g:each in="${variations}" var="variation">
            <span class="label">
                <span class="variation-selection" variation-item="${variation.id}" type="${fieldName}">
                    <input type="checkbox" class="multiple" ${selectedVariations?.contains(variation.id as String) ? "checked='checked'" : ""}>
                    <input type="hidden" value="${variation.id}">
                </span>
                <span class="check-label">
                    <% boolean isMultiple = false %>
                    <g:each in="${variation.options}" var="option">
                        ${(isMultiple ? ' - ' : '' ) + option.label}
                        <% isMultiple = true %>
                    </g:each>
                </span>
            </span>
        </g:each>
    </div>
</div>