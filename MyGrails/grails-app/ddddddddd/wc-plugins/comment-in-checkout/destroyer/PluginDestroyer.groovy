import com.webcommander.util.PluginDestroyUtil

class PluginDestroyer {

    public void destroy() {
        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            util.removeSiteConfig("checkout_page", "comment_in_checkout")
        } catch(Exception e) {
            e.printStackTrace()
        } finally {
            util.closeConnection()
        }
    }
}