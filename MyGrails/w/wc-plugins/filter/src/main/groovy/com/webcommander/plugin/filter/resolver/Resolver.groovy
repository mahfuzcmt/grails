package com.webcommander.plugin.filter.resolver

/**
 * Created by sharif ul islam on 25/02/2018.
 */
abstract class Resolver {

    abstract Object doResolve(Map context)

    Object resolve(Map context) {
        return doResolve(context)
    }

}