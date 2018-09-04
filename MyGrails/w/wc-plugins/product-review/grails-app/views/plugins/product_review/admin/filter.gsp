<form action="${app.relativeBaseUrl()}productReviewAdmin/loadAppView" method="post" class="edit-popup-form">
    <div class="double-input-row">
        <div class="form-row">
            <label><g:message code="product.name"/></label>
            <input type="text" name="productName" class="large" value=""/>
        </div><div class="form-row">
            <label><g:message code="reviewer.name"/></label>
            <input type="text" name="reviewerName" class="large"/>
        </div>
    </div>
    <div class="form-row datefield-between">
        <label><g:message code="review.date.between"/></label>
        <input type="text" class="datefield-from smaller" name="dateFrom"><span class="date-field-separator"><span>-</span></span><input type="text" class="datefield-to smaller" name="dateTo"/>
    </div>
    <div class="double-input-row">
        <div class="form-row">
            <label><g:message code="rating.between"/></label>
            <div class="rating" score="0" name="ratingFrom"></div>
        </div><div class="form-row">
            <label>&nbsp;</label>
            <div class="rating" score="0" name="ratingTo"></div>
        </div>
    </div>
    <div class="button-line">
        <button type="submit" class="submit-button"><g:message code="search"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>