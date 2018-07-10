<div class="special-point-customer-group">
    <% def customerSize = customers.size(), groupSize = customerGroups.size() %>
    <% def maxSize = Math.max(customerSize, groupSize) %>
    <table class="customer-list">
        <tr>
            <th><g:message code="customer"/></th>
            <th><g:message code="customer.group"/></th>
        </tr>
        <g:if test="${maxSize > 0}">
            <g:each in="${0..(maxSize-1)}" var="itr">
                <tr>
                    <td>
                        ${customers[itr]?.name}
                    </td>
                    <td>
                        ${customerGroups[itr]?.name}
                    </td>
                </tr>
            </g:each>
        </g:if>
    </table>
</div>