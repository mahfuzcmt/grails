<form action="${app.relativeBaseUrl()}assetLibrary/restoreCss" method="post" class="edit-popup-form" >
    <g:if test="${versions.size() > 0}">
        <div class="form-row">
            <label><g:message code="version"/> </label>
            <g:select class="medium" name="version" from="${versions}" keys="${versions}"></g:select>
        </div>
        <div class="button-line">
            <button type="submit" class="submit-button"><g:message code="fetch.content"/></button>
            <button type="button" class="cancel-button"><g:message code="cancel"/></button>
        </div>
    </g:if>
    <g:else>
        <span><g:message code="css.previous.version.unavailable"/></span>
    </g:else>
</form>