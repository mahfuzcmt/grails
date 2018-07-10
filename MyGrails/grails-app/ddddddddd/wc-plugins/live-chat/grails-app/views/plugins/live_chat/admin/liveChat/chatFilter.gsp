<%@ page import="com.webcommander.plugin.live_chat.constants.NamedConstants; com.webcommander.admin.Operator; com.webcommander.plugin.live_chat.ChatTag" %>
<form action="${app.relativeBaseUrl()}productReviewAdmin/loadAppView" method="post" class="edit-popup-form">
    <div class="double-input-row">
        <div class="form-row">
            <label><g:message code="visitor.name"/></label>
            <input type="text" name="customerName" class="large" value=""/>
        </div><div class="form-row chosen-wrapper">
            <label><g:message code="chat.related"/></label>
            <ui:domainSelect domain="${ChatTag}" name="tag" prepend="${["": g.message(code: "none")]}" class="large"/>
        </div>
    </div>
    <div class="form-row datefield-between">
        <label><g:message code="date.between"/></label>
        <input type="text" class="datefield-from smaller" name="dateFrom"><span class="date-field-separator">-</span><input type="text" class="datefield-to smaller" name="dateTo"/>
    </div>
    <div class="double-input-row">
        <div class="form-row">
            <label><g:message code="containing.text"/></label>
            <input name="searchText" type="text" class="large">
        </div><div class="form-row">
            <label><g:message code="phone"/></label>
            <input name="phone" type="text" class="large">
        </div>
    </div>
    <div class="double-input-row">
        <div class="form-row">
            <label><g:message code="supporter"/></label>
            <ui:domainSelect domain="${com.webcommander.admin.Operator}" name="agentId" prepend="${["": g.message(code: "none")]}" text="fullName" class="large" filter="${{ inList("id", agentIds) }}"/>
        </div><div class="form-row">
            <label><g:message code="rating"/></label>
            <ui:namedSelect key="${NamedConstants.CHAT_RATING_TYPE}" prepend="${["": g.message(code: "none")]}" name="rating" class="large"/>
        </div>
    </div>

    <div class="button-line">
        <button type="submit" class="submit-button"><g:message code="search"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>