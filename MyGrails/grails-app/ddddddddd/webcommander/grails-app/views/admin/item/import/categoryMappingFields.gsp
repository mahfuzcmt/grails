<% int mappedElement = 0, totalElement = 0; %>
<div id="category-mapping-wrap">
<g:if test="${columnNames}">
    <h3><g:message code="database.excel.mapping"/></h3>
    <table class="category-mapping-table">
        <thead class="mapping-table-head">
        <tr class="first-row multi-conditions">
            <td>
                <div class="category-table-heading">
                    <g:message code="mapped.with" />
                </div>
            </td>
            <g:each in="${categoryFields}" var="field">
                <g:set var="isRequired" value="${["name"].contains(field.key)}"/>
                <td>
                    <div class="${isRequired ? 'mandatory' : ''} ${field.key == "image" ? "category-image-mapping-field-" : ""}">
                        <g:message code="${field.value}"/>
                    </div>
                </td>
            </g:each>
        </tr>
        </thead>
        <tbody class="mapping-table-body">
        <tr>
            <td>
                <div class="category-table-heading">
                    <g:message code="excel.field" />
                </div>
            </td>
            <g:each in="${categoryFields}" var="field">
                <g:set var="isRequired" value="${["name"].contains(field.key)}"/>
                <% totalElement++; %>
                <td class="chosen-wrapper">
                    <select name="category.${field.key}" class="large" ${isRequired ? 'validation="required"' : ''}>
                        <option value=""><g:message code="not.map"/></option>
                        <g:each in="${ columnNames }" var="opt" status="i">
                            <g:if test="${opt ==~ /(?i)$field.value/}">
                                <% mappedElement++; %>
                                <option value="${i}" selected>${opt}</option>
                            </g:if>
                            <g:else>
                                <option value="${i}">${opt}</option>
                            </g:else>
                        </g:each>
                    </select>
                </td>
            </g:each>
        </tr>
        </tbody>
    </table>
    <div class="category-table-record">
        <g:message code="category.mapping.status.text" encodeAs="raw" args="${["<span>${categorySheetRows}</span>", "<span>${mappedElement}</span>", "<span>${totalElement-mappedElement}</span>"]}"/>
    </div>
</g:if>
<g:if test="${columnNames?.size() < 1 && categorySheet}">
    <p>No columns found to map</p>
</g:if>
</div>