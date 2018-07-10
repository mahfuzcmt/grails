import com.webcommander.util.PluginDestroyUtil

class PluginDestroyer {

    PluginDestroyUtil destroyUtil = PluginDestroyUtil.getInstance();
    public void destroy() {
        try {
            destroyUtil.removeSiteConfig('quote')
                        .removeEmailTemplates('get-quote')
                        .removeSiteMessage('get.quote')
                        .removePermission('quote')
                        .dropTable('quote_item_variations', 'quote_item', 'quote')
        }catch (Exception e) {
            e.printStackTrace()
        }
        finally {
            destroyUtil.closeConnection();
        }
    }
}