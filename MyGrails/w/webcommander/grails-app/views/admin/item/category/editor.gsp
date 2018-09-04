<div class="bmui-tab left-side-header create-edit-panel">
    <div class="bmui-tab-header-container">
        <div class="bmui-tab-header" data-tabify-tab-id="basic" data-tabify-url="${app.relativeBaseUrl()}categoryAdmin/loadCategoryProperties?id=${categoryId}&property=basic">
            <span class="title"><g:message code="basic"/></span>
        </div>
        <div class="bmui-tab-header" data-tabify-tab-id="metatags" data-tabify-url="${app.relativeBaseUrl()}categoryAdmin/loadCategoryProperties?id=${categoryId}&property=metatags">
            <span class="title"><g:message code="metatag"/></span>
        </div>
        <div class="bmui-tab-header" data-tabify-tab-id="link" data-tabify-url="${app.relativeBaseUrl()}categoryAdmin/loadCategoryProperties?id=${categoryId}&property=products">
            <span class="title"><g:message code="products"/></span>
        </div>
        <div class="bmui-tab-header" data-tabify-tab-id="productSettings" data-tabify-url="${app.relativeBaseUrl()}categoryAdmin/loadCategoryProperties?id=${categoryId}&property=productSettings">
            <span class="title"><g:message code="product.settings"/></span>
        </div>
        <div class="bmui-tab-header" data-tabify-tab-id="advanced" data-tabify-url="${app.relativeBaseUrl()}categoryAdmin/loadCategoryProperties?id=${categoryId}&property=advanced">
            <span class="title"><g:message code="advanced"/></span>
        </div>
        <plugin:hookTag hookPoint="categoryEditorTabHeader"/>
    </div><div class="bmui-tab-body-container">
        <div id="bmui-tab-basic">
        </div>
        <div id="bmui-tab-metatags">
        </div>
        <div id="bmui-tab-link">
        </div>
        <div id="bmui-tab-productSettings">
        </div>
        <div id="bmui-tab-advanced">
        </div>
        <plugin:hookTag hookPoint="categoryEditorTabBody"/>
    </div>
</div>