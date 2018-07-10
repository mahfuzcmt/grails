<%@ page import="com.webcommander.constants.DomainConstants;com.webcommander.constants.NamedConstants" %>
<div class="mandatory ref-selector-row">
    <g:if test="${type}">
        <label><g:message code="${NamedConstants.NAVIGATION_ITEM_MESSAGE_KEYS[type]}"/></label>
        <g:if test="${type == DomainConstants.NAVIGATION_ITEM_TYPE.EMAIL}">
            <input type="text" class="medium" validation="required email" name="itemRef" value="${ref}">
        </g:if>
        <g:elseif test="${type == DomainConstants.NAVIGATION_ITEM_TYPE.URL}">
            <input type="text" class="medium" validation="required" name="itemRef" value="${ref}">
        </g:elseif>
        <g:elseif test="${type == DomainConstants.NAVIGATION_ITEM_TYPE.AUTO_GENERATED_PAGE}">
            <g:select name="itemRef" from="${[g.message(code: "login"), g.message(code: "registration"), g.message(code: "profile"), g.message(code: "cart.details.list"), g.message(code: "checkout")]}" value="${ref}" keys="${['login', 'register', 'profile', 'cart', 'checkout']}"
                      class="medium"/>
        </g:elseif>
        <g:else>
            <g:if test="${items.size() > 0}">
                <g:select from="${items}" optionKey="id" optionValue="name" validation="required" name="itemRef" value="${ref}" class="medium"></g:select>
            </g:if>
            <g:else>
                <span validation="fail"><g:message code="no.${NamedConstants.NAVIGATION_ITEM_MESSAGE_KEYS[type]}.found"/>.</span>
                <span class="create-new link" type="${type}"><g:message code="create.new"/></span>
            </g:else>
        </g:else>
    </g:if>
</div>