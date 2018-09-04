package com.webcommander.converter.json

import grails.util.Holders
import grails.core.GrailsApplication

import javax.xml.crypto.Data
import java.text.SimpleDateFormat

class JSON {
    final static SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    private StringWriter writer
    private Map config = [:];

    public static void writeJSONString(Object value, Writer out, Map config = [:]) {
        if(value == null){
            out.write("null");
            return;
        } else if(value instanceof String){
            out.write('\"');
            out.write(value.encodeAsJavaScript());
            out.write('\"');
            return;
        } else if(value in GString) {
            out.write('\"');
            out.write(value.toString().encodeAsJavaScript());
            out.write('\"');
            return;
        } else if(value instanceof Double){
            if(((Double)value).isInfinite() || ((Double)value).isNaN())
                out.write("null");
            else
                out.write(value.toString());
            return;
        } else if(value instanceof Float){
            if(((Float)value).isInfinite() || ((Float)value).isNaN())
                out.write("null");
            else
                out.write(value.toString());
            return;
        } else if(value instanceof Number){
            out.write(value.toString());
            return;
        } else if(value instanceof Boolean){
            out.write(value.toString());
            return;
        } else if(value instanceof Map){
            MapMarshaller.writeJSONString((Map)value, out, config);
            return;
        } else if(value instanceof List){
            ListMarshaller.writeJSONString((List)value, out, config);
            return;
        } else if(value instanceof Date) {
            out.write("\"");
            out.write(dateFormatter.format(value));
            out.write("\"");
            return
        }
        GrailsApplication grailsApp = Holders.grailsApplication;
        if(grailsApp.isDomainClass(value.class)) {
            DomainMarshaller.writeJSONString(value, out, config)
            return;
        }
        out.write(value.toString());
    }

    public String escape(String str) {
        return str
    }

    public JSON(Object ob) {
        this.writer = new StringWriter();
        writeJSONString(ob, writer)
    }

    public JSON(Object ob, config) {
        this.config = config
        this.writer = new StringWriter();
        writeJSONString(ob, writer, config)
    }

    public String toString() {
        writer.toString();
    }
}
