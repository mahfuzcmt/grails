package com.webcommander.common

import com.webcommander.annotations.Initializable
import com.webcommander.constants.NamedConstants
import com.webcommander.events.AppEventManager
import com.webcommander.manager.CloudStorageManager
import com.webcommander.manager.PathManager
import com.webcommander.models.MockStaticResource
import com.webcommander.models.blueprints.AbstractStaticResource
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.util.AppUtil
import com.webcommander.util.FileUtil
import com.webcommander.webcommerce.CloudConfig
import org.apache.commons.io.FilenameUtils
import org.im4java.core.ConvertCmd
import org.im4java.core.IMOperation
import org.im4java.core.Info
import org.springframework.web.multipart.MultipartFile

@Initializable
class ImageService {

    static RESIZABLE_IMAGE_SIZES = [
        "store-logo": [thumb: [150, 60]],
        "manufacturer-logo": [
            thumb: [150, 60],
            200: [200, 200],
            300: [300, 300],
            400: [400, 400]
        ],
        "brand-logo":  [
                thumb: [150, 60],
                200: [200, 200],
                300: [300, 300],
                400: [400, 400]
        ],
        "album-image": [
            thumb: [150, 150],
            gallery: [200, 200]
        ],
        "product-image": [
            150: [150, 150],
            300: [300, 300],
            450: [450, 450],
            600: [600, 600],
            900: [900, 900]
        ],
        "category-image": [
            150: [150, 150],
            300: [300, 300],
            450: [450, 450],
            600: [600, 600],
            900: [900, 900]
        ],
        "favicon-image": [
           16: [16, 16]
        ]
    ]

    static void initialize() {

        AppEventManager.on("after-remove-resource", { AbstractStaticResource resource ->

            String type = NamedConstants.DOMAIN_IMAGE_RESIZE_TYPE[resource.getClass().getSimpleName()]
            if (!type || !resource.getResourceName()) {
                return
            }

            Map sizes = RESIZABLE_IMAGE_SIZES[type]
            String fileSuffix = resource.getResourceName()

            File targetImage = new File(PathManager.getResourceRoot(resource.resourceRelativePath))
            File origin = new File(targetImage.absolutePath)

            sizes.each {
                String fileName = "${it.key}-" + fileSuffix
                String deleteFilePath = origin.getParent() + File.separator + fileName
                FileUtil.deleteQuietly(new File(deleteFilePath))
                if(CloudStorageManager.isCloudEnable(NamedConstants.CLOUD_CONFIG.DEFAULT, resource) && resource.getBaseUrl()) {
                    CloudStorageManager.deleteData("resources/"+resource.getRelativeUrl()+fileName, NamedConstants.CLOUD_CONFIG.DEFAULT)
                }
            }
        })

    }

    Map getSizes(String type) {
        return RESIZABLE_IMAGE_SIZES[type]
    }

    def uploadImage(MultipartFile uploadedImage, String type, AbstractStaticResource resource, Long expectedSize = null, Boolean keepOrigin = true) {
        if (!uploadedImage.getContentType().contains("image")) {
            throw new ApplicationRuntimeException("uploaded.file.not.recognizable.image.format")
        }
        Long size = Math.ceil(uploadedImage.getSize())
        if (expectedSize && size > expectedSize) {
            throw new ApplicationRuntimeException("uploaded.file.size.larger.than.expected", [AppUtil.convertToByteNotation(size), AppUtil.convertToByteNotation(expectedSize)])
        }
        createCopies(uploadedImage.inputStream, uploadedImage.originalFilename, resource, getSizes(type), keepOrigin)
    }

    void createCopies(InputStream uploadedStream, String sourceName,  AbstractStaticResource resource, Map sizes, Boolean keepOrigin = true) {
        String copyTargetName = resource.resourceName ?: sourceName
        sourceName = sourceName?.toValidFileName()
        copyTargetName = copyTargetName.toValidFileName()
        resource.resourceName = copyTargetName
        File targetImage = new File(PathManager.getResourceRoot(resource.resourceRelativePath))
        File entityDirectory = targetImage.parentFile
        if (!entityDirectory.exists()) {
            entityDirectory.mkdirs()
            entityDirectory.setWritable(true, true)
        }
        if(sourceName && FilenameUtils.getExtension(sourceName) != FilenameUtils.getExtension(copyTargetName)) {
            File sourceImage = new File(entityDirectory, sourceName)
            sourceImage << uploadedStream
            typeConvert(sourceImage.absolutePath, targetImage.absolutePath)
            sourceImage.delete()
        } else {
            targetImage.withOutputStream { out ->
                out << uploadedStream
            }
        }
        uploadedStream.close()
        try {
            if(CloudStorageManager.isCloudEnable(NamedConstants.CLOUD_CONFIG.DEFAULT, resource)) {
                CloudConfig cloudConfig = CloudStorageManager.uploadData(targetImage, NamedConstants.CLOUD_CONFIG.DEFAULT, resource.resourceRelativeUrl)
                if(cloudConfig) {
                    resource.cloudConfig = cloudConfig
                }
            }
        } catch (Exception ignore) {
            log.error(ignore.message)
        }
        AppEventManager.fire("custom-resource-file-uploaded", [targetImage.absolutePath])
        if(sizes) {
            createResizedCopies(targetImage.absolutePath, resource, sizes, keepOrigin)
        }
    }

    private void typeConvert(String inFile, String outFile) {
        ConvertCmd cmd = new ConvertCmd()
        IMOperation operation = new IMOperation()
        if(inFile.endsWith(".gif") && !outFile.endsWith(".gif")) {
            inFile += "[0]"
        }
        operation.addImage(inFile)
        operation.addImage(outFile)
        cmd.run(operation)
    }

    void createResizedCopies(String filePath, AbstractStaticResource resource, Map sizes, Boolean keepOrigin = true) {
        try {
            File origin = new File(filePath)
            String fileSuffix = origin.name
            ConvertCmd cmd = new ConvertCmd()
            Info info = new Info(filePath, false)
            int xRes = info.getImageWidth()
            int yRes = info.getImageHeight()
            sizes.each {
                String copyFilePath = origin.getParent() + File.separator + "${it.key}-" + fileSuffix
                thumbnail(cmd, filePath, xRes, yRes, it.value[0], it.value[1], copyFilePath)
                if(CloudStorageManager.isCloudEnable(NamedConstants.CLOUD_CONFIG.DEFAULT, resource)) {
                    CloudStorageManager.uploadData(new File(copyFilePath), NamedConstants.CLOUD_CONFIG.DEFAULT, resource.getResourceRelativeUrl(it.key.toString()))
                }
                AppEventManager.fire("custom-resource-file-uploaded", [copyFilePath])
            }
            if(!keepOrigin) {
                origin.delete()
            }
        } catch (Throwable e) {
            log.error e.message, e
            throw e
        }
    }

    private void thumbnail(ConvertCmd cmd, String inFile, double width, double height, int newWidth, int newHeight, String outFile) {
        double xFactor = newWidth / width
        double yFactor = newHeight / height
        if (xFactor < 1 || yFactor < 1) {
            IMOperation operation = new IMOperation()
            operation.addImage(inFile)
            operation.resize(newWidth, newHeight)
            operation.addImage(outFile)
            cmd.run(operation)
        } else {
            new File(inFile).withInputStream { _inp ->
                new File(outFile).withOutputStream { _oup ->
                    _oup << _inp
                }
            }
        }
    }

    void reformat(String inFile, String outFile) {
        inFile = inFile.toValidFileName()
        outFile = outFile.toValidFileName()
        IMOperation operation = new IMOperation()
        operation.addImage(inFile)
        operation.addImage(outFile)
        new ConvertCmd().run(operation)
        AppEventManager.fire("custom-resource-file-uploaded", [outFile])
    }
}
