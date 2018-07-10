package com.webcommander.webcommerce

import com.webcommander.constants.NamedConstants
import com.webcommander.item.ProductRawData
import com.webcommander.manager.HookManager
import grails.gorm.transactions.Transactional
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.grails.plugins.web.taglib.RenderTagLib
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

@Transactional
class ExportService {
    ProductService productService
    CategoryService categoryService
    @Autowired
    @Qualifier("org.grails.plugins.web.taglib.RenderTagLib")
    RenderTagLib g;

    private void addHeaderRow(XSSFSheet sheet, Map header) {
        Row row = sheet.createRow(0)
        header.eachWithIndex { def entry, int i ->
            Cell cell = row.createCell(i)
            cell.setCellValue(entry.value)
        }
    }

    void addProductRow(XSSFSheet sheet, Map header, ProductRawData product, int rowCount) {
        Row row = sheet.createRow(rowCount)
        product.properties
        header.eachWithIndex { def entry, int i ->
            Cell cell = row.createCell(i)
            cell.setCellValue(product[entry.key] ?: "");
        }
    }

    private void addCategoryRow(XSSFSheet sheet, Map header, Category category, int rowCount) {
        Row row = sheet.createRow(rowCount)
        header.eachWithIndex { def entry, int i ->
            Cell cell = row.createCell(i)
            def value = "";
            switch (entry.key) {
                case "metaTags":
                    value = category.metaTags.collect { it.name + "," + it.value  }.join(",");
                    break;
                case "shippingProfile":
                case "taxProfile":
                    value = category[entry.key]?.name
                    break
                case "parent":
                    value = category.parent ? category.parent?.sku : "";
                    break;
                default:
                    value = category[entry.key] ?: "";
            }
            cell.setCellValue(value);
        }
    }

    def export(Map params, OutputStream stream) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        if (params.exportProduct.toBoolean()) {
            Map productInfo = params.product;
            XSSFSheet productSheet = workbook.createSheet("Product");
            Map header = [:];
            (NamedConstants.PRODUCT_IMPORT_FIELDS + NamedConstants.PRODUCT_IMPORT_EXTRA_FIELDS).each {
                if (productInfo[it.key].toBoolean()) {
                    header[it.key] = productInfo.label[it.key] ?: g.message(code: it.value);
                }
            }
            addHeaderRow(productSheet, header);
            List products = productService.getProducts([parent: "all"]);
            Integer rowCount = 1;
            for (Product product : products) {
                addProductRow(productSheet, header, new ProductRawData(product), rowCount++);
                rowCount = HookManager.hook("productExport", rowCount, product, header, productSheet)
            }
        }
        if (params.exportCategory.toBoolean()) {
            Map categoryInfo = params.category;
            XSSFSheet productSheet = workbook.createSheet("Category");
            Map header = [:];
            NamedConstants.CATEGORY_IMPORT_FIELDS.each {
                if (categoryInfo[it.key].toBoolean()) {
                    header[it.key] = categoryInfo.label[it.key] ?: g.message(code: NamedConstants.CATEGORY_IMPORT_FIELDS[it.key]);
                }
            }
            addHeaderRow(productSheet, header);
            List categories = categoryService.getCategories([:]);
            Integer rowCount = 1;
            for (Category category : categories) {
                addCategoryRow(productSheet, header, category, rowCount++);
            }
        }
        workbook.write(stream)
        stream.close()
    }
}
