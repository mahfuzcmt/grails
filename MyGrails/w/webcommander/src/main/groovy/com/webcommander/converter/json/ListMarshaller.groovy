package com.webcommander.converter.json

/**
 * Created by sajedur on 18-02-2015.
 */
class ListMarshaller {

    public static void writeJSONString(List list, Writer out, Map config = [:]) {
        if(list == null){
            out.write("null");
            return;
        }

        boolean first = true;
        out.write('[');
        list.each { value ->
            if(first) {
                first = false;
            } else {
                out.write(',');
            }
            JSON.writeJSONString(value, out, config);
        }
        out.write(']');
    }
}
