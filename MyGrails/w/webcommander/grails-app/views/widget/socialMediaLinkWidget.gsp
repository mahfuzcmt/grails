<g:applyLayout name="_widget">
    <div class="social-media-link-bar ${config.display_option == "H" ? "horizontal" : "vertical"}">
        <g:each in="${config.socialMediaConfig}" var="profile">
            <span class="social-media-link">
                <a class="${profile.key}" href="${profile.value}" target="_blank"></a>
            </span>
        </g:each>
    </div>
</g:applyLayout>