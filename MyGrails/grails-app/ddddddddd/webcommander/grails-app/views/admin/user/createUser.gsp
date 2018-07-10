<form class="create-edit-form" method="post" action="${app.relativeBaseUrl()}user/save">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="operator.info"/></h3>
            <div class="info-content"><g:message code="section.text.create.operator"/></div>
        </div>
        <div class="form-section-container">
            <div class="double-input-row">
                <div class="form-row mandatory">
                    <label><g:message code="full.name"/><span class="suggestion">e.g. Peter Smith</span></label>
                    <input type="text" class="medium"  name="fullName" maxlength="200" validation="required rangelength[2, 200]">
                </div><div class="form-row mandatory">
                    <label><g:message code="email"/><span class="suggestion">e.g. speter@abc.com</span></label>
                    <input type="text" class="medium unique" name="email" validation="required email">
                </div>
            </div>

            <div class="form-row trash-row" style="display: none;">
                <label><g:message code="what.to.do"/></label>
                <span><a onclick="return false" class="trash-duplicate-restore fake-link"><g:message code="restore"/></a> <g:message code="restore.and.close.window"/> <g:message code="or"/></span>
                <span><input type="checkbox" name="deleteTrashItem.email" class="trash-duplicate-delete multiple"> <g:message code="delete.and.save"/></span>
            </div>
            <div class="double-input-row">
                <div class="form-row chosen-wrapper">
                    <label><g:message code="administrative.status"/><span class="suggestion">e.g. Active</span></label>
                    <g:select name="active" from="${['active', 'inactive'].collect {g.message(code: it)}}" keys="${['true', 'false']}" class="medium" />
                </div><div class="form-row">
                    <label><g:message code="api.access.only"/></label>
                    <input type="checkbox" class="single" name="isAPIAccessOnly" uncheck-value="false">
                </div>
            </div>

            <div class="form-row">
                <button type="submit" class="submit-button"><g:message code="save"/></button>
                <button type="button" class="cancel-button"><g:message code="cancel"/></button>
            </div>
        </div>
    </div>
</form>