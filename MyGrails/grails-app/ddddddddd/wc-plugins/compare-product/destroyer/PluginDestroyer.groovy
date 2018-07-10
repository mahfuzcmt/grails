import com.webcommander.util.PluginDestroyUtil

class PluginDestroyer {

    PluginDestroyUtil destroyUtil = PluginDestroyUtil.getInstance();
    public void destroy() {
        try {
            destroyUtil.removeAutoPage('compare.product')
                    .removeSiteConfig('compare_product')
                    .removeSiteConfig('product', 'add_to_compare')
                    .removeSiteConfig('category_page', 'add_to_compare')
                    .removeSiteConfig('brand_manufacturer_page', 'add_to_compare')
                    .removeSiteConfig('search_page', 'add_to_compare')
                    .removeSiteConfig('customer_profile_page', 'add_to_compare')
                    .dropTable('custom_properties')
                    .removeWidget('compareProduct')
        }catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            destroyUtil.closeConnection();
        }
    }
}