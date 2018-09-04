<h2><g:message code="share" args="${[registry.name]}"/></h2>
<table>
    <colgroup>
        <col class="name-col" style="width: 40%">
        <col class="email-col" style="width: 60%">
    </colgroup>
    <tr>
        <th><g:message code="name"/></th>
        <th><g:message code="email"/></th>
    </tr>
    <g:each in="${registry.emails}" var="email">
        <tr>
            <td>${email.name.encodeAsBMHTML()}</td>
            <td>${email.email.encodeAsBMHTML()}</td>
        </tr>
    </g:each>
    <g:if test="${registry.emails.size() == 0}">
        <tr>
            <td colspan="2">
                <span class="no-data"><g:message code="gift.registry.not.shared"/> </span>
            </td>
        </tr>
    </g:if>
</table>

<form class="gift-registry-share-form" action="${app.relativeBaseUrl()}giftRegistry/share">
    <input type="hidden" value="${id}" name="id">
    <div class="form-row">
        <label><g:message code="name"/>:</label>
        <input type="text" name="name">
    </div>
    <div class="form-row mandatory">
        <label><g:message code="email"/>:</label>
        <input type="text" name="email" validation="required email">
    </div>
    <div class="form-row">
        <label><g:message code="comment"/>:</label>
        <textarea name="comment"></textarea>
    </div>
    <div class="form-row btn-row">
        <button type="submit" class="submit-button"><g:message code="share" args="${[""]}"/></button>
        <button type="button" class="cancel-button"><g:message code="back"/></button>
    </div>
</form>