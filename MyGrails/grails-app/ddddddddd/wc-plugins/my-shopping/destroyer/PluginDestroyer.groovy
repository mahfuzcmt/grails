import com.webcommander.util.PluginDestroyUtil

class PluginDestroyer {

    PluginDestroyUtil destroyUtil = PluginDestroyUtil.getInstance();
    public void destroy() {
        try {
            destroyUtil.removeSiteConfig('my_shopping')
                    .dropTable('my_shopping_mapping')
                    .removeFoldersFromSysResource('my-shopping-category')
        } catch (Exception e) {
            e.printStackTrace()
        } finally {
            destroyUtil.closeConnection()
        }
    }
}