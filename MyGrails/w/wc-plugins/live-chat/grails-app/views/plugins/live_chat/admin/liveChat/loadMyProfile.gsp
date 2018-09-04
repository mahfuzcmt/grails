<%@ page import="com.webcommander.plugin.live_chat.ChatDepartment; com.webcommander.plugin.live_chat.constants.DomainConstants" %>
<div class="toolbar-share">
    <div class="toolbar toolbar-right">
        <div class="tool-group">
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>
<div class="app-tab-content-container">
    <form action="${app.relativeBaseUrl()}liveChatAdmin/updateOperatorProfile" method="post" class="create-edit-form operator-profile-form" enctype="multipart/form-data">
        <input type="hidden" class="medium" name="id" value="${chatOperatorProfile?.id}">
        <div class="double-input-row">
            <div class="form-row">
                <label><g:message code="display.name"/></label>
                <input type="text" class="medium" name="displayName" value="${chatOperatorProfile?.displayName ? chatOperatorProfile.displayName : operatorProfile.fullName}" validation="required rangelength[2,100]" maxlength="100">
            </div>
            <div class="form-row">
                <div class="form-image-block">
                    <input type="file" name="profileImage" file-type="image" previewer="image-preview" ${chatOperatorProfile.profileImage ? 'remove-support="true"' : 'reset-support="true"'} class="medium"
                           remove-option-name="remove-image">
                    <div class="preview-image">
                        <g:set var="imagePath" value="${appResource.getChatProfileImageUrl(profileImage: chatOperatorProfile)}"/>
                        <img id="image-preview" src="${imagePath}">
                    </div>
                </div>
            </div>
        </div>
        <div class="double-input-row">
            <div class="form-row">
                <label><g:message code="chat.limit"/></label>
                <input type="text" class="medium" restrict="signed_numeric" name="chatLimit" value="${chatOperatorProfile?.chatLimit ? chatOperatorProfile.chatLimit : config[DomainConstants.CHAT_ROUTING_CONFIG.CHAT_LIMIT]}" validation="required number maxlength[2]">
            </div>
            <div class="form-row chosen-wrapper">
                <label><g:message code="skills"/></label>
                <g:select name="skills" class="medium" multiple="" from="${chatDepartments.name}" keys="${chatDepartments.id}" value="${operatorProfile ? ChatDepartment.createCriteria().list{operators{ eq("id", operatorProfile.id)}}.id : ""}" validation="required"></g:select>
            </div>
        </div>
        <div class="form-row ">
            <button type="submit" class="submit-button update-chat-operator-profile"><g:message code="update"/></button>
        </div>
    </form>
</div>

