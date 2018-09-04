import com.webcommander.util.PluginDestroyUtil

class PluginDestroyer {

    public void destroy() {
        PluginDestroyUtil destroyUtil = new PluginDestroyUtil()
        destroyUtil.removeSiteConfig("product_image_swapping")
        destroyUtil.closeConnection()
    }
}