package com.webcommander.plugin.event_management.controllers.admin

import com.webcommander.common.CommonService
import com.webcommander.plugin.event_management.VenueLocation
import com.webcommander.plugin.event_management.webmarketing.VenueService

class VenueAdminController {

    CommonService commonService
    VenueService venueService

    def loadAppView() {

    }

    def loadVenueLocationSelectionPanel() {
        params.max = params.max ?: 10
        Integer count = venueService.getVenueLocationsCount(params)
        List<VenueLocation> selectedLocations = (List<VenueLocation>)commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.offset = offset
            venueService.getVenueLocations(params)
        }
        render(view: "/plugins/event_management/admin/widget/eventSelectionPanel", model: [selectedLocations: selectedLocations, count: count])
    }

    def loadVenueLocationsForSelection() {
        params.max = params.max ?: 10
        params.hasEvent = true
        params.hasEventSession = true
        Integer count = venueService.getVenueLocationsCount(params)
        List<VenueLocation> venueLocations = (List<VenueLocation>)commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.offset = offset;
            venueService.getVenueLocations(params);
        }
        render(view: "/plugins/event_management/admin/widget/venueLocationSelectionList", model: [locations: venueLocations, count: count])
    }
}
