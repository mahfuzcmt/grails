package com.webcommander.plugin.google_product

import com.webcommander.admin.ConfigService
import com.webcommander.calculator.TaxCalculator
import com.webcommander.common.FileService
import com.webcommander.config.SiteConfig
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.manager.HookManager
import com.webcommander.manager.PathManager
import com.webcommander.models.ProductData
import com.webcommander.plugin.google_product.constants.Constants
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.Product
import com.webcommander.webcommerce.ProductService
import com.webcommander.webcommerce.TaxCode
import grails.gorm.transactions.Transactional
import groovy.xml.MarkupBuilder
import org.apache.poi.hssf.usermodel.HSSFSheet
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import grails.web.servlet.mvc.GrailsParameterMap
import org.hibernate.SessionFactory
import com.webcommander.webcommerce.Category
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

import java.text.SimpleDateFormat


class GoogleProductService {
    ConfigService configService
    SessionFactory sessionFactory
    ProductService productService
    FileService fileService
    @Autowired
    @Qualifier("com.webcommander.ApplicationTagLib")
    com.webcommander.ApplicationTagLib app
    @Autowired
    @Qualifier("com.webcommander.AppResourceTagLib")
    com.webcommander.AppResourceTagLib appResource

    static GOOGLE_CATEGORY = "google-category"
    static GOOGLE_CATEGORY_XLS = "category.xls"

    Boolean updateCategory() {
        try {
            String path = PathManager.getCustomRestrictedResourceRoot(GOOGLE_CATEGORY)
            if (!new File(path).exists()) {
                new File(path).mkdirs()
                def file = new File(path)
                file.setWritable(true, true);
            }
            URL url = new URL("http://www.google.com/basepages/producttype/taxonomy.en-GB.xls");
            InputStream inputStream = url.openConnection().getInputStream();
            String originalFilePath = path + File.separator + GOOGLE_CATEGORY_XLS;
            OutputStream outputStream = new FileOutputStream(originalFilePath)
            outputStream << inputStream;
            outputStream.close();
            inputStream.close();
            fileService.uploadModifiableResource(new File(originalFilePath), "${GOOGLE_CATEGORY}/${GOOGLE_CATEGORY_XLS}")
            return true
        } catch (Exception e) {
            return false;
        }
    }

    @Transactional
    Boolean saveConfigurations(GrailsParameterMap params) {
        def configs = []
        String type = params.type
        params.list(type).each { typ ->
            typ.each {
                configs.add([type: type, configKey: it.key, value: it.value]);
            }
        }
        if(params["google_product"].submit_variations.toBoolean()) {
            params.mapping.each {
                SiteConfig siteConfig = SiteConfig.findByTypeAndConfigKey("google_variation_mapping", it.key);
                if (siteConfig) {
                    siteConfig.value = it.value;
                } else {
                    siteConfig = new SiteConfig(type: "google_variation_mapping", configKey: it.key, value: it.value).save();
                }
                siteConfig.save();
            }
        }
        return configService.update(configs)
    }

    List excelFileToMap() {
        Map rootMap = [:]
        List rootList = []
        InputStream inputStream = fileService.getModifiableResourceStream("${GOOGLE_CATEGORY}/${GOOGLE_CATEGORY_XLS}")
        if (!inputStream) {
            return rootList
        }
        HSSFWorkbook workbook = new HSSFWorkbook (inputStream);
        HSSFSheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.iterator();
        while (rowIterator.hasNext()) {
            Row row =rowIterator.next();
            Iterator<Cell> cellIterator = row.cellIterator();
            Map map = rootMap
            List list = rootList
            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
                String cellVal = cell.getStringCellValue();
                if (map[cellVal] != null) {
                    list = map[cellVal].listRef
                    map = map[cellVal].children
                } else {
                    List listRef = []
                    map[cellVal] = [
                            listRef: listRef,
                            children: [:]
                    ]
                    list.add([name: cellVal, children: listRef])
                    break;
                }
            }
        }

        return rootList
    }

    List getCategoryInfoAsTree(String type) {
        Map nodeCatMap = [:]
        Map mappings = CategoryMapping.list().collectEntries {[(it.categoryId + ""): it.googleCategory]};
        List tree = []
        Category.where {
            isInTrash == false
            isParentInTrash == false
            isAvailable == true
            isDisposable == false
            availableFor == DomainConstants.CATEGORY_AVAILABLE_FOR.EVERYONE
            order "idx"
            def c1 = Category
            if(type == "mapped") {
                exists CategoryMapping.where {
                    def m1 = CategoryMapping
                    c1.id == m1.categoryId
                }.id()
            } else if(type == "unmapped") {
                notExists CategoryMapping.where {
                    def m1 = CategoryMapping
                    c1.id == m1.categoryId
                }.id()
            }
        }.list().each {
            String mapping = mappings["" + it.id]
            Map node = nodeCatMap["" + it.id] ?: [id: it.id, name: it.name.encodeAsBMHTML(), mapping: mapping]
            nodeCatMap["" + it.id] = node
            if(it.parent) {
                Map parentNode = nodeCatMap["" + it.parent.id]
                if(parentNode) {
                    if(parentNode.children) {
                        parentNode.children.add(node)
                    } else {
                        parentNode.children = [node]
                    }
                } else {
                    Category parent = it.parent
                    String pMapping = mappings["" + parent.id]
                    parentNode = [id: parent.id, name: parent.name.encodeAsBMHTML(), mapping: pMapping, children: [node]]
                    nodeCatMap["" + parent.id] = parentNode
                    if(type == "mapped" && !pMapping) {
                        while(true) {
                            if(!parent.parent) {
                                tree.add(parentNode)
                                break;
                            }
                            parent = parent.parent
                            pMapping = mappings["" + parent.id]
                            parentNode = [id: parent.id, name: parent.name.encodeAsBMHTML(), mapping: pMapping, children: [parentNode]]
                            nodeCatMap["" + parent.id] = parentNode
                            if(pMapping) {
                                break;
                            }
                        }
                    } else if(type == "unmapped" && pMapping) {
                        while(true) {
                            if(!parent.parent) {
                                tree.add(parentNode)
                                break;
                            }
                            parent = parent.parent
                            pMapping = mappings["" + parent.id]
                            parentNode = [id: parent.id, name: parent.name.encodeAsBMHTML(), mapping: pMapping, children: [parentNode]]
                            nodeCatMap["" + parent.id] = parentNode
                            if(!pMapping) {
                                break;
                            }
                        }
                    }
                }
            } else {
                tree.add(node)
            }
        }
        return tree
    }

    @Transactional
    Boolean mapCategory(GrailsParameterMap params) {
        Long categoryId = params.long("categoryId");
        CategoryMapping mapping = CategoryMapping.findByCategoryId(categoryId);
        if(!mapping) {
            mapping = new CategoryMapping()
        }
        mapping.categoryId = categoryId;
        mapping.googleCategory = params.googleCategory
        mapping.save()
        sessionFactory.cache.evictQueryRegions();
        return !mapping.hasErrors()
    }

    @Transactional
    Boolean removeMapping(GrailsParameterMap params) {
        Long categoryId = params.long("categoryId");
        CategoryMapping mapping = CategoryMapping.findByCategoryId(categoryId);
        mapping.delete()
        sessionFactory.cache.evictQueryRegions();
        return true
    }

    private Closure getItemFeed(Product product, Map config, List<CategoryMapping> mappings, int count, TaxCode taxCode, Map vConf = [:]) {
        def imageSize = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.PRODUCT_IMAGE, "size")[NamedConstants.PRODUCT_IMAGE_SETTINGS.DETAILS]
        return {
            Map variations = vConf.variationMap
            ProductData data = vConf.data
            def productData = data ? data : product
            Double price, salePrice
            title(productData.name.encodeAsPrintableUTF().truncate(70))
            link(app.nonRequestBaseUrl() + "product/" + productData.url)
            if (productData.description) {
                if(config["description_mapping"] == "text") {
                    description(productData.description.textify())
                } else {
                    description(productData.description)
                }
            }
            if(config["id_mapping"] == "sku") {
                "g:id"(productData.sku)
            } else {
                "g:id"(product.id)
            }
            "g:condition"(product.productCondition)
            if(config["submit_sale_price"].toBoolean() && productData.salePrice) {
                price = productData.basePrice
                salePrice = productData.salePrice
            } else if(productData.isOnSale) {
                price = productData.salePrice
            } else {
                price = productData.basePrice
            }
            "g:price"((price + (taxCode ? TaxCalculator.getTax(taxCode, price) : 0)).toPrice())
            if(salePrice) {
                "g:sale_price"((salePrice + (taxCode ? TaxCalculator.getTax(taxCode, salePrice) : 0)).toPrice())
            }
            if(productData.isInventoryEnabled && productData.availableStock < 1) {
                "g:availability"("out of stock")
            } else {
                "g:availability"("in stock")
            }
            String imageLink = appResource.getProductImageFullUrl(product: productData, imageSize: imageSize)
            "g:image_link"(imageLink)
            if(config["submit_additional_image"].toBoolean() && productData.images.size() > 1) {
                Integer size = productData.images.size() > 10 ? 10 : productData.images.size()
                for (int i = 1; i < size; i++) {
                    imageLink = appResource.getProductImagesFullUrl(image: product.images[i], imageSize: imageSize)
                    "g:additional_image_link"(imageLink)
                }
            }
            "g:google_product_category"(mappings.find { it.categoryId == product.parent.id}.googleCategory)
            if (count < 2) {
                "g:identifier_exists"("FALSE")
            }
            if (config["submit_shipping_height"].toBoolean() && productData.height) {
                "g:shipping_height"(productData.height)
            }
            if (config["submit_shipping_width"].toBoolean() && productData.width) {
                "submit_shipping_width"(productData.width)
            }
            if (config["submit_shipping_length"].toBoolean() && productData.length) {
                "submit_shipping_length"(productData.length)
            }
            if(config["submit_gtin"].toBoolean() && product.globalTradeItemNumber) {
                "g:gtin"(product.globalTradeItemNumber)
            }
            if (config["submit_shipping_weight"].toBoolean() && productData.weight) {
                "g:shipping_weight"(productData.weight + "kg")
            }
            if (config["submit_feed_expiry_date"].toBoolean()) {
                Date date = new Date() + config["feed_expiry_date"].toInteger()
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd")
                "g:expiration_date"(dateFormat.format(date))
            }
            String productType = product.parent.name;
            Category parent = product.parent.parent;
            while (parent) {
                productType = parent.name + ">" + productType;
                parent = parent.parent;
            }
            "g:product_type"(productType.encodeAsPrintableUTF())
            if (variations != null) {
                variations.each {
                    String val;
                    if (it.key == "gender") {
                        val = Constants.GENDER[it.value] ?: "male";
                    } else if (it.key == "age_group") {
                        val = Constants.AGE_GROUP[it.value] ?: "adult";
                    } else {
                        val = it.value;
                    }
                    "g:${it.key}"(val);
                }
                "g:item_group_id"(data.sku)
            }
        }

    }

    void writeXmlFeed(Writer writer) {
        List<CategoryMapping> mappings = CategoryMapping.list();
        List< Product> products
        if(mappings.size()){
            products = productService.getProducts([max: "-1", offset: "0", parentList: mappings.categoryId, category: "primary"])
        } else {
            products = []
        }
        Map config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GOOGLE_PRODUCT)
        Map variationConfig = AppUtil.getConfig("google_variation_mapping") ?: [:]

        MarkupBuilder xml = new MarkupBuilder(writer)
        xml.mkp.xmlDeclaration(version: "1.0", encoding: "utf-8");
        TaxCode taxCode = null
        String priceWithTax = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.TAX, "is_price_with_tax")
        if(config.tax_code && priceWithTax != "true") {
            taxCode = TaxCode.get(config.tax_code)
        }
        String imageSize = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.PRODUCT_IMAGE, "size")[NamedConstants.PRODUCT_IMAGE_SETTINGS.THUMBNAIL]
        xml.rss(version: "2.0", "xmlns:g":  "http://base.google.com/ns/1.0") {
            channel {
                products.each {Product product->
                    Integer count = (product.globalTradeItemNumber ? 1 : 0 ) ;
                    Boolean exclude = config["exclude_out_of_stock_product"].toBoolean() && product.isInventoryEnabled && product.availableStock <= 0;
                    if(!exclude && (config["if_identifier_rules_fail"] == "send_false" || count > 1)) {
                        Map response = [flag: false, config: []]
                        response = HookManager.hook("onGoogleProductXmlGenerate", response, product, config, variationConfig);
                        response.config.each { vConf ->
                            item getItemFeed(product, config, mappings, count, taxCode, vConf);
                        }
                        if(!response.flag) {
                            item getItemFeed(product, config, mappings, count, taxCode);
                        }
                    }
                }
            }
        }
    }

    String getXmlFeed() {
        StringWriter writer = new StringWriter()
        writeXmlFeed(writer)
        return writer.toString();
    }
}
