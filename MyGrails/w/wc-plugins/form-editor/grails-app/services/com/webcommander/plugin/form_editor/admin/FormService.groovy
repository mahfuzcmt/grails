package com.webcommander.plugin.form_editor.admin

import com.webcommander.admin.TrashService
import com.webcommander.annotations.Initializable
import com.webcommander.common.CommanderMailService
import com.webcommander.common.CommonService
import com.webcommander.common.FileService
import com.webcommander.events.AppEventManager
import com.webcommander.installation.template.TemplateContent
import com.webcommander.listener.SessionManager
import com.webcommander.manager.PathManager
import com.webcommander.models.blueprints.DisposableUtilServiceModel
import com.webcommander.plugin.form_editor.*
import com.webcommander.plugin.form_editor.constants.DomainConstants
import com.webcommander.plugin.form_editor.constants.NamedConstants
import com.webcommander.plugin.form_editor.util.MultipartUtility
import com.webcommander.task.MultiLoggerTask
import com.webcommander.task.TaskService
import com.webcommander.util.AppUtil
import com.webcommander.util.DomainUtil
import com.webcommander.util.StringUtil
import com.webcommander.util.TrashUtil
import grails.gorm.transactions.Transactional
import grails.util.Holders
import grails.web.databinding.DataBindingUtils
import grails.web.servlet.mvc.GrailsParameterMap
import org.apache.commons.io.FilenameUtils
import org.hibernate.SessionFactory
import org.springframework.web.multipart.MultipartFile
import com.webcommander.constants.NamedConstants as WCNAMECONST

@Initializable
class FormService implements DisposableUtilServiceModel {
    CommonService commonService
    TrashService trashService
    FileService fileService
    TaskService taskService
    CommanderMailService commanderMailService
    SessionFactory sessionFactory

    public static void initialize() {
        AppEventManager.on("before-template-install", {
            List<TemplateContent> contents = TemplateContent.createCriteria().list {
                eq("contentType", com.webcommander.constants.DomainConstants.WIDGET_CONTENT_TYPE.FORM)
            }
            if(contents) {
                Form.where {
                    inList("id", contents.contentId)
                }.updateAll(isDisposable: true)
                contents*.delete()
            }
        });

        AppEventManager.on("before-formField-delete", { id ->
            FormField field = FormField.proxy(id)
            field.extras.each {
                it.delete()
            }
        })

        AppEventManager.on("before-form-delete", { id ->
            Form form = Form.proxy(id)
            form.fields.each {
                AppEventManager.fire("before-formField-delete", [it.id])
                it.delete()
                AppEventManager.fire("formField-delete", [it.id])
            }
        })

        AppEventManager.on("before-formField-delete", { id ->
            FormField field = FormField.proxy(id)
            field.fields.each {
                AppEventManager.fire("before-formField-delete", [it.id])
                it.delete()
                AppEventManager.fire("formField-delete", [it.id])
            }
        })

        AppEventManager.on("before-form-delete", { id ->
            FormSubmission.createCriteria().list {
                eq("form.id", id)
            }.each {
                AppEventManager.fire("before-formSubmission-delete", [it.id])
                it.delete()
                AppEventManager.fire("formSubmission-delete", [it.id])
            }
        })
    }

    private Closure getCriteriaClosure(Map params) {
        Closure closure = {
            if (params.formData) {
                eq("form.id", params.id.toLong())
            } else {
                if (params.searchText) {
                    ilike("name", "%${params.searchText.trim().encodeAsLikeText()}%")
                }
                if (params.name) {
                    ilike("name", "%${params.name.trim().encodeAsLikeText()}%")
                }
                eq("isInTrash", false);
            }
            if (params.name) {
                ilike("name", "%${params.name.trim().encodeAsLikeText()}%")
            }
            if (params.isDisposable != "true") {
                eq("isDisposable", false)
            } else {
                eq("isDisposable", true)
            }
        }
        return closure;
    }

    private Closure getCriteriaClosureForSubmission(Map params) {
        Closure closure = {
            if (params.formData) {
                if(params['submission-range-selector']) {
                    def fromDate, toDate;
                    if(params['submission-range-selector'] == 'date') {
                        fromDate = params.dateFrom ? params.dateFrom : ""
                        toDate = params.dateTo ? params.dateTo : ""
                    } else {
                        toDate = new Date()
                        fromDate = toDate - ((params['submission-range-selector']) as Integer)
                        fromDate = fromDate.format('yyyy-MM-dd')
                        toDate = toDate.format('yyyy-MM-dd')
                    }
                    eq("form.id", params.id.toLong())
                    between("submitted", Date.parse('yyyy-MM-dd', fromDate), Date.parse('yyyy-MM-dd', toDate))
                } else {
                    eq("form.id", params.id.toLong())
                }
            } else {
                if (params.searchText) {
                    ilike("name", "%${params.searchText.trim().encodeAsLikeText()}%")
                }
                if (params.name) {
                    ilike("name", "%${params.name.trim().encodeAsLikeText()}%")
                }
                eq("isInTrash", false);
            }
            if (params.name) {
                ilike("name", "%${params.name.trim().encodeAsLikeText()}%")
            }
        }
        return closure;
    }

    private Closure getCriteriaClosureForTrash(Map params) {
        def session = AppUtil.session;
        Closure closure = {
            if (params.searchText) {
                ilike("name", "%${params.searchText.trim().encodeAsLikeText()}%")
            }
            if (params.deletedFrom) {
                Date date = params.deletedFrom.dayStart.gmt(session.timezone);
                ge("updated", date);
            }
            if (params.deletedTo) {
                Date date = params.deletedTo.dayEnd.gmt(session.timezone);
                le("updated", date);
            }
            eq("isInTrash", true);
        }
        return closure;
    }

    List<Form> getFormsForWidget() {
        return Form.where {
            eq("isDisposable", false)
            eq("isInTrash", false)
        }.list()
    }

    public int getFormsCount(Map params) {
        Closure closure = getCriteriaClosure(params)
        return Form.createCriteria().count {
            and closure
        }
    }

    public List<Form> getForms(Map params) {
        Closure closure = getCriteriaClosure(params);
        def listMap = [max: params.max, offset: params.offset];
        return Form.createCriteria().list(listMap) {
            and closure
            order(params.sort ?: "name", params.dir ?: "asc")
        }
    }

    public int getFormSubmissionCount(Map params) {
        Closure closure = getCriteriaClosureForSubmission(params)
        return FormSubmission.createCriteria().count {
            and closure
        }
    }

    public List<FormSubmission> getSubmission(Map params) {
        Closure closure = getCriteriaClosureForSubmission(params);
        def listMap = [max: params.max, offset: params.offset];
        return FormSubmission.createCriteria().list(listMap) {
            and closure
            order(params.sort ?: "submitted", params.dir ?: "desc")
        }
    }

    @Transactional
    public Boolean copyForm(Long formId) {
        Form form = Form.get(formId);
        Form cloneForm = DomainUtil.clone(form);
        Closure setUuid
        setUuid = { List<FormField> fields ->
            fields.each {
                it.uuid = StringUtil.uuid
                if (it.fields) {
                    setUuid it.fields
                }
            }
        }
        setUuid cloneForm.fields
        cloneForm.name = commonService.getCopyNameForDomain(form);
        cloneForm.save()
        return !cloneForm.hasErrors();
    }

    @Transactional
    public boolean saveForm(params) {
        Form form;
        Closure deleteField;
        deleteField = { FormField field ->
            field.extras*.delete();
            field.conditions*.delete()
            field.fields.each {
                deleteField(it);
            }
            field.delete();
        }
        if (params.id) {
            form = Form.get(params.id)
            form.fields.each { a ->
                deleteField(a)
            }
            form.fields.clear()
            FormField.where {
                def ff = FormField
                exists Form.where {
                    fields {
                        eqProperty("id", "ff.id")
                    }
                }.id()
            }.list();
        } else {
            form = new Form()
        }
        sessionFactory.currentSession.flush()
        DataBindingUtils.bindObjectToInstance(form, params, null, ["id"], null)
        Closure newField;
        newField = { config, uuid ->
            FormField field = new FormField()
            DataBindingUtils.bindObjectToInstance(field, config, null, ["id"], null)
            field.label = field.label ?: "";
            field.uuid = uuid
            if (config.extra) {
                config.extra.each { extraEntry ->
                    if (extraEntry.key.contains(".")) {
                        return;
                    }
                    if (extraEntry.value instanceof String) {
                        FormExtraProp prop = new FormExtraProp()
                        prop.type = extraEntry.key
                        prop.value = extraEntry.value
                        field.extras.add(prop)
                    } else if (extraEntry.value instanceof Object[]) {
                        extraEntry.value.each { value ->
                            FormExtraProp prop = new FormExtraProp()
                            prop.type = extraEntry.key
                            prop.value = value
                            field.extras.add(prop)
                        }
                    } else if (extraEntry.value instanceof Map) {
                        extraEntry.value.each { entry ->
                            FormExtraProp prop = new FormExtraProp()
                            prop.type = extraEntry.key
                            if (entry.value instanceof Map) {
                                prop.label = entry.value.label
                                prop.value = entry.value.value
                                prop.extraValue = entry.value.extraValue
                            } else {
                                prop.label = entry.key
                                prop.value = entry.value
                            }
                            field.extras.add(prop)
                        }
                    }
                }
            }
            if (config.conditions instanceof Map) {
                config.conditions.each { key, value ->
                    FieldCondition condition = new FieldCondition()
                    condition.action = value.action;
                    condition.dependentFieldUUID = value.dependentFieldUUID
                    condition.targetOption = value.targetOption
                    condition.formField = field
                    field.conditions.add(condition)
                }
            }
            if (config.field) {
                config.field.each { _config ->
                    if (_config.key.contains(".")) {
                        return;
                    }
                    field.fields.add(newField(_config.value, _config.key))
                }
            }
            return field
        }
        Closure newFiledGroup = { Map rowData, fields, uuid ->
            FormField fieldGroup = new FormField();
            fieldGroup.name = "";
            fieldGroup.type = "fieldGroup";
            fieldGroup.label = "";
            fieldGroup.uuid = uuid
            fields.each { field ->
                FormField formField = newField(rowData[field], field);
                fieldGroup.fields.add(formField)
            }
            return fieldGroup
        }
        List rows = params.rows instanceof List || params.rows instanceof Object[] ? params.rows : (params.rows ? [params.rows] : []);
        rows.each { row ->
            Map rowData = params[row];
            FormField field
            if (rowData.fieldid instanceof List || rowData.fieldid instanceof Object[]) {
                field = newFiledGroup(rowData, rowData.fieldid, row)
            } else {
                field = newField(rowData[rowData.fieldid], rowData.fieldid)
            }
            form.fields.add(field)
        }
        form.isDisposable = false
        form.save()
        if (params.id) AppEventManager.fire("form-update", [form.id])
        return !form.hasErrors()
    }

    @Transactional
    public boolean putFormInTrash(Long id, String at2_reply, String at1_reply) {
        TrashUtil.preProcessPutInTrash("form", id, at2_reply != null, at1_reply != null)
        try {
            return trashService.putObjectInTrash("form", Form.proxy(id), at1_reply)
        } catch (Exception e) {
            return false
        }
    }

    def putSelectedFormsInTrash(List<String> ids) {
        def result = true;
        ids.each { id ->
            if (!putFormInTrash(id.toLong(), "yes", "include")) {
                result = false;
                return true
            }
        }
        return result;
    }

    public Long countFormsInTrash() {
        return Form.createCriteria().count {
            eq("isInTrash", true)
        }
    }

    public Long countFormsInTrash(Map params) {
        return Form.createCriteria().count {
            and getCriteriaClosureForTrash(params)
        }
    }

    public Map getFormsInTrash(int offset, int max, String sort, String dir) {
        return [Form: Form.createCriteria().list(offset: offset, max: max) {
            eq("isInTrash", true)
            order(sort ?: "name", dir ?: "asc")
        }.collect {
            [id: it.id, name: it.name, updated: it.updated]
        }]
    }

    public Map getFormsInTrash(Map params) {
        def listMap = [offset: params.offset, max: params.max];
        return [Form: Form.createCriteria().list(listMap) {
            and getCriteriaClosureForTrash(params)
            order(params.sort ?: "name", params.dir ?: "asc")
        }.collect {
            [id: it.id, name: it.name, updated: it.updated]
        }]
    }

    @Transactional
    public int deleteFormSubmission(ids) {
        int formDeleted = 0
        try{
            List forms = FormSubmission.createCriteria().list {
                inList('id', ids)
            }
            forms.each {
                it.delete()
                formDeleted++
            }
            return formDeleted
        } catch (Exception ex) {
            return formDeleted
        }
    }

    @Transactional
    public boolean restoreFormFromTrash(Long id) {
        Form form = Form.get(id)
        if (!form) {
            return false
        }
        form.isInTrash = false
        form.merge()
        return !form.hasErrors()
    }

    @Transactional
    public Long restoreFormFromTrash(String field, String value) {
        Form form = Form.createCriteria().get {
            eq(field, value)
        }
        form.isInTrash = false;
        form.merge();
        return form.id;
    }

    @Transactional
    public boolean deleteTrashItemAndSaveCurrent(def field, def value) {
        Form form = Form.createCriteria().get {
            eq(field, value)
        }
        deleteForm(form.id);
        return !form.hasErrors();
    }

    public Form getFormByIdOrName(String idOrName) {
        List<Form> forms = Form.createCriteria().list {
            or {
                like("id", idOrName.encodeAsLikeText())
                like("name", idOrName.encodeAsLikeText())
            }
        }
        if (forms.size() > 0) {
            return forms.get(0)
        }
        return null
    }

    @Transactional
    public boolean deleteForm(Long id) {
        Form form = Form.proxy(id);
        try {
            form.delete();
        } catch (Throwable t) {
            return false;
        }
        return true;
    }

    private boolean sendSubmissionData(Form form, Map params) {
        String ipAddressBlock = "<div style='padding-bottom: 7px;'><span>${(params.addressString).encodeAsBMHTML()}:&nbsp;</span><span style='padding-left:5px;'>${(params.customerIP).encodeAsBMHTML()}</span></div>"
        String tempPath = SessionManager.getTempFolder().getAbsolutePath() + File.separator + StringUtil.uuid
        String submittedData = ""
        List uploads = []
        params.submit.each { k, v ->
            if (v instanceof Map) {
                return;
            }
            String fieldValue = ""
            if (v instanceof String[] || v instanceof List<String>) {
                fieldValue = v.join(",")
            } else if (v instanceof MultipartFile) {
                fieldValue = k + "." + FilenameUtils.getExtension(v.originalFilename);
                File file = fileService.uploadFile(v, null, fieldValue, null, tempPath)
                uploads.add(file)
                return
            } else if (v instanceof MultipartFile[] || v instanceof List<MultipartFile>) {
                v.eachWithIndex { mFile, index ->
                    fieldValue = k + "." + FilenameUtils.getExtension(mFile.originalFilename);
                    File file = fileService.uploadFile(mFile, null, fieldValue, null, tempPath)
                    uploads.add(file)
                }
                return
            } else {
                fieldValue = v
            }
            submittedData += "<div style='padding-bottom: 7px;'><span>${k.encodeAsBMHTML()}:&nbsp;</span><span style='padding-left:5px;'>${((v instanceof String[] || v instanceof List) ? v.join(", ") : v).encodeAsBMHTML()}</span></div>"
        }
        Map macrosAndTemplate = commanderMailService.getMacrosAndTemplateByIdentifier("form-submit-to-email")
        if (!macrosAndTemplate.emailTemplate.active) {
            return true;
        }
        Map refinedMacros = macrosAndTemplate.commonMacros
        macrosAndTemplate.macros.each {
            switch (it.key.toString()) {
                case "form_name":
                    refinedMacros[it.key] = form.name
                    break;
                case "form_submit_data":
                    refinedMacros[it.key] = ipAddressBlock + submittedData
                    break;
            }
        }
        if (form.emailSubject) {
            macrosAndTemplate.emailTemplate.subject = form.emailSubject
        }
        String senderEmail = params[form.senderEmailFieldUUID + ".senderEmail"];
        commanderMailService.sendFormMail(macrosAndTemplate.emailTemplate, macrosAndTemplate.activeHtml, macrosAndTemplate.activeText, refinedMacros, form.emailTo, form.emailCc, form.emailBcc, senderEmail, uploads)
        return true;
    }

    @Transactional
    private def saveSubmission(Form form, Map params) {
        FormSubmission formSubmission = new FormSubmission(form: form, ip: params.customerIP);
        Map uploads = [:]
        params.submit.each { k, v ->
            if (v instanceof Map) {
                return;
            }
            Boolean isFile = false
            String fieldValue
            if (v instanceof String[] || v instanceof List<String>) {
                fieldValue = v.join(",")
            } else if (v instanceof MultipartFile) {
                fieldValue = k + "." + FilenameUtils.getExtension(v.originalFilename);
                uploads[fieldValue] = v
                isFile = true
            } else if (v instanceof MultipartFile[] || v instanceof List<MultipartFile>) {
                List values = []
                v.eachWithIndex { file, index ->
                    values.add(k + "(${index})." + FilenameUtils.getExtension(file.originalFilename));
                    uploads[value] = file
                }
                fieldValue = values.join("&#")
                isFile = true
            } else {
                fieldValue = v
            }
            formSubmission.addToSubmittedDataList(new FormSubmissionData(fieldName: k, fieldValue: fieldValue, isFile: isFile))
        }
        form.submissionCount++
        form.save()
        formSubmission.save()
        String filePath = PathManager.getResourceRoot("form-submissions/submission-${formSubmission.id}")
        uploads.each { name, file ->
            fileService.uploadFile(file, WCNAMECONST.RESOURCE_TYPE.RESOURCE, name.toString(), null, filePath)
        }
        return !formSubmission.hasErrors()
    }

    private Boolean externalSubmission(Form form, Map params) {
        String tempPath = SessionManager.getTempFolder().getAbsolutePath() + File.separator + StringUtil.uuid
        MultipartUtility multipartUtility = new MultipartUtility(form.actionUrl, "UTF-8")
        multipartUtility.addHeaderField("User-Agent", "WebCommander")
        params.submit.each { key, value ->
            if (value instanceof Map) {
                return
            }
            if (value instanceof String[] || value instanceof List<String>) {
                multipartUtility.addFormField(key, value.join(","))
            } else if (value instanceof MultipartFile) {
                String fieldValue = key + "." + FilenameUtils.getExtension(value.originalFilename);
                File file = fileService.uploadFile(value, null, fieldValue, null, tempPath)
                multipartUtility.addFilePart(key, file)
            } else if (value instanceof MultipartFile[] || value instanceof List<MultipartFile>) {
                value.eachWithIndex { mFile, index ->
                    String fieldValue = key + "." + FilenameUtils.getExtension(mFile.originalFilename)
                    File file = fileService.uploadFile(mFile, null, fieldValue, null, tempPath)
                    multipartUtility.addFilePart(key, file)
                }
            } else {
                multipartUtility.addFormField(key, value)
            }
        }
        try {
            multipartUtility.finish()
        } catch (Exception ex) {
            return false
        }
        return true
    }

    @Transactional
    boolean processSubmissionData(Form form, GrailsParameterMap params) {
        if (form == null) {
            return;
        }
        Map submittedData = [:];
        Closure collectData;
        collectData = { FormField field, Map data ->
            if (field.fields && field.type == "fieldGroup") {
                field.fields.each {
                    collectData(it, data)
                }
            } else if (data.containsKey(field.name)) {
                submittedData[field.name] = data[field.name]
            }
        };

        form.fields.each {
            collectData(it, params.submit)
        }
        params.submit = submittedData;
        Boolean returnValue = saveSubmission(form, params)
        if (form.actionType == DomainConstants.FORM_ACTION_TYPE.EMAIL) {
            returnValue = sendSubmissionData(form, params)
        } else if (form.actionType == DomainConstants.FORM_ACTION_TYPE.EXTERNAL_URL) {
            returnValue = externalSubmission(form, params)
        }
        return returnValue
    }

    private void updateFiledUUIDs(FormField field, Map uuidMap) {
        String uuid = StringUtil.uuid
        uuidMap[field.uuid] = uuid
        field.uuid = uuid
        for (FormField it : field.fields) {
            updateFiledUUIDs(it, uuidMap)
        }

    }

    private void reMapFileConditionsUUIDs(FormField field, Map uuidMap) {
        for (FieldCondition it : field.conditions) {
            if (uuidMap[it.dependentFieldUUID]) {
                it.dependentFieldUUID = uuidMap[it.dependentFieldUUID]
            }
        }
        for (FormField it : field.fields) {
            reMapFileConditionsUUIDs(it, uuidMap)
        }
    }

    Form populateFormFromTemplate(String id) {
        Form form = null;
        def filePath = new File(Holders.servletContext.getRealPath("WEB-INF/system-resources/form-templates"), "${id}.json");
        if (filePath.exists()) {
            form = new Form();
            form.deSerialize(filePath.text);
            form.discard();
            Map uuidMap = [:]
            for (FormField it : form.fields) {
                updateFiledUUIDs(it, uuidMap)
            }
            for (FormField it : form.fields) {
                reMapFileConditionsUUIDs(it, uuidMap)
            }
        }
        return form
    }

    @Override
    Integer countDisposableItems(String itemType) {
        return getFormsCount([isDisposable: "true"])
    }

    @Override
    void removeDisposableItems(String itemType, MultiLoggerTask task) {
        Form.withNewSession { session ->
            List<Form> forms = this.getForms([isDisposable: "true"])
            for (Form form : forms) {
                try {
                    this.putFormInTrash(form.id, "yes", "include")
                    session.flush()
                    task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                    task.taskLogger.success("Form: $form.name", "")
                    task.meta.successCount++
                } catch (Exception e) {
                    task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                    task.taskLogger.error("Form: $form.name", e.message)
                    task.meta.errorCount++;
                }
            }
        }
    }
}
