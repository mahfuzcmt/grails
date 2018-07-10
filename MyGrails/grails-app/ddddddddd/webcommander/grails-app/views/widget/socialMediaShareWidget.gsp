<g:applyLayout name="_widget">
    <div class="social-media-share-bar ${config.display_option == "H" ? "horizontal" : "vertical"}">
        <g:each in="${config.socialMediaConfig}" var="profile">
            <span class="social-media-share">
                <a class="addthis_button_${profile}"></a>
            </span>
        </g:each>
    </div>
    <g:if test="${request.page && !request.addthis_loaded}">
        <%
            request.js_cache.push("//s7.addthis.com/js/300/addthis_widget.js")
            request.addthis_loaded = true
        %>
    </g:if>
</g:applyLayout>