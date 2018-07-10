import com.webcommander.util.PluginDestroyUtil

class PluginDestroyer {

    PluginDestroyUtil destroyUtil = PluginDestroyUtil.getInstance();
    public void destroy() {
        try {
            destroyUtil.removeWidget('flash').deleteResourceFolders('flash-widget')
        }catch (Exception e) {
            e.printStackTrace()
        }
        finally {
            destroyUtil.closeConnection();
        }
    }
}