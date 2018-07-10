package com.webcommander.plugin.star_track.controllers.admin

import com.webcommander.constants.DomainConstants
import com.webcommander.models.AddressData
import com.webcommander.plugin.star_track.calculator.StarTrackCalculator
import com.webcommander.util.AppUtil
import grails.converters.JSON

class StarTrackController {
    def loadConfig() {
        Map config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.STAR_TRACK);
        render(view: "/plugins/star_track/admin/appConfig", model: [config: config])
    }


    def test() {
        def x = new AddressData(city: "MOUNT ISA", postCode: 4825, stateCode: "QLD")
        def y = new AddressData(city: "broome", postCode: 6725, stateCode: "WA")
        Map config = [
                mode: "test",
                source: "TEAM",
                account_no: "11112222",
                user_access_key: "30405060708090",
                user_name: "TAY00002",
                password: "Tay12345",
        ]
        Double result = null
        try {
            result = StarTrackCalculator.calculateCost(config, x, y, "EXP", 1, 10, 0.01)
        } catch (Exception ex) {
        }
        render text: "${result}"
    }
}
