package utils

class AppUtil {


    static infoMessage(String message, boolean status = true) {
        return [info: message, success: status]
    }

}
