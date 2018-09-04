import com.mysql.jdbc.Driver
import grails.util.Holders
import groovy.sql.Sql

import java.sql.Connection

class PluginDestroyer {

    public void destroy() {
        Properties ppt = new Properties()
        ppt.user = Holders.config.dataSource.username
        ppt.password = Holders.config.dataSource.password
        String url = Holders.config.dataSource.url;
        Connection connection = Driver.newInstance().connect(url, ppt)
        Sql sql = new Sql(connection);
        sql.execute('DELETE FROM site_config WHERE `type` = "myob"');
        sql.execute("DROP TABLE `myob_link`");
        connection.close();
    }
}