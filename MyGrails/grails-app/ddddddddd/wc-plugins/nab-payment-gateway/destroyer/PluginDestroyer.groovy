import com.webcommander.util.PluginDestroyUtil

class PluginDestroyer {

    public void destroy() {
        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            util.removeCreditCardProcessor("NAB", "f:NAB")
        } catch (Exception e) {
            e.printStackTrace()
        } finally {
            util.closeConnection()
        }
    }
}