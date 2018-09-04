<div class="content">
    <div class="body">
        <div class="display-name"><span class="tool-icon display-name"></span><span class="value">${accountDetails.displayName}</span></div>
        <div class="mobile"><span class="tool-icon mobile"></span>${accountDetails.mobile}<span></span></div>
        <div class="phone"><span class="tool-icon phone"></span><span>${accountDetails.phone}</span></div>
        <div class="email"><span class="tool-icon email"></span><span>${accountDetails.emailAddress}</span></div>
        <div class="address"><span class="tool-icon address"></span><span>${accountDetails.addressLine1}</span></div>
    </div>
    <g:if test="${accountDetails.readOnly == false}">
        <div class="toolbar-btn edit-info btn"><i></i><g:message code="edit.address"/></div>
    </g:if>
</div>