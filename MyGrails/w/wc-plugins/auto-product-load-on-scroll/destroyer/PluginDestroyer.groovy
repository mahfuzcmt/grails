import com.webcommander.util.PluginDestroyUtil

class PluginDestroyer {

    public void destroy() {
        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            util.removeSiteConfig("category_page", "enable_load_more")
                    .removeSiteConfig("category_page", "initial_item")
                    .removeSiteConfig("category_page", "item_on_scroll")

        } catch (Exception e) {
            e.printStackTrace()
        } finally {
            util.closeConnection()
        }
    }
}