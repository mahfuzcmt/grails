package com.webcommander

import com.webcommander.admin.Country
import com.webcommander.common.CommonService
import com.webcommander.constants.DomainConstants
import com.webcommander.design.TemplateService
import com.webcommander.util.AppUtil
import com.webcommander.util.StringUtil
import com.webcommander.util.ValueOrderedTreeMap
import grails.converters.JSON

class UITagLib {
    static namespace = "ui"

    CommonService commonService
    TemplateService templateService

    def perPageCountSelector = { attrs, body ->
        def defaults = ["10": "10", "20": "20", "50": "50", "100": "100", "-1": "All"];
        if(attrs.prepand instanceof Map) {
            defaults = attrs.prepand << defaults
        }
        out << g.select(from: defaults.values(), noSelection: attrs["value"] ? (!(attrs["value"].toString() in defaults.keySet()) ? [(attrs["value"]): attrs["value"]] : null) : null, keys: defaults.keySet(), name: "${attrs["name"] ?: 'per-page-count'}", value: attrs["value"], class: "per-page-count ${attrs["class"] ?: ''} ");
    }


    def getSeoConfig = { attrs, body ->
        out << AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.SEO_CONFIG, attrs.configKey)
    }

    def countryList = { attrs, body ->
        def allCountryList = Country.createCriteria().list {
            eq("isActive", true)
        }
        if (!attrs.id) {
            attrs.id = StringUtil.uuid
        }
        Map paramMap = [from: allCountryList, optionKey: attrs.optionKey ?: 'id', optionValue: 'name'];
        paramMap.putAll(attrs);
        out << g.select(paramMap);
    }

    def captcha = { attrs, body ->
        if (AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "captcha_setting") == "enable") {
            def captchaType = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "captcha_type")
            if (captchaType == "simple_captcha") {
                out << g.include(view: "/site/captcha/simpleCaptcha.gsp")
            } else {
                String publicKey = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "captcha_public_key")
                String privateKey = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "captcha_private_key")
                if (publicKey && privateKey) {
                    out << g.include(view: "/site/captcha/reCaptcha.gsp", model: [publicKey: publicKey])
                    if (!request.recaptcha_js_loaded) {
                        app.enqueueSiteJs(src: "https://www.google.com/recaptcha/api.js?render=explicit", scriptId: "recaptcha_js_loaded")
                        request.recaptcha_js_loaded = true;
                    }
                }
            }
        }
    }

    def hierarchicalSelect = { attrs, body ->
        Class domain = attrs.domain;
        if (!domain) {
            return;
        }
        Closure getNewMap = {
            return new ValueOrderedTreeMap({ a, b ->
                a.text <=> b.text
            } as Comparator)
        }
        String key = attrs["key"] ?: "id";
        String text = attrs["text"] ?: "name";
        Map prependMap = attrs['prepend'] ?: null;
        Map customAttrs = attrs['custom-attrs'] ?: null;
        Object value = attrs['value'] ?: '';
        def values = attrs['values'] ?: null;
        boolean sorted = attrs["sorted"].toBoolean(true);
        boolean selectGroupRoot = attrs["select-group-root"].toBoolean(true);
        boolean groupAsLabel = attrs["group-as-label"].toBoolean(false);
        Map allEntry = [:]
        Map orderedForTree = sorted ? getNewMap() : [:];
        Closure filter = attrs.filter
        if (domain.declaredFields.find { it.name == "isInTrash" } && domain.declaredFields.find { it.name == "isParentInTrash" }) {
            if (filter) {
                filter = filter << {
                    eq("isInTrash", false)
                    eq("isParentInTrash", false)
                }
            } else {
                filter = {
                    eq("isInTrash", false)
                    eq("isParentInTrash", false)
                }
            }
        }
        if ("isDisposable" in domain.declaredFields.name) {
            Closure disposableFilter = {
                eq("isDisposable", false)
            }
            filter = filter ? (filter << disposableFilter) : disposableFilter
        }
        List sect = filter ? domain.createCriteria().list(filter) : domain.list()
        sect.each {
            Map entry = allEntry[it[key]];
            if (!entry) {
                entry = allEntry[it[key]] = [text: it[text], children: sorted ? getNewMap() : [:]];
            }
            if (it.parent) {
                Map parentMap = allEntry[it.parent[key]];
                if (!parentMap) {
                    parentMap = allEntry[it.parent[key]] = [text: it.parent[text], children: sorted ? getNewMap() : [:]]
                }
                parentMap.children.put(it[key], entry)
            } else {
                orderedForTree.put(it[key], entry)
            }
        }
        out << "<select class='${attrs["class"]}' ${attrs.name ? 'name="' + attrs.name + '"' : ''} ${attrs.id ? 'id="' + attrs.id + '"' : ''}"
        out << " ${key != "id" ? 'select-key="' + key + '"' : ''} ${text != "name" ? 'select-text="' + text + '"' : ''} select-values='${values ?: [value] as JSON}'"
        if (customAttrs) {
            customAttrs.each {
                if (it.value) {
                    out << "${it.key.encodeAsBMHTML()}='${it.value.encodeAsBMHTML()}'"
                }
            }
        }
        out << ">"
        if (prependMap) {
            prependMap.each {
                out << "<option value='${it.key.encodeAsBMHTML()}' class='depth-1 domain-prepend'>${it.value.encodeAsBMHTML()}</option>"
            }
        }
        def printChildren;
        int depth = 1;
        printChildren = { it ->
            boolean hasChild = !!it.value.children.size();
            if (!hasChild || selectGroupRoot || groupAsLabel) {
                if (values) {
                    out << "<option value='${it.key.encodeAsBMHTML()}' ${it.key in values ? 'selected' : ''} depth='${depth}' class='depth-${depth} ${hasChild && groupAsLabel ? "as-label" : ""}'>"
                } else {
                    out << "<option value='${it.key.encodeAsBMHTML()}' ${it.key == value ? 'selected' : ''} depth='${depth}' class='depth-${depth} ${hasChild && groupAsLabel ? "as-label" : ""}'>"
                }
                out << it.value.text.encodeAsBMHTML()
                out << "</option>"
            }
            if (hasChild) {
                depth++;
                it.value.children.each printChildren
                depth--;
            }
        }
        orderedForTree.each printChildren
        out << "</select>"
    }

    def domainSelect = { attrs, body ->
        Class domain = attrs.domain;
        if (!domain) {
            return;
        }
        String key = attrs["key"] ?: "id";
        String text = attrs["text"] ?: "name";
        Map prependMap = attrs['prepend'] ?: null;
        Map appendMap = attrs['append'] ?: null;
        Map customAttrs = attrs['custom-attrs'] ?: null;
        def value = attrs.value ?: '';
        def values = attrs.values ?: [];
        if (value) {
            values.add(value)
        }
        boolean selectGroupRoot = attrs["select-group-root"].toBoolean(true);
        boolean groupAsLabel = attrs["group-as-label"].toBoolean(false);
        Closure filter;
        if ("isInTrash" in domain.declaredFields.name) {
            filter = {
                eq("isInTrash", false)
                if ("isParentInTrash" in domain.declaredFields.name) {
                    eq("isParentInTrash", false)
                }
            }
        }
        if ("isDisposable" in domain.declaredFields.name) {
            Closure disposableFilter = {
                eq("isDisposable", false)
            }
            filter = filter ? (filter << disposableFilter) : disposableFilter
        }
        if ("isAutoGenerated" in domain.declaredFields.name) {
            Long ignoreAutoGenerated = attrs["ignoreAutoGenerated"] ?: null
            Closure isAutoGeneratedFilter = {
                or {
                    eq("isAutoGenerated", false)
                    if (ignoreAutoGenerated && ignoreAutoGenerated instanceof Long) {
                        eq("id", ignoreAutoGenerated)
                    }
                }
            }
            filter = filter ? (filter << isAutoGeneratedFilter) : isAutoGeneratedFilter
        }
        if (attrs.filter) {
            filter = filter ? (filter << attrs.filter) : attrs.filter
        }
        List sect = filter ? domain.createCriteria().list(filter) : domain.list();
        out << "<select class='${attrs["class"]}' ${attrs.name ? 'name="' + attrs.name + '"' : ''} ${attrs.id ? 'id="' + attrs.id + '"' : ''}"
        out << "${attrs.validation ? 'validation="' + attrs.validation + '"' : ''} ${attrs.disabled == 'true' ? 'disabled="true"' : ''}"
        out << " ${key != "id" ? 'select-key="' + key + '"' : ''} ${text != "name" ? 'select-text="' + text + '"' : ''} select-values='${values as JSON}'"
        if (customAttrs) {
            customAttrs.each {
                if (it.value) {
                    out << "${it.key.encodeAsBMHTML()}='${it.value.encodeAsBMHTML()}'"
                }
            }
        }
        out << ">"
        if (prependMap) {
            prependMap.each {
                out << "<option class='domain-prepend' value='${it.key.encodeAsBMHTML()}' ${it.key in values ? 'selected' : ''}>${it.value.encodeAsBMHTML()}</option>"
            }
        }
        def printChildren;
        printChildren = { it ->
            if (selectGroupRoot || groupAsLabel) {
                out << "<option value='${it[key].encodeAsBMHTML()}' ${it[key] in values ? 'selected' : ''}>"
                out << it[text].encodeAsBMHTML()
                out << "</option>"
            }
        }
        sect.each printChildren
        if (appendMap) {
            appendMap.each {
                out << "<option class='domain-append' value='${it.key}'>${it.value}</option>"
            }
        }
        out << "</select>"
    }

    def namedSelect = { attrs, body ->
        def keyMap = attrs.remove("key");
        List values = attrs.remove("values") ?: [];
        if (attrs["value"]) {
            values.add(attrs.remove("value"));
        }
        Map prependMap = attrs['prepend'] ?: null;
        Map appendMap = attrs['append'] ?: null;
        String optionKey = attrs.optionKey ?: "key"
        String optionLabel = attrs.optionLabel ?: "label"
        out << "<select "
        attrs.each {
            if (it.key == "disabled" && it.value == "false") {
                return
            }
            out << "${it.key}=\"${it.value}\""
        }
        out << ">"
        if (prependMap) {
            prependMap.each {
                out << "<option value='${it.key}'>${g.message(code: it.value)}</option>"
            }
        }
        if (keyMap instanceof Map) {
            keyMap.each {
                out << "<option value='${it.key}' ${it.key in values ? "selected" : ""}>${g.message(code: it.value)}</option>"
            }
        } else if (keyMap instanceof List) {
            keyMap.each {
                if (it instanceof Map) {
                    out << "<option value='${it[optionKey]}' ${it[optionKey] in values ? "selected" : ""}>${g.message(code: it[optionLabel])}</option>"
                } else {
                    out << "<option value='${it}' ${it in values ? "selected" : ""}>${g.message(code: it)}</option>"
                }
            }
        }
        if (appendMap) {
            appendMap.each {
                out << "<option value='${it.key}'>${g.message(code: it.value)}</option>"
            }
        }
        out << "</select>"
    }

    def radioGroup = { attrs, body ->
        request.radio_group_value = attrs.value
        request.radio_group_name = attrs.name
        out << body()
        request.removeAttribute("radio_group_value")
        request.removeAttribute("radio_group_name")
    }

    def radio = { attrs, body ->
        def value = attrs.value
        def name = attrs.remove("name") ?: request.radio_group_name
        def selected = attrs.remove("selected") ?: request.radio_group_value
        out << '<input type="radio" name="' + name.encodeAsBMHTML() + '"'
        attrs.each {
            out << " " + it.key + "='" + it.value.encodeAsBMHTML() + "'"
        }
        if (value == selected) {
            out << " checked"
        }
        out << ">"
    }

    def installedColor = { attrs, body ->
        List<Map> installedColors = templateService.getInstalledColors();
        if (attrs["showHeader"] && installedColors?.size() > 0) {
            out << "<div class=\"esp-title\">${g.message(code: "color")}</div>";
        }
        if (installedColors?.size() > 0) {
            out << "<div class=\"installed-colors colors\">"
            installedColors?.each {
                color ->
                    out << "<span class=\"color ${color.fullname == installedColor ? "selected" : ""}\" title=\"${color.name}\" name=\"${color.fullname}\" style=\"background-color: ${color.code}\"></span>";
            }
            out << "</div>"
        }
    }
}