package com.webcommander.content

import com.webcommander.AppResourceTagLib
import com.webcommander.ApplicationTagLib
import com.webcommander.common.CommonService
import com.webcommander.constants.DomainConstants
import com.webcommander.events.AppEventManager
import com.webcommander.log.WcLogManager
import com.webcommander.parser.EmailTemplateParser
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.util.AppUtil
import com.webcommander.util.JavaShell.ShellRunner
import com.webcommander.util.TrashUtil
import grails.gorm.transactions.NotTransactional
import grails.gorm.transactions.Transactional
import grails.web.context.ServletContextHolder
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

import javax.activation.MimetypesFileTypeMap
import javax.servlet.ServletContext
import java.nio.charset.StandardCharsets

@Transactional
class DocumentService {

    CommonService commonService
    @Autowired
    @Qualifier("com.webcommander.AppResourceTagLib")
    AppResourceTagLib appResource

    private Closure getCriteriaClosure(GrailsParameterMap params) {
        return {
            if (params.searchText) {
                ilike("name", "%${params.searchText.trim().encodeAsLikeText()}%")
            }
            eq("isLayout", false)
        }
    }

    @NotTransactional
    int getDocumentCount(GrailsParameterMap params) {
        return Document.createCriteria().count {
            criteria getCriteriaClosure(params)
        }
    }

    @NotTransactional
    List<Document> getDocuments(GrailsParameterMap params) {
        def listMap = [max: params.max, offset: params.offset]
        return Document.createCriteria().list(listMap) {
            criteria getCriteriaClosure(params)
            order(params.sort ?: "name", params.dir ?: "asc")
        }
    }

    @NotTransactional
    List<Document> getLayouts() {
        return Document.createCriteria().list() {
            eq("isLayout", true)
            order("type", "asc")
        }
    }

    def save(Map params) {
        Document document;
        if(params.layoutUsed.toBoolean() && params.id) {
            document = new Document()
        } else if(params.id) {
            document = Document.load(params.id)
        } else {
            document = new Document()
        }
        document.name = params.name
        document.description = params.description
        if(params.type) {
            document.type = params.type
        }
        if(params.content) {
            document.content = params.content
        }
        document.save()
        return document
    }

    Boolean delete(Long id, String at1, String at2) {
        Document document = Document.load(id)
        def _g = AppUtil.getBean(ApplicationTagLib)
        if(document.isLayout) {
            throw new ApplicationRuntimeException("" + _g.message(code: 'system.generated.document'))
        }
        if(document.active) {
            throw new ApplicationRuntimeException("" + _g.message(code: 'active.document.connot.delete'))
        }
        TrashUtil.preProcessFinalDelete("document", id, at2 != null, at1 != null);
        AppEventManager.fire("before-document-delete", [id])
        document.delete()
        AppEventManager.fire("document-delete", [id])
        return !document.hasErrors()
    }

    Boolean deleteSelected(List<Long> ids) {
        List<Document> documents = []
        def _g = AppUtil.getBean(ApplicationTagLib)
        try {
            ids.each {
                Document document = Document.get(it)
                if(document.isLayout) {
                    throw new ApplicationRuntimeException("" + _g.message(code: 'system.generated.document'))
                }
                if(document.active) {
                    throw new ApplicationRuntimeException("" + _g.message(code: 'active.document.connot.delete'))
                }
                documents.add(document)
            }
            documents.each {
                it.delete()
            }
            return true
        } catch (Exception ex) {
            return false
        }
    }

    Document copy(Map params) {
        Document orginal = Document.get(params.id)
        Document copy = new Document()

        copy.name = commonService.getCopyNameForDomain(orginal)
        copy.type = orginal.type
        copy.description = orginal.description
        copy.content = orginal.content

        return copy.save()
    }

    Document setActivetemplate(Map params) {
        Document document = Document.get(params.id)
        Document.where {
            id != document.id
            type == document.type
        }.updateAll(active: false)
        document.active = true
        return document.save(flush: true)
    }

    @NotTransactional
    Map getPdfAttachment(String identifire, String attachmentName, Map macros = [:], Boolean testMode = false) {
        Map attachment = [:]
        Document document = Document.findByTypeAndActive(DomainConstants.DOCUMENT_MAPPING[identifire], true)
        if(!document) {
            return attachment
        }
        ServletContext context = ServletContextHolder.servletContext
        String path = appResource.getResourcePhysicalPath(extension: "${appResource.TEMP}/${appResource.DOCUMENT}/")
        File html = null, pdf = null
        try {
            new File(path).mkdirs()
            File exPdf = new File(path + attachmentName + ".pdf")
            File exHtml = new File(path + attachmentName + ".html")
            if(exPdf.exists()) {
                exPdf.delete()
            }
            if(exHtml.exists()) {
                exHtml.delete()
            }
            html = new File(path + attachmentName + ".html")
            pdf = new File(path + attachmentName + ".pdf")

            String resultHtml = EmailTemplateParser.parse(document.content, macros, new StringBuilder(), true)
            html << resultHtml
            String command = "/usr/bin/xvfb-run /usr/local/bin/wkhtmltopdf ${html.path} ${pdf.path}".toString()
            ShellRunner.exeCuteCommand(command)
            if(pdf.exists()) {
                attachment.name = pdf.name
                attachment.contentType = new MimetypesFileTypeMap().getContentType(pdf)
                attachment.byte = pdf.getBytes()
            }
        } catch (Exception ex) {
            ex.printStackTrace()
        } finally {
            html.delete()
            pdf.delete()
        }
        return attachment
    }

    @NotTransactional
    def getPdfData(String identifire, String attachmentName, Map macros = [:], Boolean testMode = false) {
        Map attachment = [:]
        Document document = Document.findByTypeAndActive(identifire, true)
        if(!document) {
            return attachment
        }
        String path = appResource.getResourcePhysicalPath(extension: "${appResource.TEMP}/${appResource.DOCUMENT}/")
        File html = null, pdf = null
        try {
            new File(path).mkdirs()
            File exPdf = new File(path + attachmentName + ".pdf")
            File exHtml = new File(path + attachmentName + ".html")
            if(exPdf.exists()) {
                exPdf.delete()
            }
            if(exHtml.exists()) {
                exHtml.delete()
            }
            html = new File(path + attachmentName + ".html")
            pdf = new File(path + attachmentName + ".pdf")

            String resultHtml = EmailTemplateParser.parse(document.content, macros, new StringBuilder(), true)
            html << resultHtml
            println("HTML_PATH " + html.path)
            println("PDF_PATH " + pdf.path)
//            String command = System.getenv("WK_HTML_TO_PDF") + File.separator + "wkhtmltopdf ${html.path} ${pdf.path}".toString()
            //String command = "/usr/local/bin/wkhtmltopdf ${html.path} ${pdf.path}".toString()
            String command = "/usr/bin/xvfb-run /usr/local/bin/wkhtmltopdf ${html.path} ${pdf.path}".toString()
            //Process process = Runtime.getRuntime().exec(command);

            ShellRunner.exeCuteCommand(command)

            println("PDF_GENERATED " + pdf.exists())

            if(pdf.exists()) {
                attachment.name = pdf.name
                attachment.contentType = new MimetypesFileTypeMap().getContentType(pdf)
                attachment.byte = pdf.getBytes()
            }
        } catch (Exception ex) {
            ex.printStackTrace()
        } finally {
            //html.delete()
            pdf.delete()
        }
        return attachment
    }
}