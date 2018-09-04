package com.webcommander.admin

import com.webcommander.config.AWSConfiguration
import grails.gorm.transactions.Transactional

@Transactional
class CloudStorageService {

    def saveAWSConfig(Map config, String type) {
        AWSConfiguration configuration = new AWSConfiguration()
        configuration.bucketName = config."bucket_name"
        configuration.secretKey = config."bucket_secret_key"
        configuration.accessKey = config."bucket_access_key"
        configuration.isActive = true
        configuration.save()
        if(!configuration.hasErrors()) return true
        return false
    }

//    public Boolean is
}
