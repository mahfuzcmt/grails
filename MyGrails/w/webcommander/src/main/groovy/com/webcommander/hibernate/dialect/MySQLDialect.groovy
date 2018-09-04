package com.webcommander.hibernate.dialect

import org.hibernate.dialect.MySQL57InnoDBDialect

import java.sql.Types

/**
 * Created by zobair on 04/12/2014.
 */
class MySQLDialect extends MySQL57InnoDBDialect {
    MySQLDialect() {
        this.registerColumnType(Types.BOOLEAN, "tinyint(1)")
    }
}