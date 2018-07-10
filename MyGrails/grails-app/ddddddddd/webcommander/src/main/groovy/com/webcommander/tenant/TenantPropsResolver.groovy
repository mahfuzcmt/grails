package com.webcommander.tenant

import com.webcommander.throwables.ApplicationRuntimeException
import grails.util.Holders
import groovy.util.logging.Log

import java.sql.*
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Level

/**
 * Created by touhid on 28/08/2016.
 */
@Log
class TenantPropsResolver {
    private String JDBC_DRIVER_CLASS = DbUrlPatterns.getDriver Holders.config.webcommander.multiTenant.tenantResolverDataSource.server

    private String SERVER_URL = DbUrlPatterns.getUrl(Holders.config.webcommander.multiTenant.tenantResolverDataSource.server, Holders.config.webcommander.multiTenant.tenantResolverDataSource.host, Holders.config.webcommander.multiTenant.tenantResolverDataSource.name)
    private String SERVER_USER = Holders.config.webcommander.multiTenant.tenantResolverDataSource.username
    private String SERVER_PASSWORD = Holders.config.webcommander.multiTenant.tenantResolverDataSource.password

    private Connection connection = null
    private Statement statement = null

    private static List<String> tenantIds
    private static Map<String, TenantProps> tenantPropsCache = new ConcurrentHashMap<>()
    private static Map<String, String> aliasMaps = new ConcurrentHashMap<>()

    TenantPropsResolver() {
        try {
            openConnection()
        } catch (Throwable e) {
            log.log Level.SEVERE, "Could not open connection to database: $SERVER_URL using user: $SERVER_USER and pass: ??", e
        }
    }

    private void openConnection() throws ClassNotFoundException, SQLException {
        Class.forName(JDBC_DRIVER_CLASS)
        connection = DriverManager.getConnection(SERVER_URL, SERVER_USER, SERVER_PASSWORD)
        statement = connection.createStatement()
    }

    void closeConnection() {
        if (statement != null) {
            try {
                statement.close()
            } catch (SQLException e) {}
        }
        if (connection != null) {
            try {
                connection.close()
            } catch (SQLException e) {}
        }
    }

    private static collectAllTenantIds(){
        tenantIds = Collections.synchronizedList([])
        TenantPropsResolver resolver = new TenantPropsResolver()
        try {
            String select = "SELECT id FROM tenant_info where active = 1"
            ResultSet resultSet = resolver.statement.executeQuery(select)
            while (resultSet.next()) {
                tenantIds.add(resultSet.getString("id"))
            }
        } catch (SQLException e) {
            println("Error From collectAllTenantIds: " + e.getMessage())
        } finally {
            resolver.closeConnection()
        }
        return tenantIds
    }


    static List<String> getTenantIds() {
        if(tenantIds) {
            return tenantIds
        }
        return collectAllTenantIds()
    }

    static String lookupForTenantId(String alias) {
        if(getTenantIds().contains(alias)) {
            return alias
        }
        if(aliasMaps.containsKey(alias)) {
            return aliasMaps[alias]
        }
        return null
    }

    static void addTenant(String tenantId, TenantProps tenantProps) {
        getTenantIds().add tenantId
        tenantPropsCache[tenantId] = tenantProps
    }

    static void clearCaches() {
        tenantIds = null
        aliasMaps.clear()
        tenantPropsCache.clear()
    }

    static void reload() {
        String select = "SELECT * FROM tenant_info"
        TenantPropsResolver connection = new TenantPropsResolver()
        ResultSet resultSet = connection.statement.executeQuery(select)
        while (resultSet.next()) {
            getTenantPropForId(resultSet.getString("id"))
        }
        resultSet.close()
        tenantIds = null
        collectAllTenantIds()
    }

    static void removeTenant(String tenantId) {
        getTenantIds().remove tenantId
        tenantPropsCache.remove tenantId
    }

    static TenantProps getTenantPropForId(String tenantId) {
        TenantProps tenantProps = tenantPropsCache[tenantId]
        if(tenantProps) {
            return tenantProps
        }
        TenantPropsResolver connection = new TenantPropsResolver()
        String select = "SELECT * FROM tenant_info WHERE id = '" + tenantId + "'"
        try {
            ResultSet resultSet = connection.statement.executeQuery(select)
            tenantProps = new TenantProps()
            while (resultSet.next()) {
                tenantProps.setName(resultSet.getString("name"))
                tenantProps.setDatabase(resultSet.getString("db_name"))
                tenantProps.setPassword(resultSet.getString("password"))
                tenantProps.setUsername(resultSet.getString("username"))
                tenantProps.setHostname(resultSet.getString("host"))
                tenantProps.setServer(resultSet.getString("server"))
            }
            resultSet.close()

            select = "SELECT * FROM tenant_configs WHERE tenant_name = '" + tenantProps.name + "'"
            resultSet = connection.statement.executeQuery(select)
            Properties properties = new Properties()
            tenantProps.configs = properties
            while (resultSet.next()) {
                properties.put(resultSet.getString("key"), resultSet.getString("value"))
            }
            resultSet.close()

            select = "SELECT * FROM db_properties WHERE tenant_name = '" + tenantProps.name + "'"
            resultSet = connection.statement.executeQuery(select)
            properties = new Properties()
            tenantProps.dbProperties = properties
            while (resultSet.next()) {
                properties.put(resultSet.getString("key"), resultSet.getString("value"))
            }
            resultSet.close()

            select = "SELECT * FROM tenant_alias WHERE tenant_name = '" + tenantProps.name + "'"
            resultSet = connection.statement.executeQuery(select)
            while (resultSet.next()) {
                String alias = resultSet.getString("alias")
                tenantProps.addAlias alias
                aliasMaps.put(alias, tenantId)
            }
            resultSet.close()

            return tenantPropsCache[tenantId] = tenantProps
        } catch (SQLException e) {} finally {
            connection.closeConnection()
        }
        throw new ApplicationRuntimeException("datasouce.info.not.found.for.x", [tenantId])
    }
}