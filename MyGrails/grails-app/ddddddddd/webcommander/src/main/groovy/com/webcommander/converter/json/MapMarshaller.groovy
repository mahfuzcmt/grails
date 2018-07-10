package com.webcommander.converter.json

/**
 * Created by sajedur on 18-02-2015.
 */
class MapMarshaller {

    public static void writeJSONString(Map map, Writer out, Map config = [:]) {
        if(map == null){
            out.write("null");
            return;
        }

        boolean first = true;
        out.write('{');
        map.each { key, value ->
            if(first)
                first = false;
            else
                out.write(',');
            out.write('\"');
            out.write(key);
            out.write('\"');
            out.write(':');
            JSON.writeJSONString(value, out, config);
        }
        out.write('}');
    }
}
