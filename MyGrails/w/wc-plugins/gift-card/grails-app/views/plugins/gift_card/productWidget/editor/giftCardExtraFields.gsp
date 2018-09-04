<g:applyLayout name="_productwidget">
    <div class="gift-card-fields">
        <div class="form-header">
            <span class="title"><g:message code="how.would.you.like.to.recieve.your.gift.card"/></span>
            <div class="form-row">
                <input type="radio" name="sendingType" value="email" toggle-target="sending-type-by-email">
                <label><g:message code="by.email"/> </label>
            </div>
            <div class="form-row">
                <input type="radio" name="sendingType" value="post" toggle-target="sending-type-by-post">
                <label><g:message code="by.post"/> </label>
            </div>
        </div>
        <div class="form-content">
            <div class="double-input-row">
                <div class="form-row">
                    <label><g:message code="recipients.first.name"/><span class="suggestion"><g:message code="recipients.first.name.suggestion"/></span></label>
                    <input type="text" class="medium" name="firstName" value="" validation="skip@if{self::hidden} required rangelength[1,100]" maxlength="100">
                </div><div class="form-row">
                <label><g:message code="recipients.last.name"/><span class="suggestion"><g:message code="recipients.first.name.suggestion"/></span></label>
                <input type="text" class="medium" name="lastName" value="">
            </div>
            </div>
            <div class="double-input-row">
                <div class="form-row mandatory">
                    <label><g:message code="recipients.email.address"/><span class="suggestion"><g:message code="recipients.email.address.suggestion"/></span></label>
                    <input type="text" class="medium" name="email" value="" validation="skip@if{self::hidden} required email">
                </div><div class="form-row">
                <label><g:message code="sender.name"/><span class="suggestion"><g:message code="sender.name.suggestion"/></span></label>
                <input type="text" class="medium" name="sender" value="">
            </div>
            </div>
            <div class="sending-type-by-post">
                <div class="double-input-row">
                    <div class="form-row">
                        <label><g:message code="phone"/>:<span class="suggestion"><g:message code="phone.suggestion"/></span></label>
                        <input type="text" class="medium" name="email" value="">
                    </div><div class="form-row">
                    <label><g:message code="mobile"/>:<span class="suggestion"><g:message code="mobile.suggestion"/></span></label>
                    <input type="text" class="medium" name="sender" value="">
                </div>
                </div>
                <div class="form-row mandatory">
                    <label><g:message code="recipients.address.line"/><span class="suggestion"><g:message code="recipients.address.line.suggestion"/></span></label>
                    <input type="text" class="medium" name="message" value="" validation="skip@if{self::hidden} required">
                </div>
                <div class="form-row mandatory">
                    <label><g:message code="city"/><span class="suggestion"><g:message code=".city.suggestion"/></span></label>
                    <input type="text" class="medium" name="message" value="" validation="skip@if{self::hidden} required">
                </div>
                <div class="form-row mandatory">
                    <label><g:message code="post.code"/><span class="suggestion"><g:message code="post.code.suggestion"/></span></label>
                    <input type="text" class="medium" name="message" value="" validation="skip@if{self::hidden} required">
                </div>
                <div class="form-row mandatory">
                    <label><g:message code="state"/><span class="suggestion"><g:message code="state.suggestion"/></span></label>
                    <input type="text" class="medium" name="message" value="" validation="skip@if{self::hidden} required">
                </div>
                <div class="form-row mandatory">
                    <label><g:message code="country"/><span class="suggestion"><g:message code="country.suggestion"/></span></label>
                    <input type="text" class="medium" name="message" value="" validation="skip@if{self::hidden} required">
                </div>
            </div>
            <div class="form-row mandatory">
                <label><g:message code="message"/><span class="suggestion"><g:message code="message"/></span></label>
                <input type="text" class="medium" name="message" value="">
            </div>
        </div>
    </div>
</g:applyLayout>