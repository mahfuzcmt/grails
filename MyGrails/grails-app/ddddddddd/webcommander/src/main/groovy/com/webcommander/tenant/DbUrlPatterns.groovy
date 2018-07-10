package com.webcommander.tenant

class DbUrlPatterns {
    static String getUrl(serverType, host, dbName) {
        switch (serverType) {
            case "mysql":
                return "jdbc:mysql://$host/$dbName?useUnicode=yes&characterEncoding=UTF-8&createDatabaseIfNotExist=true"
            case "derby_embedded":
                return "jdbc:derby:$dbName"
            case "mssql":
                return "jdbc:sqlserver://$host;databaseName=$dbName"
        }
    }

    static String getDriver(serverType) {
        switch (serverType) {
            case "mysql":
                return "com.mysql.jdbc.Driver"
            case "derby_embedded":
                return "org.apache.derby.jdbc.EmbeddedDriver"
            case "mssql":
                return "com.microsoft.sqlserver.jdbc.SQLServerDriver"
        }
    }
}