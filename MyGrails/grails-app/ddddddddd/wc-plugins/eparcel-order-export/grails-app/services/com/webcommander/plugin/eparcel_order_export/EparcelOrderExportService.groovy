package com.webcommander.plugin.eparcel_order_export

import com.webcommander.admin.Customer
import com.webcommander.config.StoreDetail
import com.webcommander.webcommerce.Order
import com.webcommander.webcommerce.Product
import org.supercsv.cellprocessor.ift.CellProcessor
import org.supercsv.io.CsvMapWriter
import org.supercsv.prefs.CsvPreference

import javax.servlet.http.HttpServletResponse
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class EparcelOrderExportService {


    public byte[] exportEParcel(List<Order> orders, Map params) {
        StoreDetail detail = StoreDetail.first()
        String storeName = detail.name;
        Closure getEmptyRow = { labels ->
            Map rowMap = [:];
            labels.each {
                rowMap.put it, ""
            }
            return rowMap;
        }
        StringWriter stringWriter = new StringWriter();
        CsvMapWriter writer = new CsvMapWriter(stringWriter, CsvPreference.EXCEL_PREFERENCE);
        def errorIds = [];
        orders.each {Order order ->
            String[] headerLabels = ["A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "AA", "AB", "AC", "AD", "AE",
                                     "AF", "AG", "AH", "AI", "AJ", "AK", "AL", "AM", "AN", "AO", "AP", "AQ", "AR", "AS", "AT", "AU", "AV", "AW", "AX", "AY", "AZ"] as String[];
            Map rowMap = getEmptyRow(headerLabels);
            rowMap.A = "C";
            rowMap.D = params.chargeCode;
            Boolean hasError = false;
            Customer customer = order.customer;
            if(customer){
                rowMap.F = customer.firstName + (customer.lastName ? " " + customer.lastName : "")
            } else {
                rowMap.F = order.billing.firstName + (order.billing.lastName ? " " + order.billing.lastName : "");

            }
            if(order.shipping) {
                order.shipping.with { address ->
                    rowMap.H = addressLine1
                    rowMap.I = address.addressLine2 ?: ""
                    rowMap.L = address.city ?: ""
                    rowMap.M = address.state?.code ?: ""
                    rowMap.N = address.postCode ?: ""
                    rowMap.O = address.country.code
                    rowMap.P = address.phone ?: ""
                    rowMap.Q = address.phone ? "Y" : "N"
                    rowMap.R = address.fax ?: ""
                    rowMap.AY = address.email
                }
            } else {
                hasError = true;
                errorIds.add(order.id)
            }
            rowMap.S = params.deliveryInstruction
            rowMap.T = params.requireSignature == "1" ? "Y" : "N"
            rowMap.W = params.addToAddressBook == "1" ? "Y" : "N"
            rowMap.Y = order.id;
            rowMap.Z = "Y"
            rowMap.AE = storeName
            rowMap.AZ = params.emailNotificationType
            detail.address.with { address ->
                rowMap.AF = address.addressLine1
                rowMap.AG = address.addressLine2 ?: ""
                rowMap.AJ = address.city ?: ""
                rowMap.AK = address.state?.code ?: ""
                rowMap.AL = address.postCode ?: ""
                rowMap.AM = address.country?.code ?: ""
            }
            if(!hasError) {
                writer.write(rowMap, headerLabels, new CellProcessor[52]);
                order.items.each { item ->
                    Map productRowMap = getEmptyRow(headerLabels)
                    productRowMap.A = "A"
                    Product product = Product.get(item.productId)
                    productRowMap.B = product?.weight ?: ""
                    productRowMap.C = product?.length ?: ""
                    productRowMap.D = product?.width ?: ""
                    productRowMap.E = product?.height ?: ""
                    productRowMap.F = item.quantity
                    writer.write(productRowMap, headerLabels, new CellProcessor[52]);
                }
            }
        }
        writer.close();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipOutputStream out = new ZipOutputStream(byteArrayOutputStream);
        out.putNextEntry(new ZipEntry("order.csv"));
        byte[] data = stringWriter.toString().getBytes();
        out.write(data, 0, data.length);
        out.closeEntry()
        if(errorIds.size() > 0) {
            StringBuffer sb = new StringBuffer();
            sb.append("Following order can not be exported: \r\n");
            for(int i = 0; i < errorIds.size(); i++) {
                sb.append(errorIds[i] + "\r\n");
            }
            out.putNextEntry(new ZipEntry("errorLog.txt"));
            byte[] d = sb.toString().getBytes();
            out.write(d, 0, d.length);
            out.closeEntry();
        }
        out.close();
        byteArrayOutputStream.close();
        return byteArrayOutputStream.toByteArray();
    }
    public void writeToOutputStream(Map params, HttpServletResponse response) {
        List<String> orderRanges = params.list("orderId")
        Set<Long> orderIds = new HashSet<>()
        orderRanges.each {
            if(it.contains("-")) {8
                Long begin = it.substring(0, it.indexOf("-")).toString().toLong(0)
                Long end = it.substring(it.indexOf("-") + 1, it.length()).toString().toLong(0)
                int len = end - begin
                for(int i = 0; i <= len; i++) {
                    orderIds.add(begin++)
                }
            } else {
                orderIds.add(it.toLong(0))
            }
        }
        List<Order> orders = orderIds ? Order.findAllByIdInList(orderIds as List) : []
        byte[] eParcel = exportEParcel(orders, params);
        response.setHeader("Pragma", "public");
        response.setHeader("Expires", "0");
        response.setHeader("Content-Type", "application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=orders.zip");
        response.outputStream << eParcel;
    }
}
