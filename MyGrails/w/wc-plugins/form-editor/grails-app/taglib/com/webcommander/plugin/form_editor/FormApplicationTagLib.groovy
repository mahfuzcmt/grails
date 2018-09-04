package com.webcommander.plugin.form_editor

import com.webcommander.ApplicationTagLib
import com.webcommander.plugin.form_editor.constants.DomainConstants
import com.webcommander.util.DomainUtil
import com.webcommander.util.StringUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

class FormApplicationTagLib {

    static namespace = "wcform"

    @Autowired
    @Qualifier("com.webcommander.ApplicationTagLib")
    ApplicationTagLib app

    def adminJSs = { attr, body ->
        out << body()
        out << app.javascript(src: 'plugins/form-editor/js/wcform/wcform.js')
        out << app.javascript(src: 'plugins/form-editor/js/moment-js/moment.js')
        out << app.javascript(src: 'plugins/form-editor/js/combodate-js/combodate.js')
    }

    def fieldDump = { attrs, body ->
        String prefix = attrs.prefix ? attrs.prefix.encodeAsBMHTML() + "." : ""
        DomainUtil.getObjectAsMap(attrs.obj).each { prop ->
            if(prop.key == "DEFAULT" || (attrs.exclude && attrs.exclude.contains(prop.key))) {
                return;
            }
            if(prop.key == "extras") {
                prop.value.each { each ->
                    String id = each.id ?: StringUtil.uuid
                    String scopePrefix = prefix + "extra." + each.type.encodeAsBMHTML() + "." + id
                    out << "<input type='hidden' prop-id='${id}' name='${scopePrefix}.label' value=\"${each.label.encodeAsBMHTML() ?: ''}\">"
                    out << "<input type='hidden' prop-id='${id}' name='${scopePrefix}.value' value=\"${each.value.encodeAsBMHTML() ?: ''}\">"
                    out << "<input type='hidden' prop-id='${id}' name='${scopePrefix}.extraValue' value=\"${each.extraValue.encodeAsBMHTML() ?: ''}\">"
                }
            } else if(prop.key == "fields") {
                prop.value.each { each ->
                    def uuid = each.uuid ?: StringUtil.uuid;
                    out << fieldDump(prefix: prefix + "field." + uuid, obj: each, exclude: ["configs"])
                }
            } else if(prop.key == "conditions") {
                prop.value.each { FieldCondition condition ->
                    def uuid = StringUtil.uuid;
                    out << "<input type='hidden' name='${prefix}conditions.${uuid}.targetOption' value='${condition.targetOption.encodeAsBMHTML()}' class='condition' group-id='${uuid}'>"
                    out << "<input type='hidden' name='${prefix}conditions.${uuid}.action' value='${condition.action}' class='condition' group-id='${uuid}' >"
                    out << "<input type='hidden' name='${prefix}conditions.${uuid}.dependentFieldUUID' value='${condition.dependentFieldUUID}' class='condition' group-id='${uuid}'>"
                }
            } else {
                out << "<input type='hidden' name='$prefix${prop.key.encodeAsBMHTML()}' value=\"${prop.value.encodeAsBMHTML() ?: ''}\">"
            }
        }
    }

    def renderBlock = {attrs, body ->
        String fieldId = attrs.field.uuid ?: StringUtil.uuid;
        Boolean isEmptyBlock = attrs.field.type == DomainConstants.FORM_FIELD.EMPTY
        out << "<div class='block block-${attrs.blockNo}${isEmptyBlock ? " sortable" : ""}'>"
        if(!isEmptyBlock) {
            out << "<div class='form-field ${attrs.field.type}'>"
            out << '<span class="value-cache" style="display: none">'
            out << "<input type=\"hidden\" name=\"fieldid\" value=\"${fieldId}\">"
            out << wcform.fieldDump(obj: attrs.field, prefix: fieldId, exclude: ["configs"])
            out << '</span>'
            out << '</div>'
        }
        out << '<span class="tool-icon remove remove-field"></span>'
        out << "</div>"
    }

    def renderRow = {attrs, body ->
        FormField field = attrs.field
        Integer noOfBlock = field.type == "fieldGroup" ? field.fields.size() : 1;
        out << "<div class='row block-container-${noOfBlock}'>"
        if(field.type == "fieldGroup") {
            field.fields.eachWithIndex { FormField entry, int i ->
                out << wcform.renderBlock(field: entry, blockNo: i + 1)
            }
        } else {
            out << wcform.renderBlock(field: field, blockNo: 1)
        }
        out << '<span class="close-option-header">\
                   <span class="settings"></span>\
                </span>'
        out << "</div>"
    }
}