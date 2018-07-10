<div class="dashlet-web-content-design">
    <g:each in="${dashletItems}" var="item"><div class="dashlet-ribbon ${item.uiClass}" tabId="${item.contentId}" ui_class="${item.uiClass}">
            <span class="icon"></span>
            <span class="title"><g:message code="${item.title}"/></span>
        </div></g:each>
</div>
