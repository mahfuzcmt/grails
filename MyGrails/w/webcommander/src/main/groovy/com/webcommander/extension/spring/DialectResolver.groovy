package com.webcommander.extension.spring

import com.webcommander.hibernate.dialect.MySQLDialect
import com.webcommander.tenant.TenantContext
import com.webcommander.tenant.TenantPropsResolver
import com.webcommander.util.AppUtil
import org.hibernate.dialect.DerbyTenSevenDialect
import org.hibernate.dialect.Dialect
import org.hibernate.dialect.SQLServer2012Dialect

class DialectResolver {
    private static Map<String, Class> serverDialectInstancesMap = [:]
    private static Map<String, Class> serverDialectClassMap = [mysql: MySQLDialect, mssql: SQLServer2012Dialect, derby: DerbyTenSevenDialect]

    static Dialect getCurrentDialect() {
        String server = TenantPropsResolver.getTenantPropForId(TenantContext.currentTenant).server
        Dialect dialect = serverDialectInstancesMap[server]
        if (!dialect) {
            dialect = serverDialectClassMap[server].newInstance()
            serverDialectInstancesMap[server] = dialect
        }
        return dialect
    }
}