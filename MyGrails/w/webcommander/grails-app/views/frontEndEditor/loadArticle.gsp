<div class="fee-widget-config-panel article">
    <g:form controller="frontEndEditor" action="saveWidget" class="config-form" onsubmit="return false;">
        <input type="hidden" name="widgetType" value="${widget.widgetType}">
        <input type="hidden" name="selectedArticleId" value="${params.selectedArticleId}"/>
        <input type="hidden" class="selectFormOnly" value="${selectFormOnly ? 1 : 0}"/>
        <input type="hidden" class="singleSelect" value="${singleSelect ? 1 : 0}"/>
        <input type="hidden" class="hideCreateSection" value="${hideCreateSection ? 1 : 0}"/>
        <input type="hidden" class="articleSelectedRows" name="articles" value="${defaultArticle ? defaultArticle.join(",") : ''}"/>
        <g:include view="frontEndEditor/loadInnerArticle.gsp" params="${[
                offset    : params.offset, max: params.max, articleTitle: params.articleTitle,
                newContent: params.newContent, articleName: params.articleName,
                section   : params.section, sectionFilter: params.sectionFilter,
                sortBy    : params.sortBy,
        ]}"/>
        <div class="fee-button-wrapper fee-pu-content-footer">
            <button type="button" class="fee-back-button fee-pu-button" style="display: none;">Back</button>
            <button class="fee-save fee-pu-button" type="submit"><g:message code="${selectFormOnly ? 'select' : 'save'}"/></button>
            <button class="fee-cancel fee-pu-button" type="button"><g:message code="cancel"/></button>
        </div>
    </g:form>
</div>