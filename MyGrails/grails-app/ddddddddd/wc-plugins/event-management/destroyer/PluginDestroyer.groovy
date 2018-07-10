import com.webcommander.util.AppUtil
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
        sql.execute("DROP TABLE IF EXISTS `event_image`");
        sql.execute("DROP TABLE IF EXISTS `event_session_topic`");
        sql.execute("DROP TABLE IF EXISTS `event_ticket`");
        sql.execute("DROP TABLE IF EXISTS `equipment_invitation`");
        sql.execute("DROP TABLE IF EXISTS `venue_location_invitation`");
        sql.execute("DROP TABLE IF EXISTS `event_session`");
        sql.execute("DROP TABLE IF EXISTS `event_meta_tag`");
        sql.execute("DROP TABLE IF EXISTS `event`");
        sql.execute("DROP TABLE IF EXISTS `venue_location_image`");
        sql.execute("DROP TABLE IF EXISTS `venue_location_section`");
        sql.execute("DROP TABLE IF EXISTS `venue_location`");
        sql.execute("DROP TABLE IF EXISTS `venue`");
        sql.execute("DROP TABLE IF EXISTS `equipment`");
        sql.execute("DROP TABLE IF EXISTS `equipment_type`");
        sql.execute("delete from `user_permission` where permission_id in (select id from `permission` where `type`='event')");
        sql.execute("delete from `role_permission` where permission_id in (select id from `permission` where `type`='event')");
        sql.execute("delete from `owner_permission` where permission_id in (select id from `permission` where `type`='event')");
        sql.execute("delete from `entity_permission` where permission_id in (select id from `permission` where `type`='event')");
        sql.execute("DELETE FROM `permission` WHERE `type` = 'event' ");
        connection.close();
        File resources = new File(Holders.servletContext.getRealPath("/resources/event"));
        if(resources.exists()) {
            resources.deleteDir();
        }
    }
}