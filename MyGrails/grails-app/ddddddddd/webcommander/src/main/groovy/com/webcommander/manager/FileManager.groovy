package com.webcommander.manager

import com.webcommander.AppResourceTagLib
import com.webcommander.tenant.TenantContext
import com.webcommander.util.FileUtil
import com.webcommander.webcommerce.CloudConfig

class FileManager {

    public String destinationAbsolutePath
    public String destinationRelativePath
    public Boolean isPublicCloudResource = true

    public static final TYPE_MODIFIABLE_RESOURCE = "MODIFIABLE_RESOURCE"
    public static final TYPE_RESOURCE = "RESOURCE"
    public static final TYPE_PUB = "PUB"
    public static final TYPE_TEMPLATE = "TEMPLATE"


    FileManager setIsPublicCloudResource(Boolean isPublicCloudResource) {
        this.isPublicCloudResource = isPublicCloudResource
        return this
    }


    private def putFileIntoWCSystem(def source, String cloudResourceType){
        File destinationFile = new File(destinationAbsolutePath)
        if (!destinationFile.parentFile.exists()){
            destinationFile.parentFile.mkdirs()
        }
        if (source instanceof String){
            destinationFile.createNewFile()
            destinationFile.write(source)
        }else if (source instanceof File){
            FileUtil.move(source, destinationFile)
        }else if (source instanceof InputStream){
            if (destinationFile.exists()){
                destinationFile.delete()
            }
            destinationFile << source
        }
       return CloudStorageManager.uploadData(destinationFile, cloudResourceType, destinationRelativePath, isPublicCloudResource)
    }


    private def putDirectoryIntoWCSystem(String source, String cloudResourceType){
        File destinationFile = new File(destinationAbsolutePath)
        File sourceFile = new File(source)
        if (!destinationFile.exists()){
            destinationFile.mkdirs()
        }
        if (sourceFile.exists()){
            CloudConfig cloudConfig = CloudStorageManager.uploadDirectory(cloudResourceType, source, destinationRelativePath)
            FileUtil.move(sourceFile, destinationFile)
            return cloudConfig
        }
        return null
    }

    private CloudConfig cleanAndPutDirectoryIntoWCSystem(String source, String cloudResourceType) {
        removeFileFromSystem(cloudResourceType)
        return putDirectoryIntoWCSystem(source, cloudResourceType)
    }


    Boolean isExistFile(String cloudResourceType) {
        File destinationFile = new File(destinationAbsolutePath)
        boolean isExist = false
        if (destinationFile.exists()) {
            isExist = true
        }
        if (CloudStorageManager.isCloudEnable()) {
            isExist =  CloudStorageManager.isExistObject(destinationRelativePath, cloudResourceType)
        }
        return isExist
    }


    CloudConfig cleanAndMoveDirectory(String source, String cloudResourceType){
        return cleanAndPutDirectoryIntoWCSystem(source, cloudResourceType)
    }

    CloudConfig moveDirectory(String source, String cloudResourceType){
        return putDirectoryIntoWCSystem(source, cloudResourceType)
    }


    CloudConfig putFileIntoSystem(File source, String cloudResourceType){
       return putFileIntoWCSystem(source, cloudResourceType)
    }

    CloudConfig putFileIntoSystemRecursive(File source, File cloudResourceType){
       return putFileIntoWCSystem(source, cloudResourceType)
    }

    CloudConfig putFileIntoSystem(InputStream source, String cloudResourceType){
      return  putFileIntoWCSystem(source, cloudResourceType)
    }

    CloudConfig putFileIntoSystem(String source, String cloudResourceType){
       return putFileIntoWCSystem(source, cloudResourceType)
    }

    CloudConfig updateFileIntoSystem(File source, String cloudResourceType){
       return putFileIntoWCSystem(source, cloudResourceType)
    }

    CloudConfig updateFileIntoSystem(String source, String cloudResourceType){
      return putFileIntoWCSystem(source, cloudResourceType)
    }

    void removeFileFromSystemByCloudConfig(CloudConfig cloudConfig){
        removeLocalFile()
        if (cloudConfig != null){
            CloudStorageManager.deleteData(destinationRelativePath, cloudConfig)
        }
    }


    private void removeLocalFile(){
        File file = new File(destinationAbsolutePath)
        if(file.exists()) {
            FileUtil.deleteQuietly(file)
        }
    }


    void removeFileFromSystem(String cloudResourceType){
        removeLocalFile()
        CloudStorageManager.deleteData(destinationRelativePath, cloudResourceType)
    }


    FileManager setDestinationPath(String resourceType, String relativePath){
        switch (resourceType){
            case TYPE_MODIFIABLE_RESOURCE:
                destinationRelativePath = "${AppResourceTagLib.MODIFIABLE_RESOURCES}/${relativePath}"
                destinationAbsolutePath = "${PathManager.getRoot()}${AppResourceTagLib.WEB_INF_MODIFIABLE_RESOURCES}/${TenantContext.currentTenant}/${relativePath}"
                break
            case TYPE_RESOURCE:
                destinationRelativePath = "${AppResourceTagLib.RESOURCES}/${relativePath}"
                destinationAbsolutePath = "${PathManager.getRoot()}${AppResourceTagLib.RESOURCES}/${TenantContext.currentTenant}/${relativePath}"
                break
            case TYPE_PUB:
                destinationRelativePath = "${AppResourceTagLib.PUB}/${relativePath}"
                destinationAbsolutePath = "${PathManager.getRoot()}${AppResourceTagLib.PUB}/${TenantContext.currentTenant}/${relativePath}"
                break
            case TYPE_TEMPLATE:
                destinationRelativePath = "${AppResourceTagLib.TEMPLATE}/${relativePath}"
                destinationAbsolutePath = "${PathManager.getRoot()}${AppResourceTagLib.TEMPLATE}/${TenantContext.currentTenant}/${relativePath}"
                break
        }
        return this
    }


    InputStream readFileContentFromSystem(String cloudResourceType){
        InputStream inputStream = CloudStorageManager.getDataStream(destinationRelativePath, cloudResourceType)
        if(!inputStream) {
            File file = new File(destinationAbsolutePath)
            if (!file.exists()) {
                return null
            }
            inputStream = new FileInputStream(file)
        }
        return inputStream
    }


    static FileManager getInstance(){
        return new FileManager()
    }

}
