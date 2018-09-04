<span class="tool-icon tool-tip-close"></span>
<div class="content">
    <span class="title"><g:message code="${title}"/></span>
    <span class="description"><g:message code="${description}"/></span>
</div>
<div class="pop-bottom">
    <span class="order">${count.toInteger().encodeAsBMHTML()} of ${params.wizard ? 8 : 7}</span>
    <button type="button" class="submit-button-next"><g:message code="next"/></button>
    <button type="button" class="submit-button-skip"><g:message code="skip"/></button>
</div>