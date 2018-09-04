import com.webcommander.util.PluginDestroyUtil

class PluginDestroyer {

    public void destroy() {
        PluginDestroyUtil destroyUtil = new PluginDestroyUtil()
        try {
            destroyUtil.removeWidget('filter').removeSiteConfig('filter_page').removeAutoPage('filter').deleteResourceFolders('filter');
            destroyUtil.dropTable('filter_profile_category', 'filter_profile_filter', 'filter_profile', 'filter').removeDefaultImages('filter');
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            destroyUtil.closeConnection();
        }
    }
}