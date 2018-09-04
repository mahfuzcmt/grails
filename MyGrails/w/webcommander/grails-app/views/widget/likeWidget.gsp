<g:applyLayout name="_widget">
    <div class="like-bar ${config.orientation == "H" ? 'horizontal' : 'vertical'}">
        <g:set var="like_url" value="${app.currentURL()}"/>
        <g:each in="${config.socialMediaConfig}" var="profile">
            <span class="${profile}-like-us">
                <g:if test="${profile == "googleplus"}">
                    <g:if test="${editMode}"></g:if>
                    <g:else>
                        <g:if test="${!request.gplus_loaded}">
                            <%
                                request.js_cache.push("//apis.google.com/js/plusone.js")
                                request.gplus_loaded = true
                            %>
                        </g:if>
                        <%= "<g" + ":plusone" %> class="google-plus" size="medium" href="${like_url.encodeAsURL()}" <%= "></g" + ":plusone>" %>
                    </g:else>
                </g:if>
                <g:elseif test="${profile == "twitter"}">
                    <iframe src="//platform.twitter.com/widgets/tweet_button.html" allowtransparency="true" frameborder="0" scrolling="no"></iframe>
                </g:elseif>
                <g:elseif test="${profile == "pinterest"}">
                    <g:if test="${editMode}"></g:if>
                    <g:else>
                        <g:if test="${!request.pinterest_js_loaded}">
                            <%
                                request.js_cache.push("//assets.pinterest.com/js/pinit_main.js")
                                request.pinterest_js_loaded = true
                            %>
                        </g:if>
                        <a href="//www.pinterest.com/pin/create/button/?url=${like_url.encodeAsURL()}"  data-pin-do="buttonPin" data-pin-config="beside" data-pin-color="red" data-pin-height="20"></a>
                    </g:else>
                </g:elseif>
                <g:else>
                    <iframe src="//www.facebook.com/plugins/like.php?href=${like_url.encodeAsURL()}&width=80&height=25&colorscheme=light&layout=button_count&action=like&show_faces=false&send=false" scrolling="no" frameborder="0" style="border:none; overflow:hidden;" allowTransparency="true">
                    </iframe>
                </g:else>
            </span>
        </g:each>
    </div>
</g:applyLayout>
