<%@ page import="com.webcommander.admin.Customer" %>
<g:applyLayout name="_widget">
    <g:if test="${session.customer}">
        <g:set var="customer" value="${Customer.get(session.customer)}"/>
        <div class="content">
            <div><g:message code="you.logged.as" args="${[(customer.firstName + (customer.lastName ? ' ' + customer.lastName : ''))]}"/></div>
            <div class="logout-link">
                <a href="${app.relativeBaseUrl()}customer/logout"><g:message code="logout"/></a>
            </div>
            <div class="pro-link">
                <a href="${app.relativeBaseUrl()}customer/profile"><g:message code="my.profile"/></a>
            </div>
        </div>
    </g:if>
    <g:else>
        <form class="login-form valid-verify-form" action="${app.relativeBaseUrl()}customer/loginFromWidget" method="post">
            <g:if test="${request.page}">
                <input type="hidden" name="pageUrl" value="${request.page.url}">
                <input type="hidden" name="widget-id" value="${widget.id}">
            </g:if>
            <div class="form-row mandatory">
                <label>${config.name_label}</label>
                <input type="text" class="medium" name="name" validation="required email rangelength[2,50]" value="" placeholder="Email">
            </div>
            <div class="form-row mandatory">
                <label>${config.password_label}</label>
                <input type="password" class="medium" name="password" validation="required rangelength[2,50]" value="" placeholder="Password">
            </div>
            <g:if test="${config.useCaptcha}">
                <ui:captcha/>
            </g:if>
            <div class="form-row submit-row">
                <button class="login-button"><g:message code="login"/></button>
            </div>
            <g:if test="${config.reset_password_active == "activated"}">
                <div class="form-row">
                    <span class="lost-password"><a href="${app.relativeBaseUrl()}customer/resetPassword">${config.reset_password_label.encodeAsBMHTML()}</a></span>
                </div>
            </g:if>
            <g:if test="${config.reg_link_active == "activated"}">
                <div class="form-row">
                    <span><g:message code="dont.have.account"/></span>
                    <span class="account-register"><a href="${app.relativeBaseUrl()}customer/register">${config.reg_link_label.encodeAsBMHTML()}</a></span>
                </div>
            </g:if>
        </form>
    </g:else>
</g:applyLayout>