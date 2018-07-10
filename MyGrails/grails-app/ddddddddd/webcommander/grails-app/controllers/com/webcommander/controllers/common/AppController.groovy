package com.webcommander.controllers.common

import com.webcommander.AppResourceTagLib
import com.webcommander.admin.AdministrationService
import com.webcommander.admin.City
import com.webcommander.admin.ConfigService
import com.webcommander.admin.State
import com.webcommander.authentication.annotations.RequiresAdmin
import com.webcommander.beans.SiteMessageSource
import com.webcommander.common.CommonService
import com.webcommander.common.FileService
import com.webcommander.common.ImageService
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.content.RemoteRepositoryService
import com.webcommander.manager.HookManager
import com.webcommander.manager.PathManager
import com.webcommander.models.MockStaticResource
import com.webcommander.task.OfflineQueryQueueService
import com.webcommander.tenant.TenantPropsResolver
import com.webcommander.util.AppUtil
import com.webcommander.util.StringUtil
import com.webcommander.webcommerce.Currency
import grails.converters.JSON
import grails.util.Holders
import org.apache.commons.httpclient.HttpStatus
import org.apache.commons.io.FilenameUtils
import org.hibernate.sql.JoinType
import org.springframework.web.multipart.MultipartFile

class AppController {
    AdministrationService administrationService
    SiteMessageSource siteMessageSource
    CommonService commonService
    ConfigService configService
    ImageService imageService
    FileService fileService
    OfflineQueryQueueService offlineQueryQueueService
    RemoteRepositoryService remoteRepositoryService

    def domainSelector() {
        String fullClassName = "com.webcommander." + params.domain;
        Class domain = Class.forName(fullClassName, false, this.class.classLoader);
        render(ui.domainSelect(domain: domain, key: params.key, text: params.text))
    }

    def changeCurrency() {
        Currency currency = Currency.get(params.currencyId);
        currency.discard()
        session.currency = currency
        render([status: "success", message: g.message(code: "currency.change.success")] as JSON)
    }

    def hierarchySelector() {
        String fullClassName = "com.webcommander." + params.domain;
        Class domain = Class.forName(fullClassName, false, this.class.classLoader);
        render(ui.hierarchicalSelect(domain: domain, key: params.key, text: params.text));
    }

    def loadStateForCountry() {
        Long countryId = params.long("id") ?: 0
        def states = null;
        if (params.id) {
            states = administrationService.getStatesForCountry(countryId);
        }
        render(view: "/admin/customer/stateFormFieldView", model: [states: states]);
    }

    def loadCities() {
        Long stateId = params.long("state")
        String postCode = params.postCode
        String selectedCity = params.city ?: null
        String validation = params.validation
        String fieldName = params.fieldName ?: "city"
        List cities = stateId && postCode ? commonService.getCitiesForStateAndPostCode(stateId, postCode) : []
        Boolean cityExists = stateId ? City.countByState(State.proxy(stateId)) : false
        render(view: "/common/citySelector", model: [cities: cities, selectedCity: selectedCity, validation: validation, cityExists: cityExists, fieldName: fieldName])
    }

    def loadCitiesByCountryOrState() {
        Long stateId = params.long("state")
        Long countryId = params.long("country")
        String postCode = params.postCode
        String selectedCity = params.city ?: null
        String validation = params.validation
        String fieldName = params.cityFieldName ?: "city"
        List cities = []
        if (postCode && countryId) {
            cities = commonService.getCitiesForCountryAndPostCode(countryId, postCode)
        } else if (stateId && postCode) {
            cities = commonService.getCitiesForStateAndPostCode(stateId, postCode)
        }
        Boolean cityExists = false;
        if (countryId) {
            cityExists = City.createCriteria().count {
                createAlias("state", "s", JoinType.LEFT_OUTER_JOIN)
                projections {
                    distinct("name")
                }
                eq("s.country.id", countryId)
            }
        } else if (stateId) {
            cityExists = City.countByState(State.proxy(stateId))
        }
        render(view: "/common/citySelector", model: [cities: cities, selectedCity: selectedCity, validation: validation, cityExists: cityExists, fieldName: fieldName])
    }

    def viewAttachments() {
        Long id = params.long("id")
        int attType = params.int("att_type") ?: 0;
        String hookPrefix = params.type
        Map listMap = [:];
        String opName = params.is_final ? "delete" : "put-trash"
        if (attType == 3) {
            HookManager.hook(hookPrefix + "-$opName-veto-list", listMap, id)
        } else if (attType == 2) {
            HookManager.hook(hookPrefix + "-$opName-at2-list", listMap, id)
        } else if (attType == 1) {
            HookManager.hook(hookPrefix + "-$opName-at1-list", listMap, id)
        }
        render(view: "/admin/common/attachmentList", model: [listMap: listMap.subMap([params.entity])]);
    }

    def siteMessage() {
        if (params.system) {
            render text: g.message(code: params.code)
        } else {
            render text: siteMessageSource.convert(params.code) ?: ""
        }
    }

    def embeddedLoginPopup() {
        render(view: "/admin/embedded_login")
    }

    @RequiresAdmin
    def imageUploadForm() {
        render(view: "/common/imageUploadForm")
    }

    @RequiresAdmin
    def uploadWceditorImage() {
        List<MultipartFile> files = params.list('file[]')
        if(files.size() == 0){
            files = params.list('file')
        }
        Map responses = [:]
        Integer status = HttpStatus.SC_NOT_ACCEPTABLE
        boolean hasError = false
        def type = params['type']
        files.eachWithIndex{ MultipartFile file, int index ->
            if(!hasError){
                if (file.empty) {
                    hasError = true
                    responses.clear()
                    status = HttpStatus.SC_OK
                    responses = [error: true, message: g.message(code: "file.cannot.be.empty")]
                } else if (file.size > 2 * 1024 * 1024) {
                    hasError = true
                    responses.clear()
                    status = HttpStatus.SC_OK
                    responses = [error: true, message: g.message(code: "file.must.less.than", args: ['2MB'])]
                } else if (!file.contentType.startsWith('image')) {
                    hasError = true
                    responses.clear()
                    status = HttpStatus.SC_OK
                    responses = [error: true, message: g.message(code: "file.type.error")]
                } else {
                    String cloudType = NamedConstants.CLOUD_CONFIG.SYSTEM_DEFAULT
                    String fileName = file.originalFilename
                    String fileBaseName = FilenameUtils.getBaseName(fileName), fileExt = FilenameUtils.getExtension(fileName)
                    int i = 1
                    while (fileService.isExistPubResource("${AppResourceTagLib.EDITOR_UPLOADED_IMAGE}/${fileName}", cloudType)) {
                        fileName = "$fileBaseName-$i.$fileExt"
                        i++
                    }
                    fileService.putPubResource(file.getInputStream(), "${AppResourceTagLib.EDITOR_UPLOADED_IMAGE}/${fileName}", cloudType)
                    Map response = [url: appResource.getEditorUploadedImageUrl(resourceName: fileName)]
                    if(type == "redactor") {
                        responses.put('file' + index.toString(), response)
                    }else{
                        responses.put('url', response.url)
                    }
                    status = HttpStatus.SC_OK
                }
            }
        }
        response.setStatus(status)
        render(responses as JSON)
    }


    @RequiresAdmin
    def modifyImage() {
        Map responses = null
        Integer status = HttpStatus.SC_NOT_ACCEPTABLE
        String widgetType = params.widgetType
        def fileName = params.name
        String sourceImageUrl = params.sourceImageUrl;
        String uuid = params.widgetUUId ?: StringUtil.uuid

        String relativePath = appResource.getWidgetTempRelativePath(type: 'image', uuid: uuid)
        String resourcePath = appResource.getImageWidgetTempResourceURL(widgetTempRelativePath: relativePath)
        String filePath = PathManager.getRoot(resourcePath)

        def content = sourceImageUrl.toURL().getBytes();
        File f = new File(filePath);
        if (!f.exists()) {
            f.mkdirs();
        }

        String originalFilePath = "${filePath}/${fileName}";

        File file = new File(originalFilePath);
        file.setBytes(content)

        status = HttpStatus.SC_OK
        responses = [url: resourcePath + fileName , name: fileName, status: status]

        render(responses as JSON)
    }

    @RequiresAdmin
    def uploadWceditorFile(){
        List<MultipartFile> files = params.list('file[]')
        if(files.size() == 0){
            files = params.list('file')
        }
        Map responses = [:]
        Integer status = HttpStatus.SC_NOT_ACCEPTABLE
        boolean hasError = false
        def type = params['type']
        files.eachWithIndex { MultipartFile file, int index ->
            if(!hasError) {
                if (file.empty) {
                    hasError = true
                    responses.clear()
                    status = HttpStatus.SC_OK
                    responses = [status: 'error', message: g.message(code: "file.cannot.be.empty")]
                } else if (file.size > 10 * 1024 * 1024) {
                    hasError = true
                    responses.clear()
                    status = HttpStatus.SC_OK
                    responses = [status: 'error', message: g.message(code: "file.must.less.than", args: ['10MB'])]
                } else if (file.originalFilename.lastIndexOf('.') < 0) {
                    hasError = true
                    responses.clear()
                    status = HttpStatus.SC_OK
                    responses = [status: 'error', message: g.message(code: "file.should.contain.extension")]
                } else {
                    String cloudType = NamedConstants.CLOUD_CONFIG.SYSTEM_DEFAULT
                    String fileName = file.originalFilename
                    String fileBaseName = FilenameUtils.getBaseName(fileName), fileExt = FilenameUtils.getExtension(fileName)
                    int i = 1
                    while (fileService.isExistPubResource("${AppResourceTagLib.EDITOR_UPLOADED_FILE}/${fileName}", cloudType)) {
                        fileName = "$fileBaseName-$i.$fileExt"
                        i++
                    }
                    fileService.putPubResource(file.getInputStream(), "${AppResourceTagLib.EDITOR_UPLOADED_FILE}/${fileName}", cloudType)
                    Map response = [url: appResource.getEditorUploadedFileUrl(resourceName: fileName), name: file.originalFilename]
                    if(type == "redactor") {
                        responses.put('file' + index.toString(), response)
                    }else{
                        responses.put('url', response.url)
                        responses.put('name', fileName)
                    }
                    status = HttpStatus.SC_OK
                }
            }
        }
        response.setStatus(status)
        render(responses as JSON)
    }

    @RequiresAdmin
    def modifyFavoriteWidget() {
        String widgetType = params.widgetType
        if (!widgetType) return;
        widgetType = widgetType.trim();
        List favoriteWidgets = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "favorite_widgets")?.split(",") ?: []
        favoriteWidgets.remove(widgetType)
        if (params.operation == "add") {
            favoriteWidgets.add(widgetType)
        }
        if (configService.update([[type: DomainConstants.SITE_CONFIG_TYPES.GENERAL, configKey: "favorite_widgets", value: favoriteWidgets.join(",")]])) {
            render([status: "success"] as JSON)
        } else {
            render([status: "error", message: g.message(code: "operation.failed")] as JSON)
        }
    }

    @RequiresAdmin
    def modifyFavoriteProductWidget() {
        String widgetType = params.widgetType
        if (!widgetType) return;
        widgetType = widgetType.trim();
        List favoriteWidgets = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "favorite_product_widgets")?.split(",") ?: []
        favoriteWidgets.remove(widgetType)
        if (params.operation == "add") {
            favoriteWidgets.add(widgetType)
        }
        if (configService.update([[type: DomainConstants.SITE_CONFIG_TYPES.GENERAL, configKey: "favorite_product_widgets", value: favoriteWidgets.join(",")]])) {
            render([status: "success"] as JSON)
        } else {
            render([status: "error", message: g.message(code: "operation.failed")] as JSON)
        }
    }

    def wceditorImages() {
        File wcEditorImages = new File(AppUtil.session.servletContext.getRealPath("pub/editor-uploaded-image/"));
        List data = []
        if (wcEditorImages.exists()) {
            wcEditorImages.listFiles().each { File file ->
                if (URLConnection.guessContentTypeFromName(file.getName())?.contains("image")) {
                    String url = app.relativeBaseUrl() + "pub/editor-uploaded-image/${file.name}";
                    data.add([thumb: url, url: url, title: file.name])
                }
            }
        }
        render(data as JSON)
    }

    def wceditorFiles() {
        File wcEditorFiles = new File(AppUtil.session.servletContext.getRealPath("pub/editor-uploaded-file/"));
        List data = []
        if (wcEditorFiles.exists()) {
            wcEditorFiles.listFiles().each { File file ->
                String url = app.relativeBaseUrl() + "pub/editor-uploaded-file/${file.name}";

                data.add([title: "", name: file.name, url: url, size: AppUtil.convertToByteNotation(file.size())])
            }
        }
        render(data as JSON)
    }

    def isRestarted() {
        Long time = params.long("time")
        Long serverStartTime = Holders.servletContext?.start_time;
        render([status: "success", isRestarted: time < serverStartTime] as JSON)
    }

    def isAlive() {
        render text: "I'm Alive";
    }

    def demoTemplate() {
        if (params.color) {
            session.template_preview = false
            session.template_demo = true;
            session.template_demo_color = params.color
        }
        redirect(uri: "/")
    }

    def performOfflineQuery() {
        if (params.token?.trim() == DomainConstants.DEPLOY_TOKEN) {
            Map result = offlineQueryQueueService.performQueries();
            result.status = "success"
            render(result as JSON)
        } else {
            render([status: "error", message: "Invalid secret key"] as JSON)
        }
    }

}
