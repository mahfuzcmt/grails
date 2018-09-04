<div class="review-show-panel">

    <input type="hidden" name="productId" value="${params.productId}"/>
    <g:if test="${count > 0}">
        <div class="main-container">
            <div class="total-review-panel">
                <h2 class="review-heading"><g:message code="review.rating" /></h2>
                <div class="avg-review-label">
                    <span class="label"><g:message code="average.rating"/>: </span><span class="review-rating read-only" score="${avgRating}"></span>
                </div>
                <div class="total"><span class="total"><g:message code="total.review.rating"/>: ${count}</span></div>
                <g:if test="${totalComment > 0}">
                    <g:set var="start" value="${params.long("offset") + 1}"/>
                    <g:set var="end" value="${(params.long("max") + (params.long("offset"))) < totalComment ? (params.long("max") + (params.long("offset"))) : totalComment }"/>
                    <span class="visible-review-label"><g:message code="showing.review.out.of" args="${[start, end, totalComment]}"/> </span>
                </g:if>
            </div>
            <g:if test="${totalComment > 0}">
                <div class="review-container">
                    <g:each in="${reviews}" var="review" status="i">
                        <div class="single-review-block${ i == 0 ? " first" : ""}${i == reviews.size()-1 ? " last" : ""}">
                            <div class="review-head">
                                <g:if test="${review.rating > 0.0 }">
                                    <span class="review-rating read-only" score="${review.rating}"></span>
                                </g:if>
                                <span class="review-date">${review.created.toSiteFormat(true, false, session.timezone)}</span>
                            </div>
                            <div class="reviewer-name">
                                <g:message code="reviewer.name"/>: ${(review.customer ? review.customer.firstName + " " + review.customer.lastName : review.name).encodeAsBMHTML()}
                            </div>
                            <div class="review-description">
                                ${review.review.encodeAsBMHTML()}
                            </div>
                        </div>
                    </g:each>
                </div>
            </g:if>
        </div>

        <g:if test="${params.max.toInteger() < totalComment}">
            <div class="page-initiator-container">
                <paginator total="${totalComment}" offset="${params.offset}" max="${params.max}"></paginator>
            </div>
        </g:if>

    </g:if>
    <g:else>
        <div class="main-container">
            <span class="no-review-message"><g:message code="no.review.message"/></span>
        </div>
    </g:else>

</div>