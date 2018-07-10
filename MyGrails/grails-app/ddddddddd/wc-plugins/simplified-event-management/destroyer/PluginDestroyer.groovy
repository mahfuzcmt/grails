import com.webcommander.util.AppUtil
import com.mysql.jdbc.Driver
import grails.util.Holders
import com.webcommander.manager.PathManager
import groovy.sql.Sql

import java.sql.Connection

class PluginDestroyer {

    void destroy() {
        Properties ppt = new Properties()
        ppt.user = Holders.config.dataSource.username
        ppt.password = Holders.config.dataSource.password
        String url = Holders.config.dataSource.url
        Connection connection = Driver.newInstance().connect(url, ppt)
        Sql sql = new Sql(connection)
        sql.execute("DROP TABLE IF EXISTS `simplified_event_image`")
        sql.execute("DROP TABLE IF EXISTS `simplified_event_ticket`")
        sql.execute("DROP TABLE IF EXISTS `simplified_event_meta_tag`")
        sql.execute("DROP TABLE IF EXISTS `simplified_event`")
        sql.execute("DROP TABLE IF EXISTS `simplified_event_ticket_inventory_adjustment`")
        sql.execute("delete from `user_permission` where permission_id in (select id from `permission` where `type`='simplified_event')")
        sql.execute("delete from `role_permission` where permission_id in (select id from `permission` where `type`='simplified_event')")
        sql.execute("delete from `owner_permission` where permission_id in (select id from `permission` where `type`='simplified_event')")
        sql.execute("delete from `entity_permission` where permission_id in (select id from `permission` where `type`='simplified_event')")
        sql.execute("DELETE FROM `permission` WHERE `type` = 'simplified_event' ")
        sql.execute("DELETE FROM `site_config` WHERE type='e_commerce' AND config_key='update_ticket_stock'")
        sql.execute("DELETE FROM `message_source` WHERE message_key='your.requested.event.date.expired'")
        sql.execute("DELETE FROM `message_source` WHERE message_key='requested.quantity.ticket.not.available'")
        sql.execute("DELETE FROM `message_source` WHERE message_key='you.can.buy.maximum.quantity.for.event'")
        sql.execute("DELETE FROM `widget_content` WHERE type='simplified_event'")
        sql.execute("DELETE FROM `page_widget` where widget_id in (select id from `widget` where `widget_type`='simplified_event')")
        sql.execute("DELETE FROM `layout_widget` where widget_id in (select id from `widget` where `widget_type`='simplified_event')")
        sql.execute("DELETE FROM `dock_section_widget` where widget_id in (select id from `widget` where `widget_type`='simplified_event')")
        sql.execute("DELETE FROM `widget` WHERE widget_type='simplified_event'")
        connection.close()
        File imageAndPersonalizedFiles = new File(Holders.servletContext.getRealPath("/resources/simplified-event"))
        if(imageAndPersonalizedFiles.exists()) {
            imageAndPersonalizedFiles.deleteDir()
        }
        File emailTemplatesTicketFromMR = new File(PathManager.getCustomRestrictedResourceRoot("email-templates/new-purchase-ticket-simplified-event"))
        if(emailTemplatesTicketFromMR.exists()) {
            emailTemplatesTicketFromMR.deleteDir()
        }
        File emailTemplatesEventFromMR = new File(PathManager.getCustomRestrictedResourceRoot("email-templates/personalized-program-for-simplified-event"))
        if(emailTemplatesEventFromMR.exists()) {
            emailTemplatesEventFromMR.deleteDir()
        }
        //Below lines are for all destroy
        /*File emailTemplatesTicketFromSR = new File(PathManager.getRestrictedResourceRoot("email-templates/new-purchase-ticket-simplified-event"))
        if(emailTemplatesTicketFromSR.exists()) {
            emailTemplatesTicketFromSR.deleteDir()
        }
        File emailTemplatesEventFromSR = new File(PathManager.getRestrictedResourceRoot("email-templates/personalized-program-for-simplified-event"))
        if(emailTemplatesEventFromSR.exists()) {
            emailTemplatesEventFromSR.deleteDir()
        }*/
    }
}