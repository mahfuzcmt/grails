package com.webcommander.plugin.awstats.controllers.admin

import com.webcommander.authentication.annotations.License
import grails.util.Holders
import groovy.io.FileType

import java.text.DateFormatSymbols
import java.text.SimpleDateFormat

class AwstatsController {

    @License(required = "allow_awstats_feature")
    def loadAppView() {
        render(view: "/plugins/awstats/admin/loadAppView")
    }

    def statistics() {
        String serverName = Holders.config.grails.server.hostname;
        if (serverName.startsWith("www.")) {
            serverName = serverName.substring(4)
        }
        Calendar calendar = Calendar.getInstance(session.timezone)
        String month = params.month ? params.month : new SimpleDateFormat("MM").format(new Date())
        String year = params.year ? params.year : calendar.get(Calendar.YEAR)
        String statsDirPath = servletContext.getRealPath("/WEB-INF/awstats/html")
        File statsDir = new File(statsDirPath)
        List yearDir = []
        Boolean found = false
        String requestedFile = params.link
        if(statsDir.exists() && statsDir.isDirectory()) {
            statsDir.eachFile(FileType.DIRECTORIES, {File dir->
                yearDir.add(dir.name)
                found = true
            })
        }

        String prepandText = "<tr><td class=\"aws\" valign=\"middle\"><b>Reported period:</b></td><td class=\"aws\" valign=\"middle\"><select class=\"aws_formfield\" name=\"month\">";
        if (month == "all") {
            prepandText += "<option selected=\"true\" value=\"all\">Yearly</option>";
        } else {
            prepandText += "<option value=\"all\">Yearly</option>";
        }
        DateFormatSymbols dfs = new DateFormatSymbols();
        String[] months = dfs.getMonths();
        months.eachWithIndex { String entry, int i ->
            i++
            if ((i < 10 ? "0" + i : i) == month) {
                prepandText += "<option selected value=\"$i\">$entry</option>";
            } else {
                prepandText += "<option value=\"$i\">$entry</option>";
            }
        }
        prepandText +="</select>";

        prepandText +="<select class=\"aws_formfield\" name=\"year\">";
        yearDir.eachWithIndex { Object entry, int i ->
            if (yearDir[i] == year) {
                prepandText +="<option selected value=\"${yearDir[i]}\">${yearDir[i]}</option>";
            } else {
                prepandText +="<option value=\"${yearDir[i]}\">${yearDir[i]}</option>";
            }
        } 
        prepandText +="</select>";
        prepandText +="<input type=\"submit\" value=\" OK \" class=\"aws_button\"/></td></tr>";
        if ((month != "all") && month.length() == 1 ) {
            month = "0" + month;
        }

        if(!requestedFile) {
            requestedFile = "awstats.${serverName}.html"
        }

        File htmlFile = new File(statsDirPath + File.separator + year + File.separator + month + File.separator + requestedFile)
        if (htmlFile.exists()) {
            String html = htmlFile.text
            html = html.replaceFirst("<tr><td class=\"aws\" valign=\"middle\"><b>Reported period:</b></td><td class=\"aws\" valign=\"middle\"><span style=\"font-size: 14px;\">.*</span></td></tr>", prepandText)
            html = html.replaceAll("form name=\"FormDateFilter\" action=\"/awstats/awstats\\.pl\\?config=${serverName}&amp;staticlinks[^\"]*\"", "form name=\"FormDateFilter\" action=\"${app.relativeBaseUrl()}awstats/statistics\"")
            html = html.replaceAll("/app/webroot/", "")
            html = html.replaceAll(/a href=\"awstats\.(${serverName}\.\w+\.html)/, { all, f ->
                return "a href=\"${app.relativeBaseUrl()}awstats/statistics?month=$month&year=$year&link=awstats.$f"
            });
            render(text: html, contentType: "text/html")
        } else {
            render(view: "/plugins/awstats/admin/noStat", model: [prepandText: prepandText, serverName: serverName])
        }
    }

    def staticResources() {
        String fileName = params.fileName ?: ""
        File file
        if(fileName.startsWith("icon/")) {
            file = new File(servletContext.getRealPath("/plugins/awstats/images/$params.fileName"))
        }
        if(file && file.exists()) {
            String mimeType = URLConnection.guessContentTypeFromName(file.getName());
            response.setHeader("Content-Type", mimeType)
            response.setHeader("Content-Length", "${file.length()}")
            InputStream inputStream = new FileInputStream(file)
            response.outputStream << inputStream
            inputStream.close()
            response.outputStream.flush()
        }
    }
}
