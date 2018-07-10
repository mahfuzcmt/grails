package com.webcommander.plugin.discount.resolver

/**
 * Created by sharif ul islam on 12/03/2018.
 */
abstract class Resolver {

    abstract Object doResolve(Map context)

    Object resolve(Map context) {
        return doResolve(context)
    }

}