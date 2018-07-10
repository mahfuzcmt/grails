<g:applyLayout name="_widget">
    <%
        if(request.page) {
            request.js_cache.push("plugins/facebook/js/site-widget/facebook.widget.js")
        }
    %>
    <div class="facebook-buttons-container">
        <g:if test="${config.tab_enabled == "true"}">
            <button class="fb-add-to-page"><g:message code="add.to.page"/></button>
        </g:if>
        <g:if test="${config.share_enabled == "true"}">
            <button class="fb-share"><g:message code="fb.share"/></button>
        </g:if>
        <g:if test="${config.invite_enabled == "true"}">
            <button class="fb-invite" data-message="${config.invite_message.encodeAsBMHTML()}"><g:message code="fb.invite"/></button>
        </g:if>
    </div>
</g:applyLayout>