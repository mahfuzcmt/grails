<form class="review-setting-form" class="create-edit-form" action="${app.relativeBaseUrl()}setting/saveConfigurations" method="post" id="reviewSettingForm">
    <input type="hidden" name="type" value="review_rating">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="review.rating"/></h3>
            <div class="info-content"><g:message code="section.text.setting.review.rating"/></div>
        </div>
        <div class="form-section-container">
            <div class="double-input-row">
                <div class="form-row">
                    <label>&nbsp;</label>
                    <input type="checkbox" class="single" toggle-target="display-on" name="review_rating.product_review" value="on" uncheck-value="off" ${config.product_review == 'on' ? "checked='checked'" : ""}>
                    <span><g:message code="review"/></span>
                </div><div class="form-row thicker-row display-on chosen-wrapper">
                    <label><g:message code="who.can.review"/><span class="suggestion"><g:message code="suggestion.setting.product.review.who.can.review"/></span></label>
                    <g:select class="medium " name="review_rating.who_can_review" from="${[every_one: g.message(code: "every.one") , registered: g.message(code: "registered.customer")]}" value="${config.who_can_review}" optionKey="key" optionValue="value"/>
                </div>
            </div>
            <div class="double-input-row">
                <div class="form-row thicker-row display-on chosen-wrapper">
                    <label><g:message code="show.review"/></label>
                    <g:select class="medium " name="review_rating.show_review" from="${[wait: g.message(code: "wait.for.approval"), immediately: g.message(code: "immediately")]}" value="${config.show_review}" optionKey="key" optionValue="value"/>
                </div><div class="form-row thicker-row display-on chosen-wrapper">
                    <label><g:message code="review.per.page"/><span class="suggestion"><g:message code="suggestion.setting.product.review.review.per.page"/></span></label>
                    <g:select class="medium " name="review_rating.review_per_page" from="${[5, 10, 20]}" value="${config.review_per_page}"/>
                </div>
            </div>
            <div class="form-row ">
                <button type="submit" class="submit-button review-setting-form-submit"><g:message code="update"/></button>
            </div>
        </div>
    </div>
</form>