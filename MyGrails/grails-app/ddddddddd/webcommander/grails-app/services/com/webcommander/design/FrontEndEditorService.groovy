package com.webcommander.design

import com.webcommander.AppResourceTagLib
import com.webcommander.Page
import com.webcommander.common.ImageService
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.content.ContentService
import com.webcommander.content.PageService
import com.webcommander.events.AppEventManager
import com.webcommander.listener.SessionManager
import com.webcommander.manager.CloudStorageManager
import com.webcommander.manager.PathManager
import com.webcommander.models.MockStaticResource
import com.webcommander.util.AppUtil
import com.webcommander.util.FileUtil
import com.webcommander.widget.Widget
import com.webcommander.widget.WidgetContent
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.util.Holders
import grails.web.servlet.mvc.GrailsParameterMap
import org.apache.commons.io.FileUtils
import org.apache.poi.hwpf.HWPFDocumentCore
import org.apache.poi.hwpf.converter.WordToHtmlConverter
import org.apache.poi.hwpf.converter.WordToHtmlUtils
import org.apache.poi.xwpf.converter.xhtml.XHTMLConverter
import org.apache.poi.xwpf.converter.xhtml.XHTMLOptions
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.MessageSource
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.multipart.MultipartFile
import org.w3c.dom.Document

import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

@Transactional
class FrontEndEditorService {
    ImageService imageService
    MessageSource messageSource
    WidgetService widgetService
    ContentService contentService
    PageService pageService

    @Autowired
    @Qualifier("com.webcommander.AppResourceTagLib")
    com.webcommander.AppResourceTagLib appResource

    @Autowired
    @Qualifier("com.webcommander.ApplicationTagLib")
    com.webcommander.ApplicationTagLib appLib

    Widget saveWidget(GrailsParameterMap params) {
        Widget widget
        def widgetId = params.long("widgetId")
        if (widgetId) {
            widget = Widget.get(widgetId)
            widget.widgetContent.retainAll([])
        } else {
            widget = new Widget(uuid: params.uuid, widgetType: params.widgetType)
            widget.widgetContent = []
        }
        widget.title = params.title ?: ''
        widget.containerType = params.containerType
        widget.containerId = params.long("containerId")
        if (this.respondsTo("beforeSave${widget.widgetType.capitalize()}Widget")) {
            this."beforeSave${widget.widgetType.capitalize()}Widget"(widget, params)
        }
        widgetService."save${widget.widgetType.capitalize()}Widget"(widget, params)
        if (this.respondsTo("afterSave${widget.widgetType.capitalize()}Widget")) {
            this."afterSave${widget.widgetType.capitalize()}Widget"(widget, params)
        }
        widget.discard()
        widget.widgetContent*.discard()
        return widget
    }

    Widget saveImageWidget(GrailsParameterMap params, def request, def session, def baseUrl) {
        params.upload_type = "local"
        AppEventManager.off("widget-" + params.uuid + "-before-save")
        if (params.upload_type == "local") {
            MultipartFile uploadedImage = request.getFile('localImage')
            if (uploadedImage?.originalFilename) {
                String originalName = uploadedImage.originalFilename;
                String filePath = appResource.getWidgetTempRelativePath(type: 'image', uuid: params.uuid)
                MockStaticResource mockResource = new MockStaticResource(relativeUrl: filePath, resourceName: originalName)
                imageService.uploadImage(uploadedImage, NamedConstants.IMAGE_RESIZE_TYPE.IMAGE_WIDGET, mockResource);
                uploadWidgetLocalResource(params, originalName, session)
            }
        } else {
            AppEventManager.off("widget-" + params.uuid + "-before-save")
            AppEventManager.one("widget-" + params.uuid + "-before-save", "session-" + session.id, { widget ->
                File resource = new File(Holders.servletContext.getRealPath("${appResource.getWidgetRelativeUrl(uuid: widget.uuid, type: 'image')}"))
                if (resource.exists()) {
                    resource.deleteDir()
                }
                CloudStorageManager.deleteData("${appResource.getWidgetCloudRelativeUrl(uuid: widget.uuid, type: 'image')}", NamedConstants.CLOUD_CONFIG.SYSTEM_DEFAULT)
            })
        }
        def result = widgetService.saveWidget("Image", params)
        return result
    }


    private void uploadWidgetLocalResource(Map params, String originalName, def session) {
        String relativeResourcePath = appResource.getWidgetTempPath(type: params.widgetType, uuid: params.uuid)
        params.local_url = relativeResourcePath + originalName
        AppEventManager.off("widget-" + params.uuid + "-before-save")
        AppEventManager.one("widget-" + params.uuid + "-before-save", "session-" + session.id, { widget ->
            String relativeUrl = "${appResource.getWidgetRelativeUrl(uuid: widget.uuid, type: widget.widgetType)}"
            String modifiedLocalUrl = relativeUrl + originalName
            File targetFile = new File(Holders.servletContext.getRealPath(relativeUrl))
            if (targetFile.exists()) {
                targetFile.deleteDir()
            }
            if (!targetFile.parentFile.exists()) {
                targetFile.parentFile.mkdirs()
            }
            File sourceFile = new File(Holders.servletContext.getRealPath(relativeResourcePath))
            if (sourceFile.exists()) {
                FileUtil.move(sourceFile, targetFile)
                String uploadLocation = appResource.getWidgetCloudRelativeUrl(uuid: widget.uuid, type: widget.widgetType)
                CloudStorageManager.uploadData(new File(targetFile, originalName), NamedConstants.CLOUD_CONFIG.SYSTEM_DEFAULT, uploadLocation + originalName)
            }
            Map _params = JSON.parse widget.params
            widget.content = appLib.customResourceBaseUrl() + modifiedLocalUrl
            widget.params = _params;
        })
    }


    Widget saveNavigationWidget(GrailsParameterMap params) {
        def widgetId = params.long("widgetId")
        Widget widget = Widget.get(widgetId)
        if (widget) {
            widget.title = params.title ?: ''
            widget.params = ([
                    orientation: params.orientation ?: "V",
                    showImage  : 'hide'
            ] as JSON).toString()
            if (widget.widgetContent && widget.widgetContent.size() > 0) {
                widget.widgetContent.each {
                    widgetContentIns ->
                        widgetContentIns.contentId = params.int("navigationId")
                }
            } else {
                WidgetContent widgetContent = new WidgetContent(contentId: params.int("navigationId"))
                widgetContent.widget = widget
                widgetContent.type = DomainConstants.WIDGET_CONTENT_TYPE.NAVIGATION
                widget.widgetContent.add(widgetContent)
            }
            widget.save()
            return widget
        } else {
            widget = new Widget(widgetType: params.widgetType, widgetContent: [], uuid: params.uuid, containerType: params.containerType, containerId: params.long("containerId"))
            widget.params = ([
                    orientation: params.orientation ?: "V",
                    showImage  : 'hide'
            ] as JSON).toString()
            widget.title = params.title ?: ''
            WidgetContent widgetContent = new WidgetContent(contentId: params.int("navigationId"))
            widgetContent.widget = widget
            widgetContent.type = DomainConstants.WIDGET_CONTENT_TYPE.NAVIGATION
            widget.widgetContent.add(widgetContent)
            widget.discard()
            widget.widgetContent*.discard()
            return widget
        }
    }

    private def beforeSaveArticleWidget(widget, params) {
        if (params.articles) {
            params.article = "${params.articles ?: ''}".split(",")
        }
        if (params.new && params.new.name) {
            if (params.selectedArticleId) {
                params.new.id = params.selectedArticleId
            }
            def session = RequestContextHolder.currentRequestAttributes().getSession()
            params.new.isPublished = "true"
            def article = contentService.saveArticle(params.new, session.admin)
            params.article = article?.id
        }
    }

    private def beforeSaveImageWidget(widget, params) {
        widget.title = params?.title ?: ''
        if (!params.local_url && widget.id > 0) {
            params.local_url = widget.content
        }
    }

    private def afterSaveImageWidget(Widget widget, GrailsParameterMap params) {
        if (params.imageParams) {
            String modifiedLocalUrl = "resources/image-widget/" + widget.uuid + "/" + params.imageParams.originalName
            File targetFile = new File(Holders.servletContext.getRealPath("resources") + "/image-widget/" + widget.uuid + "/")
            if (targetFile.exists()) {
                targetFile.deleteDir()
            }
            if (!targetFile.parentFile.exists()) {
                targetFile.parentFile.mkdirs()
            }
            File sourceFile = new File(params.imageParams.filePath)
            if (sourceFile.exists()) {
                FileUtils.moveDirectory(sourceFile, targetFile)
            }
            widget.content = params.imageParams.baseUrl + modifiedLocalUrl
        }
    }

    Map uploadFileAndGetContent(def params, def request) {
        Map response = [type: "error", message: ""]
        MultipartFile uploadedFile = request.getFile('content')
        if (!uploadedFile) {
            return response
        }
        String tempLocation = SessionManager.getTempFolder().absolutePath + File.separator
        InputStream uploadedStream = uploadedFile.inputStream
        String fileLocation = tempLocation + UUID.randomUUID().toString().substring(0, 5) + "-" + uploadedFile.getOriginalFilename()
        OutputStream out = new FileOutputStream(fileLocation)
        out << uploadedStream
        out.close()
        uploadedStream.close()
        File file = new File(fileLocation)
        switch (uploadedFile.contentType) {
            case "text/plain":
                response.type = "success"
                response.message = file.text
                return response
            case "application/msword":
                try {
                    HWPFDocumentCore wordDocument = WordToHtmlUtils.loadDoc(new FileInputStream("C:\\tmp\\word\\word.doc"))

                    WordToHtmlConverter wordToHtmlConverter = new WordToHtmlConverter(
                            DocumentBuilderFactory.newInstance().newDocumentBuilder()
                                    .newDocument())
                    wordToHtmlConverter.processDocument(wordDocument)
                    Document htmlDocument = wordToHtmlConverter.getDocument()
                    ByteArrayOutputStream outxxx = new ByteArrayOutputStream()
                    DOMSource domSource = new DOMSource(htmlDocument)
                    StreamResult streamResult = new StreamResult(outxxx)

                    TransformerFactory tf = TransformerFactory.newInstance()
                    Transformer serializer = tf.newTransformer()
                    serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8")
                    serializer.setOutputProperty(OutputKeys.INDENT, "yes")
                    serializer.setOutputProperty(OutputKeys.METHOD, "html")
                    serializer.transform(domSource, streamResult)
                    outxxx.close()

                    response.type = "success"
                    response.message = new String(outxxx.toByteArray())
                    return response
                }
                catch (Exception ex) {
                    log.error(ex.getMessage(), ex)
                }
                response.message = "Unable to parse document"
                return response
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
                try {
                    InputStream docx = new FileInputStream(new File(fileLocation))
                    XWPFDocument document = new XWPFDocument(docx)
                    XHTMLOptions options = XHTMLOptions.create()
                    String htmlFileLocation = tempLocation + UUID.randomUUID().toString() + ".html"
                    OutputStream outx = new FileOutputStream(new File(htmlFileLocation))
                    XHTMLConverter.getInstance().convert(document, outx, options)

                    response.type = "success"
                    response.message = (new File(htmlFileLocation)).text
                    return response
                }
                catch (Exception ex) {
                    log.error(ex.getMessage(), ex)
                }

                response.message = messageSource.getMessage("unable.to.parse.document", [] as Object[], Locale.getDefault())
                return response
            default:
                response.message = messageSource.getMessage("file.type.not.supported.only.text.pdf.doc.file.supports", [] as Object[], Locale.getDefault())
                return response
        }
    }

    def deletePageFromTrash(Page page, def params){
        boolean deleteStatus
        AppEventManager.fire("before-page-delete", [page.id])
        deleteStatus = pageService.deleteTrashItemAndSaveCurrent("name", params.name)
        AppEventManager.fire("page-delete", [page.id])
        return deleteStatus
    }
}