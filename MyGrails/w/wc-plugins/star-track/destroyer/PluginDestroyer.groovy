import com.webcommander.util.PluginDestroyUtil

class PluginDestroyer {

    PluginDestroyUtil destroyUtil = PluginDestroyUtil.getInstance();
    public void destroy() {
        try {
            destroyUtil.removeSiteConfig('star_track')
                        .dropTable('shipping_policy_extension')
        } catch (Exception e) {
            e.printStackTrace()
        } finally {
            destroyUtil.closeConnection();
        }
    }
}