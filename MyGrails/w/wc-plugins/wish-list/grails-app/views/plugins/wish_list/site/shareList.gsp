<h1><g:message code="share" args="${[wishList.name]}"/></h1>
<g:if test="${wishList.emails.size() == 0}">
    <tr>
        <td colspan="2">
            <span class="no-data"><g:message code="wish.list.not.shared"/> </span>
        </td>
    </tr>
</g:if>
<g:else>
    <table class="wish-list-share-tbl">
        <colgroup>
            <col class="name-column">
            <col class="email-column">
        </colgroup>
        <tr>
            <th><g:message code="name"/></th>
            <th><g:message code="email"/></th>
        </tr>
        <g:each in="${wishList.emails}" var="email">
            <tr>
                <td class="name-column">${email.name.encodeAsBMHTML()}</td>
                <td class="email-column">${email.email.encodeAsBMHTML()}</td>
            </tr>
        </g:each>
    </table>
</g:else>
<form class="wish-list-share-form" action="${app.relativeBaseUrl()}wishlist/share">
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