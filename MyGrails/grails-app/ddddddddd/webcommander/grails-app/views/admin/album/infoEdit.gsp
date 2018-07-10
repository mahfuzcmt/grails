<form action="${app.relativeBaseUrl()}album/save" method="post" class="create-edit-form">
    <input type="hidden" name="id" value="${album.id}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="album.info"/></h3>
            <div class="info-content"><g:message code="section.text.album.info"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row mandatory">
                <label><g:message code="album.title"/><span class="suggestion">e.g. Nature</span></label>
                <input type="text" class="medium unique" name="name" validation="required rangelength[2,100]" maxlength="100" value="${album.name.encodeAsBMHTML()}">
            </div>
            <div class="form-row trash-row" style="display: none;">
                <label><g:message code="what.to.do"/></label>
                <span><a onclick="return false" class="trash-duplicate-restore fake-link"><g:message code="restore"/></a> <g:message code="restore.and.close.window"/> <g:message code="or"/></span>
                <span><input type="checkbox" name="deleteTrashItem.name" class="trash-duplicate-delete multiple"> <g:message code="delete.and.save"/></span>
            </div>
            <g:if test="${!album.id}">
                <div class="form-row mandatory">
                    <label><g:message code="thumb.size"/><span class="suggestion">e.g. 320 x 240</span></label>
                    <div class="twice-input-row">
                        <input type="text" name="thumbX" class="smaller" validation="required digits max[950] min[150] maxlength[3]" maxlength="3" restrict="numeric" value="${album.thumbX}"><span>x</span><input type="text" name="thumbY" class="smaller" validation="required digits max[950] min[150] maxlength[3]" maxlength="3" restrict="numeric" value="${album.thumbY}">
                    </div>
                </div>
            </g:if>
            <div class="form-row">
                <label><g:message code="album.description"/><span class="suggestion">e.g. Pictures of the Flowers</span></label>
                <textarea class="medium" validation="maxlength[255]" maxlength="250" name="description">${album.description}</textarea>
            </div>
            <div class="form-row">
                <button type="submit" class="submit-button"><g:message code="${album.id ? "update" : "save"}"/></button>
                <button type="button" class="cancel-button"><g:message code="cancel"/></button>
            </div>
        </div>
    </div>
</form>