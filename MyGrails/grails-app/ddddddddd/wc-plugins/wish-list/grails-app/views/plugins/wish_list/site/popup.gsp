<%@ page import="com.webcommander.plugin.wish_list.WishList" %>
<div class="wish-list-popup">
    <div class="header">
        <span class="close-popup close-icon"></span>
        <span class="status-message"><g:message code="add.to.wish.list"/></span>
    </div>
    <div class="body">
        <input name="productId" type="hidden" value="${productId}"/>
        <g:if test="${count <= 0}">
            <div class="no-wish-list-message"><g:message code="no.wish.list.found"/> </div>
            <div class="form-row">
                <label><g:message code="name"/></label>
                <input type="text" name="name" validation="required">
            </div>
        </g:if>
        <g:else>
            <label><g:message code="select.wish.list"/></label>
            <ui:domainSelect filter="${{eq("customer", customer)}}" name="wishList" domain="${WishList}"/>
        </g:else>
    </div>
    <div class="footer">
        <g:if test="${count <= 0}">
            <button class="submit-button create-wish-list" type="submit"><g:message code="create"/></button>
        </g:if>
        <g:else>
            <button class="submit-button add-to-wish-list et_pdp_add_to_wish_list" type="submit"><g:message code="add"/></button>
        </g:else>
        <button class="form-reset close-popup" onclick="return false"><g:message code="cancel"/></button>
    </div>
</div>