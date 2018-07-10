package com.webcommander.controllers.admin.content

import com.webcommander.common.CommonService
import com.webcommander.content.RemoteRepositoryService
import grails.converters.JSON
import grails.util.Holders
import org.apache.commons.io.FilenameUtils

class AssetLibraryController {
    CommonService commonService
    RemoteRepositoryService remoteRepositoryService

    def loadExplorerView() {
        render(view: "/admin/assetLibrary/explorerView", model: [d: true])
    }

    def explorerPanel() {
        def filter = [:]
        params.max = params.max?.toInteger() ?: 10
        params.offset = params.offset?.toInteger() ?: 0
        filter.searchText = params.searchText
        filter.hasAnyFolders = true
        String repo = params.repository ?: "pub"
        String path = remoteRepositoryService.getRelativeRepositoryBase(repo)
        String parentPath
        if (params.id) {
            path = path + params.id.substring(params.id.indexOf("/"))
            parentPath = params.id
        } else {
            parentPath = repo
        }
        String realPath = Holders.servletContext.getRealPath(path)
        File file = new File(realPath)
        String relativePath = parentPath + "/"
        Map remoteData = remoteRepositoryService.getFolderMap(relativePath)
        def folders = remoteData ? remoteData.folders : retrieveChildFolders(parentPath, file, filter)
        def files = remoteData ? remoteData.files : retrieveChildFiles(parentPath, file, filter)
        Integer folderCount = folders.size()
        Integer count = folders.size() + files.size()
        if (folders.size() > params.offset) {
            folders = commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
                offset = offset < 0 ? 0 : offset
                params.offset = offset
                params.max = max
                max = max + offset
                if (max > folders.size() || max == -1) {
                    max = folders.size()
                }
                return folders[offset..<max]
            }
        } else {
            folders = []
        }
        if (folders.size() < params.max || params.max == -1) {
            files = commonService.withOffset(params.max - folders.size(), params.offset - folderCount, count) { max, offset, _count ->
                offset = offset < 0 ? 0 : offset
                max = max + offset
                if (max > files.size() || max < 0) {
                    max = files.size()
                }
                return files[offset..<max]
            }
        } else {
            files = []
        }
        render(view: "/admin/assetLibrary/explorerPanel", model: [folders: folders, files: files, count: count])
    }

    private def retrieveChildFiles(String parentPath, File file, Map filter) {
        def newChildren = []
        if (file.exists()) {
            def children = file.listFiles()
            children.each {
                if (it.isFile()) {
                    String extension = FilenameUtils.getExtension(it.name)
                    def child = [
                            path   : parentPath + "/" + it.name,
                            name   : it.name,
                            parent : parentPath,
                            type   : "file",
                            size   : it.size(),
                            clazz  : extension,
                            isLocal: false
                    ]
                    if (filter.searchText) {
                        if (child.name.toLowerCase().contains(filter.searchText.toLowerCase())) {
                            newChildren.add(child)
                        }
                    } else {
                        newChildren.add(child)
                    }
                }
            }
        }
        return newChildren
    }

    private def retrieveChildFolders(String parentPath, File file, Map filter) {
        def newChildren = []
        if (file.exists()) {
            def children = file.listFiles()
            children.each {
                if (it.isDirectory()) {
                    def child = [
                            path    : parentPath + "/" + it.name,
                            name    : it.name,
                            parent  : parentPath,
                            hasChild: hasChild(it, filter.hasAnyFolders),
                            type    : "folder",
                            isLocal : false
                    ]
                    if (filter.searchText) {
                        if (child.name.toLowerCase().contains(filter.searchText.toLowerCase())) {
                            newChildren.add(child)
                        }
                    } else {
                        newChildren.add(child)
                    }
                }
            }
        }
        return newChildren
    }

    private Boolean hasChild(File file, def hasAnyFolders) {
        if (hasAnyFolders) {
            Boolean hasChild = false
            file.listFiles().each {
                if (!it.isFile()) {
                    hasChild = true
                    return true
                }
            }
            return hasChild
        } else {
            return file.list().length > 0
        }
    }

    def loadCssEditor() {
        render(view: "/admin/template/loadCssEditor")
    }

    def loadCodeEditor() {
        render(view: "/admin/template/loadCodeEditor")
    }

    def loadSimpleXmlEditor() {
        render(view: "/admin/template/loadSimpleXmlEditor")
    }

    def loadAdvanceXmlEditor() {
        render(view: "/admin/template/loadAdvanceXmlEditor")
    }

    def loadScriptEditor() {
        render(view: "/admin/template/loadScriptEditor", model: [mode: params.mode])
    }

    def restore() {
        def templateFolder = Holders.servletContext.getRealPath("vcs/template")
        if (params.version) {
            def cssContent = new File(templateFolder + File.separator + "/css" + File.separator + params.version).text
            render([content: cssContent] as JSON)
        } else {
            File dir = new File(templateFolder + File.separator + "/css")
            if (dir.exists()) {
                String[] versions = dir.list().sort()
                render(view: "/admin/template/cssRestore", model: [versions: versions])
            } else {
                render([status: 'error', message: 'none'] as JSON)
            }
        }
    }

}
