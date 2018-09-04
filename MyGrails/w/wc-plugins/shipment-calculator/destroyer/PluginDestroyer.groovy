import com.webcommander.util.PluginDestroyUtil

class PluginDestroyer {

    public void destroy() {
        PluginDestroyUtil destroyUtil = new PluginDestroyUtil()
        destroyUtil.removeSiteConfig("shipping", "shipment_calculator_cart_details_page").removeSiteConfig("shipping", "shipment_calculator_checkout_page")
        destroyUtil.removeProductWidget("shipmentCalculator")
        destroyUtil.closeConnection()
    }
}