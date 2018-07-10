<% int mappedElement = 0, totalElement = 0; %>
<div id="product-mapping-wrap">
    <g:if test="${columnNames?.size() > 0}">
        <h3><g:message code="database.excel.mapping"/></h3>
        <table class="product-mapping-table">
            <thead class="mapping-table-head">
                <tr class="first-row multi-conditions">
                    <td>
                        <div class="product-table-heading">
                            <g:message code="mapped.with" />
                        </div>
                    </td>
                <g:each in="${productFields}" var="field">
                    <g:set var="isRequired" value="${["name", "basePrice"].contains(field.key)}"/>
                    <td>
                        <div class="${isRequired ? 'mandatory' : ''} ${field.value == 'image' ? 'product-image-mapping-field-' : (field.value == 'video' ? 'product-video-mapping-field-' : '')}">
                            <g:message code="${field.value}"/>
                        </div>
                    </td>
                </g:each>
                </tr>
            </thead>
            <tbody class="mapping-table-body">
                <tr>
                    <td>
                        <div class="product-table-heading">
                            <g:message code="excel.field" />
                        </div>
                    </td>
                <g:each in="${productFields}" var="field">
                    <g:set var="isRequired" value="${["name", "basePrice"].contains(field.key)}"/>
                    <% totalElement++; %>
                    <td class="chosen-wrapper">
                        <select name="product.${field.key}" class="large" ${isRequired ? 'validation="required"' : ''}>
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
        <div class="product-table-record">
            <g:message code="product.mapping.status.text" encodeAs="raw" args="${["<span>${productSheetRows}</span>", "<span>${mappedElement}</span>", "<span>${totalElement-mappedElement}</span>"]}"/>
        </div>
    </g:if>
    <g:if test="${columnNames?.size() < 1 && productSheet}">
        <p>No columns found to map</p>
    </g:if>
</div>