<div class="header">
    <div class="toolbar toolbar-right">
    </div>
</div>
<div class="body navigation-item-image-editor">
    <g:set var="url" value="navigation/updateItemImage"/>
    <g:each in="${items}" var="item">
        <div class="item form-row preview-image thicker-row same-line-preview">
            <label>${item.label}</label>
            <div class="medium">
                <input type="file" name="item-image" file-type="image" size-limit="20480" ajax-url="${url}" submit-data="${item.id}" submit="auto" previewer="navigation-item-image-${item.id}"
                        ${item.image ? 'remove-support="true"' : ""} reset-support="false">
            </div>
            <img ${item.image ? "" : "style='display: none'"} src="${item.image ? (appResource.getNavigationItemImageURL(navigationItem: item) + "?uuid=" + java.lang.Math.random()) : ''}" id="navigation-item-image-${item.id}">
        </div>
    </g:each>
</div>