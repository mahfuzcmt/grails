<%@ page import="com.webcommander.util.AppUtil; com.webcommander.constants.DomainConstants" %>
<g:if test="${config.enable_like.toBoolean()}">
    <g:set var="config" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.PRODUCT)}"/>
    <g:set var="like_url" value="${app.currentURL()}"/>
    <g:applyLayout name="_productwidget">
        <g:each var="profile" status="i" in="${['facebook', 'twitter', 'pinterest', 'googleplus']}">
            <g:set var="profileName" value="like_${profile}"/>
            <g:if test="${config[profileName].toBoolean()}">
                <span class="${profile}-like-us">
                    <g:if test="${profile == "googleplus"}">
                        <app:enqueueSiteJs src="//apis.google.com/js/plusone.js" scriptId="plusone"/>
                        <%= "<g" + ":plusone" %> class="google-plus" size="medium" href="${like_url.encodeAsURL()}" <%= "></g" + ":plusone>" %>
                    </g:if>
                    <g:elseif test="${profile == "twitter"}">
                        <iframe src="//platform.twitter.com/widgets/tweet_button.html" allowtransparency="true" frameborder="0" scrolling="no"></iframe>
                    </g:elseif>
                    <g:elseif test="${profile == "pinterest"}">
                        <g:if test="${!request.pinterest_js_loaded}">
                            <app:enqueueSiteJs src="//assets.pinterest.com/js/pinit_main.js" sriptId="pinterest"/>
                            <g:set var="pinterest_js_loaded" value="${true}" scope="request"/>
                        </g:if>
                        <a href="//www.pinterest.com/pin/create/button/?url=${like_url.encodeAsURL()}}" data-pin-do="buttonPin" data-pin-config="beside" data-pin-color="red" data-pin-height="20"></a>
                    </g:elseif>

                    <g:else>
                        <iframe src="//www.facebook.com/plugins/like.php?href=${like_url.encodeAsURL()}&width=80&height=25&colorscheme=light&layout=button_count&action=like&show_faces=false&send=false"
                                scrolling="no" frameborder="0" style="border:none; overflow:hidden;" allowTransparency="true">
                        </iframe>
                    </g:else>
                </span>
            </g:if>
        </g:each>
        <g:if test="${config.tell_friend.toBoolean()}">
            <span class="tell-friend" title="<g:message code='tell.friend'/>"></span>
        </g:if>
    </g:applyLayout>
</g:if>
