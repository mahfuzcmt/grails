package com.webcommander.manager

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.AmazonClientException
import com.amazonaws.AmazonServiceException
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.AmazonS3Exception
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.amazonaws.services.s3.model.DeleteObjectRequest
import com.amazonaws.services.s3.model.DeleteObjectsRequest
import com.amazonaws.services.s3.model.ListObjectsRequest
import com.amazonaws.services.s3.model.ObjectListing
import com.amazonaws.services.s3.model.PutObjectRequest
import com.amazonaws.services.s3.model.S3Object
import com.amazonaws.services.s3.model.S3ObjectSummary
import com.amazonaws.services.s3.transfer.MultipleFileDownload
import com.amazonaws.services.s3.transfer.MultipleFileUpload
import com.amazonaws.services.s3.transfer.TransferManager
import com.amazonaws.services.s3.transfer.TransferManagerBuilder
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.models.MockStaticResource
import com.webcommander.models.blueprints.AbstractStaticResource
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.CloudConfig
import org.apache.commons.io.FilenameUtils

class CloudStorageManager {

    private static Map getS3Config() {
        return AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.AWS_S3)
    }

    private static Map getCloudConfig() {
        return AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.CLOUD_STORAGE)
    }

    private static CloudConfig getCloudConfig(String configurationType) {
        CloudConfig cloudConfig
        if(configurationType.equals(NamedConstants.CLOUD_CONFIG.DEFAULT)) {
          cloudConfig = CloudConfig.createCriteria().get {
              eq("isDefault", true)
          }
        } else if(configurationType.equals(NamedConstants.CLOUD_CONFIG.SYSTEM_DEFAULT)) {
            cloudConfig = CloudConfig.createCriteria().get {
                eq("isSystemDefault", true)
            }
        }
        return cloudConfig
    }

    static List<CloudConfig> getAllCloudConfig() {
        List<CloudConfig> cloudConfig = CloudConfig.list()
        return cloudConfig
    }


    private static AmazonS3Client getS3Client(CloudConfig cloudConfig) {
        AmazonS3Client client = null
        if (cloudConfig.accessKye && cloudConfig.secretKey && cloudConfig.bucketName && cloudConfig.isEnable) {
            AWSCredentials credentials = new BasicAWSCredentials(cloudConfig.accessKye, cloudConfig.secretKey)
            client = new AmazonS3Client(credentials)
        }
        return client
    }

    private static AmazonS3Client getS3Client(String type) {
        CloudConfig cloudConfig = getCloudConfig(type)
        if (!cloudConfig) {
            return null
        }
        return getS3Client(cloudConfig)
    }

    private static uploadAWSS3Directory(CloudConfig cloudConfig, String source, String cloudPrefix){
        TransferManager transferManager = TransferManagerBuilder.standard().withS3Client(getS3Client(cloudConfig)).build()
        try {
            MultipleFileUpload multipleFileUpload = transferManager.uploadDirectory(cloudConfig.bucketName, cloudPrefix, new File(source), true)
            multipleFileUpload.waitForCompletion()
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage())
            return false
        } catch (InterruptedException e) {
            System.err.println(e.getMessage())
            return false
        }
        transferManager.shutdownNow()
        return true
    }

    private static String getBucketName(String type) {
        String bucketName = null
        if(type.equals(NamedConstants.RESOURCE_TYPE.RESOURCE)) {
            if(cloudConfig.aws_is_enabled && cloudConfig.aws_enable_resource_bucket) {
                bucketName = cloudConfig.aws_resource_bucket_name
            } else if(s3Config.resource_enabled) {
                bucketName = s3Config.resource_bucket_name
            }
        } else if(type){
            bucketName = s3Config.resource_bucket_name
        }
        return bucketName
    }

    static Boolean isExistAWSS3Object(String type, String location) {
        AmazonS3Client client = getS3Client(type)
        try {
            CloudConfig cloudConfig = getCloudConfig(type)
            location = location.replace('\\', '/')
            return client.doesObjectExist(cloudConfig.bucketName, location)
        } catch (AmazonServiceException ase) {
            return false
        } catch (AmazonClientException ace) {
            return false
        }
    }

    static Boolean uploadToAWSS3(File input, String type, String location, Boolean isPublic = true) {
        AmazonS3Client client = getS3Client(type)
        if(client == null) { return false }
        try {
            CloudConfig cloudConfig = getCloudConfig(type)
            location = location.replace('\\', '/')
            PutObjectRequest request = new PutObjectRequest(cloudConfig.bucketName, location, input)
            if (isPublic){
                request.withCannedAcl(CannedAccessControlList.PublicRead)
            }
            client.putObject(request)
            return true
        } catch (AmazonServiceException ase) {
            return false
        } catch (AmazonClientException ace) {
            return false
        }
    }

    static Boolean deleteBulkAWSS3Data(String location, String type) {
        return _deleteAWSS3Data(getS3FileList(location, type, getCloudConfig(type)), type)
    }

    private static Boolean _deleteAWSS3Data(def location, String type) {
        CloudConfig cloudConfig = getCloudConfig(type)
        return _deleteAWSS3Data(location, cloudConfig)
    }

    private static Boolean _deleteAWSS3Data(def location, CloudConfig cloudConfig) {
        AmazonS3Client client = getS3Client(cloudConfig)
        if(client == null) {
            return false
        }
        try {
            DeleteObjectRequest request
            if(location instanceof List) {
                List<S3ObjectSummary> objs = location
                def multiObjectDeleteRequest = new DeleteObjectsRequest(cloudConfig.bucketName)
                multiObjectDeleteRequest.setKeys(objs.key.collect {new DeleteObjectsRequest.KeyVersion(it)})
            } else {
                location = location.replace('\\', '/')
                request = new DeleteObjectRequest(cloudConfig.bucketName, location)
            }
            client.deleteObject(request)
        } catch (Throwable ignore) {
        }
    }

    static CloudConfig uploadPrivateData(File input, String type, String location) {
        return uploadData(input, type, location, false)
    }

    static CloudConfig uploadDirectory(String type, String sourceLocation, String bucketDestination) {
        CloudConfig cloudConfig = getCloudConfig(type)
        if (!cloudConfig){
            return null
        }
        if (cloudConfig.cloudType && cloudConfig.cloudType.equals(DomainConstants.CLOUD_TYPE.AWS_S3)) {
            if (uploadAWSS3Directory(cloudConfig, sourceLocation, bucketDestination)){
                return cloudConfig
            }
        }
        return null
    }

    static CloudConfig uploadData(File input, String type, String location, Boolean isPublic = true) {
        CloudConfig cloudConfig = getCloudConfig(type)
        if (!cloudConfig){
            return null
        }
        location = location.replace('\\', '/').replace("//", "/")
        if (cloudConfig.cloudType && cloudConfig.cloudType.equals(DomainConstants.CLOUD_TYPE.AWS_S3)) {
            if (uploadToAWSS3(input, type, location, isPublic)){
                return cloudConfig
            }
        }
        return null
    }

    static Boolean deleteData(String location, CloudConfig cloudConfig) {
        location = location.replace('\\', '/')
        if (cloudConfig.cloudType && cloudConfig.cloudType.equals(DomainConstants.CLOUD_TYPE.AWS_S3)) {
             return _deleteAWSS3Data(location, cloudConfig)
        }
        return false
    }

    static Boolean deleteData(String location, String type) {
        CloudConfig cloudConfig = getCloudConfig(type)
        if (!cloudConfig) {
            return false
        }
        return deleteData(location, cloudConfig)
    }

    static Boolean deleteBulkData(String location, String type) {
        CloudConfig cloudConfig = getCloudConfig(type)
        if (!cloudConfig) {
            return false
        }
        location = location.replace('\\', '/')
        if (cloudConfig.cloudType && cloudConfig.cloudType.equals(DomainConstants.CLOUD_TYPE.AWS_S3)) {
            return deleteBulkAWSS3Data(location, type)
        }
        return false
    }

    static List<S3ObjectSummary> getS3FileList(String path, String type, CloudConfig cloudConfig) {
        String delimiter = "/"
        if(!path.endsWith(delimiter)) {
            path += delimiter
        }
        try {
            path = path.replace('\\', '/')
            def s3Client = getS3Client(type)
            ObjectListing objects = s3Client.listObjects(cloudConfig.bucketName, path)
            return objects.objectSummaries
        } catch (Exception ex) {
            ex.printStackTrace()
            return null
        }
    }

    static List<String> getFileList(String path, String type) {
        CloudConfig cloudConfig = getCloudConfig(type)
        if (!cloudConfig) {
            return null
        }
        path = path.replace('\\', '/')
        if (cloudConfig.cloudType && cloudConfig.cloudType.equals(DomainConstants.CLOUD_TYPE.AWS_S3)) {
            return getS3FileList(path, type, cloudConfig)?.key
        }
    }


    static Boolean isExistObject(String path, String type) {
        CloudConfig cloudConfig = getCloudConfig(type)
        if (!cloudConfig) {
            return null
        }
        if (cloudConfig.cloudType && cloudConfig.cloudType.equals(DomainConstants.CLOUD_TYPE.AWS_S3)) {
            return isExistAWSS3Object(type, path)
        }
    }

    static InputStream getDataStream(String filePath, String type) {
        CloudConfig cloudConfig = getCloudConfig(type)
        if (!cloudConfig) {
            return null
        }
        filePath = filePath.replace('\\', '/')
        if (cloudConfig.cloudType && cloudConfig.cloudType.equals(DomainConstants.CLOUD_TYPE.AWS_S3)) {
            def s3Client = getS3Client(type)
            try {
                def s3Object = s3Client.getObject(cloudConfig.bucketName, filePath)
                return s3Object.getObjectContent()
            } catch (AmazonS3Exception e) {
                return null
            }
        }
    }

    static String getDataString(String filePath, String type) {
        CloudConfig cloudConfig = getCloudConfig(type)
        if (!cloudConfig) {
            return null
        }
        filePath = filePath.replace('\\', '/')
        if (cloudConfig.cloudType && cloudConfig.cloudType.equals(DomainConstants.CLOUD_TYPE.AWS_S3)) {
            def s3Client = getS3Client(type)
            return s3Client.getObjectAsString(cloudConfig.bucketName, filePath)
        }
    }

    static def copyData(String source, String destination, String type) {
        CloudConfig cloudConfig = getCloudConfig(type)
        if (!cloudConfig) {
            return null
        }
        return getS3Client(type).copyObject(cloudConfig.bucketName, source, cloudConfig.bucketName, destination)
    }

    static String getStaticS3Url() {
        if(s3Config.static_enabled) {
            return s3Config.static_url ?: ""
        }
        return null
    }

    static CloudConfig isCloudEnable() {
        CloudConfig cloudConfig = getCloudConfig(NamedConstants.CLOUD_CONFIG.SYSTEM_DEFAULT)
        if (cloudConfig && cloudConfig.isEnable) {
            return cloudConfig
        }
        return null
    }

    static CloudConfig getDefaultCloudConfig() {
        return isCloudEnable(NamedConstants.CLOUD_CONFIG.DEFAULT)
    }

    static CloudConfig isCloudEnable(String configurationType) {
        if (!configurationType) {
            return null
        }
        CloudConfig cloudConfig = getCloudConfig(configurationType)
        if (cloudConfig && cloudConfig.isEnable) {
            return cloudConfig
        }
        return null
    }

    static CloudConfig isCloudEnable(String configurationType, AbstractStaticResource resource) {
        CloudConfig cloudConfig = CloudStorageManager.isCloudEnable(configurationType)
        if (!cloudConfig) {
            return null
        }
        if ((resource instanceof MockStaticResource) && !resource.isUploadToCloud()) {
            return null
        }
        return cloudConfig
    }

    static String getCloudBaseURL(String cloudType = NamedConstants.CLOUD_CONFIG.SYSTEM_DEFAULT) {
        CloudConfig cloudConfig = getCloudConfig(cloudType)
        if (cloudConfig) {
            return cloudConfig.baseUrl
        }
        return null
    }
}