import com.webcommander.util.PluginDestroyUtil

class PluginDestroyer {

    PluginDestroyUtil destroyUtil = PluginDestroyUtil.getInstance();
    public void destroy() {
        try {
            destroyUtil.removeAutoPage('wish.list').removeSiteConfig('wish_list').removeSiteConfig('product', 'add_to_wish_list').removeSiteConfig('category_page', 'add_to_wish_list');
            destroyUtil.removeSiteConfig('search_page', 'add_to_wish_list').removeSiteConfig('customer_profile_page', 'add_to_wish_list');
            destroyUtil.dropTable('wish_list_email', 'wish_list_item', 'wish_list').removeEmailTemplates('wish-list-share')
        }catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            destroyUtil.closeConnection();
        }
    }
}