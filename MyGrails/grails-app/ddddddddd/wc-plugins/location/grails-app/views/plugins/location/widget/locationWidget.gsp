<g:applyLayout name="_widget">
    <app:enqueueSiteJs src="${"//maps.googleapis.com/maps/api/js?key=${config.api_key}" + "&v=3&libraries=places"}" scriptId="google-map-api"/>
    <app:enqueueSiteJs src="${"plugins/location/js/site-js/location-map.js"}" scriptId="location-map"/>

    <div class="location-container" location-pin="${config.pin_url}">
        <div class="loader"></div>
    </div>

    <div class="autocomplete-suggestions chosen-results" style="height: 150px; width: 240px">

        <div class="button-container">
            <h5>FIND A STORE NEAR YOU</h5>
            <ul>
              <li><a class="countryChoice activeCountry">AUSTRALIA</a></li>
              <li><a class="countryChoice">INTERNATIONAL</a></li>
            </ul>
        </div>
        </br>

        <text>Enter your suburb or postcode</text> </br>
        <input id="tags" autocomplete="off">
        <button id="findLocation">Find</button>
    </div>

</g:applyLayout>