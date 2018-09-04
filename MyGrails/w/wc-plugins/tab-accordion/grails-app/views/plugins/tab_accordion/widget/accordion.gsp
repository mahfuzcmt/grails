<div class="tab-accordion-accordion">
    <g:each in="${widget.widgetContent}" var="content" status="i">
        <div class="label-bar">
            <a class="toggle-icon"></a>${content.extraProperties?.encodeAsBMHTML()}
        </div>
        <div class="accordion-item" content-id="${content.contentId}">
            <render:renderPageContent value="${pages[i].body}"/>
        </div>
    </g:each>
</div>