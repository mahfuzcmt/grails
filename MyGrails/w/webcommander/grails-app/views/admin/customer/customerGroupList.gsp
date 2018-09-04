<h4 class="group-label"><g:message code="customer.groups"/></h4>
    <div class="view-content-block">
    <g:each in="${customerGroups}" var="customerGroup" status="i">
        <div class="view-list-row">
            ${customerGroup.name.encodeAsBMHTML()}
        </div>
    </g:each>
</div>
