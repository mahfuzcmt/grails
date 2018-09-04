package com.webcommander.plugin.section_slider.controllers.admin.design


/**
 * Created by zobair on 27/10/2014.
 */
class WidgetController {
    def sectionSliderShortConfig() {
        render(view: "/plugins/section_slider/shortConfig", model: [noAdvance: true])
    }
}
