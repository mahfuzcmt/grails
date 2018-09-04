package com.webcommander.plugin.discount.filter

/**
 * Created by sharif ul islam on 28/03/2018.
 */
abstract class Filter {

    abstract Object doFilter(Map context)

    Object filter(Map context) {
        return doFilter(context)
    }

}