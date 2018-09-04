import com.webcommander.util.PluginDestroyUtil

class PluginDestroyer {

    PluginDestroyUtil destroyUtil = PluginDestroyUtil.getInstance();
    public void destroy() {
        try {
            destroyUtil.removeSiteConfig('save_cart')
                        .dropTable('saved_cart_item_variations', 'saved_cart_item', 'saved_cart')
        } catch (Exception e) {
            e.printStackTrace()
        } finally {
            destroyUtil.closeConnection();
        }
    }
}