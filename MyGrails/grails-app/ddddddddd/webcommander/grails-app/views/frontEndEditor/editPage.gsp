<div class="fee-common-popup-content">
    <%@ page import="com.webcommander.content.Navigation; com.webcommander.design.Layout; com.webcommander.constants.DomainConstants" %>
    <form action="${app.relativeBaseUrl()}frontEndEditor/savePage" method="post" class="create-edit-form">
        <div class="header-line">
            <span class="title">Add Page</span>
            <span class="icon close"></span>
        </div>

        <div class="body">
            <div class="popup_form_fields">
                <div class="notice" style="display: none"></div>

                <div class="fee-form-row fee-multi-input-row">
                    <div class="fee-form-element mandatory">
                        <label><g:message code="page.name"/><span class="str">*</span></label>
                        <input type="text" name="name" class="large unique prefill-value-to" value="${page.name}" validation="required rangelength[2, 100]"
                               maxlength="100" data-prefill-value-to="[name='title']">
                    </div>
                    <div class="fee-form-element mandatory">
                        <label><g:message code="page.title"/><span class="str">*</span></label>
                        <input type="text" name="title" class="large" value="${page.title}" validation="required rangelength[2, 250]" maxlength="250">
                    </div>
                </div>
                <div class="form-row trash-row" style="display: none; padding-top: 40px;">
                    <label><g:message code="what.to.do"/></label>
                    <span class="restore-close-window"><a onclick="return false" class="trash-duplicate-restore fake-link" style="color: #1199C4;font-style: italic;cursor: pointer;"><g:message code="restore"/></a> <g:message code="restore.and.close.window"/> <g:message code="or"/></span>
                    <span><input type="checkbox" name="deleteTrashItem.name" class="trash-duplicate-delete multiple"> <g:message code="delete.and.save"/></span>
                </div>
                <div class="fee-form-row">
                    <div class="fee-form-element mandatory">
                        <label><g:message code="layout"/></label>
                        %{--<ui:domainSelect name="layoutid" class="large always-bottom" domain="${Layout}" value="${page.layout?.id}" prepend="${[(''): g.message(code: 'no.layout')]}"/>--}%
                        <ui:domainSelect name="layoutid" class="large always-bottom" domain="${Layout}" value="${page.layout?.id}"/>
                    </div>
                </div>
                <div class="fee-form-row">
                    <div class="fee-form-element mandatory">
                        <label><g:message code="navigation"/></label>
                        <ui:domainSelect domain="${Navigation}" class="always-bottom" custom-attrs="${[multiple: 'true']}" name="linkedNavigations"/>
                    </div>
                </div>
            </div>
        </div>
        <div class="button-line">
            <button type="button" class="cancel-button fee-pu-button"><g:message code="cancel"/></button>
            <button type="submit" class="submit-button fee-pu-button fee-save edit-popup-form-submit"><g:message code="${page.id ? "update" : "save"}"/></button>
        </div>
    </form>
</div>