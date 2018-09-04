package com.webcommander.tenant

/**
 * Created by touhid on 28/08/2016.*/
class TenantProps {
    private String name
    private String username
    private String password
    private String hostname
    private String database
    private String server
    private Properties dbProperties
    private Properties configs
    private List<String> aliases

    List<String> getAliases() {
        return aliases
    }

    void setAliases(List<String> aliases) {
        this.aliases = aliases
    }

    void addAlias(String alias) {
        (aliases ?: (aliases = [])).add(alias)
    }

    String getUrl() {
        return DbUrlPatterns.getUrl(server, hostname, database)
    }

    String getDatabase() {
        return database
    }

    void setDatabase(String database) {
        this.database = database
    }

    String getName() {
        return name
    }

    void setName(String name) {
        this.name = name
    }

    String getUsername() {
        return username
    }

    void setUsername(String username) {
        this.username = username
    }

    String getPassword() {
        return password
    }

    void setPassword(String password) {
        this.password = password
    }

    String getHostname() {
        return hostname
    }

    void setHostname(String hostname) {
        this.hostname = hostname
    }

    String getServer() {
        return server
    }

    void setServer(String server) {
        this.server = server
    }

    Properties getDbProperties() {
        return dbProperties
    }

    void setDbProperties(Properties properties) {
        this.dbProperties = properties
    }

    Properties getConfigs() {
        return configs
    }

    void setConfigs(Properties configs) {
        this.configs = configs
    }
}