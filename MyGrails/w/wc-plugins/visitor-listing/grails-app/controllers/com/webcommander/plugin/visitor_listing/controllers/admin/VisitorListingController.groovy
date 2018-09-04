package com.webcommander.plugin.visitor_listing.controllers.admin

import com.webcommander.authentication.annotations.License
import com.webcommander.plugin.visitor_listing.manager.VisitorListManager

import javax.servlet.http.HttpSession

class VisitorListingController {

    @License(required = "allow_live_visitor_feature")
    def loadAppView() {
        session.lastAccessedTime
        params.max = params.max ?: "10";
        params.offset = params.offset ?: "0"
        int count = VisitorListManager.count();
        List<HttpSession> visitors = VisitorListManager.getsVisitors(params)
        render(view: "/plugins/visitor_listing/admin/appView", model: [visitors: visitors, count: count]);
    }


}
