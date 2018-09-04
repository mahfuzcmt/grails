package com.webcommander.plugin.myob

import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject


class MyobTagLib {
    MyobService myobService

    static namespace = "myob"

    def companyFileSelector = {Map attrs, body ->
        String name = attrs.name;
        List<JSONObject> companyFiles = myobService.companyFiles;
        String classNames = attrs.class;
        String value = attrs.value;
        out << g.select(name: name, class: classNames, from: companyFiles, optionKey: "Uri", optionValue: "Name", value: value)
    }

    def taxSelector = {Map attrs, body ->
        String name = attrs.name;
        String classNames = attrs.class;
        JSONObject noSelection = new JSONObject();
        noSelection.UID = "";
        noSelection.Code = g.message(code: "none");
        List<JSONObject> taxes = new JSONArray();
        taxes.add(noSelection);
        if(!request.myobTaxes){
            request.myobTaxes = myobService.taxes
        }
        taxes.addAll(request.myobTaxes)
        String value = attrs.value;
        out << g.select(name: name, class: classNames, from: taxes, optionKey: "UID", optionValue: "Code", value: value)

    }

}
