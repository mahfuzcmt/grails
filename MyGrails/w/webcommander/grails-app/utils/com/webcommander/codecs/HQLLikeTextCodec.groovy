package com.webcommander.codecs

/**
 * Created with IntelliJ IDEA.
 * Operator: sabah
 * Date: 18/11/13
 * Time: 3:29 PM
 * To change this template use File | Settings | File Templates.
 */
class HQLLikeTextCodec {
    public static CharSequence encode(Object target) {
        return target.replace("_", "\\_").replace("%", "\\%").replace("'", "''");
    }
}
