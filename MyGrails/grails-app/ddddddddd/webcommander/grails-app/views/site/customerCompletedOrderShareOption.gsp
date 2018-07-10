<div class="customer-share-options">
    <label>Share Using</label>
    <div class="share-medium">
        <g:each in="${[[name:'facebook', value: ''], [name: 'twitter', value: ''], [name: 'googleplus', value: ''], [name: 'linkedin', value: '']]}" var="profile" status="i">
            <div class="${profile.name}-share">
                <img src="${appResource.getSocialMediaIconURL(profileName: profile.name)}"/>
                <span class="image-text">${profile.name.capitalize()}</span>
            </div>
        </g:each>
    </div>
</div>