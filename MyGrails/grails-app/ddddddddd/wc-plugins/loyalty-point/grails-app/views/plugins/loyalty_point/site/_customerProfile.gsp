<div class="loyalty-profile">
    <div class="loyality_point_quentity">
        <g:message code="loyalty.point.store.message" args="${[totalPoints]}"/>
    </div>
    <p><g:message code="loyalty.point.customer.profile.text"/></p>
    <div class="button-line">
        <g:if test="${configs.enable_store_credit == "true"}">
            <span class="button loyalty-button claim-rewards" target="claimRewards"><g:message code="claim.rewards"/></span>
        </g:if>
        <span class="button loyalty-button view-history" target="history"><g:message code="view.history"/></span>
    </div>
</div>
