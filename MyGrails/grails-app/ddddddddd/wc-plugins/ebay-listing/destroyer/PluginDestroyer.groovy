import com.webcommander.util.PluginDestroyUtil

class PluginDestroyer {

    PluginDestroyUtil destroyUtil = PluginDestroyUtil.getInstance();
    public void destroy() {
        try {
            destroyUtil.removeSiteConfig('ebay_listing')
                    .dropTable("ebay_category_profile_mapping")
                    .dropTable("ebay_item_mapping")
                    .dropTable("ebay_listing_profile_ebay_payment_method")
                    .dropTable("ebay_profile_mapping")
                    .dropTable("ebay_listing_profile")
                    .dropTable("ebay_listing_profile_setting")
                    .dropTable("ebay_payment_method_ebay_meta_value")
                    .dropTable("ebay_meta_value")
                    .dropTable("ebay_payment_method")
                    .dropTable("ebay_postage")
                    .dropTable("ebay_pricing")
                    .dropTable("ebay_pricing_profile")
                    .dropTable("ebay_return_policy")
                    .dropTable("ebay_update_schedule_hours")
                    .dropTable("ebay_update_schedule_months")
                    .dropTable("ebay_update_schedule_minutes")
                    .dropTable("ebay_update_schedule_days")
                    .dropTable("ebay_update_schedule_dates")
                    .dropTable("ebay_update_schedule")
        } catch (Exception e) {
            e.printStackTrace()
        }
        finally {
            destroyUtil.closeConnection()
        }
    }
}