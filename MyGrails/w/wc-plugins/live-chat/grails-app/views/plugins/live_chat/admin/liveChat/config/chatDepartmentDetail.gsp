<div class="info-row">
    <label><g:message code="name"/></label>
    <span class="value">${chatDepartment.name.encodeAsBMHTML()}</span>
</div>
<div class="info-row">
    <label><g:message code="welcome.message"/></label>
    <span class="value">${chatDepartment.defaultWelcomeMessage.encodeAsBMHTML()}</span>
</div>
<div class="info-row">
    <label><g:message code="description"/></label>
    <span class="value">${chatDepartment.description.encodeAsBMHTML()}</span>
</div>
<h4 class="group-label"><g:message code="agents"/></h4>
<div class="view-content-block">
    <table>
        <tr>
            <th><g:message code="name"/></th>
            <th><g:message code="email"/></th>
        </tr>
        <g:each in="${chatDepartment.operators}" var="operator">
                <tr class="data-row">
                    <td>${operator.fullName.encodeAsBMHTML()}</td>
                    <td>${operator.email.encodeAsBMHTML()}</td>
                </tr>
            </g:each>
    </table>
</div>