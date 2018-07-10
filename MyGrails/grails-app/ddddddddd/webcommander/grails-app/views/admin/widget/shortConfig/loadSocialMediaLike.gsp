<g:applyLayout name="_widgetShortConfig">
    <div class="sidebar-group">
        <div class="sidebar-group-label"><g:message code="orientation"/></div>
        <div class="sidebar-group-body">
            <div>
                <input type="radio" name="orientation" value="H" ${config.orientation == "H" ? "checked" : ""}>
                <label><g:message code="horizontal"/></label>
            </div>
            <div>
                <input type="radio" name="orientation" value="V" ${config.orientation == "V" ? "checked" : ""}>
                <label><g:message code="vertical"/></label>
            </div>
        </div>
    </div>
    <g:each var="profile" status="i" in="${['facebook', 'twitter', 'googleplus', 'pinterest']}">
        <div class="sidebar-group like-${profile}">
            <input type="checkbox" class="single" name="socialMediaConfig" value="${profile}" ${config.socialMediaConfig.contains(profile) ? 'checked="checked"' : ''}>
            <img src="${appResource.getSocialMediaLikeIconURL(profileName: profile)}">
        </div>
    </g:each>
</g:applyLayout>