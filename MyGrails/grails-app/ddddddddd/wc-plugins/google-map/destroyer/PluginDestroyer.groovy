import com.webcommander.util.PluginDestroyUtil

class PluginDestroyer {

    PluginDestroyUtil destroyUtil = PluginDestroyUtil.getInstance();
    public void destroy() {
        try {
            destroyUtil.removeWidget('googleMap')
        }catch (Exception e) {
            e.printStackTrace()
        }
        finally {
            destroyUtil.closeConnection();
        }
    }
}