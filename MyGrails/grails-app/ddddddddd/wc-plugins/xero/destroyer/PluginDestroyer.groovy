import com.webcommander.util.PluginDestroyUtil

class PluginDestroyer {

    PluginDestroyUtil destroyUtil = PluginDestroyUtil.getInstance();
    public void destroy() {
        try {
            destroyUtil.removeSiteConfig('xero')
                    .removeSiteConfig('xero_item')
                    .dropTable('xero_track')
        } catch (Exception e) {
            e.printStackTrace()
        } finally {
            destroyUtil.closeConnection();
        }
    }
}