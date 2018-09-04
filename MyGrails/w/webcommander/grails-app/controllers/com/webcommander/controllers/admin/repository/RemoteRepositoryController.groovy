package com.webcommander.controllers.admin.repository

import com.webcommander.authentication.annotations.Restriction
import com.webcommander.authentication.annotations.Restrictions
import com.webcommander.constants.NamedConstants
import com.webcommander.content.RemoteRepositoryService
import com.webcommander.manager.CloudStorageManager
import com.webcommander.manager.PathManager
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.util.Base64DataInputStream
import com.webcommander.util.AppUtil
import com.webcommander.util.ZipUtil
import grails.converters.JSON
import com.webcommander.admin.Operator
import grails.util.Holders
import groovy.util.slurpersupport.NodeChild
import groovy.util.slurpersupport.NodeChildren
import groovy.xml.StreamingMarkupBuilder
import org.apache.commons.io.FilenameUtils

class RemoteRepositoryController {
    RemoteRepositoryService remoteRepositoryService

    @Restriction(permission = "asset_library.upload.file")
    def doPut() {
        def isBase64 = request.getHeader("base64")
        String repository = remoteRepositoryService.getRelativeRepositoryBase(params.repository)
        String relativePath = params.repository + "/" + params.path
        String realPath = PathManager.getRoot(repository + "/" + params.path)
        def input
        if(isBase64) {
            InputStream inputStream = request.getInputStream()
            input = new Base64DataInputStream(inputStream)
        } else {
            input = request.getInputStream()
        }
        File file = new File(realPath)
        if(file.exists()) {
            remoteRepositoryService.moveToVCS(file, params.repository + "/" + params.path, session.admin)
        }
        checkRepositoryAvailable(repository)
        file.withOutputStream { fos ->
            fos << input
        }
        if(params.fileSize) {
            if(Math.abs(params.fileSize.toLong(0) - file.size()) > 1) {
                deleteFile(file)
            } else {
                remoteRepositoryService.uploadRemoteResource(file, relativePath)
            }
        }
        render([status: 'success', message: g.message(code: "file.save.success")] as JSON)
    }

    private def checkRepositoryAvailable(repository) {
        String realPath = PathManager.getRoot(repository)
        File file = new File(realPath)
        if(!file.exists()) {
            file.mkdirs()
        }
    }

    def download() {
        Map pathInfo = remoteRepositoryService.getPathInfo(params.path)
        String filePath = PathManager.getRoot(pathInfo.baseRepository + "/" + pathInfo.path)
        File file = new File(filePath)
        def ctx = Holders.servletContext
        def remoteStream = CloudStorageManager.getDataStream(params.path, NamedConstants.CLOUD_CONFIG.SYSTEM_DEFAULT)
        InputStream fis = remoteStream ?: new FileInputStream(file)
        String mimeType = ctx.getMimeType(file.getAbsolutePath())
        response.setContentType(mimeType != null? mimeType:"application/octet-stream")
        response.setContentLength((int) file.length())
        response.setHeader("Content-Disposition", "attachment; filename=\"" + file.name + "\"")
        def os = response.getOutputStream()
        byte[] bufferData = new byte[1024]
        int read
        while((read = fis.read(bufferData))!= -1){
            os.write(bufferData, 0, read)
        }
        os.flush()
        os.close()
        fis.close()
    }

    @Restriction(permission = "asset_library.create.directory", params_match_key = "repository", params_match_value = "pub")
    def doMkdir() {
        String repository = remoteRepositoryService.getRelativeRepositoryBase(params.repository)
        String filePath = PathManager.getRoot(repository + "/" + params.path)
        File file = new File(filePath)
        if(file.mkdirs()) {
            render([status: 'success',  message: g.message(code: "directory.create.success")] as JSON)
        } else {
            render([status: 'error',  message: g.message(code: "directory.create.failure")] as JSON)
        }
    }

    @Restrictions([
         @Restriction(permission = "asset_library.remove.file", params_match_key = "repository", params_match_value = "pub"),
         @Restriction(permission = "asset_library.remove.directory", params_match_key = "repository", params_match_value = "pub")
    ])
    def doDelete() {
        String repository = remoteRepositoryService.getRelativeRepositoryBase(params.repository)
        String filePath = PathManager.getRoot repository + "/" + params.path
        File file = new File(filePath)
        String code = file.isDirectory()? "directory.delete.success" : "file.delete.success"
        if(file.exists()) {
            deleteFile(file)
        }
        String sourcePath = params.repository + "/" + params.path
        CloudStorageManager.deleteData(sourcePath, NamedConstants.CLOUD_CONFIG.SYSTEM_DEFAULT)
        render([status: 'success',  message: g.message(code: code)] as JSON)
    }

    def private deleteFile(File file){
        file.listFiles().each {
            deleteFile(it)
        }
        file.delete()
    }

    @Restriction(permission = "asset_library.rename.file.directory", params_match_key = "repository", params_match_value = "pub")
    def doMove() {
        String repository = remoteRepositoryService.getRelativeRepositoryBase(params.repository)
        String filePath = PathManager.getRoot repository + "/" + params.path
        String sourcePath = params.repository + "/" + params.path
        String destination = request.getHeader("destination")
        def pathInfo = remoteRepositoryService.getPathInfo(destination)
        String parentPath = PathManager.getRoot repository + "/" + pathInfo.parent

        String message
        Closure cloudUpdate = {
            def copyCloud = CloudStorageManager.copyData(sourcePath, destination, NamedConstants.CLOUD_CONFIG.SYSTEM_DEFAULT)
            if(copyCloud != null) {
                CloudStorageManager.deleteData(sourcePath, NamedConstants.CLOUD_CONFIG.SYSTEM_DEFAULT)
                if(FilenameUtils.getName(sourcePath) != FilenameUtils.getName(destination)) {
                    message = (!FilenameUtils.getName(destination).contains(".")) ? "directory.rename.success" : "file.rename.success"
                } else {
                    message = (!FilenameUtils.getName(destination).contains(".")) ? "directory.move.success" : "file.move.success"
                }
            }
        }

        if (new File(parentPath).exists()) {
            String targetPath = PathManager.getRoot repository + "/" + pathInfo.path
            File file = new File(filePath)
            if(file.exists()) {
                File destFile = new File(targetPath)
                if(destFile.exists() && targetPath != filePath){
                    remoteRepositoryService.moveToVCS(destFile, destination, session.admin)
                }
                file.renameTo(new File(targetPath))
                if(file.getName() != destFile.getName()) {
                    message = destFile.isDirectory() ? "directory.rename.success" : "file.rename.success"
                } else {
                    message = destFile.isDirectory() ? "directory.move.success" : "file.move.success"
                }
            }
            cloudUpdate()
        } else {
            cloudUpdate()
            throw new ApplicationRuntimeException("parent.directory.not.exists")
        }

        render([status: 'success',  message: g.message(code: message)] as JSON)
    }

    def doPropfind() {
        int depth = request.getHeader("depth").toInteger()
        NodeChild xmlProperties = request.XML
        NodeChildren properties = xmlProperties.prop
        def mapProperties = []
        properties.childNodes().each {
            mapProperties.add(it.@name.toString())
        }
        def xmlBuilder = new StreamingMarkupBuilder()
        String repositoryBase = remoteRepositoryService.getRelativeRepositoryBase(params.repository)
        String filePath = params.path ? ("/" + params.path) : ""
        String realPath = PathManager.getRoot repositoryBase + filePath
        File file = new File(realPath)
        def href = app.baseUrl() + params.repository + filePath
        if(file.exists()) {
            def xmlString = xmlBuilder.bind{
                mkp.xmlDeclaration()
                mkp.declareNamespace(D: 'DAV:')
                'D:multistat'{
                    'D:response'{
                        'D:href'(href)
                        'D:propstat'{
                            'D:prop'{
                                'D:displayname'(file.name)
                                if(file.isDirectory()) {
                                    'D:resourcetype'{
                                        'D:collection'()
                                    }
                                } else {
                                    'D:resourcetype'()
                                }
                            }
                            'D:status'("HTTP/1.1 200 OK")
                        }
                    }
                    if(depth == 1 && !file.isFile()){
                        def files = file.listFiles()
                        files.each { f ->
                            'D:response'{
                                'D:href'(href + "/" + f.name)
                                'D:propstat'{
                                    'D:prop'{
                                        'D:displayname'(f.name)
                                        if(f.isDirectory()) {
                                            'D:resourcetype'{
                                                'D:collection'()
                                            }
                                            int folderCount = 0
                                            if(f.list().length) {
                                                f.listFiles().each{
                                                    if(!it.isFile()) {
                                                        folderCount++
                                                    }
                                                }
                                            }
                                            'D:getfoldercount'(folderCount)
                                        } else {
                                            'D:resourcetype'()
                                        }
                                    }
                                    'D:status'("HTTP/1.1 200 OK")
                                }
                            }
                        }
                    }
                }
            }
            render(text:xmlString, contentType: "text/xml")
        } else {
            render([status: 'error'] as JSON)
        }
    }

    def showRevision() {
        def filePathInfo = remoteRepositoryService.getPathInfo(params.path)
        String realPath = PathManager.getRoot filePathInfo.vcsRepository + "/" + filePathInfo.parent
        File file = new File(realPath)
        def files = []
        def totalUsage = 0
        if(file.exists()) {
            def fileList = file.listFiles()
            fileList.each {
                def f = [:]
                List<String> fileInfo = it.name.split("-")
                if(fileInfo.size() == 3) {
                    String time = fileInfo.remove(0)
                    String id = fileInfo.remove(0)
                    f.originalFileName = fileInfo.join("-")
                    if(params.name == f.originalFileName){
                        f.time = new Date(time.toLong()).gmt().toAdminFormat(true, false, session.timezone)
                        f.user = Operator.get(id.toLong()).fullName
                        f.size = AppUtil.convertToByteNotation(it.size())
                        f.path = filePathInfo.repository + "/" + filePathInfo.parent  + "/" + it.name
                        totalUsage = totalUsage + it.size()
                        files.add(f)
                    }
                }
            }
        }
        String total = AppUtil.convertToByteNotation(totalUsage)
        render(view: "/admin/assetLibrary/revisions", model: [files: files, total: total, filePath: params.path])
    }

    def replaceWithRevision() {
        def originalFilePathInfo = remoteRepositoryService.getPathInfo(params.originalFilePath)
        String originalFilePath = originalFilePathInfo.baseRepository + "/" + originalFilePathInfo.path
        String realPath = PathManager.getRoot originalFilePath
        File originalFile = new File(realPath)
        remoteRepositoryService.moveToVCS(originalFile, params.originalFilePath, session.admin)
        def replacingFilePathInfo = remoteRepositoryService.getPathInfo(params.filePath)
        String realReplacePath = PathManager.getRoot replacingFilePathInfo.vcsRepository + "/" + replacingFilePathInfo.path
        File file = new File(realReplacePath)
        file.withInputStream { it ->
            originalFile << it
        }
        render([status: 'success', message: g.message(code: "file.replaced.success")] as JSON)
    }

    def clearAllRevisions() {
        def filePathInfo = remoteRepositoryService.getPathInfo(params.filePath)
        String realPath = PathManager.getRoot filePathInfo.vcsRepository + "/" + filePathInfo.parent
        File file = new File(realPath)
        if(file.exists()) {
            def fileList = file.listFiles()
            fileList.each {
                def f = [:]
                List<String> fileInfo = it.name.split("-")
                String time = fileInfo.remove(0)
                String id = fileInfo.remove(0)
                f.originalFileName = fileInfo.join("-")
                if(params.name == f.originalFileName){
                    it.delete()
                }
            }
        }
        render([status: 'success', message: g.message(code: "all.revisions.have.been.cleared")] as JSON)
    }

    def extract() {
        Boolean result = false
        try {
            String path = PathManager.getRoot params.filePath
            File file = new File(path)
            if (file.exists() && file.isFile()) {
                ZipUtil.extract(new FileInputStream(file), file.parent)
                result = true
            }
        } catch (Exception ex) {
            result = false
        }
        if(result) {
            render([status: "success", message: g.message(code: "file.extract.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "file.extract.failure")] as JSON)
        }
    }

    def selectFileFromAssetLibraryForWidget() {
        render(view: "/admin/assetLibrary/loadAssetLibrary", model: [d: 0])
    }

    def isFileExists() {
        String path = PathManager.getRoot params.filePath
        File file = new File(path, params.fileName)
        render([status: "success", isExists: file.exists()] as JSON)
    }
}