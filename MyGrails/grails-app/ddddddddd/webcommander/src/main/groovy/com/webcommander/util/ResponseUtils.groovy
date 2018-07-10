package com.webcommander.util

import com.webcommander.constants.DomainConstants
import com.webcommander.constants.ResponseCodes

/**
 * Created by sharif ul islam on 14/03/2018.
 */
class ResponseUtils {

    public static final String STATUS_TYPE_SUCCESS = "success";
    public static final String STATUS_TYPE_ERROR = "error";

    static boolean isSuccess(Map<String, Object> response) {
        if (Integer.valueOf(String.valueOf(response.get(DomainConstants.RESPONSE_CODE))) == (ResponseCodes.SUCCESS_CODE) ) {
            return true;
        }
        return false;
    }

    static boolean isError(Map<String, Object> response) {
        if (Integer.valueOf(String.valueOf(response.get(DomainConstants.RESPONSE_CODE))) != (ResponseCodes.SUCCESS_CODE) ) {
            return true;
        }
        return false;
    }

    static String getResponseMessage(Map<String, Object> response) {
        return (String) response.get(DomainConstants.RESPONSE_MESSAGE);
    }

    static int getResponseCode(Map<String, Object> response) {
        if (response.get(DomainConstants.RESPONSE_CODE)) {
            return (Integer) response.get(DomainConstants.RESPONSE_CODE);
        }
        return 0;
    }

    static String getResponseStatus(String responseCode) {
        if (isSuccessResponse(responseCode)) {
            return ResponseUtils.STATUS_TYPE_SUCCESS;
        }
        return ResponseUtils.STATUS_TYPE_ERROR;
    }

    static boolean isSuccessResponse(String responseCode) {
        if (responseCode && responseCode.equals("S200")) {
            return true;
        }
        return false;
    }

}
