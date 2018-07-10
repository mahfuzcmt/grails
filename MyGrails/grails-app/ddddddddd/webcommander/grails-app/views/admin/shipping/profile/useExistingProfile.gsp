<div class="profile-editor-panel">
    <g:if test="${profileList.size() > 0}">
        <div class="edit-popup-form">
            <div class="create-profile-option">
                <g:each in="${profileList}" var="profile">
                    <div class="profile-listing" entity-id="${profile.id}">
                        <span class="label">${profile.name}</span>
                        <div class="description">${profile.description}</div>
                    </div>
                </g:each>
            </div>
            <div class="rule-copy-selection">
                <div class="form-row">
                    <ui:namedSelect class="medium" class="profile-copy-type" name="copyType" key="${com.webcommander.constants.NamedConstants.COPY_SHIPPING_PROFILE_KEYS}"/>
                </div>
            </div>
            <div class="button-line">
                <button type="submit" class="submit-button edit-popup-form-submit"><g:message code="next"/> </button>
                <button type="button" class="cancel-button"><g:message code="cancel"/></button>
            </div>
        </div>
    </g:if>
    <g:else>
        <span class="no-profile-to-select"><g:message code="no.shipping.profile.created"/></span>
    </g:else>
</div>