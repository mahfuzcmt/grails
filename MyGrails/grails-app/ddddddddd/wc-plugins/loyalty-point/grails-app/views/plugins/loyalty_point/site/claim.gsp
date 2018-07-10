<h1><g:message code="loyalty.points"/></h1>
<div class="loyalty-claim">
    <form  method="post" action="${app.baseUrl()}loyaltyPoint/convert">
        <div class="loyality_point_quentity">
            <g:message code="loyalty.point.store.message" args="${[totalPoints]}"/>
        </div>

        <div class="form-row">
            <label><g:message code="convert.loyalty.point.to"/></label>
            <loyaltyPoint:convertTo validation="required" class="medium" name="convertTo" />
        </div>
        <div class="form-row">
            <label><g:message code="convert.amount"/></label>
            <input type="text" validation="required digits gt[0]" restrict="numeric" name="convertAmount">
        </div>
        <div class="button-line">
            <button type="button" class="loyalty-button back" target="customerProfile"><g:message code="back"/> </button>
            <button type="submit" class="convert-loyalty submit-button"><g:message code="convert"/> </button>
        </div>
    </form>
</div>
