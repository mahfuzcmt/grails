<div class="content">

    <table>
        <colgroup>
            <col class="name-column">
            <col class="code-column">
            <col class="symbol-column">
            <col class="country-column">
            <col class="conversion-rate-column">

            <g:if test="${params.base}"><col class="actions-column"></g:if>

        </colgroup>
        <tr>
            <th><g:message code="name"/></th>
            <th><g:message code="code"/></th>
            <th><g:message code="symbol"/></th>
            <th><g:message code="country"/></th>
            <th><g:message code="conversion.rate"/></th>
            <g:if test="${params.base}"> <th class="actions-column"><g:message code="actions"/></th></g:if>

        </tr>

        <g:if test="${currencies}">
            <g:each in="${currencies}" var="currency">
                <tr class="${currency.base ? "base-currency highlighted" : "" + (currency.active ? "" : "inactive")} data-row">

                <g:if test="${!params.base}">
                    <g:set var="id" value="${currency.id}"/>
                    <input type="hidden" name="currencyId" value="${id}">
                </g:if>

                    <td>${currency.name.encodeAsBMHTML()}</td>
                    <td>${currency.code.encodeAsBMHTML()}</td>
                    <td>${currency.symbol.encodeAsBMHTML()}</td>
                    <td>
                        <g:if test="${currency.country}">
                            <g:set var="country" value="${currency.country}"/>
                            <span class="flag-icon ${country.code.toLowerCase()}"></span>
                            <span class="name">${country.name.encodeAsBMHTML()}</span>
                        </g:if>
                    </td>

                    <g:if test="${params.base}">
                    <td><g:message code="base"/></td>
                    <td class="actions-column">
                        <span class="action-navigator collapsed" entity-id="${currency.id}" entity-name="${currency.name.encodeAsBMHTML()}"></span>
                    </td>
                    </g:if>
                    <g:else>
                    <td class="editable conversion-rate" validation="number price gt[0]">
                        <input type="hidden" name="${id}.conversionRate" value="${currency.conversionRate}"><span class="value">${currency.conversionRate}</span>
                    </td>
                    </g:else>

                </tr>
            </g:each>
        </g:if>
        <g:else>
            <tr class="table-no-entry-row">
                <td colspan="7"><g:message code="no.currency.added"/></td>
            </tr>
        </g:else>
    </table>

    <g:if test="${params.base}">
        <div class="form-row"></div>
        <div class="triple-input-row">
            <div class="form-row">
                <button class="button add-currency" type="button">+ <g:message code="add.currency"/></button>
            </div>
            <div class="form-row chosen-wrapper">
                <label><g:message code="conversion"/><span class="suggestion">  e.g. Manual Conversion</span> </label>
                <select name="manualConversion" toggle-target="manual-conversion"  class="medium">
                    <option value="true" selected><g:message code="manual.conversion"/></option>
                </select>
            </div>
        </div>
    </g:if>
</div>
