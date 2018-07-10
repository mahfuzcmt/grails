package com.webcommander.plugin.popup.controllers.site

import com.webcommander.plugin.popup.Popup

class PopupController {

    def popupContent() {
        Popup popup = Popup.findByIdentifier(params.identifier)
        if(popup == null) {
            render text: ""
            return;
        }
        render(view: "/plugins/popup/site/_popup", model: [popup: popup])
    }
}