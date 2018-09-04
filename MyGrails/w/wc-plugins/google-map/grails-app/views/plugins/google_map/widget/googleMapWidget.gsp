<g:applyLayout name="_widget">
    <app:enqueueSiteJs src="${"//maps.googleapis.com/maps/api/js?key=${config.api_key}&v=3&libraries=places"}" scriptId="google-map-api"/>
    <app:enqueueSiteJs src="${"plugins/google-map/js/site-js/google-map.js"}" scriptId="google-map"/>
    <div class="google-map-container" map-lat="${config.latitude}" map-lng="${config.longitude}" map-zoom="${config.zoom}" map-radius="${config.radius}" map-pin="${config.pin_url}">
        <div class="loader"></div>
    </div>
    <textarea class="popup-text" style="display: none">${config.popup_text?.encodeAsBMHTML()}</textarea>
</g:applyLayout>