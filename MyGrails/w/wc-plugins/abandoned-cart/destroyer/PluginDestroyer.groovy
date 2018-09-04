import com.webcommander.util.PluginDestroyUtil

class PluginDestroyer {
    void destroy() {
        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            util.dropTable("abandoned_cart_item_variations", "abandoned_cart_item", "abandoned_cart")
            util.removeSiteConfig("abandoned_cart")
            util.removeEmailTemplates("abandoned-cart")
        } catch(Exception e) {
            e.printStackTrace()
        } finally {
            util.closeConnection()
        }
    }
}