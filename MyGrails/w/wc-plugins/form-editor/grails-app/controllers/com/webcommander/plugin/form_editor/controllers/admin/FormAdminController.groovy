package com.webcommander.plugin.form_editor.controllers.admin

import com.webcommander.authentication.annotations.License
import com.webcommander.common.CommonService
import com.webcommander.constants.DomainConstants
import com.webcommander.plugin.form_editor.Form
import com.webcommander.plugin.form_editor.FormSubmission
import com.webcommander.plugin.form_editor.FormSubmissionData
import com.webcommander.plugin.form_editor.admin.FormService
import com.webcommander.plugin.form_editor.constants.NamedConstants
import com.webcommander.throwables.AttachmentExistanceException
import com.webcommander.util.AppUtil
import com.webcommander.widget.Widget
import grails.converters.JSON
import grails.util.Holders
import org.grails.web.gsp.io.GrailsConventionGroovyPageLocator
import org.supercsv.io.CsvListWriter
import org.supercsv.prefs.CsvPreference
import java.nio.charset.StandardCharsets


class FormAdminController {
    CommonService commonService
    FormService formService
    GrailsConventionGroovyPageLocator groovyPageLocator

    @License(required = "allow_form_builder_feature")
    def loadAppView() {
        render(view: "/plugins/form_editor/admin/appView");
    }

    def leftPanel() {
        params.max = "-1"
        params.offset = "0"
        Integer count = formService.getFormsCount(params)
        List<Form> forms = formService.getForms(params)
        render(view: "/plugins/form_editor/admin/leftPanel", model: [forms: forms, count: count, selected: params.long("selected")]);
    }

    def explorerView() {
        Form form = params.id ? Form.get(params.id) : null
        render(view: "/plugins/form_editor/admin/explorerView", model: [form: form]);
    }

    def loadCreateForm() {
        File listFile = new File(servletContext.getRealPath("WEB-INF/system-resources/form-templates"), "template-list.json");
        List templates = JSON.parse(listFile.text)
        render(view: "/plugins/form_editor/admin/createPopup", model: [templates: templates]);
    }

    @License(required = "allow_form_builder_feature")
    def edit() {
        Form form;
        if(params.type == 'template') {
            form = formService.populateFormFromTemplate(params.id)
        } else if(params.id) {
            form = Form.get params.id
        }
        if(form == null) {
            form = new Form()
            form.name = "Form " + Form.count
        }
        render(view: "/plugins/form_editor/admin/editor/editor", model: [form: form]);
    }

    def fieldConfig() {
        String view = params.type
        if(view == "dropDown" || view == "radioButton" || view == "checkBox") {
            view = "select"
        }
        String source = groovyPageLocator.findViewByPath("/plugins/form_editor/admin/editor/config/${view}") ? "/plugins/form_editor/admin/editor/config/${view}" : "/plugins/form_editor/admin/editor/config/noConfig"
        render(view: source)
    }

    @License(required = "allow_form_builder_feature")
    def save() {
        Closure sanitizeParams
        sanitizeParams = { Map map ->
            Map result = [:]
            map.each {
                if (!it.key.contains(".")) {
                    if(it.value instanceof Map) {
                        result[it.key] = sanitizeParams(it.value)
                    } else {
                        result[it.key] = it.value
                    }
                }
            }
            return result
        };
        Map sanitizeData = sanitizeParams(params);
        if(formService.saveForm(sanitizeData)) {
            render([status: "success", message: g.message(code: "form.saved.successfully")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "form.could.not.saved")] as JSON)
        }
    }

    def delete() {
        try {
            Long id = params.long("id")
            if(formService.putFormInTrash(id, params.at2_reply, params.at1_reply)) {
                render([status: "success", message: g.message(code: "form.delete.success")] as JSON)
            } else {
                render([status: "error", message: g.message(code: "form.delete.failure")] as JSON)
            }
        } catch(AttachmentExistanceException att) {
            render([status: "error", error_code: "attachment.exists", attachments: att.attachmentInfo] as JSON)
        }
    }

    def preview() {
        Form form =  Form.get(params.id)
        Widget widget = new Widget(widgetType: DomainConstants.WIDGET_TYPE.FORM, title: form.name)
        widget.discard()
        render(view: "/plugins/form_editor/admin/preview", model: [form: form, widget: widget])
    }

    def deleteSelected() {
        if (formService.putSelectedFormsInTrash(params.list("ids").collect{it.toLong()})) {
            render([status: "success", message: g.message(code: "selected.forms.delete.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "selected.forms.could.not.delete")] as JSON)
        }
    }

    def loadFormData() {
        params.formData = true
        Integer count = formService.getFormSubmissionCount(params)
        params.max = params.max ?: "10"
        params.offset = params.offset ?: "0"
        List<FormSubmission> formSubmission = commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.offset = offset
            formService.getSubmission(params)
        }
        render(view: "/plugins/form_editor/admin/submission/loadSubmittedData", model: [count: count, formSubmission: formSubmission])
    }

    def view() {
        Long id = params.long('id');
        LinkedList<FormSubmissionData> submissionDataList = FormSubmissionData.where {
            eq("formSubmission.id", id)
        }.list()
        render(view: "/plugins/form_editor/admin/submission/infoView", model: [submissionDataList: submissionDataList]);
    }

    def copy() {
        Long id = params.long('id')
        if (formService.copyForm(id)) {
            render([status: "success", message: g.message(code: "form.copy.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "form.copy.failure")] as JSON)
        }
    }

    def addCondition() {
        Map data = JSON.parse(params.data)
        data.fields = data.fields.findAll {
            it.uuid != data .activeField.uuid
        }
        render(view: "/plugins/form_editor/admin/editor/addCondition", model: data)
    }

    def downloadSubmission() {
        String extension = appResource.getFormSubmissionFilePath(params)
        String filePath = appResource.getRootPhysicalPath(extension: extension)
        File file = new File(filePath);
        if(!file.exists()) {
            response.setStatus(404)
            render(text: "")
            return;
        }
        response.setHeader("Content-Type", "application/octet-stream")
        response.setHeader("Content-Length", "${file.length()}")
        response.setHeader("Content-Disposition", "attachment; filename=\"${file.name}\"")
        InputStream inputStream = new FileInputStream(file)
        response.outputStream << inputStream
        inputStream.close()
        response.outputStream.flush();
    }

    def advanceFilter() {
        render(view: "/plugins/form_editor/admin/filter");
    }

    def export() {
        Form form = Form.get(params.id);
        response.setHeader("Content-Type", "application/octet-stream")
        response.setHeader("Content-Disposition", "attachment; filename=\"${form.name}.json\"")
        ByteArrayInputStream inputStream = new ByteArrayInputStream(form.serialize().getBytes(StandardCharsets.UTF_8))
        response.outputStream << inputStream
        inputStream.close()
        response.outputStream.flush();
    }

    def exportSubmission() {
        params.formData = true;
        List<FormSubmission> submissions = formService.getSubmission(params);
        List<String> headers = [];
        Map<String, Integer> headerIndex = [:]
        CsvListWriter listWriter = null
        Integer index = 0
        submissions.each {
            it.submittedDataList.each {
                if(!headerIndex.containsKey(it.fieldName)) {
                    headerIndex[it.fieldName] = index++
                    headers.add(it.fieldName)
                }
            }
        }
        headerIndex["Submission Date"] = index++
        headers.add("Submission Date")
        try {
            response.setHeader("Content-Type", "text/csv");
            response.setHeader("Content-Disposition", "attachment; filename=submissions.csv")
            OutputStreamWriter writer = new OutputStreamWriter(response.outputStream)
            listWriter = new CsvListWriter(writer, CsvPreference.EXCEL_PREFERENCE)
            String[] fields = headers.toArray()
            listWriter.writeHeader(fields)
            submissions.each {
                String[] fieldValueList = new String[index]
                it.submittedDataList.each {
                    Integer i = headerIndex[it.fieldName]
                    fieldValueList[i] = it.fieldValue
                }
                Integer i = headerIndex["Submission Date"]
                fieldValueList[i] = it.submitted.toAdminFormat(true, false, session.timezone)
                listWriter.write(fieldValueList);
            }
        } finally {
            if( listWriter != null ) {
                listWriter.close()
            }
        }
    }

    def deleteSubmittedData() {
        List<Long> ids = params.list("id").collect{it.toLong()}
        def totalDeletedForms = formService.deleteFormSubmission(ids);
        if( totalDeletedForms == ids.size() ) {
            render([status: "success", message: g.message(code: "success.submitted.form.delete")] as JSON)
        } else if (totalDeletedForms == 0) {
            render([status: "error", message: g.message(code: "error.delete.submitted.form")] as JSON)
        } else {
            render([status: "success", message: g.message(code: "selected.not.deleted", args: [ids.size() - totalDeletedForms, ids.size(), g.message(code: "submitted.form")])] as JSON)
        }
    }

    def advanceFilterSubmittedForm() {
        render(view: "/plugins/form_editor/admin/submission/filter", model: [searchCriteria: NamedConstants.FORM_SUBMISSION_FILTER_MESSAGE_KEYS]);
    }
}
