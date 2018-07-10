<form action="${app.relativeBaseUrl()}blogAdmin/loadCommentAppView" method="post" class="edit-popup-form">
    <div class="double-input-row">
        <div class="form-row">
            <label><g:message code="name"/></label>
            <input type="text" name="name" class="large"/>
        </div><div class="form-row">
            <label><g:message code="email"/></label>
            <input type="text" name="email" class="large"/>
        </div>
    </div>
    <div class="double-input-row">
        <div class="form-row">
            <label><g:message code="content"/></label>
            <input type="text" name="content" class="large" value="${params.searchText ?: ""}"/>
        </div><div class="form-row chosen-wrapper">
            <label><g:message code="status"/></label>
            <g:select class="large" name="status" from="${[g.message(code: "any"), g.message(code: "approved"), g.message(code: "pending"), g.message(code: "rejected")]}" keys="${["", "approved", "pending", "rejected"]}"/>
        </div>
    </div>
    <div class="double-input-row">
        <div class="form-row">
            <label><g:message code="marked.as.spam"/></label>
            <input type="radio" name="isSpam" value="true"/>
            <span><g:message code="yes"/></span>&nbsp;&nbsp;&nbsp;
            <input type="radio" name="isSpam" value="false"/>
            <span><g:message code="no"/></span>
        </div><div class="form-row chosen-wrapper">
            <label><g:message code="post"/></label>
            <select class="large" name="post">
                <option value=""><g:message code="any"/></option>
                <g:each in="${posts}" var="post">
                    <option value="${post.id}">${post.name}</option>
                </g:each>
            </select>
        </div>
    </div>
    <div class="form-row datefield-between">
        <label><g:message code="created.between"/></label>
        <input type="text" class="datefield-from smaller" name="createdFrom"><span class="date-field-separator">-</span><input type="text" class="datefield-to smaller" name="createdTo"/>
    </div>
    <div class="form-row datefield-between">
        <label><g:message code="updated.between"/></label>
        <input type="text" class="datefield-from smaller" name="updatedFrom"><span class="date-field-separator">-</span><input type="text" class="datefield-to smaller" name="updatedTo"/>
    </div>
    <div class="button-line">
        <button type="submit" class="submit-button"><g:message code="search"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>