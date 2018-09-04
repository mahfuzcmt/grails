package com.webcommander.content

import com.webcommander.constants.NamedConstants
import com.webcommander.manager.CloudStorageManager
import com.webcommander.manager.PathManager
import com.webcommander.models.MockStaticResource
import com.webcommander.models.blueprints.AbstractStaticResource
import com.webcommander.tenant.TenantContext
import com.webcommander.webcommerce.CloudConfig
import grails.gorm.transactions.Transactional
import org.apache.commons.collections.map.HashedMap

class RemoteRepositoryService {

    def moveToVCS(File file, String path, user) {
        def pathInfo = getPathInfo(path)
        File vcsFolder = new File(pathInfo.vcsFullpath)
        if(!vcsFolder.exists()) {
            vcsFolder.mkdirs()
        }
        String time = new Date().getTime().toString()
        File vcsFile = new File(vcsFolder.parentFile, time + "-" + user + "-" + file.getName())
        file.renameTo(vcsFile)
    }

    String getRepositoryBase(String repository) {
        PathManager.getRoot(getRelativeRepositoryBase(repository))
    }

    String getRelativeRepositoryBase(String repository) {
        switch (repository) {
            case "pub" :
                return "pub/" + TenantContext.currentTenant
            case "template" :
                return "template/" + TenantContext.currentTenant
        }
    }

    String getRelativeRepositoryVCSBase(String repository) {
        switch (repository){
            case "pub" :
                return "WEB-INF/vcs/pub/" + TenantContext.currentTenant
            case "template" :
                return "WEB-INF/vcs/template/" + TenantContext.currentTenant
        }
    }

    Map getPathInfo(String path) {
        path = path.replace("\\", "/")
        List<String> list = path.split("/")
        List<String> parentPaths = list[1..< list.size() - 1]
        String parent = parentPaths.join("/")
        String repository = list.remove(0)
        String filePath = list.join("/")
        String vcsRepository = getRelativeRepositoryVCSBase(repository)
        String baseRepository = getRelativeRepositoryBase(repository)
        return [
            repository: repository,
            path: filePath,
            fullpath: PathManager.getRoot(baseRepository),
            vcsFullpath: PathManager.getRoot(vcsRepository),
            parent: parent,
            vcsRepository: vcsRepository,
            baseRepository: baseRepository
        ]
    }

    @Transactional
    void uploadRemoteResource(File targetFile, String resourceRelativeUrl) {
        try {
            CloudStorageManager.uploadData(targetFile, NamedConstants.CLOUD_CONFIG.SYSTEM_DEFAULT, resourceRelativeUrl)
        } catch (Exception ignore) {
            log.error(ignore.message)
        }
    }

    Map getFolderMap(String path, String type = NamedConstants.CLOUD_CONFIG.SYSTEM_DEFAULT) {
        List urls = CloudStorageManager.getFileList(path, type)
        if(urls == null) {
            return null
        }
        Map dirData = [:]
        List folders = dirData.folders = []
        List files = dirData.files = []

        String tempPath = path
        urls.each {
            Boolean valid = true
            String url = it
            String[] dirs = url.split("/")
            Map data = [
                path    : "",
                name    : "",
                parent  : "",
                hasChild: false,
                type    : "folder",
                isLocal : false
            ]
            String _path = url.substring(tempPath.length(), url.length())
            if(_path.contains("/")) {
                data.path = tempPath + _path.substring(0, _path.indexOf("/"))
                data.name = data.path.substring(data.path.lastIndexOf("/") + 1, data.path.length())
                data.parent = data.path.substring(0, data.path.lastIndexOf("/" + data.name))
                folders.each {
                    if(it.path == data.path && it.name == data.name) {
                        valid = false
                    }
                }
                if(valid) {
                    folders.add(data)
                }
            }


            Map child = [
                path   : "",
                name   : "",
                parent : "",
                type   : "file",
                size   : 0,
                clazz  : "",
                isLocal: false
            ]
            child.path = url
            child.name = url.substring(url.lastIndexOf("/") + 1, url.length())
            child.parent = url.substring(0, url.indexOf(child.name) - 1)
            child.clazz = child.name.substring(child.name.lastIndexOf(".") + 1, child.name.length())
            if(tempPath.equals(child.parent + "/")) {
                files.add(child)
            }
        }

        return dirData
    }

}
