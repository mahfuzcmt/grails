<%@ page import="com.webcommander.webcommerce.Product; com.webcommander.constants.DomainConstants; com.webcommander.constants.NamedConstants;com.webcommander.webcommerce.Category" %>
<g:if test="${type == DomainConstants.NAVIGATION_ITEM_TYPE.PRODUCT}">
    <div class="ref-selector-row product-selector">
        <div class="form-row \">
            <label><g:message code="product.category"/></label>
            <ui:hierarchicalSelect class="medium category-selector" domain="${Category}" prepend="${["": g.message(code: "all.categories"), "root" : g.message(code: "root")]}"/>
        </div>
        <div class="form-row product-row">
            <label><g:message code="product"/></label>
            <g:select from="${Product.findAllByIsInTrash(false)}" optionKey="id" optionValue="name" validation="required" name="linkTo" value="${linkTo}" class="medium"></g:select>
        </div>
    </div>
</g:if>
<g:else>
    <div class="form-row ref-selector-row">
        <label><g:message code="${NamedConstants.NAVIGATION_ITEM_MESSAGE_KEYS[type]}"/></label>
        <g:if test="${type == DomainConstants.NAVIGATION_ITEM_TYPE.EMAIL || type == DomainConstants.NAVIGATION_ITEM_TYPE.URL}">
            <input type="text" class="medium" validation="required@if{self::visible} ${type}" name="linkTo" value="${linkTo}">
        </g:if>
        <g:elseif test="${type == DomainConstants.NAVIGATION_ITEM_TYPE.AUTO_GENERATED_PAGE}">
            <g:select name="linkTo" from="${[g.message(code: "login"), g.message(code: "registration"), g.message(code: "profile"), g.message(code: "cart.details.list"), g.message(code: "checkout")]}"
                      value="${linkTo}" keys="${['login', 'register', 'profile', 'cart', 'checkout']}" class="medium"/>
        </g:elseif>
        <g:else>
            <g:if test="${items.size() > 0}">
                <g:select from="${items}" optionKey="id" optionValue="name" validation="required@if{self::visible}" name="linkTo" value="${linkTo}" class="medium"></g:select>
            </g:if>
            <g:else>
                <span validation="fail@if{self::visible}"><g:message code="no.${NamedConstants.NAVIGATION_ITEM_MESSAGE_KEYS[type]}.found"/></span>
            </g:else>
        </g:else>
    </div>
</g:else>