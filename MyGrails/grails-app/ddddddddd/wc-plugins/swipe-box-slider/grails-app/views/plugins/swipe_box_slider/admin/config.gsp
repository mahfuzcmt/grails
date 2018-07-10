<div class="gallery-config-view create-edit-form">
    <div class="double-input-row">
        <div class="form-row mandatory">
            <label><g:message code="no.of.column"/></label>
            <input type="text" name="item_per_column" value="${widgetConfig['item_per_column']?: '3'}" validation="digits required min[1]" class="small" restrict="numeric">
        </div><div class="form-row">
            <label><g:message code="title"/></label>
            <input type="checkbox" name="altText" value="true" ${widgetConfig['altText'] == 'true' ? 'checked' : ''} class="single">
        </div>
    </div>
    <div class="double-input-row">
        <div class="form-row chosen-wrapper">
            <label><g:message code="overflow"/></label>
            <g:select name="overflow" from="${[g.message(code: 'pagination'), g.message(code: 'scroll')]}" value="${widgetConfig['overflow']}" keys="${['p', 's']}" class="small"/>
        </div><div class="form-row mandatory">
            <label><g:message code="item.per.page"/></label>
            <input type="text" name="max" value="${widgetConfig['max']?: '10'}" validation="digits required min[2]" class="small" restrict="numeric">
        </div>
    </div>
    <div class="button-line">
        <button type="button" class="previous"><g:message code="previous"/></button>
        <button type="submit" class="edit-popup-form-submit submit-button apply"><g:message code="update"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</div>