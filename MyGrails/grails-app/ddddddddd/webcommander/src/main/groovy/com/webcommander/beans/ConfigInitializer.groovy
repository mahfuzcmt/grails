package com.webcommander.beans

import com.webcommander.annotations.Initializable
import com.webcommander.constants.NamedConstants
import com.webcommander.events.AppEventManager
import com.webcommander.util.AppUtil
import org.springframework.stereotype.Component

import static com.webcommander.constants.DomainConstants.SITE_CONFIG_TYPES

@Component
@Initializable
class ConfigInitializer {
    static void initialize() {
        AppEventManager.on("${SITE_CONFIG_TYPES.PRODUCT_IMAGE}-refresh-site-config ${SITE_CONFIG_TYPES.PRODUCT_IMAGE}-after-settings-updated", ConfigInitializer.&setProductImageConfig)
        AppEventManager.on("${SITE_CONFIG_TYPES.CATEGORY_IMAGE}-refresh-site-config ${SITE_CONFIG_TYPES.CATEGORY_IMAGE}-after-settings-updated", ConfigInitializer.&setCategoryImageConfig)
    }

    private static setProductImageConfig(List site_config) {
        Map existingMap = AppUtil.getConfig(SITE_CONFIG_TYPES.PRODUCT_IMAGE).size
        NamedConstants.PRODUCT_IMAGE_SETTINGS.each { key, value ->
            Integer width = site_config.find {
                it.key == value + '_width'
            }?.toInteger()
            Integer height = site_config.find {
                it.key == value + '_height'
            }?.toInteger()
            if(width && height) {
                existingMap[value] = getProductImageSize(Math.max(width, height))
            }
        }
    }

    private static setProductImageConfig(Map site_config) {
        Map configMap = [:]
        NamedConstants.PRODUCT_IMAGE_SETTINGS.each { key, value ->
            configMap[value] = getProductImageSize(Math.max(site_config[value + '_width'].toInteger(0), site_config[value + '_height'].toInteger(0)))
        }
        site_config.size = configMap
    }

    private static setCategoryImageConfig(List site_config) {
        Map existingMap = AppUtil.getConfig(SITE_CONFIG_TYPES.CATEGORY_IMAGE).size
        NamedConstants.CATEGORY_IMAGE_SETTINGS.each { key, value ->
            Integer width = site_config.find {
                it.key == value + '_width'
            }?.toInteger()
            Integer height = site_config.find {
                it.key == value + '_height'
            }?.toInteger()
            if(width && height) {
                existingMap[value] = getCategoryImageSize(Math.max(width, height))
            }
        }
    }

    private static setCategoryImageConfig(Map site_config) {
        Map configMap = [:]
        NamedConstants.CATEGORY_IMAGE_SETTINGS.each { key, value ->
            configMap[value] = getCategoryImageSize(Math.max(site_config[value + '_width'].toInteger(0), site_config[value + '_height'].toInteger(0)))
        }
        site_config.size = configMap
    }

    private static String getProductImageSize(Integer size) {
        if (size <= 600) {
            return "" + ((int) Math.ceil(size / 150) * 150)
        }
        return "900"
    }

    private static String getCategoryImageSize(Integer size) {
        return getProductImageSize(size)
    }

}
