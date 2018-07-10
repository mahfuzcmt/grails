<div class="left-scroller scroll-navigator"></div>

    <div class="one-line-scroll-content item-wrapper content-overflow">
        <g:each in="${venues}" var="venue">
            <div class="item-thumb blocklist-item ${venue.id == selected ? "selected" : "" }" item-id="${venue.id}" item-name="${venue.name}">
                <span class="float-menu-navigator"></span>
                <span class="icon shipping-profile"></span>
                <span class="item-title listitem-title">${venue.name.encodeAsBMHTML()}</span>
            </div>
        </g:each>
    </div>

<div class="right-scroller scroll-navigator"></div>