import com.webcommander.util.PluginDestroyUtil

class PluginDestroyer {

    public void destroy() {
        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            util.removeWidget("gallery", '%"gallery":"swipeBoxSlider"%')
        } catch (Exception e) {
            e.printStackTrace()
        } finally {
            util.closeConnection()
        }
    }
}