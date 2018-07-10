<%@ page import="com.webcommander.content.NavigationService;com.webcommander.constants.NamedConstants" %>
<div class='auto-populate-navigtion'>
    <g:set var="items" value="${NavigationService.auto_population_item_type}"/>
    <g:each in="${items}" var="item">
        <div class='auto-populate-option' item-type="${item}">
            <span class="label"><g:message code="${NamedConstants.NAVIGATION_ITEM_MESSAGE_KEYS[item]}"/></span>
        </div>
    </g:each>
</div>