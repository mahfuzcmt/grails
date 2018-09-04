package com.webcommander.plugin.get_price

import com.webcommander.admin.ConfigService
import com.webcommander.calculator.TaxCalculator
import com.webcommander.common.FileService
import com.webcommander.config.StoreDetail
import com.webcommander.constants.NamedConstants
import com.webcommander.manager.HookManager
import com.webcommander.models.ProductData
import com.webcommander.util.StringUtil
import com.webcommander.webcommerce.Category
import com.webcommander.constants.DomainConstants
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.CombinedProduct
import com.webcommander.webcommerce.Product
import com.webcommander.webcommerce.ProductService
import com.webcommander.webcommerce.TaxCode
import grails.gorm.transactions.Transactional
import groovy.xml.MarkupBuilder
import org.apache.commons.lang.StringEscapeUtils
import org.apache.poi.hssf.usermodel.HSSFSheet
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import grails.web.servlet.mvc.GrailsParameterMap
import org.hibernate.SessionFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.w3c.dom.Document
import org.w3c.dom.Element

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

class GetPriceService {
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

    static GET_PRICE_CATEGORY = "get-price-category"
    static GET_PRICE_CATEGORY_XLS = "category.xls"

    Boolean updateCategory(List categories) {
        try {
            String relativePath = GET_PRICE_CATEGORY + File.separator + GET_PRICE_CATEGORY_XLS
            String path = appResource.getCustomRestrictedResourcePath(relativePath: GET_PRICE_CATEGORY)
            if (!new File(path).exists()) {
                new File(path).mkdirs()
                def file = new File(path)
                file.setWritable(true, true)
            }
            Workbook wb = new HSSFWorkbook()
            Sheet sheet = wb.createSheet("getPriceCategory")
            categories.eachWithIndex { v, i ->
                Row row = sheet.createRow((short)i)
                v.split("\\,\\ ").toList().eachWithIndex { vl, j ->
                    row.createCell(j).setCellValue(vl)
                }
            }
            String filePath = appResource.getCustomRestrictedResourcePath(relativePath: relativePath)
            FileOutputStream fileOut = new FileOutputStream(filePath)
            wb.write(fileOut)
            fileOut.close()
            fileService.uploadModifiableResource(new File(filePath), relativePath)
            return true
        } catch (Exception e) {
            e.printStackTrace()
            return false
        }
    }

    List excelFileToMap() {
        Map rootMap = [:]
        List rootList = []
        InputStream inputStream = fileService.getModifiableResourceStream(GET_PRICE_CATEGORY + File.separator + GET_PRICE_CATEGORY_XLS)
        if (!inputStream) {
            return rootList
        }
        HSSFWorkbook workbook = new HSSFWorkbook (inputStream)
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
        Map mappings = GetPriceCategoryMapping.list().collectEntries {[(it.categoryId + ""): it.getPriceCategory]};
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
                exists GetPriceCategoryMapping.where {
                    def m1 = GetPriceCategoryMapping
                    c1.id == m1.categoryId
                }.id()
            } else if(type == "unmapped") {
                notExists GetPriceCategoryMapping.where {
                    def m1 = GetPriceCategoryMapping
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
        Long categoryId = params.long("categoryId")
        GetPriceCategoryMapping mapping = GetPriceCategoryMapping.findByCategoryId(categoryId)
        if(!mapping) {
            mapping = new GetPriceCategoryMapping()
        }
        mapping.categoryId = categoryId
        mapping.getPriceCategory = params.getPriceCategory
        mapping.save()
        sessionFactory.cache.evictQueryRegions()
        return !mapping.hasErrors()
    }

    @Transactional
    Boolean removeMapping(GrailsParameterMap params) {
        Long categoryId = params.long("categoryId");
        GetPriceCategoryMapping mapping = GetPriceCategoryMapping.findByCategoryId(categoryId);
        mapping.delete()
        sessionFactory.cache.evictQueryRegions();
        return true
    }

    Element getProductNode(Document doc, Product product, Category parent = null, Map vConf = [:]) {
        ProductData data = vConf.data
        def productData = data ?: product
        def imageSize = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.PRODUCT_IMAGE, "size")[NamedConstants.PRODUCT_IMAGE_SETTINGS.DETAILS]

        Element element = doc.createElement("PRODUCT")
        element.setAttribute("NUM", "" + product.id)

        Element productNumber = doc.createElement("PRODUCT_NUM")
        productNumber.setTextContent("" + product.id)
        element.appendChild(productNumber)

        Element upc = doc.createElement("UPC")
        upc.setTextContent("" + productData.sku)
        element.appendChild(upc)

        Element product_name = doc.createElement("PRODUCT_NAME")
        product_name.setTextContent(StringEscapeUtils.escapeXml(data ? vConf.name : product.name))
        element.appendChild(product_name)

        Element category_name = doc.createElement("CATEGORY_NAME")
        category_name.setTextContent(StringEscapeUtils.escapeXml(parent ? parent.name : Category.findByProductsInList([product])?.name))
        element.appendChild(category_name)

        if (product.model) {
            Element model = doc.createElement("MODEL")
            model.setTextContent(productData.model)
            element.appendChild(model)
        }

        Element description = doc.createElement("DESCRIPTION")
        String text = StringUtil.RemoveHTMLTag(productData.description.replaceAll('&nbsp;', ''))
        description.setTextContent(StringEscapeUtils.escapeXml(text))
        element.appendChild(description)

        Element product_url = doc.createElement("PRODUCT_URL")
        product_url.setTextContent(app.nonRequestBaseUrl() + "product/${productData.url}")
        element.appendChild(product_url)

        Element price = doc.createElement("PRICE")
        Double pPrice = productData.basePrice
        if (product.isCombined && !product.isCombinationPriceFixed) {
            pPrice = getCombinationPrice(product);
        }
        price.setTextContent("${resolveTax(pPrice).toPrice()}")
        element.appendChild(price)

        if (productData.isOnSale) {
            Element sale_price = doc.createElement("SALE_PRICE")
            sale_price.setTextContent("${resolveTax(productData.salePrice).toPrice()}")
            element.appendChild(sale_price)
        }

        Element image = doc.createElement("IMAGE")
        String url = appResource.getProductImageFullUrl(product: productData, imageSize: imageSize)
        image.setTextContent(url)
        element.appendChild(image)

        if (product.manufacturer) {
            Element manufacturer = doc.createElement("MANUFACTURER")
            manufacturer.setTextContent("${StringEscapeUtils.escapeXml(product.manufacturer.name)}")
            element.appendChild(manufacturer)
        }
        return element
    }

    def getProductXmlFeed(List<Product> productList) {
        Map config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GET_PRICE)
        StringWriter writer = new StringWriter()
        MarkupBuilder xml = new MarkupBuilder(writer)
        xml.mkp.xmlDeclaration(version: "1.0", encoding: "utf-8")

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance()
        DocumentBuilder builder = factory.newDocumentBuilder()
        Document doc = builder.newDocument()

        Element root = doc.createElement("STORE")
        root.setAttribute("URL", "" + app.siteBaseUrl())
        root.setAttribute("DATE", new Date().getDateString())
        root.setAttribute("TIME", new Date().getTimeString())
        root.setAttribute("NAME", StoreDetail.findAll().first().name)
        doc.appendChild(root)

        Element productElm = doc.createElement("PRODUCTS")
        root.appendChild(productElm)
        productList.each { product ->
            if(!product.description) {
                return
            }
            if (product.parents.size() > 0 ) {
                product.parents.each { parent ->
                    Map response = [flag: false, config: []]
                    response = HookManager.hook("onGetPriceXmlGenerate", response, product, config);
                    response.config.each { vConf ->
                        Element xmlElm = getProductNode(doc, product, parent, vConf)
                        productElm.appendChild(xmlElm)
                    }
                    if (!response.flag) {
                        Element xmlElm = getProductNode(doc, product, parent)
                        productElm.appendChild(xmlElm)
                    }
                }
            } else {
                Map response = [flag: false, config: []]
                response = HookManager.hook("onGetPriceXmlGenerate", response, product, config);
                response.config.each { vConf ->
                    Element xmlElm = getProductNode(doc, product, null, vConf)
                    productElm.appendChild(xmlElm)
                }
                if (!response.flag) {
                    Element xmlElm = getProductNode(doc, product)
                    productElm.appendChild(xmlElm)
                }
            }
        }

        return AppUtil.XMLNodeToString(doc)
    }

    def getCategoryXmlFeed() {
        List<GetPriceCategoryMapping> mappings = GetPriceCategoryMapping.list()
        List<Category> categoryList
        if(mappings.size()){
            categoryList = Category.createCriteria().list {
                'in'("id", mappings.categoryId)
                eq("isDisposable", false)
                eq("isInTrash", false)
            }
        } else {
            categoryList = []
        }

        StringWriter writer = new StringWriter()
        MarkupBuilder xml = new MarkupBuilder(writer)
        xml.mkp.xmlDeclaration(version: "1.0", encoding: "utf-8")
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance()
        DocumentBuilder builder = factory.newDocumentBuilder()
        Document doc = builder.newDocument()
        Element root = doc.createElement("STORE")
        root.setAttribute("URL", "" + app.siteBaseUrl())
        root.setAttribute("DATE", new Date().getDateString())
        root.setAttribute("TIME", new Date().getTimeString())
        root.setAttribute("NAME", StoreDetail.findAll().first().name)
        doc.appendChild(root)

        categoryList.each { cat ->
            Element category = doc.createElement("cat")

            Element name = doc.createElement("name")
            name.setTextContent(StringEscapeUtils.escapeXml(cat.name))
            category.appendChild(name)

            Element link = doc.createElement("link")
            link.setTextContent(app.siteBaseUrl() + "getPrice/product?id=${cat.id}")
            category.appendChild(link)

            root.appendChild(category)
        }
        return AppUtil.XMLNodeToString(doc)
    }

    private List<Category> getParentCategories(Category category) {
        def cateList = [category]
        Category parent = category.parent
        while(parent) {
            cateList.add(parent)
            parent = parent.parent
        }
        return cateList
    }

    private Double resolveTax(Double price) {
        String priceWithTax = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.TAX, "is_price_with_tax")
        def code = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GET_PRICE, "tax_code")
        TaxCode taxCode = TaxCode.get(code)
        if(priceWithTax != "true" && taxCode) {
            price += TaxCalculator.getTax(taxCode, price);
        }
        return price
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
