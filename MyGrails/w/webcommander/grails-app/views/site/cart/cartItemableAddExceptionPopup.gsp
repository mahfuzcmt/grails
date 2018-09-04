<%@ page import="com.webcommander.parser.EmailTemplateParser" %>
<div class="exception-popup">
    <div class="header">
        <span class="close-popup close-icon"></span>
            <span class="status-message"><g:message code="items.could.not.be.added.your.cart" args="${[totalItems, exceptions.size()]}"/></span>
    </div>
    <div class="body">
        <table>
            <colgroup>
                <col class="item">
                <col class="error-message">
            </colgroup>
            <tr>
                <th><g:message code="item"/></th>
                <th><g:message code="message"/></th>
            </tr>
            <g:each in="${exceptions}" var="ex">
                <tr>
                    <td>${ex.itemName}</td>
                    <td>${ex.errorMessage}</td>
                </tr>
            </g:each>
        </table>
    </div>
    <div class="footer">
        <div class="button-item">
            <a class="cart-page-button button et_ecommerce_view_cart" et-category="button" href="${app.relativeBaseUrl() + "cart/details"}"><g:message code="view.cart"/></a>
        </div>
    </div>
</div>