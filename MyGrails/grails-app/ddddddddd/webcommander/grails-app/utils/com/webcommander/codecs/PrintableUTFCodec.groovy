package com.webcommander.codecs


import org.codehaus.groovy.runtime.NullObject
/**
 * Created by sadlil
 */
class PrintableUTFCodec {
    public static CharSequence encode(Object target) {
        if(target instanceof NullObject || target == null) {
            return "";
        }

        return target.replaceAll("[\\p{Cc}\\p{Cf}\\p{Co}\\p{Cn}]", "");
    }
}
