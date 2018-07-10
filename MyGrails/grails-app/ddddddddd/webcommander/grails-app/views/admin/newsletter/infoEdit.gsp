<%@ page import="com.webcommander.common.Email" %>
<g:form class="create-edit-form newsletter-form" controller="newsletter" action="saveNewsletter">
    <input type="hidden" name="id" value="${newsletter.id}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="newsletter.info"/></h3>
            <div class="info-content"><g:message code="section.text.newsletter.info"/></div>
        </div>
        <div class="form-section-container">
            <div class="double-input-row">
                <div class="form-row mandatory">
                    <label><g:message code="title"/><span class="suggestion"> e.g. Name of Newsletter</span> </label>
                    <g:textField class="large unique" name="title" value="${newsletter.title}" maxlength="250" validation="required rangelength[2,255]"/>
                </div><div class="form-row">
                    <label><g:message code="schedule.time"/><span class="suggestion"> e.g. 2015-01-20 11:04:49</span> </label>
                    <input name="scheduleTime" type="text" class="large timefield" show-select-today value="${newsletter.scheduleTime ? newsletter.scheduleTime.toDatePickerFormat(true, session.timezone) : ""}">
                </div>
            </div>
            <div class="form-row chosen-wrapper">
                <label><g:message code="administrative.status"/><span class="suggestion">e.g. Active</span></label>
                <g:select name="active" from="${['active', 'inactive'].collect {g.message(code: it)}}" keys="${['true', 'false']}" value="${newsletter.isActive}"/>
            </div>
            <div class="double-input-row">
                <div class="form-row mandatory">
                    <label><g:message code="from"/><span class="suggestion"> e.g. peter@abc.com</span> </label>
                    <g:textField name="sender" class="large" value="${newsletter.sender ?: sender}" validation="required email"/>
                </div><div class="form-row mandatory newsletter-receiver-row">
                    <label><g:message code="to"/><span class="suggestion"> e.g. Selected Customers list  </span> </label>
                    <input type="text" name="receiver" class="large" readonly validation="required" error-position="after" validate-on="call-only" value="${receiverValue}">
                    <span class="tool-icon add-receiver" title="<g:message code="add.receiver"/>"></span>
                    <g:each in="${newsletter.newsletterReceivers.findAll { it.receiverType == "customer"}}" var="receiver">
                        <input type="hidden" class="selected-recipient" name="customer" value="${receiver.receiverId}">
                    </g:each>
                    <g:each in="${newsletter.newsletterReceivers.findAll { it.receiverType == "customerGroup"}}" var="receiver">
                        <input type="hidden" class="selected-recipient" name="customerGroup" value="${receiver.receiverId}">
                    </g:each>
                    <g:each in="${newsletter.newsletterReceivers.findAll { it.receiverType == "recipientEmail"}}" var="receiver">
                        <input type="hidden" class="selected-recipient" name="recipientEmail" value="${receiver.receiverId}">
                    </g:each>
                    <g:each in="${newsletter.newsletterReceivers.findAll { it.receiverType == "subscriber"}}" var="receiver">
                        <input type="hidden" name="includeAllSubscriber" value="${true}">
                    </g:each>
                    <g:each in="${newsletter.newsletterReceivers.findAll { it.receiverType == "email"}}" var="receiver">
                        <g:set var="email" value="${Email.get(receiver.receiverId)}"/>
                        <input type="hidden" class="selected-recipient" name="recipientEmail" value="${email.name.encodeAsHTML()}">
                        <input type="hidden" class="selected-recipient" name="recipientName" value="${email.email.encodeAsHTML()}"></div>
                    </g:each>
                </div>
            </div>
            <div class="form-row mandatory">
                <label><g:message code="subject"/><span class="suggestion"> e.g. Subject of Newsletter</span> </label>
                <g:textField name="subject" value="${newsletter.subject}" maxlength="250"  validation="required maxlength[250]" class="large"/>
            </div>
            <div class="form-row  mandatory tinymce-container">
                <label><g:message code="body"/><span class="suggestion"> Put a detailed description of your newsletter. You can insert a link, table, image, video or other cool stuff in here.</span> </label>
                <textarea class="wceditor no-auto-size xx-larger" toolbar-type="advanced" name="description" validation="required">${newsletter.body}</textarea>
            </div>
            <div class="form-row">
                <button type="submit" class="submit-button"><g:message code="save"/></button>
                <button type="button" class="save-and-send"><g:message code="save.and.send"/></button>
                <button type="button" class="cancel-button"><g:message code="cancel"/></button>
            </div>
        </div>
    </div>
</g:form>
