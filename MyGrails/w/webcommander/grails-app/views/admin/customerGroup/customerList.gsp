<h4 class="group-label"><g:message code="customers"/></h4>
<div class="view-content-block">
    <g:each in="${customers}" var="customer" status="i">
        <div class="view-list-row">
            ${customer.firstName.encodeAsBMHTML() + (customer.isCompany ? "" : " " + customer.lastName?.encodeAsBMHTML())} ${customer.isInTrash ? "(" + g.message(code: "in.trash") + ")" : ""}
        </div>
    </g:each>
</div>
