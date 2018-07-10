import com.webcommander.util.PluginDestroyUtil

class PluginDestroyer {

    public void destroy() {
        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            util.removeCreditCardProcessor("EPATH", "f:EPath")
        } catch (Exception e) {
            e.printStackTrace()
        } finally {
            util.closeConnection()
        }
    }
}