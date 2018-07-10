package com.webcommander.plugin.my_shopping

import com.webcommander.admin.ConfigService
import com.webcommander.calculator.TaxCalculator
import com.webcommander.common.FileService
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.manager.HookManager
import com.webcommander.models.ProductData
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.CombinedProduct
import com.webcommander.webcommerce.Product
import com.webcommander.webcommerce.ProductService
import com.webcommander.webcommerce.TaxCode
import grails.gorm.transactions.Transactional
import groovy.xml.MarkupBuilder
import org.apache.poi.hssf.usermodel.HSSFSheet
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import com.webcommander.webcommerce.Category
import grails.web.servlet.mvc.GrailsParameterMap
import org.hibernate.SessionFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

import javax.lang.model.element.Name

/**
 * Created by sajed on 6/1/2014.
 */
@Transactional
class MyShoppingService {
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

    static MY_SHOPPING_CATEGORY = "my-shopping-category"
    static CATEGORY_XLS = "category.xls"

    List excelFileToMap() {
        Map rootMap = [:]
        List rootList = []
        InputStream inputStream = fileService.getRestrictedResourceStream("${MY_SHOPPING_CATEGORY}/${CATEGORY_XLS}")
        HSSFWorkbook workbook = new HSSFWorkbook(inputStream)
        HSSFSheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.iterator();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
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
        Map mappings = MyShoppingMapping.list().collectEntries { [(it.categoryId + ""): it.path] };
        List tree = []
        Category.where {
            isInTrash == false
            isParentInTrash == false
            isAvailable == true
            isDisposable == false
            availableFor == DomainConstants.CATEGORY_AVAILABLE_FOR.EVERYONE
            order "idx"
            def c1 = Category
            if (type == "mapped") {
                exists MyShoppingMapping.where {
                    def m1 = MyShoppingMapping
                    c1.id == m1.categoryId
                }.id()
            } else if (type == "unmapped") {
                notExists MyShoppingMapping.where {
                    def m1 = MyShoppingMapping
                    c1.id == m1.categoryId
                }.id()
            }
        }.list().each {
            String mapping = mappings["" + it.id]
            Map node = nodeCatMap["" + it.id] ?: [id: it.id, name: it.name.encodeAsBMHTML(), mapping: mapping]
            nodeCatMap["" + it.id] = node
            if (it.parent) {
                Map parentNode = nodeCatMap["" + it.parent.id]
                if (parentNode) {
                    if (parentNode.children) {
                        parentNode.children.add(node)
                    } else {
                        parentNode.children = [node]
                    }
                } else {
                    Category parent = it.parent
                    String pMapping = mappings["" + parent.id]
                    parentNode = [id: parent.id, name: parent.name.encodeAsBMHTML(), mapping: pMapping, children: [node]]
                    nodeCatMap["" + parent.id] = parentNode
                    if (type == "mapped" && !pMapping) {
                        while (true) {
                            if (!parent.parent) {
                                tree.add(parentNode)
                                break;
                            }
                            parent = parent.parent
                            pMapping = mappings["" + parent.id]
                            parentNode = [id: parent.id, name: parent.name.encodeAsBMHTML(), mapping: pMapping, children: [parentNode]]
                            nodeCatMap["" + parent.id] = parentNode
                            if (pMapping) {
                                break;
                            }
                        }
                    } else if (type == "unmapped" && pMapping) {
                        while (true) {
                            if (!parent.parent) {
                                tree.add(parentNode)
                                break;
                            }
                            parent = parent.parent
                            pMapping = mappings["" + parent.id]
                            parentNode = [id: parent.id, name: parent.name.encodeAsBMHTML(), mapping: pMapping, children: [parentNode]]
                            nodeCatMap["" + parent.id] = parentNode
                            if (!pMapping) {
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
        MyShoppingMapping mapping = MyShoppingMapping.findByCategoryId(categoryId);
        if (!mapping) {
            mapping = new MyShoppingMapping()
        }
        mapping.categoryId = categoryId;
        mapping.myShoppingCategory = params.category
        mapping.path = params.path
        mapping.save()
        sessionFactory.cache.evictQueryRegions();
        return !mapping.hasErrors()
    }

    @Transactional
    Boolean removeMapping(GrailsParameterMap params) {
        Long categoryId = params.long("categoryId");
        MyShoppingMapping mapping = MyShoppingMapping.findByCategoryId(categoryId);
        mapping.delete()
        sessionFactory.cache.evictQueryRegions();
        return true
    }

    private Closure getItemFeed(Product product, Map config, List<MyShoppingMapping> mappings, TaxCode taxCode, Map vConf = [:]) {
        def imageSize = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.PRODUCT_IMAGE, "size")[NamedConstants.PRODUCT_IMAGE_SETTINGS.DETAILS]
        return {
            ProductData data = vConf.data
            def productData = data ?: product;
            Double price;
            String imageLink;
            Name(data ? vConf.name : product.name)
            Product_URL(app.nonRequestBaseUrl() + "product/" + productData.url)
            description(productData.description.textify().truncate(255))
            Code(productData.sku)
            if(productData.isOnSale) {
                price = productData.salePrice
            } else {
                price = productData.basePrice
            }
            if(taxCode) {
                price += TaxCalculator.getTax(taxCode, price);
            }
            Price(price)
            if(productData.isInventoryEnabled && productData.availableStock <= 0) {
                InStock("N")
            } else {
                InStock("Y")
            }
            imageLink = appResource.getProductImageFullUrl(product: productData, imageSize: imageSize)
            Image_URL(app.nonRequestBaseUrl() + imageLink)
            Category(mappings.find { it.categoryId == product.parent.id }.myShoppingCategory)
        }

    }

    void writeXmlFeed(Writer writer) {
        List<MyShoppingMapping> mappings = MyShoppingMapping.list();
        List<Product> products
        Map config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.MY_SHOPPING)
        Map filerMap = [max: "-1", offset: "0", parentList: mappings.categoryId, category: "primary"];
        if(config.submit_combination == "false") {
            filerMap.notCombined = true
        }
        if (mappings.size()) {
            products = productService.getProducts(filerMap)
        } else {
            products = []
        }
        TaxCode taxCode = null
        String priceWithTax = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.TAX, "is_price_with_tax")
        if(priceWithTax != "true" && config.tax_code != "none") {
            taxCode = TaxCode.get(config.tax_code);
        }
        MarkupBuilder xml = new MarkupBuilder(writer)
        xml.mkp.xmlDeclaration(version: "1.0", encoding: "utf-8")
        xml.productset {
            products.each { Product prod ->
                Map response = [flag: false, config: []]
                response = HookManager.hook("onMyShoppingXmlGenerate", response, prod, config);
                response.config.each { vConf ->
                    item getItemFeed(prod, config, mappings, taxCode, vConf);
                }
                if (!response.flag && prod.description) {
                    product getItemFeed(prod, config, mappings, taxCode);
                }
            }
        }
    }

    String getXmlFeed() {
        StringWriter writer = new StringWriter()
        writeXmlFeed(writer)
        return writer.toString();
    }

    private Double getCombinationPrice(Product product) {
        List<CombinedProduct> combinedProducts = CombinedProduct.where {
            baseProduct == product
        }.list()
        Double price = 0;
        combinedProducts.eachWithIndex { CombinedProduct it, int i ->
            Double effectivePrice = it.includedProduct.isOnSale ? it.includedProduct.salePrice : it.includedProduct.basePrice;
            price += effectivePrice * it.quantity
        }
        return price
    }
}