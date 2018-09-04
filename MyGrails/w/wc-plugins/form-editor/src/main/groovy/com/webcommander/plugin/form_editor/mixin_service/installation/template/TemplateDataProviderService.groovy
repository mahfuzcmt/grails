package com.webcommander.plugin.form_editor.mixin_service.installation.template

import com.webcommander.models.TemplateData
import com.webcommander.plugin.form_editor.Form
import com.webcommander.plugin.form_editor.FormField
import com.webcommander.plugin.form_editor.admin.FormService
import com.webcommander.util.DomainUtil
import grails.util.Holders

class TemplateDataProviderService {
    private static FormService _formService
    private static FormService getFormService() {
        return _formService ?: (_formService = Holders.grailsApplication.mainContext.getBean(FormService))
    }
    Map collectFormTypeContent(TemplateData templateData, Form form) {
        Map data = DomainUtil.toMap(form, [exlude: ["fields"]])
        Closure collectFiled;
        collectFiled = {FormField field ->
            Map filedData = DomainUtil.toMap(field, [exlude: ["fields", "extras", "conditions"]]);
            filedData.extras = field.extras.collect {
                return DomainUtil.toMap(it)
            }
            filedData.conditions = field.conditions.collect {
                return DomainUtil.toMap(it, [exlude: ["formField"]])
            }
            filedData.fields = field.fields.collect {
                return collectFiled(it)
            }
            return filedData
        }
        data.fields = form.fields.collect {
            return collectFiled(it)
        }
        return data
    }

    List<Map> collectFormTypeContents(TemplateData templateData) {
        List<Form> forms = formService.getForms([:])
        return forms.collect {
            return collectFormTypeContent(templateData, it)
        }
    }
}
