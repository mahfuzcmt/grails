<g:if test="${message?.size() > 0}">
    <span class="message-block ${status}">${message}</span>
</g:if>
<div class="wrap">
    <table width="100%" border="1">
        <colgroup>
            <col class="full-name-col">
            <col class="address-col">
            <col class="action-col">
        </colgroup>
        <thead>
        <tr>
            <th><g:message code="full.name"/></th>
            <th><g:message code="address"/></th>
            <th><g:message code="action"/></th>
        </tr>
        </thead>
        <tbody>
        <g:if test="${addresses.size() > 0}">
            <g:each in="${addresses}" var="address">
                <tr class="${address.id == activeAddress.id ? 'active-row' : ''}">
                    <td class="name-cell"><div class="wrapper" data-label="<g:message code="full.name"/>:">${address.firstName.encodeAsBMHTML()} ${address.lastName.encodeAsBMHTML()}</div></td>
                    <td class="address-cell"><div class="wrapper" data-label="<g:message code="address"/>:">${address.addressLine1.encodeAsBMHTML() + ", " + (address.city ? address.city.encodeAsBMHTML() + ", " : "") + (address.state ? address.state.name + ", " : "") + address.country.name}</div></td>
                    <td class="action-cell">
                        <div class="wrapper" data-label="<g:message code="action"/>:">
                            <span class="action-icon edit" title='<g:message code="edit"/>' address-id="${address.id}"></span>
                            <g:if test="${address.id != activeAddress.id}">
                                <span class="action-icon delete" title='<g:message code="delete"/>' address-id="${address.id}"></span>
                                <span class="action-icon active" title='<g:message code="make.active"/>' address-id="${address.id}"></span>
                            </g:if>
                        </div>
                    </td>
                </tr>
            </g:each>
        </g:if>
        <g:else>
            <tr>
                <td colspan="3"><g:message code="no.address.found"/></td>
            </tr>
        </g:else>
        </tbody>
    </table>
    <div class="button-line">
        <span class="create-new button"><g:message code="create.new"/></span>
    </div>
</div>
