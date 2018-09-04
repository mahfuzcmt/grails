package com.webcommander.codecs

import org.codehaus.groovy.runtime.NullObject


/**
 * Created by zobair on 20/06/13.
 */
class BMHTMLCodec {
    public static CharSequence encode(Object target) {
        if(target instanceof NullObject || target == null) {
            return "";
        }
        CharSequence encoded = target.encodeAsHTML();
        return encoded.replace("\n", "<br>").replace("  ", " &nbsp;");
    }

    public static String decode(Object target) {
       return target.decodeHTML();
    }
}