import com.webcommander.util.PluginDestroyUtil

class PluginDestroyer {

    PluginDestroyUtil destroyUtil = PluginDestroyUtil.getInstance();
    public void destroy() {
        try {
            destroyUtil.removeAutoPage('general.event', 'venue').removeWidget('generalEvent').removeSiteConfig('e_commerce', 'update_general_event_ticket_stock');
            destroyUtil.removeSiteMessage('book.now', 'your.requested.event.date.expired', 'requested.quantity.ticket.not.available', 'you.can.buy.maximum.quantity.for.event');
            destroyUtil.removeEmailTemplates('new-purchase-ticket-general-event', 'personalized-program-for-general-event').removePermission('general_event');
            destroyUtil.dropTable('general_event_week_days', 'general_event_daily_events', 'general_event_venue_location_image', 'general_event_ticket_inventory_adjustment_seat_number',
                    'general_event_ticket_inventory_adjustment', 'general_event_venue_location_section', 'general_event_meta_tag', 'general_event_checkout_field_options',
                    'general_event_checkout_field', 'general_event_checkout_field_title', 'general_event_customer', 'general_event_customer_group', 'general_event_custom_field_data',
                    'general_event_recurring_events', 'general_event_image', 'general_event', 'general_event_equipment', 'general_event_venue_location', 'general_event_venue');
            destroyUtil.deleteResourceFolders('general-event', 'general-event-venue-location').removeDefaultImages('general-event', 'venue-location');
        }catch (Exception e) {
            e.printStackTrace()
        }
        finally {
            destroyUtil.closeConnection();
        }
    }
}