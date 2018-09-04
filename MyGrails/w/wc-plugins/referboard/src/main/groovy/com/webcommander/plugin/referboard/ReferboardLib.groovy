package com.webcommander.plugin.referboard

import com.webcommander.constants.DomainConstants
import com.webcommander.util.AppUtil
import com.webcommander.util.HttpUtil
import grails.converters.JSON

import javax.servlet.http.Cookie

class ReferboardLib {
    static CALLBACK_URL = 'https://www.referboard.com/referSales/purchase';
    static CHECK_RETAILER_URL = "https://www.referboard.com/webservice/";

    static Map captureReferboardParams(Map data, type = 'data') {
        Map result = [
            rf_product: data['rf_product'] ?: '',
            rf_buyer_ip: data['rf_buyer_ip'] ?: '',
            rf_rproduct: data['rf_rproduct'] ?: '',
            rf_user: data['rf_user'] ?: '',
            buyer_history: data['buyer_history'] ?: 0
        ]
        if(type == 'cookie') {
            Cookie cookie = AppUtil.request.getCookies().find { it.name.startsWith("rb_order_info") };
            if(cookie) {
                Map cookie_data = JSON.parse(cookie.value)
                result['rf_product'] = result['rf_product'] ?: (cookie_data['rf_product'] ?:  '')
                result['rf_buyer_ip'] = result['rf_buyer_ip'] ?: (cookie_data['rf_buyer_ip'] ?:  '')
                result['rf_rproduct'] = result['rf_rproduct'] ?: (cookie_data['rf_rproduct'] ?:  '')
                result['rf_user'] = result['rf_user'] ?: (cookie_data['rf_user'] ?:  '')
                result['buyer_history'] = result['buyer_history'] ?: (cookie_data['buyer_history'] ?:  0)
            }

        }
        if(result['rf_product']) {
            AppUtil.session.referboard = result
        }
        return result
    }

    static def checkTransactionData(Map $data){
        Map error = [:];
        /**
         * optional
         */
        if(!$data['cid']){
            $data['cid'] = '';
        }

        if(!$data['extra']){
            $data['extra'] = '';
        }

        /**
         * mandatory
         */
        if(!$data['id']){
            error['id'] = 'ref_product/id missed';
        }

        if(!$data['email']){
            error['email'] = 'customer email missed';
        }

        if(!$data['amount']){
            error['amount'] = 'missed';
        }

        if(!$data['buyer_ip']){
            error['buyer_ip'] = 'missed';
        }

        if(!$data['buyer_history']){
            error['buyer_history'] = 'missed';
        }

        if(!$data['currency']){
            error['currency'] = 'missed';
        }

        if(!$data['tid']){
            error['tid'] = 'transaction id missed';
        }


        /**
         * api key
         */
        if(!$data['rkey']){
            Map configs = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.REFERBOARD)
            if(!configs.api_key){
                error['rkey'] = 'api key missed';
            }else{
                $data['rkey'] = configs.api_key
            }
        }

        if(error.isEmpty()){
            return true
        }
        return error;
    }

    static void sendTransactionData(Map data){
        Boolean checkResult = checkTransactionData(data);
        if(checkResult){
            HttpUtil.doPostRequest(CALLBACK_URL, HttpUtil.serializeMap(data))
        }
    }
}
