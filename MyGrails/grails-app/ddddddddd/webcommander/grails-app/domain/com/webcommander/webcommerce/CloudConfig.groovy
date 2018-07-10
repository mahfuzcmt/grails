package com.webcommander.webcommerce

import com.webcommander.constants.DomainConstants

class CloudConfig {

    Long id
    String accessKye
    String secretKey
    String bucketName = null
    String cloudType = DomainConstants.CLOUD_TYPE.AWS_S3
    String baseUrl
    String title
    Boolean isEnable = true
    Boolean isDefault = false
    Boolean isSystemDefault = false


    static constraints = {
        title(nullable: true)
        bucketName(nullable: true)
    }
}
