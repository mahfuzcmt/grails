<%@ page import="com.webcommander.admin.Country" %>
<div class="add-currency-popup">
    <div class="header">
        <div class="search-form search-currency">
            <input type="text" class="search-text">
            <button class="icon-search" type="button"></button>
        </div>
    </div>
    <div class="body">
        <table>
            <colgroup>
                <col class="name-column">
                <col class="action-column">
            </colgroup>
            <g:each in="${currencies}" var="currency">
                <tr>
                    <td class="code">
                        <g:if test="${currency.country}">
                            <g:set var="code" value="${currency.country.code}"/>
                            <span class="flag-icon ${code.toLowerCase()}"></span>
                        </g:if>
                        ${currency.code.encodeAsBMHTML()}
                    </td>
                    <td class="action">
                        <button type="button" data-id="${currency.id}" class="select-currency"><g:message code="select"/></button>
                    </td>
                </tr>
            </g:each>
        </table>
    </div>
    <div class="button-line">
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</div>