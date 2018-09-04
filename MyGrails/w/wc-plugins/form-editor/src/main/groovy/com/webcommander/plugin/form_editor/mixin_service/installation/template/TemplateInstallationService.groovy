package com.webcommander.plugin.form_editor.mixin_service.installation.template

import com.webcommander.installation.template.InstallationDataHolder
import com.webcommander.models.TemplateData
import com.webcommander.plugin.form_editor.FieldCondition
import com.webcommander.plugin.form_editor.Form
import com.webcommander.plugin.form_editor.FormExtraProp
import com.webcommander.plugin.form_editor.FormField
import com.webcommander.util.DomainUtil
import com.webcommander.util.StringUtil

class TemplateInstallationService {

    Long saveFormTypeWidgetContent(TemplateData templateData, InstallationDataHolder installationDataHolder, Map data) {
        Form form = new Form()
        DomainUtil.populateDomainInst(form, data, [exclude: ["fields"]])
        if(form.hasErrors()) {
            return null
        }
        form.save()
        Closure saveField;
        Map uuidMap = [:]
        saveField = {Map fieldData ->
            String uuid = StringUtil.uuid
            uuidMap[fieldData.uuid] = uuid
            FormField field = new FormField(uuid: uuid)
            DomainUtil.populateDomainInst(field, fieldData, [exclude: ["fields", "extras", "conditions", "uuid"]])
            fieldData.extras.each {
                FormExtraProp extraProp = new FormExtraProp()
                DomainUtil.populateDomainInst(extraProp, it)
                field.extras.add extraProp
            }
            fieldData.conditions.each {
                FieldCondition condition = new FieldCondition(formField: field)
                DomainUtil.populateDomainInst(condition, it, [exclude: ["formField"]])
                field.conditions.add(condition)
            }
            fieldData.fields.each {
                field.fields.add saveField(it)
            }
            return field
        }
        data.fields.each {
            form.fields.add saveField(it)
        }
        if (uuidMap[form.senderEmailFieldUUID]) {
            form.senderEmailFieldUUID = uuidMap[form.senderEmailFieldUUID]
        }
        Closure reMapUUID;
        reMapUUID = { FormField field ->
            field.conditions.each {
                if(uuidMap[it.dependentFieldUUID]) {
                    it.dependentFieldUUID = uuidMap[it.dependentFieldUUID]
                }
            }
            field.fields.each {
                reMapUUID(it)
            }
        }
        form.fields.each {
            reMapUUID(it)
        }
        form.save()
        return form.id
    }
}
