<div class="item-row location-row ${clazz}" location-id="${location.id}">
    <div class="left-column">
        <input type="hidden" name="location-id" value="${location.id}">
        <div class="wrap">
            <g:each in="${location.sections}" var="section">
                <div class="">${section.name.encodeAsBMHTML()}</div>
            </g:each>
        </div>
        <span class="tool-icon edit edit-section"></span>
    </div>
    <div class="right-column">
        <div class="row">
            <div class="column location-name">${location.name.encodeAsBMHTML()}</div>
            <div class="column venue-name">${location.venue.name.encodeAsBMHTML()}</div>
            <div class="column actions-column">
                <span class="action-navigator collapsed" entity-id="${location?.id}" entity-venueId="${location.venue?.id}" entity-name="${location?.name.encodeAsBMHTML()}"></span>
            </div>
        </div>
    </div>
</div>