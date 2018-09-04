<form action="${app.relativeBaseUrl()}siteMap/create" method="post" class="edit-popup-form two-column">
    <div class="form-row">
        <input type="checkbox" value="page" name="page" class="multiple">
        <label><g:message code="page"/></label>
    </div>
    <div class="form-row">
        <input type="checkbox" value="category" name="category" class="multiple">
        <label><g:message code="category"/></label>
    </div>
    <div class="form-row">
        <input type="checkbox" value="brand" name="brand" class="multiple">
        <label><g:message code="brand"/></label>
    </div>
    <div class="form-row">
        <input type="checkbox" value="product" name="product" class="multiple">
        <label><g:message code="product"/></label>
    </div>
    <div class="form-row">
        <input type="checkbox" value="manufacturer" name="manufacturer" class="multiple">
        <label><g:message code="manufacturer"/></label>
    </div>
    <div class="button-line">
        <button type="submit" class="submit-button"><g:message code="save"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>

</form>
