<g:applyLayout name="_widgetShortConfig">
    <div class="sidebar-group">
        <label><g:message code="display.option"/> </label>
        <div>
            <input type="radio" name="display_option" class="radio" value="H" ${config.display_option ? (config.display_option == "H" ? "checked" : "") : "checked"}>
            <g:message code="horizontal"/>
            <input type="radio" name="display_option" class="radio" value="V" ${config.display_option ? (config.display_option == "V" ? "checked" : "") : ""}>
            <g:message code="vertical"/>
        </div>
    </div>
    <g:each var="profile" status="i" in="${[[name: 'twitter', label: 'Twitter',  value: 'twitter'], [name: 'facebook', label: 'Facebook',  value: 'facebook'], [name: 'linkedin', label: 'Linkedin',  value: 'linkedin'], [name: 'stumbleupon', label: 'StumbleUpon',  value: 'stumbleupon_badge'], [name: 'digg', label: 'Digg',  value: 'digg'], [name: 'reddit', label: 'Reddit',  value: 'reddit'],[name: 'delicious', label: 'Delicious',  value: 'delicious'],[name: 'myspace', label: 'MySpace',  value: 'myspace'], [name: 'email', label: 'Email',  value: 'email'], [name: 'print', label: 'Print',  value: 'print'],[name: 'blogger', label: 'Blogger',  value: 'blogger'], [name: 'friendfeed', label: 'Friend Feed',  value: 'friendfeed'], [name: 'livejournal', label: 'Live Journal',  value: 'livejournal'], [name: 'wordpress', label: 'Wordpress',  value: 'wordpress'], [name: 'googleplus', label: 'Google+',  value: 'google']]}">
        <div class="sidebar-group shareBar-${profile.name} social-link">
            <div class="sidebar-group-label">
                <input type="checkbox" class="single" name="socialMediaConfig" value="${profile.value}" ${config.socialMediaConfig.contains(profile.value) ? 'checked="checked"' : ''}>
                <img src="${appResource.getSocialMediaIconURL(profileName: profile.name)}">
                <label>${profile.label}</label>
            </div>
        </div>
    </g:each>
</g:applyLayout>