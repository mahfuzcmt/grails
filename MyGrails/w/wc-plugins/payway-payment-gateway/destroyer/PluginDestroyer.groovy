import com.webcommander.util.PluginDestroyUtil

class PluginDestroyer {
    void destroy() {
        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            util.removeCreditCardProcessor("PAYWAY", "f:PayWay")
                    .deleteWebInfFolder("certificates/payway.cert")
        } catch (Exception e) {
            e.printStackTrace()
        } finally {
            util.closeConnection()
        }
    }
}