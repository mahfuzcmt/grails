package com.webcommander.plugin.discount.processor

/**
 * Created by sharif ul islam on 28/03/2018.
 */
abstract class Processor {

    abstract Object doProcess(Map context)

    Object process(Map context) {
        return doProcess(context)
    }

}