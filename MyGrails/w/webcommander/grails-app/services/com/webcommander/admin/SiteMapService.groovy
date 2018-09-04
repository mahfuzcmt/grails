package com.webcommander.admin

import com.webcommander.AppResourceTagLib
import com.webcommander.Page
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.content.PageService
import com.webcommander.manager.CloudStorageManager
import com.webcommander.manager.PathManager
import com.webcommander.webcommerce.CategoryService
import com.webcommander.webcommerce.Category
import com.webcommander.webcommerce.Product
import com.webcommander.webcommerce.ProductService
import groovy.xml.MarkupBuilder
import org.apache.commons.io.FileUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

class SiteMapService {
    @Autowired
    @Qualifier("com.webcommander.ApplicationTagLib")
    com.webcommander.ApplicationTagLib app

    @Autowired
    @Qualifier("com.webcommander.AppResourceTagLib")
    AppResourceTagLib appResource

    ProductService productService
    CategoryService categoryService
    PageService pageService
    ConfigService configService

    Boolean createFile(String data) {
        if(data) {
            String filePath = PathManager.getResourceRoot(appResource.SITEMAP_RESOURCE_URI)
            def sitemapFile = new File(filePath)
            File parentDir = sitemapFile.getParentFile()
            if (!parentDir.exists()) {
                parentDir.mkdir()
            }
            sitemapFile.createNewFile()
            FileUtils.writeStringToFile(sitemapFile, data, "UTF-8")
            CloudStorageManager.uploadData(sitemapFile, NamedConstants.CLOUD_CONFIG.SYSTEM_DEFAULT, appResource.SEO_UPLOAD)
            return true
        } else {
            return false
        }
    }

    Map readFile() {
        String ret
        Map data = [:]
        try {
            String filePath = PathManager.getResourceRoot(appResource.SITEMAP_RESOURCE_URI)
            String cloudText = CloudStorageManager.getDataString(appResource.SITEMAP_RESOURCE_URI, NamedConstants.CLOUD_CONFIG.SYSTEM_DEFAULT)
            ret = cloudText ?: new File(filePath).getText()
            data.found = "yes"
            data.sitemap = ret;
        } catch ( FileNotFoundException e ) {
            ret = "No Sitemap Found"
            data.found = "no"
            data.sitemap = ret;
        }
        return data
    }

    String createSiteMap(Map params) {
        try {
            PathManager.getResourceRoot(appResource.SITEMAP_RESOURCE_URI);
        } catch ( FileNotFoundException e) {
            String directoryPath = appResource.getSeoUplodRoot()
            def directory = new File(directoryPath)
            if( !directory.exists() ) {
                directory.mkdirs()
            }
            String filePath = PathManager.getResourceRoot(appResource.SITEMAP_RESOURCE_URI);
            def sitemapFile = new File(filePath)
            sitemapFile.createNewFile()
        }

        StringWriter write = new StringWriter()
        MarkupBuilder xml = new MarkupBuilder(write)

        xml.urlset {

            if (params.page) {
                List<Page> pageList = pageService.getPages([visibility: "open"])
                pageList.each {
                    String location = app.siteBaseUrl() + it.url
                    String lastMod = it.updated
                    url {
                        loc(location)
                        lastmod(lastMod)
                    }
                }

            }

            if (params.product) {
                List<Product> productList = Product.createCriteria().list {
                    and {
                        eq("isAvailable", true)
                        eq("availableFor", DomainConstants.PRODUCT_AVAILABLE_FOR.EVERYONE)
                        eq("isInTrash", false)
                        eq("isDisposable", false)
                    }
                }
                productList.each {
                    String location = app.siteBaseUrl() + "product/" + it.url
                    String lastMod = it.updated
                    url {
                        loc(location)
                        lastmod(lastMod)
                    }
                }
            }

            if (params.category) {
                List<Category> categoryList = categoryService.getCategories([isAvailable: "true"])
                categoryList.each {
                    String location = app.siteBaseUrl() + "category/" + it.url
                    String lastMod = it.updated
                    if(it.availableFor.equals(DomainConstants.CATEGORY_AVAILABLE_FOR.EVERYONE)) {
                        url {
                            loc(location)
                            lastmod(lastMod)
                        }
                    }
                }
            }

        }
        createFile( write.toString() )
        return write.toString();
    }
}