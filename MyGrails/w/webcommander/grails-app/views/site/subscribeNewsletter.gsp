<div class="newsletter subscribe valid-verify-form">
    <h3 class="title"><g:message code="newsletter.subscription"/></h3>
    <div class="form-row">
        <label><g:message code="title"/>:</label>
        <g:select name="title" keys="${['mr.', 'mrs.', 'miss.']}" from="${[g.message(code: 'mr.'), g.message(code: 'mrs.'), g.message(code: 'miss.')]}"/>
    </div>
    <div class="form-row mandatory">
        <label><g:message code="first.name"/>:</label>
        <input type="text" class="medium subscription-name" name="firstName" validation="required maxlength[250]" maxlength="250">
    </div>
    <div class="form-row">
        <label><g:message code="last.name.surname"/>:</label>
        <input type="text" class="medium subscription-name" name="lastName" validation="maxlength[250]" maxlength="250">
    </div>
    <div class="form-row mandatory">
        <label><g:message code="email"/>:</label>
        <input type="text" class="medium subscription-email" name="email" validation="required single_email maxlength[250]" maxlength="250">
    </div>
    <div class="form-row button-container">
        <label>&nbsp</label>
        <button class="newsletter-subscription submit-button"><g:message code="subscribe"/></button>
    </div>
</div>
