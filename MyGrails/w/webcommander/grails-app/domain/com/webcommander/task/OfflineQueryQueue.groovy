package com.webcommander.task

class OfflineQueryQueue {

    String methodName

    String arg0
    String arg1
    String arg2
    String arg3

    static constraints = {
        methodName(blank: false)
        arg1(nullable: true)
        arg2(nullable: true)
        arg3(nullable: true)
    }
}