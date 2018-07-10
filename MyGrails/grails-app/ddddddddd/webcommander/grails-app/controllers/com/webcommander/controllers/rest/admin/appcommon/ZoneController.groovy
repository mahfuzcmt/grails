package com.webcommander.controllers.rest.admin.appcommon

import com.webcommander.admin.Zone
import com.webcommander.admin.ZoneService
import com.webcommander.util.RestProcessor

class ZoneController extends RestProcessor {
    ZoneService zoneService

    def zoneList() {
        List<Zone> zones = zoneService.getZones([max: -1, offset: 0, isDefault: false])
        rest zones: zones
    }
}