<g:applyLayout name="_widgetShortConfig">
    <div class="sidebar-group">
        <div class="sidebar-group-label"><g:message code="display.option"/></div>
        <div class="sidebar-group-body">
            <div>
                <input type="radio" name="display_option" class="radio" value="H" ${config.display_option == "H" ? "checked" : ""}>
                <label><g:message code="horizontal"/></label>
            </div>
            <div>
                <input type="radio" name="display_option" class="radio" value="V" ${config.display_option == "V" ? "checked" : ""}>
                <label><g:message code="vertical"/></label>
            </div>
        </div>
    </div>
    <g:each var="profile" status="i" in="${[[name: 'feed', label: 'RSS/Feedburner URL'], [name: 'twitter', label: 'Twitter URL'], [name: 'facebook', label: 'Facebook URL'],[name: 'googleplus', label: 'Google+ URL'],[name: 'linkedin', label: 'Linkedin URL'], [name: 'stumbleupon', label: 'StumbleUpon URL'],[name: 'digg', label: 'Digg URL'], [name: 'reddit', label: 'Reddit URL'],[name: 'delicious', label: 'Delicious URL',value: ''],[name: 'youtube', label: 'YouTube URL'], [name: 'myspace', label: 'MySpace URL']]}">
        <div class="sidebar-group ${profile.name} social-link">
            <div class="sidebar-group-label">
                <img src="${appResource.getSocialMediaIconURL(profileName: profile.name)}">
                <label>${profile.label}</label>
            </div>
            <div class="sidebar-group-body">
                <div class="form-row without-label">
                    <input type="hidden" name="socialProfileName" value="${profile.name}">
                    <input type="text" class="sidebar-input" name="socialProfileLink" value="${config.socialMediaConfig.find{ it.key == profile.name}?.value}" validation="url">
                </div>
            </div>
        </div>
    </g:each>
</g:applyLayout>