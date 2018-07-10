<div class="wc-site-popup" id="site-popup-body-${popup.identifier}" style="display: none">
    <div class="header">
        <span class="close-popup close-icon"></span>
        <span class="popup-title">${popup.name.encodeAsBMHTML()}</span>
    </div>
    <div class="content">
        <plugin:hookTag hookPoint="sitePopup">${popup.content}</plugin:hookTag>
    </div>
</div>