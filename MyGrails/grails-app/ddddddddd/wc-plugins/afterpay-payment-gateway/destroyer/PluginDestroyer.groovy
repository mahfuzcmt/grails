import com.webcommander.util.PluginDestroyUtil

class PluginDestroyer {

    void destroy() {
        PluginDestroyUtil destroyUtil = new PluginDestroyUtil()
        try {
            destroyUtil.removePaymentGateway('APY')
                       .removePaymentMeta('APY');
            destroyUtil.removeSiteMessage("price.installment.amount");
        } catch(Exception e) {
            e.printStackTrace()
        } finally {
            util.closeConnection()
        }
    }
}