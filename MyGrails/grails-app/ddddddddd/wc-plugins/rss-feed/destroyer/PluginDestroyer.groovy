import com.webcommander.util.PluginDestroyUtil

class PluginDestroyer {

    public void destroy() {
        PluginDestroyUtil destroyUtil = new PluginDestroyUtil()
        destroyUtil.removeWidget("rssFeed")
        destroyUtil.closeConnection()
    }
}