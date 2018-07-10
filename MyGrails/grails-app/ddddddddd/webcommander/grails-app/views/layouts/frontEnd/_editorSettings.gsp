<%@ page import="com.webcommander.Page" %>
<div class="fee-admin-bar">
    <div class="fee-am-logo">
        <img src="${app.systemResourceBaseUrl()}images/site/wc-logo.png">
    </div>
    <div class="fee-am-left">
        <div class="fee-edit-page">
            <form action="${app.baseUrl()}editor" method="get">
                <ui:domainSelect name="id" class="fee-page-list" domain="${Page}" prepend="${['': g.message(code: "select.")]}" value="${page?.id}"/>
            </form>
        </div>
        <div class="fee-page-selector" data-type="page-selector">
            <a href="javascript:void(0)" class="fee-am-btn fee-add-item add-new-page">+ Add Page</a>
        </div>
    </div>
    <div class="fee-am-right" data-screen="full">
        <div class="fee-am-btn-group">
            <button class="fee-am-btn fee-no-gap"><span class="fee-icon fee-icon-no-gap"></span></button>
            <button class="fee-am-btn fee-gap fee-active"><span class="fee-icon fee-icon-gap"></span></button>
        </div>
        <button class="fee-am-btn fee-preview">Preview</button>
    </div>
</div>