<%@ page import="com.webcommander.constants.DomainConstants" %>
<div class="body">
    <table>
        <colgroup>
            <col style="width: 20%">
            <col style="width: 20%">
            <col style="width: 20%">
        </colgroup>
        <tr>
            <th><g:message code="first.name"/></th>
            <th><g:message code="last.name.surname"/></th>
            <th><g:message code="email"/></th>
        </tr>
        <g:set var="isNotFound" value="${true}" />
        <g:each in="${customers}" var="customer">
            <%
                if(customer == newCustomer) {
                    isNotFound = false
                }
            %>
            <tr class="customer-row ${customer.id == params.long("id") ? "selected": ""} ${customer == newCustomer ? "highlighted": ""}">
                <input type="hidden" name="id" value="${customer.id}"/>
                <td>${customer.firstName.encodeAsBMHTML()} &nbsp; ${customer.isCompany ? "<span class='mark-icon company'></span>" : ""}</td>
                <td>${customer.lastName.encodeAsBMHTML()}</td>
                <td>${customer.userName.encodeAsBMHTML()}</td>
            </tr>

        </g:each>
        <g:if test="${newCustomer && isNotFound && newCustomer.status == DomainConstants.CUSTOMER_STATUS.ACTIVE}">
            <tr class="customer-row highlighted">
                <input type="hidden" name="id" value="${newCustomer.id}"/>
                <td>${newCustomer.firstName.encodeAsBMHTML()} &nbsp; ${newCustomer.isCompany ? "<span class='mark-icon company'></span>" : ""}</td>
                <td>${newCustomer.lastName.encodeAsBMHTML()}</td>
                <td>${newCustomer.userName.encodeAsBMHTML()}</td>
            </tr>
        </g:if>
    </table>
</div>
<div class="footer">
    <paginator total="${count}" offset="${params.offset}" max="${10}"></paginator>
</div>
