<%@ page import="com.webcommander.webcommerce.Product; com.webcommander.util.AppUtil; com.webcommander.constants.DomainConstants" %>
<div class="review-panel">
    <div class="message-container"></div>
    <div class="review-view-panel">
        <g:include controller="productReview" action="loadReview" model="${[params: params]}"/>
    </div>
    <div class="write-review-panel">
        <g:set var="who" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.REVIEW_RATING, "who_can_review")}"/>
        <g:if test="${who == "every_one" || session.customer}">
            <div>
                <button class="write-review"><g:message code="write.review"/> </button>
            </div>
            <div class="review-form-container" style="display: none">
                <form  method="post" class="review-form" action="${app.relativeBaseUrl()}productReview/saveReview">
                    <input type="hidden" name="productId" value="${params.productId}"/>
                    <g:if test="${!session.customer}">
                        <div class="form-row mandatory">
                            <label><g:message code="name"/>:</label>
                            <input name="name" type="text" validation="required rangelength[3,50]"/>
                        </div>
                        <div class="form-row mandatory">
                            <label><g:message code="email"/>:</label>
                            <input type="text" name="email" validation="required email" />
                        </div>
                    </g:if>

                    <div class="form-row">
                        <label><g:message code="write.your.review" />:</label>
                        <textarea name="review" validation="maxlength[5000]" maxlength="5000"></textarea>
                    </div>
                    <div class="review-row form-row">
                        <label></label>
                        <div class="rating"></div>
                    </div>
                    <g:set var="captchaConfig" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL)}"/>
                    <g:if test="${captchaConfig.captcha_setting == 'enable'}">
                        <ui:captcha/>
                    </g:if>

                    <div class="form-row btn-row">
                        <button type="submit"><g:message code="submit" /></button>
                        <button type="button" class="cancel"><g:message code="cancel" /></button>
                    </div>
                </form>
            </div>
        </g:if>
        <g:else>
            <div class="message-container">
                <g:set var="url" value="/product/${Product.get(params.productId).url}#review"/>
                <g:message code="please"/><a class="login" href="${app.relativeBaseUrl()}customer/login?referer=${java.net.URLEncoder.encode(url)}"> login </a> <g:message code="to.add.review"/>
            </div>
        </g:else>
    </div>
</div>
