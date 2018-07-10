import com.webcommander.util.PluginDestroyUtil

class PluginDestroyer {

    public void destroy() {
        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            util.removeSiteConfig("awaiting_payment")
            util.removeEmailTemplates("awaiting-payment")
        } catch(Exception e) {
            e.printStackTrace()
        } finally {
            util.closeConnection()
        }
    }
}