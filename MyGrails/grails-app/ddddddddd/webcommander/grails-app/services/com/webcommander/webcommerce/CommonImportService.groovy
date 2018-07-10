package com.webcommander.webcommerce

import com.webcommander.common.MetaTag
import com.webcommander.task.TaskLogger
import com.webcommander.util.AppUtil
import grails.util.Holders
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row

class CommonImportService {

    String getCellValue(Row row, int colNum) {
        String cellValue = null
        Cell cell = row.getCell(colNum, Row.RETURN_BLANK_AS_NULL)
        if(cell != null) {
            cell.setCellType(Cell.CELL_TYPE_STRING)
            cellValue = cell.stringCellValue
        }
        return cellValue
    }

    InputStream findImageStream(String image, String imageLocation) {
        if(imageLocation.startsWith("/")) {
            imageLocation = imageLocation.substring(1)
        }
        try {
            File imageDir = new File(Holders.servletContext.getRealPath(imageLocation))
            File file = new File(imageDir, image);
            if(file.exists()) {
                return new FileInputStream(file)
            }
        } catch(Exception exc) {
            log.error("Import Image Error: " + exc)
            return null
        }
        return null
    }

    List<MetaTag> generateMetaTags(String metaTagStr) {
        List<MetaTag> metaTags = []
        if(metaTagStr) {
            List<String> splitData = metaTagStr.split(",")
            for(int i = 0; i < splitData.size(); i += 2) {
                String name = splitData[i].trim()
                String value = splitData[i+1].trim()
                MetaTag metaTag = MetaTag.findByName(name)
                if(metaTag && value) {
                    metaTag.value = value
                    metaTag.merge()
                } else if(name && value) {
                    metaTag = new MetaTag()
                    metaTag.name = name
                    metaTag.value = value
                    metaTag.save()
                }
                metaTags.add(metaTag)
            }
        }
        return metaTags
    }

    public void emptyFieldWarning(String field, TaskLogger taskLogger, String logFor) {
        taskLogger.warning(logFor, "${field}.field.empty")
    }

    public void dataNotFoundWarning(String field, TaskLogger taskLogger, String logFor) {
        taskLogger.warning(logFor, "${field}.not.found")
    }
}