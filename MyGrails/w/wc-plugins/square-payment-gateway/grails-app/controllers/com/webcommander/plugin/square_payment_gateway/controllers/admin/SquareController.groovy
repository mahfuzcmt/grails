package com.webcommander.plugin.square_payment_gateway.controllers.admin

import com.webcommander.constants.DomainConstants
import com.webcommander.plugin.square_payment_gateway.SquareService
import com.webcommander.util.AppUtil

class SquareController {

    SquareService squareService

    def oAuthCallback(){
        render squareService.processOAuthCallback(params)
    }

}
